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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class LsdPostRenderer {
    private static final Identifier LSD_ID = Identifier.of(HuzzClient.MOD_ID, "lsd");
    private static final String UNIFORM_BLOCK_NAME = "LsdUniforms";
    private static final int UBO_SIZE = 64;
    private static final int UNIFORM_BUFFER_USAGE = 130;

    private static ObjectAllocator frameAllocator;
    private static GpuBuffer lsdUbo;
    private static PostEffectProcessor lastKnownProcessor;
    private static boolean loadErrorLogged;

    private LsdPostRenderer() {
    }

    public static void captureAllocator(ObjectAllocator allocator) {
        frameAllocator = allocator;
    }

    public static void applyLsd() {
        MinecraftClient client = MinecraftClient.getInstance();
        HuzzConfig config = HuzzClient.getConfigManager().getConfig();
        if (!config.isLsdEnabled() || client.player == null || client.world == null) {
            return;
        }

        PostEffectProcessor processor = getProcessor(client);
        if (processor == null) {
            return;
        }

        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();
        replaceUniformBuffer(
            processor,
            width,
            height,
            HuzzClient.getLsdController().shaderTime(),
            HuzzClient.getLsdController().colorIntensity(),
            HuzzClient.getLsdController().morphStrength(),
            HuzzClient.getLsdController().aberrationStrength()
        );
        processor.render(client.getFramebuffer(), frameAllocator == null ? ObjectAllocator.TRIVIAL : frameAllocator);
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
            PostEffectProcessor processor = (PostEffectProcessor) getOrLoadProcessor.invoke(cache, LSD_ID, Set.of(PostEffectProcessor.MAIN));
            if (processor != null && processor != lastKnownProcessor) {
                lastKnownProcessor = processor;
                attachUniformBuffer(processor);
            }
            return processor;
        } catch (Exception exception) {
            if (!loadErrorLogged) {
                loadErrorLogged = true;
                HuzzClient.LOGGER.error("Failed to load LSD shader", exception);
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
        if (lsdUbo == null) {
            lsdUbo = createBufferCompat();
        }

        List<PostEffectPass> passes = ((PostEffectProcessorAccessor) processor).huzzclient$getPasses();
        if (passes.isEmpty()) {
            return;
        }

        Map<String, GpuBuffer> uniformBuffers = ((PostEffectPassAccessor) passes.get(0)).huzzclient$getUniformBuffers();
        uniformBuffers.put(UNIFORM_BLOCK_NAME, lsdUbo);
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
                throw new RuntimeException("Unable to create LSD uniform buffer", secondError);
            }
        }
    }

    private static void replaceUniformBuffer(
        PostEffectProcessor processor,
        int viewWidth,
        int viewHeight,
        float time,
        float colorIntensity,
        float morphStrength,
        float aberrationStrength
    ) {
        attachUniformBuffer(processor);
        if (lsdUbo == null) {
            return;
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(lsdUbo, false, true)) {
            Std140Builder builder = Std140Builder.intoBuffer(view.data());
            builder.putVec2(viewWidth, viewHeight);
            builder.putFloat(time);
            builder.putFloat(colorIntensity);
            builder.putFloat(morphStrength);
            builder.putFloat(aberrationStrength);
        }
    }
}
