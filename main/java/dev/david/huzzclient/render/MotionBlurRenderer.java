package dev.david.huzzclient.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.mixin.PostEffectPassAccessor;
import dev.david.huzzclient.mixin.PostEffectProcessorAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import java.nio.ByteBuffer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MotionBlurRenderer {
    private static final Identifier MOTION_BLUR_ID = Identifier.of(HuzzClient.MOD_ID, "motion_blur");
    private static final String UNIFORM_BLOCK_NAME = "MotionBlurUniforms";
    private static final int UBO_SIZE = 304;
    private static final int MAX_SHADER_SAMPLES = 180;
    private static final int UNIFORM_BUFFER_USAGE = 130;
    private static final float LEGACY_MAX_FRAMES = 24.0F;
    private static final float MAX_BLEND_FACTOR = 2.4F;

    private static final Matrix4f tempMvInverse = new Matrix4f();
    private static final Matrix4f tempProjInverse = new Matrix4f();
    private static final Matrix4f tempPrevModelView = new Matrix4f();
    private static final Matrix4f tempPrevProjection = new Matrix4f();

    private static long lastNano;
    private static float currentBlur;
    private static float camDX;
    private static float camDY;
    private static float camDZ;

    private static ObjectAllocator frameAllocator;
    private static GpuBuffer motionBlurUbo;
    private static PostEffectProcessor lastKnownProcessor;
    private static boolean loadErrorLogged;

    private MotionBlurRenderer() {
    }

    public static void captureAllocator(ObjectAllocator allocator) {
        frameAllocator = allocator;
    }

    public static void setFrameMotionBlur(
        Matrix4f modelView,
        Matrix4f prevModelView,
        Matrix4f projection,
        Matrix4f prevProjection,
        float deltaX,
        float deltaY,
        float deltaZ
    ) {
        tempMvInverse.set(modelView).invert();
        tempProjInverse.set(projection).invert();
        tempPrevModelView.set(prevModelView);
        tempPrevProjection.set(prevProjection);
        camDX = deltaX;
        camDY = deltaY;
        camDZ = deltaZ;
    }

    public static void applyMotionBlur() {
        MinecraftClient client = MinecraftClient.getInstance();
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        long now = System.nanoTime();
        float deltaTime = lastNano == 0L ? 0.0F : (now - lastNano) / 1_000_000_000.0F;
        lastNano = now;

        if (!shouldRenderMotionBlur(client, config)) {
            currentBlur = 0.0F;
            return;
        }

        applyMotionBlurInternal(client, config, deltaTime);
    }

    private static boolean shouldRenderMotionBlur(MinecraftClient client, HuzzConfig config) {
        if (!config.isMotionBlurEnabled() || client.world == null || client.player == null) {
            return false;
        }

        return client.options.getPerspective().isFirstPerson();
    }

    private static void applyMotionBlurInternal(MinecraftClient client, HuzzConfig config, float deltaTime) {
        PostEffectProcessor processor = getProcessor(client);
        if (processor == null) {
            return;
        }


        float baseStrength = config.getMotionBlurFrames() * 0.08F;
        baseStrength = Math.min(baseStrength, 6.0F);
        float scaledStrength = baseStrength;
        updateBlurStrength(scaledStrength, deltaTime * 2.0F);

        if (currentBlur <= 0.001F) {
            return;
        }

        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        int sampleAmount = Math.max(12, Math.min(MAX_SHADER_SAMPLES, 20 + config.getMotionBlurFrames() * 3));
        replaceUniformBuffer(processor, currentBlur, width, height, sampleAmount, true, 1);
        processor.render(client.getFramebuffer(), frameAllocator == null ? ObjectAllocator.TRIVIAL : frameAllocator);
    }

    private static float getRefreshRateScale() {
        int fps = MinecraftClient.getInstance().getCurrentFps();
        int refreshRate = detectRefreshRate();
        if (fps <= 0 || refreshRate <= 0) {
            return 1.0F;
        }
        return Math.max(0.35F, Math.min(1.0F, fps / (float) refreshRate));
    }

    private static int detectRefreshRate() {
        MinecraftClient client = MinecraftClient.getInstance();
        long windowHandle = client.getWindow().getHandle();
        long monitor = GLFW.glfwGetWindowMonitor(windowHandle);
        if (monitor == 0L) {
            monitor = GLFW.glfwGetPrimaryMonitor();
        }
        if (monitor == 0L) {
            return 60;
        }

        GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);
        return mode == null ? 60 : mode.refreshRate();
    }

    private static void updateBlurStrength(float targetStrength, float deltaTime) {
        float lerp = deltaTime <= 0.0F ? 1.0F : Math.min(1.0F, deltaTime * 25.0F);
        currentBlur += (targetStrength - currentBlur) * lerp;
    }

    private static PostEffectProcessor getProcessor(MinecraftClient client) {
        try {
            ShaderLoader shaderLoader = client.getShaderLoader();
            Object cache = getShaderLoaderCache(shaderLoader);
            if (cache == null) {
                return null;
            }

            Method getOrLoadProcessor = cache.getClass().getDeclaredMethod("getOrLoadProcessor", Identifier.class, Set.class);
            getOrLoadProcessor.setAccessible(true);
            PostEffectProcessor processor = (PostEffectProcessor) getOrLoadProcessor.invoke(cache, MOTION_BLUR_ID, Set.of(PostEffectProcessor.MAIN));
            if (processor != null && processor != lastKnownProcessor) {
                lastKnownProcessor = processor;
                attachUniformBuffer(processor);
            }
            return processor;
        } catch (Exception exception) {
            if (!loadErrorLogged) {
                loadErrorLogged = true;
                HuzzClient.LOGGER.error("Failed to load motion blur shader", exception);
            }
            return null;
        }
    }

    private static Object getShaderLoaderCache(ShaderLoader shaderLoader) throws IllegalAccessException {
        for (Field field : shaderLoader.getClass().getDeclaredFields()) {
            if (!field.getType().getName().endsWith("ShaderLoader$Cache")) {
                continue;
            }
            field.setAccessible(true);
            return field.get(shaderLoader);
        }
        return null;
    }

    private static void attachUniformBuffer(PostEffectProcessor processor) {
        if (motionBlurUbo == null) {
            motionBlurUbo = createBufferCompat();
        }

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor) processor).huzzclient$getPasses();
        if (passes.isEmpty()) {
            return;
        }

        Map<String, GpuBuffer> uniformBuffers = ((PostEffectPassAccessor) passes.get(0)).huzzclient$getUniformBuffers();
        uniformBuffers.put(UNIFORM_BLOCK_NAME, motionBlurUbo);
    }

    private static GpuBuffer createBufferCompat() {
        Object device = RenderSystem.getDevice();
        try {
            Method createBuffer = device.getClass().getMethod("createBuffer", java.util.function.Supplier.class, Integer.TYPE, Long.TYPE);
            return (GpuBuffer) createBuffer.invoke(device, (java.util.function.Supplier<String>) () -> HuzzClient.MOD_ID + ":" + UNIFORM_BLOCK_NAME, UNIFORM_BUFFER_USAGE, (long) UBO_SIZE);
        } catch (ReflectiveOperationException firstError) {
            try {
                Method createBuffer = device.getClass().getMethod("createBuffer", java.util.function.Supplier.class, Integer.TYPE, ByteBuffer.class);
                return (GpuBuffer) createBuffer.invoke(device, (java.util.function.Supplier<String>) () -> HuzzClient.MOD_ID + ":" + UNIFORM_BLOCK_NAME, UNIFORM_BUFFER_USAGE, ByteBuffer.allocateDirect(UBO_SIZE));
            } catch (ReflectiveOperationException secondError) {
                secondError.addSuppressed(firstError);
                throw new RuntimeException("Unable to create MotionBlur uniform buffer", secondError);
            }
        }
    }

    private static void replaceUniformBuffer(
        PostEffectProcessor processor,
        float blendFactor,
        int viewWidth,
        int viewHeight,
        int sampleAmount,
        boolean useDepth,
        int blurAlgorithm
    ) {
        attachUniformBuffer(processor);
        if (motionBlurUbo == null) {
            return;
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(motionBlurUbo, false, true)) {
            Std140Builder builder = Std140Builder.intoBuffer(view.data());
            builder.putMat4f(tempMvInverse);
            builder.putMat4f(tempProjInverse);
            builder.putMat4f(tempPrevModelView);
            builder.putMat4f(tempPrevProjection);
            builder.putVec3(camDX, camDY, camDZ);
            builder.putVec2(viewWidth, viewHeight);
            builder.putFloat(blendFactor);
            builder.putInt(sampleAmount);
            builder.putInt(blurAlgorithm);
            builder.putInt(useDepth ? 1 : 0);
        }
    }
}
