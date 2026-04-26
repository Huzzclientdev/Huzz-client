package dev.david.huzzclient.ui;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public final class HuzzConfigScreen extends Screen {
    private static final int SCREEN_MARGIN = 12;
    private static final int COLUMN_GAP = 10;
    private static final int COLUMN_HEADER_HEIGHT = 14;
    private static final int MODULE_HEADER_HEIGHT = 16;
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_GAP = 2;
    private static final int MODULE_GAP = 8;
    private static final int FOOTER_HEIGHT = 20;
    private static final int SCROLL_STEP = 18;
    private static final int TOGGLE_WIDTH = 28;
    private static final int SMALL_BUTTON_WIDTH = 28;
    private static final int KEY_BUTTON_WIDTH = 72;
    private static final int GRID_BUTTON_SIZE = 18;
    private static final int GRID_BUTTON_GAP = 4;
    private static final List<ColumnGroup> COLUMNS = List.of(
        new ColumnGroup("Render", List.of(
            Module.BLOCK_ESP,
            Module.STORAGE_ESP,
            Module.HOLE_ESP,
            Module.BEDROCK_HOLE_ESP,
            Module.PLAYER_ESP,
            Module.MOB_ESP,
            Module.BREAK_PROGRESS,
            Module.FULL_BRIGHT,
            Module.CUSTOM_FOV,
            Module.ITEM_NAMETAGS,
            Module.MOTION_BLUR,
            Module.TIME_CHANGER,
            Module.NO_RENDER,
            Module.BLOCK_SELECT,
            Module.FAKE_SPAWNER,
            Module.FAKE_ELYTRA,
            Module.BEDROCK_PROTECT,
            Module.HUD
        )),
        new ColumnGroup("Movement", List.of(
            Module.SWING_SPEED,
            Module.DOUBLE_CLICK,
            Module.FREE_CAM,
            Module.FREE_LOOK,
            Module.AUTO_REPLENISH,
            Module.AUTO_BRIDGE,
            Module.AIM_ASSIST,
            Module.AIM_FOV,
            Module.AUTO_TOOL,
            Module.AUTO_CRYSTAL,
            Module.TUNNEL_BASE_FINDER,
            Module.AUTO_EAT,
            Module.FAST_USE,
            Module.AUTO_TOTEM,
            Module.AUTO_ROCKET
        )),
        new ColumnGroup("Player", List.of(
            Module.PLAYER_NOTIFIER,
            Module.AUTO_DISCONNECT,
            Module.DUAL_ARM_ANIMATION,
            Module.SKIN_PROTECT,
            Module.FAKE_MEDIA,
            Module.FAKE_PAY,
            Module.FAKE_STATS,
            Module.NAME_PROTECT
        )),
        new ColumnGroup("Utility", List.of(
            Module.AUTO_RECONNECT,
            Module.SILENT_SET_HOME,
            Module.STASH_FINDER,
            Module.BLOCK_NOTIFIER,
            Module.CHAT_MACRO,
            Module.CRAFTER_MACRO
        ))
    );

    private final Screen parent;
    private final HuzzConfig workingCopy;

    private Module listeningForKeybind = Module.NONE;
    private final EnumSet<Module> collapsedModules = EnumSet.noneOf(Module.class);
    private int scrollOffset;

    private String blockEspDraft = "";
    private String storageEspDraft = "";
    private String blockNotifierDraft = "";
    private String fakeSpawnerDraft = "";
    private String fakeElytraDraft = "";
    private String nameProtectDraft = "";
    private String skinProtectDraft = "";
    private String chatMacroDraft = "";
    private String autoReconnectDraft = "";
    private String fakeStatsTitleDraft = "";
    private String fakeStatsMoneyDraft = "";
    private String fakeStatsShardsDraft = "";
    private String fakeStatsKillsDraft = "";
    private String fakeStatsDeathsDraft = "";
    private String fakeStatsKeyallDraft = "";
    private String fakeStatsPlaytimeDraft = "";
    private String fakeStatsTeamDraft = "";
    private String fakeStatsRegionDraft = "";

    private TextFieldWidget blockEspField;
    private TextFieldWidget storageEspField;
    private TextFieldWidget blockNotifierField;
    private TextFieldWidget fakeSpawnerField;
    private TextFieldWidget fakeElytraField;
    private TextFieldWidget nameProtectField;
    private TextFieldWidget skinProtectField;
    private TextFieldWidget chatMacroField;
    private TextFieldWidget autoReconnectField;
    private TextFieldWidget fakeStatsTitleField;
    private TextFieldWidget fakeStatsMoneyField;
    private TextFieldWidget fakeStatsShardsField;
    private TextFieldWidget fakeStatsKillsField;
    private TextFieldWidget fakeStatsDeathsField;
    private TextFieldWidget fakeStatsKeyallField;
    private TextFieldWidget fakeStatsPlaytimeField;
    private TextFieldWidget fakeStatsTeamField;
    private TextFieldWidget fakeStatsRegionField;
    private int chatMacroDraftKeyCode = HuzzConfig.NO_KEY;
    private boolean listeningForChatMacroKey;

    public HuzzConfigScreen(Screen parent) {
        super(Text.literal("HuzzClient"));
        this.parent = parent;
        this.workingCopy = HuzzClient.getConfigManager().getConfig().copy();
        this.collapsedModules.addAll(EnumSet.allOf(Module.class));
        this.collapsedModules.remove(Module.NONE);
        resetDraftsFromWorkingCopy();
    }

    @Override
    protected void init() {
        clearChildren();
        clearTextFieldRefs();
        Layout layout = layout();
        scrollOffset = clamp(scrollOffset, 0, maxScroll(layout));
        buildWidgets(layout);
        addFooterButtons(layout);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        Layout layout = layout();
        Module headerModule = moduleAtHeader(layout, click.x(), click.y());
        if (headerModule != Module.NONE && click.button() == 0) {
            if (!collapsedModules.add(headerModule)) {
                collapsedModules.remove(headerModule);
            }
            rebuildScreen();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Layout layout = layout();
        if (!isInside(layout.left(), layout.viewportTop(), layout.right(), layout.viewportBottom(), mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        int maxScroll = maxScroll(layout);
        if (maxScroll <= 0) {
            return true;
        }
        scrollOffset = clamp(scrollOffset - (int) Math.round(verticalAmount * SCROLL_STEP), 0, maxScroll);
        rebuildScreen();
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (listeningForKeybind != Module.NONE) {
            setModuleKeyCode(listeningForKeybind, keyCode == GLFW.GLFW_KEY_ESCAPE ? HuzzConfig.NO_KEY : keyCode);
            listeningForKeybind = Module.NONE;
            rebuildScreen();
            return true;
        }
        if (listeningForChatMacroKey) {
            chatMacroDraftKeyCode = keyCode == GLFW.GLFW_KEY_ESCAPE ? HuzzConfig.NO_KEY : keyCode;
            listeningForChatMacroKey = false;
            rebuildScreen();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        saveWorkingCopy();
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        HuzzUi.drawBackdrop(context, width, height);
        Layout layout = layout();

        context.drawTextWithShadow(textRenderer, title, layout.left(), 6, HuzzUi.TEXT_PRIMARY);
        context.drawText(textRenderer, Text.literal("Right Ctrl to reopen"), layout.left() + 58, 6, HuzzUi.TEXT_MUTED, false);

        for (int columnIndex = 0; columnIndex < COLUMNS.size(); columnIndex++) {
            ColumnLayout columnLayout = layout.columns().get(columnIndex);
            drawColumn(context, layout, columnLayout, COLUMNS.get(columnIndex));
        }

        context.drawText(textRenderer, Text.literal(footerMessage()), layout.left(), layout.bottom() - 12, HuzzUi.TEXT_MUTED, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void buildWidgets(Layout layout) {
        for (int columnIndex = 0; columnIndex < COLUMNS.size(); columnIndex++) {
            ColumnLayout columnLayout = layout.columns().get(columnIndex);
            int moduleTop = columnLayout.bodyTop() - scrollOffset;
            for (Module module : COLUMNS.get(columnIndex).modules()) {
                if (isVisible(layout, moduleTop, moduleHeight(module))) {
                    addModuleWidgets(columnLayout, module, moduleTop);
                }
                moduleTop += moduleHeight(module) + MODULE_GAP;
            }
        }
    }

    private void addModuleWidgets(ColumnLayout columnLayout, Module module, int moduleTop) {
        addModuleToggle(columnLayout, module, moduleTop);
        if (isCollapsed(module)) {
            return;
        }

        switch (module) {
            case BLOCK_ESP -> addEspWidgets(columnLayout, moduleTop, true);
            case STORAGE_ESP -> addEspWidgets(columnLayout, moduleTop, false);
            case HOLE_ESP, BEDROCK_HOLE_ESP -> addHoleEspWidgets(columnLayout, moduleTop);
            case BREAK_PROGRESS, AUTO_REPLENISH, AUTO_TOOL -> {
            }
            case AIM_FOV -> addAimFovWidgets(columnLayout, moduleTop);
            case STASH_FINDER -> addStashFinderWidgets(columnLayout, moduleTop);
            case BLOCK_NOTIFIER -> addBlockNotifierWidgets(columnLayout, moduleTop);
            case CHAT_MACRO -> addChatMacroWidgets(columnLayout, moduleTop);
            case SWING_SPEED -> addSwingSpeedWidgets(columnLayout, moduleTop);
            case DOUBLE_CLICK -> addDoubleClickWidgets(columnLayout, moduleTop);
            case AUTO_RECONNECT -> addAutoReconnectWidgets(columnLayout, moduleTop);
            case FREE_CAM -> addFreeCamWidgets(columnLayout, moduleTop);
            case AUTO_BRIDGE -> { }
            case TUNNEL_BASE_FINDER -> addTunnelBaseFinderWidgets(columnLayout, moduleTop);
            case AIM_ASSIST -> addAimAssistWidgets(columnLayout, moduleTop);
            case FREE_LOOK, ITEM_NAMETAGS -> addKeyOnlyWidgets(columnLayout, moduleTop, module);
            case FAST_USE -> addFastUseWidgets(columnLayout, moduleTop);
            case FULL_BRIGHT -> addFullBrightWidgets(columnLayout, moduleTop);
            case CUSTOM_FOV -> addCustomFovWidgets(columnLayout, moduleTop);
            case FAKE_STATS -> addFakeStatsWidgets(columnLayout, moduleTop);
            case AUTO_TOTEM -> addAutoTotemWidgets(columnLayout, moduleTop);
            case AUTO_ROCKET -> addAutoRocketWidgets(columnLayout, moduleTop);
            case AUTO_CRYSTAL -> addAutoCrystalWidgets(columnLayout, moduleTop);
            case NAME_PROTECT -> addNameProtectWidgets(columnLayout, moduleTop);
            case SKIN_PROTECT -> addSkinProtectWidgets(columnLayout, moduleTop);
            case FAKE_SPAWNER -> addFakeSpawnerWidgets(columnLayout, moduleTop);
            case FAKE_ELYTRA -> addFakeElytraWidgets(columnLayout, moduleTop);
            case CRAFTER_MACRO -> addCrafterMacroWidgets(columnLayout, moduleTop);
            case NO_RENDER -> addNoRenderWidgets(columnLayout, moduleTop);
            case PLAYER_NOTIFIER, PLAYER_ESP, AUTO_DISCONNECT, FAKE_MEDIA, FAKE_PAY, SILENT_SET_HOME, DUAL_ARM_ANIMATION, BEDROCK_PROTECT, AUTO_EAT, NONE -> {
            }
            case MOB_ESP -> addMobEspWidgets(columnLayout, moduleTop);
            case MOTION_BLUR -> addMotionBlurWidgets(columnLayout, moduleTop);
            case TIME_CHANGER -> addTimeChangerWidgets(columnLayout, moduleTop);
            case BLOCK_SELECT -> addBlockSelectWidgets(columnLayout, moduleTop);
            case HUD -> addHudWidgets(columnLayout, moduleTop);
        }

        if (needsAutoKeybindRow(module)) {
            addKeybindButton(columnLayout, moduleTop, optionCount(module) - 1, module);
        }
    }

    private void addEspWidgets(ColumnLayout columnLayout, int moduleTop, boolean blockEsp) {
        int tracerTop = rowTop(moduleTop, 0);
        boolean tracers = blockEsp ? workingCopy.isBlockEspTracers() : workingCopy.isStorageEspTracers();
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, tracerTop + 2, 28, 12, Text.literal(tracers ? "On" : "Off"), tracers, button -> {
            if (blockEsp) {
                workingCopy.setBlockEspTracers(!workingCopy.isBlockEspTracers());
            } else {
                workingCopy.setStorageEspTracers(!workingCopy.isStorageEspTracers());
            }
            rebuildScreen();
        }));

        addKeybindButton(columnLayout, moduleTop, 1, blockEsp ? Module.BLOCK_ESP : Module.STORAGE_ESP);

        int addTop = rowTop(moduleTop, 2);
        TextFieldWidget field = addTextField(columnLayout, addTop, blockEsp ? blockEspDraft : storageEspDraft, 48, "block id");
        if (blockEsp) {
            blockEspField = field;
        } else {
            storageEspField = field;
        }
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, addTop + 2, 28, 12, Text.literal("Add"), true, button -> {
            captureDrafts();
            String entered = blockEsp ? blockEspDraft : storageEspDraft;
            if (blockEsp) {
                workingCopy.addBlockEspTarget(entered);
                blockEspDraft = "";
            } else {
                workingCopy.addStorageEspTarget(entered);
                storageEspDraft = "";
            }
            rebuildScreen();
        }));

        List<String> targets = blockEsp ? workingCopy.getBlockEspTargets() : workingCopy.getStorageEspTargets();
        for (int index = 0; index < targets.size(); index++) {
            int targetTop = rowTop(moduleTop, 3 + index);
            String target = targets.get(index);
            addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, targetTop + 2, 28, 12, Text.literal("Rem"), false, button -> {
                if (blockEsp) {
                    workingCopy.removeBlockEspTarget(target);
                } else {
                    workingCopy.removeStorageEspTarget(target);
                }
                rebuildScreen();
            }));
        }
    }

    private void addStashFinderWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-1", "+1", true,
            () -> workingCopy.setStashFinderThreshold(workingCopy.getStashFinderThreshold() - 1),
            () -> workingCopy.setStashFinderThreshold(workingCopy.getStashFinderThreshold() + 1));
    }

    private void addHoleEspWidgets(ColumnLayout columnLayout, int moduleTop) {
    }

    private void addTunnelBaseFinderWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "<", ">", true,
            () -> workingCopy.setTunnelBaseFinderMode(
                workingCopy.getTunnelBaseFinderMode() == HuzzConfig.TunnelBaseFinderMode.MODE_1X2
                    ? HuzzConfig.TunnelBaseFinderMode.MODE_3X3
                    : HuzzConfig.TunnelBaseFinderMode.MODE_1X2),
            () -> workingCopy.setTunnelBaseFinderMode(
                workingCopy.getTunnelBaseFinderMode() == HuzzConfig.TunnelBaseFinderMode.MODE_1X2
                    ? HuzzConfig.TunnelBaseFinderMode.MODE_3X3
                    : HuzzConfig.TunnelBaseFinderMode.MODE_1X2));
    }

    private void addMobEspWidgets(ColumnLayout columnLayout, int moduleTop) {
        int tracerTop = rowTop(moduleTop, 1);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, tracerTop + 2, 28, 12,
            Text.literal(workingCopy.isMobEspTracers() ? "On" : "Off"),
            workingCopy.isMobEspTracers(),
            button -> {
                workingCopy.setMobEspTracers(!workingCopy.isMobEspTracers());
                rebuildScreen();
            }));
    }

    private void addTimeChangerWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-1000", "+1000", true,
            () -> workingCopy.setTimeChangerValue(workingCopy.getTimeChangerValue() - 1000L),
            () -> workingCopy.setTimeChangerValue(workingCopy.getTimeChangerValue() + 1000L));
    }

    private void addAimFovWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-5", "+5", true,
            () -> workingCopy.setAimFovRadius(workingCopy.getAimFovRadius() - 5),
            () -> workingCopy.setAimFovRadius(workingCopy.getAimFovRadius() + 5));
    }

    private void addHudWidgets(ColumnLayout columnLayout, int moduleTop) {
        addHudToggle(columnLayout, moduleTop, 0, workingCopy.isHudFpsEnabled(), () -> workingCopy.setHudFpsEnabled(!workingCopy.isHudFpsEnabled()));
        addHudToggle(columnLayout, moduleTop, 1, workingCopy.isHudMinimapEnabled(), () -> workingCopy.setHudMinimapEnabled(!workingCopy.isHudMinimapEnabled()));
        addHudToggle(columnLayout, moduleTop, 2, workingCopy.isHudClockEnabled(), () -> workingCopy.setHudClockEnabled(!workingCopy.isHudClockEnabled()));
        addHudToggle(columnLayout, moduleTop, 3, workingCopy.isHudArmorEnabled(), () -> workingCopy.setHudArmorEnabled(!workingCopy.isHudArmorEnabled()));
    }

    private void addAimAssistWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-1", "+1", true,
            () -> workingCopy.setAimAssistSlot(workingCopy.getAimAssistSlot() - 1),
            () -> workingCopy.setAimAssistSlot(workingCopy.getAimAssistSlot() + 1));

        int targetTop = rowTop(moduleTop, 1);
        int buttonRight = columnLayout.innerRight() - 4;
        addDrawableChild(new HuzzActionButton(buttonRight - 88, targetTop + 2, 28, 12, Text.literal("Head"),
            workingCopy.getAimAssistTarget() == HuzzConfig.AimAssistTarget.HEAD,
            button -> {
                workingCopy.setAimAssistTarget(HuzzConfig.AimAssistTarget.HEAD);
                rebuildScreen();
            }));
        addDrawableChild(new HuzzActionButton(buttonRight - 58, targetTop + 2, 28, 12, Text.literal("Neck"),
            workingCopy.getAimAssistTarget() == HuzzConfig.AimAssistTarget.NECK,
            button -> {
                workingCopy.setAimAssistTarget(HuzzConfig.AimAssistTarget.NECK);
                rebuildScreen();
            }));
        addDrawableChild(new HuzzActionButton(buttonRight - 28, targetTop + 2, 28, 12, Text.literal("Chest"),
            workingCopy.getAimAssistTarget() == HuzzConfig.AimAssistTarget.CHEST,
            button -> {
                workingCopy.setAimAssistTarget(HuzzConfig.AimAssistTarget.CHEST);
                rebuildScreen();
            }));

        addStepButtons(columnLayout, moduleTop, 2, "-5%", "+5%", true,
            () -> workingCopy.setAimAssistStrength(workingCopy.getAimAssistStrength() - 0.05D),
            () -> workingCopy.setAimAssistStrength(workingCopy.getAimAssistStrength() + 0.05D));

        int visibleTop = rowTop(moduleTop, 3);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, visibleTop + 2, 28, 12,
            Text.literal(workingCopy.isAimAssistVisibleOnly() ? "On" : "Off"),
            workingCopy.isAimAssistVisibleOnly(),
            button -> {
                workingCopy.setAimAssistVisibleOnly(!workingCopy.isAimAssistVisibleOnly());
                rebuildScreen();
            }));
    }

    private void addHudToggle(ColumnLayout columnLayout, int moduleTop, int rowIndex, boolean enabled, Runnable toggleAction) {
        int top = rowTop(moduleTop, rowIndex);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, top + 2, 28, 12,
            Text.literal(enabled ? "On" : "Off"),
            enabled,
            button -> {
                toggleAction.run();
                rebuildScreen();
            }));
    }

    private void addMotionBlurWidgets(ColumnLayout columnLayout, int moduleTop) {
        int blurTop = rowTop(moduleTop, 0);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, blurTop + 2, 28, 12,
            Text.literal(workingCopy.isMotionBlurEnabled() ? "On" : "Off"),
            workingCopy.isMotionBlurEnabled(),
            button -> {
                workingCopy.setMotionBlurEnabled(!workingCopy.isMotionBlurEnabled());
                rebuildScreen();
            }));

        addStepButtons(columnLayout, moduleTop, 1, "-1", "+1", true,
            () -> workingCopy.setMotionBlurFrames(workingCopy.getMotionBlurFrames() - 1),
            () -> workingCopy.setMotionBlurFrames(workingCopy.getMotionBlurFrames() + 1));
    }

    private void addNoRenderWidgets(ColumnLayout columnLayout, int moduleTop) {
        int weatherTop = rowTop(moduleTop, 0);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, weatherTop + 2, 28, 12,
            Text.literal(workingCopy.isNoRenderWeather() ? "On" : "Off"),
            workingCopy.isNoRenderWeather(),
            button -> {
                workingCopy.setNoRenderWeather(!workingCopy.isNoRenderWeather());
                rebuildScreen();
            }));

        int particlesTop = rowTop(moduleTop, 1);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, particlesTop + 2, 28, 12,
            Text.literal(workingCopy.isNoRenderParticles() ? "On" : "Off"),
            workingCopy.isNoRenderParticles(),
            button -> {
                workingCopy.setNoRenderParticles(!workingCopy.isNoRenderParticles());
                rebuildScreen();
            }));

        int fogTop = rowTop(moduleTop, 2);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, fogTop + 2, 28, 12,
            Text.literal(workingCopy.isNoRenderOverworldFog() ? "On" : "Off"),
            workingCopy.isNoRenderOverworldFog(),
            button -> {
                workingCopy.setNoRenderOverworldFog(!workingCopy.isNoRenderOverworldFog());
                rebuildScreen();
            }));

        int cullingTop = rowTop(moduleTop, 3);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, cullingTop + 2, 28, 12,
            Text.literal(workingCopy.isNoRenderChunkCulling() ? "On" : "Off"),
            workingCopy.isNoRenderChunkCulling(),
            button -> {
                workingCopy.setNoRenderChunkCulling(!workingCopy.isNoRenderChunkCulling());
                rebuildScreen();
            }));
    }

    private void addBlockSelectWidgets(ColumnLayout columnLayout, int moduleTop) {
        int modeTop = rowTop(moduleTop, 0);
        int right = columnLayout.innerRight() - 4;
        HuzzConfig.BlockSelectionMode selectionMode = workingCopy.getBlockSelectionMode();
        addDrawableChild(new HuzzActionButton(right - 70, modeTop + 2, 22, 12, Text.literal("Off"),
            selectionMode == HuzzConfig.BlockSelectionMode.OFF,
            button -> {
                workingCopy.setBlockSelectionMode(HuzzConfig.BlockSelectionMode.OFF);
                rebuildScreen();
            }));
        addDrawableChild(new HuzzActionButton(right - 46, modeTop + 2, 22, 12, Text.literal("Box"),
            selectionMode == HuzzConfig.BlockSelectionMode.FILLED,
            button -> {
                workingCopy.setBlockSelectionMode(HuzzConfig.BlockSelectionMode.FILLED);
                rebuildScreen();
            }));
        addDrawableChild(new HuzzActionButton(right - 22, modeTop + 2, 22, 12, Text.literal("Wire"),
            selectionMode == HuzzConfig.BlockSelectionMode.WIREFRAME,
            button -> {
                workingCopy.setBlockSelectionMode(HuzzConfig.BlockSelectionMode.WIREFRAME);
                rebuildScreen();
            }));

        int colorTop = rowTop(moduleTop, 1);
        addDrawableChild(new HuzzActionButton(right - 58, colorTop + 2, 16, 12, Text.literal("<"), false,
            button -> {
                workingCopy.setBlockSelectionColor(previousHighlightColor(workingCopy.getBlockSelectionColor()));
                rebuildScreen();
            }));
        addDrawableChild(new HuzzActionButton(right - 18, colorTop + 2, 16, 12, Text.literal(">"), false,
            button -> {
                workingCopy.setBlockSelectionColor(nextHighlightColor(workingCopy.getBlockSelectionColor()));
                rebuildScreen();
            }));
    }

    private void addBlockNotifierWidgets(ColumnLayout columnLayout, int moduleTop) {
        int addTop = rowTop(moduleTop, 1);
        blockNotifierField = addTextField(columnLayout, addTop, blockNotifierDraft, 48, "block id");
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, addTop + 2, 28, 12, Text.literal("Add"), true, button -> {
            captureDrafts();
            workingCopy.addBlockNotifierTarget(blockNotifierDraft);
            blockNotifierDraft = "";
            rebuildScreen();
        }));

        List<String> targets = workingCopy.getBlockNotifierTargets();
        for (int index = 0; index < targets.size(); index++) {
            int targetTop = rowTop(moduleTop, 2 + index);
            String target = targets.get(index);
            addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, targetTop + 2, 28, 12, Text.literal("Rem"), false, button -> {
                workingCopy.removeBlockNotifierTarget(target);
                rebuildScreen();
            }));
        }
    }

    private void addChatMacroWidgets(ColumnLayout columnLayout, int moduleTop) {
        int addTop = rowTop(moduleTop, 0);
        chatMacroField = addTextField(columnLayout, addTop, chatMacroDraft, 256, "message");
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, addTop + 2, 28, 12, Text.literal("Add"), true, button -> {
            captureDrafts();
            if (workingCopy.addChatMacro(chatMacroDraft, chatMacroDraftKeyCode)) {
                chatMacroDraft = "";
                chatMacroDraftKeyCode = HuzzConfig.NO_KEY;
            }
            rebuildScreen();
        }));

        int bindTop = rowTop(moduleTop, 1);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - KEY_BUTTON_WIDTH - 4, bindTop + 2, KEY_BUTTON_WIDTH, 12,
            Text.literal(listeningForChatMacroKey ? "Press key" : (chatMacroDraftKeyCode == HuzzConfig.NO_KEY
                ? "No key"
                : InputUtil.Type.KEYSYM.createFromCode(chatMacroDraftKeyCode).getLocalizedText().getString())),
            listeningForChatMacroKey,
            button -> {
                listeningForChatMacroKey = !listeningForChatMacroKey;
                listeningForKeybind = Module.NONE;
                rebuildScreen();
            }));

        List<HuzzConfig.ChatMacroEntry> macros = workingCopy.getChatMacros();
        for (int index = 0; index < macros.size(); index++) {
            int macroTop = rowTop(moduleTop, 2 + index);
            HuzzConfig.ChatMacroEntry entry = macros.get(index);
            addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, macroTop + 2, 28, 12, Text.literal("Rem"), false, button -> {
                workingCopy.removeChatMacro(entry);
                rebuildScreen();
            }));
        }
    }

    private void addSwingSpeedWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-0.25", "+0.25", true,
            () -> workingCopy.setSwingSpeedMultiplier(workingCopy.getSwingSpeedMultiplier() - 0.25D),
            () -> workingCopy.setSwingSpeedMultiplier(workingCopy.getSwingSpeedMultiplier() + 0.25D));
    }

    private void addDoubleClickWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-50", "+50", true,
            () -> workingCopy.setDoubleClickDelayMs(workingCopy.getDoubleClickDelayMs() - 50),
            () -> workingCopy.setDoubleClickDelayMs(workingCopy.getDoubleClickDelayMs() + 50));
    }

    private void addAutoReconnectWidgets(ColumnLayout columnLayout, int moduleTop) {
        int fieldTop = rowTop(moduleTop, 0);
        autoReconnectField = addTextField(columnLayout, fieldTop, autoReconnectDraft, 128, "server ip");
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, fieldTop + 2, 28, 12, Text.literal("Set"), true, button -> {
            captureDrafts();
            workingCopy.setAutoReconnectAddress(autoReconnectDraft);
            rebuildScreen();
        }));
        addStepButtons(columnLayout, moduleTop, 1, "-1s", "+1s", true,
            () -> workingCopy.setAutoReconnectDelayMs(workingCopy.getAutoReconnectDelayMs() - 1000),
            () -> workingCopy.setAutoReconnectDelayMs(workingCopy.getAutoReconnectDelayMs() + 1000));
    }

    private void addFreeCamWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-1", "+1", true,
            () -> workingCopy.setFreeCamSpeed(workingCopy.getFreeCamSpeed() - 1),
            () -> workingCopy.setFreeCamSpeed(workingCopy.getFreeCamSpeed() + 1));
        addKeybindButton(columnLayout, moduleTop, 1, Module.FREE_CAM);
    }

    private void addKeyOnlyWidgets(ColumnLayout columnLayout, int moduleTop, Module module) {
        addKeybindButton(columnLayout, moduleTop, 0, module);
    }

    private void addFastUseWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-1", "+1", true,
            () -> workingCopy.setFastUseCooldownTicks(workingCopy.getFastUseCooldownTicks() - 1),
            () -> workingCopy.setFastUseCooldownTicks(workingCopy.getFastUseCooldownTicks() + 1));
    }

    private void addFullBrightWidgets(ColumnLayout columnLayout, int moduleTop) {
        int methodTop = rowTop(moduleTop, 0);
        int buttonRight = columnLayout.innerRight() - 4;
        addDrawableChild(new HuzzActionButton(buttonRight - 58, methodTop + 2, 28, 12, Text.literal("Gam"),
            workingCopy.getFullBrightMethod() == HuzzConfig.FullBrightMethod.GAMMA,
            button -> {
                workingCopy.setFullBrightMethod(HuzzConfig.FullBrightMethod.GAMMA);
                rebuildScreen();
            }));
        addDrawableChild(new HuzzActionButton(buttonRight - 28, methodTop + 2, 28, 12, Text.literal("NVis"),
            workingCopy.getFullBrightMethod() == HuzzConfig.FullBrightMethod.NIGHT_VISION,
            button -> {
                workingCopy.setFullBrightMethod(HuzzConfig.FullBrightMethod.NIGHT_VISION);
                rebuildScreen();
            }));

        int fadeTop = rowTop(moduleTop, 1);
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, fadeTop + 2, 28, 12,
            Text.literal(workingCopy.isFullBrightFade() ? "On" : "Off"),
            workingCopy.isFullBrightFade(),
            button -> {
                workingCopy.setFullBrightFade(!workingCopy.isFullBrightFade());
                rebuildScreen();
            }));

        addStepButtons(columnLayout, moduleTop, 2, "-5%", "+5%", true,
            () -> workingCopy.setFullBrightDefaultGamma(workingCopy.getFullBrightDefaultGamma() - 0.05D),
            () -> workingCopy.setFullBrightDefaultGamma(workingCopy.getFullBrightDefaultGamma() + 0.05D));
        addKeybindButton(columnLayout, moduleTop, 3, Module.FULL_BRIGHT);
    }

    private void addCustomFovWidgets(ColumnLayout columnLayout, int moduleTop) {
        int top = rowTop(moduleTop, 0);
        int sliderLeft = columnLayout.innerLeft() + 38;
        int sliderRight = columnLayout.innerRight() - 4;
        int sliderWidth = Math.max(32, sliderRight - sliderLeft);
        int minFov = HuzzConfig.MIN_CUSTOM_FOV_DEGREES;
        int maxFov = HuzzConfig.MAX_CUSTOM_FOV_DEGREES;
        double range = maxFov - minFov;
        double sliderValue = (workingCopy.getCustomFovDegrees() - minFov) / range;

        addDrawableChild(new SliderWidget(sliderLeft, top + 2, sliderWidth, 12, Text.empty(), sliderValue) {
            @Override
            protected void updateMessage() {
                setMessage(isHovered() ? Text.literal(Integer.toString(workingCopy.getCustomFovDegrees())) : Text.empty());
            }

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
                updateMessage();
                super.renderWidget(context, mouseX, mouseY, deltaTicks);
            }

            @Override
            protected void applyValue() {
                int value = minFov + (int) Math.round(this.value * range);
                workingCopy.setCustomFovDegrees(value);
            }
        });
    }

    private void addAutoTotemWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-50", "+50", true,
            () -> workingCopy.setAutoTotemDelayMs(workingCopy.getAutoTotemDelayMs() - 50),
            () -> workingCopy.setAutoTotemDelayMs(workingCopy.getAutoTotemDelayMs() + 50));
    }

    private void addAutoRocketWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-1s", "+1s", true,
            () -> workingCopy.setAutoRocketDelayMs(workingCopy.getAutoRocketDelayMs() - 1000),
            () -> workingCopy.setAutoRocketDelayMs(workingCopy.getAutoRocketDelayMs() + 1000));
    }

    private void addAutoCrystalWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-1", "+1", true,
            () -> workingCopy.setAutoCrystalDelayMs(workingCopy.getAutoCrystalDelayMs() - 1),
            () -> workingCopy.setAutoCrystalDelayMs(workingCopy.getAutoCrystalDelayMs() + 1));
    }

    private void addNameProtectWidgets(ColumnLayout columnLayout, int moduleTop) {
        int fieldTop = rowTop(moduleTop, 0);
        nameProtectField = addTextField(columnLayout, fieldTop, nameProtectDraft, 24, "name");
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, fieldTop + 2, 28, 12, Text.literal("Set"), true, button -> {
            captureDrafts();
            workingCopy.setNameProtectName(nameProtectDraft);
            rebuildScreen();
        }));
    }

    private void addSkinProtectWidgets(ColumnLayout columnLayout, int moduleTop) {
        int fieldTop = rowTop(moduleTop, 0);
        skinProtectField = addTextField(columnLayout, fieldTop, skinProtectDraft, 16, "username");
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, fieldTop + 2, 28, 12, Text.literal("Set"), true, button -> {
            captureDrafts();
            workingCopy.setSkinProtectName(skinProtectDraft);
            rebuildScreen();
        }));
    }

    private void addFakeSpawnerWidgets(ColumnLayout columnLayout, int moduleTop) {
        int fieldTop = rowTop(moduleTop, 0);
        fakeSpawnerField = addTextField(columnLayout, fieldTop, fakeSpawnerDraft, 64, "block id");
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, fieldTop + 2, 28, 12, Text.literal("Set"), true, button -> {
            captureDrafts();
            workingCopy.setFakeSpawnerBlockId(fakeSpawnerDraft);
            rebuildScreen();
        }));
    }

    private void addFakeElytraWidgets(ColumnLayout columnLayout, int moduleTop) {
        int fieldTop = rowTop(moduleTop, 0);
        fakeElytraField = addTextField(columnLayout, fieldTop, fakeElytraDraft, 64, "item id");
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, fieldTop + 2, 28, 12, Text.literal("Set"), true, button -> {
            captureDrafts();
            workingCopy.setFakeElytraItemId(fakeElytraDraft);
            rebuildScreen();
        }));
    }

    private void addFakeStatsWidgets(ColumnLayout columnLayout, int moduleTop) {
        int modeTop = rowTop(moduleTop, 0);
        int buttonRight = columnLayout.innerRight() - 4;
        addDrawableChild(new HuzzActionButton(buttonRight - 58, modeTop + 2, 28, 12, Text.literal("Edit"),
            workingCopy.getFakeStatsMode() == HuzzConfig.FakeStatsMode.EDIT_ALL,
            button -> {
                workingCopy.setFakeStatsMode(HuzzConfig.FakeStatsMode.EDIT_ALL);
                rebuildScreen();
            }));
        addDrawableChild(new HuzzActionButton(buttonRight - 28, modeTop + 2, 28, 12, Text.literal("Live"),
            workingCopy.getFakeStatsMode() == HuzzConfig.FakeStatsMode.KEEP_REAL_STATS_AND_UPDATE_MONEY,
            button -> {
                workingCopy.setFakeStatsMode(HuzzConfig.FakeStatsMode.KEEP_REAL_STATS_AND_UPDATE_MONEY);
                rebuildScreen();
            }));

        if (workingCopy.getFakeStatsMode() != HuzzConfig.FakeStatsMode.EDIT_ALL) {
            return;
        }

        fakeStatsTitleField = addTextField(columnLayout, rowTop(moduleTop, 1), fakeStatsTitleDraft, 32, "title");
        fakeStatsMoneyField = addTextField(columnLayout, rowTop(moduleTop, 2), fakeStatsMoneyDraft, 24, "money");
        fakeStatsShardsField = addTextField(columnLayout, rowTop(moduleTop, 3), fakeStatsShardsDraft, 24, "shards");
        fakeStatsKillsField = addTextField(columnLayout, rowTop(moduleTop, 4), fakeStatsKillsDraft, 24, "kills");
        fakeStatsDeathsField = addTextField(columnLayout, rowTop(moduleTop, 5), fakeStatsDeathsDraft, 24, "deaths");
        fakeStatsKeyallField = addTextField(columnLayout, rowTop(moduleTop, 6), fakeStatsKeyallDraft, 24, "keyall");
        fakeStatsPlaytimeField = addTextField(columnLayout, rowTop(moduleTop, 7), fakeStatsPlaytimeDraft, 24, "playtime");
        fakeStatsTeamField = addTextField(columnLayout, rowTop(moduleTop, 8), fakeStatsTeamDraft, 32, "team");
        fakeStatsRegionField = addTextField(columnLayout, rowTop(moduleTop, 9), fakeStatsRegionDraft, 32, "region");
    }

    private void addCrafterMacroWidgets(ColumnLayout columnLayout, int moduleTop) {
        int totalWidth = GRID_BUTTON_SIZE * 3 + GRID_BUTTON_GAP * 2;
        int left = columnLayout.innerLeft() + Math.max(6, (columnLayout.innerWidth() - totalWidth) / 2);
        for (int row = 0; row < 3; row++) {
            int y = rowTop(moduleTop, row) + 1;
            for (int col = 0; col < 3; col++) {
                int slot = row * 3 + col;
                boolean selected = workingCopy.isCrafterSlotSelected(slot);
                int x = left + col * (GRID_BUTTON_SIZE + GRID_BUTTON_GAP);
                addDrawableChild(new HuzzActionButton(x, y, GRID_BUTTON_SIZE, 14, Text.literal(selected ? "X" : ""), selected, button -> {
                    workingCopy.setCrafterSlotSelected(slot, !workingCopy.isCrafterSlotSelected(slot));
                    rebuildScreen();
                }));
            }
        }
    }

    private TextFieldWidget addTextField(ColumnLayout columnLayout, int top, String value, int maxLength, String placeholder) {
        int fieldLeft = columnLayout.innerLeft() + 50;
        int fieldWidth = columnLayout.innerWidth() - 86;
        TextFieldWidget field = new TextFieldWidget(textRenderer, fieldLeft, top + 1, Math.max(32, fieldWidth), 14, Text.literal(placeholder));
        field.setMaxLength(maxLength);
        field.setDrawsBackground(false);
        field.setEditableColor(HuzzUi.TEXT_PRIMARY);
        field.setUneditableColor(HuzzUi.TEXT_MUTED);
        field.setText(value);
        addDrawableChild(field);
        return field;
    }

    private void addFooterButtons(Layout layout) {
        addDrawableChild(new HuzzActionButton(layout.right() - 130, layout.bottom() - 16, 36, 12, Text.literal("Reset"), false, button -> {
            captureDrafts();
            workingCopy.copyFrom(new HuzzConfig());
            blockEspDraft = "";
            storageEspDraft = "";
            blockNotifierDraft = "";
            fakeSpawnerDraft = "";
            fakeElytraDraft = "";
            resetDraftsFromWorkingCopy();
            listeningForKeybind = Module.NONE;
            listeningForChatMacroKey = false;
            rebuildScreen();
        }));
        addDrawableChild(new HuzzActionButton(layout.right() - 88, layout.bottom() - 16, 38, 12, Text.literal("Panic"), false, button -> activatePanic()));
        addDrawableChild(new HuzzActionButton(layout.right() - 44, layout.bottom() - 16, 32, 12, Text.literal("Done"), true, button -> close()));
    }

    private void addModuleToggle(ColumnLayout columnLayout, Module module, int moduleTop) {
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - TOGGLE_WIDTH - 4, moduleTop + 2, TOGGLE_WIDTH, 12,
            Text.literal(isEnabled(module) ? "On" : "Off"),
            isEnabled(module),
            button -> {
                toggleModule(module);
                rebuildScreen();
            }));
    }

    private void addStepButtons(ColumnLayout columnLayout, int moduleTop, int rowIndex, String minusLabel, String plusLabel, boolean accent, Runnable minusAction, Runnable plusAction) {
        int top = rowTop(moduleTop, rowIndex);
        int right = columnLayout.innerRight() - 4;
        addDrawableChild(new HuzzActionButton(right - 58, top + 2, SMALL_BUTTON_WIDTH, 12, Text.literal(minusLabel), false, button -> {
            minusAction.run();
            rebuildScreen();
        }));
        addDrawableChild(new HuzzActionButton(right - 28, top + 2, SMALL_BUTTON_WIDTH, 12, Text.literal(plusLabel), accent, button -> {
            plusAction.run();
            rebuildScreen();
        }));
    }

    private void addKeybindButton(ColumnLayout columnLayout, int moduleTop, int rowIndex, Module module) {
        int top = rowTop(moduleTop, rowIndex);
        boolean listening = listeningForKeybind == module;
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - KEY_BUTTON_WIDTH - 4, top + 2, KEY_BUTTON_WIDTH, 12,
            Text.literal(keybindLabel(module, listening)),
            listening,
            button -> {
                listeningForKeybind = listeningForKeybind == module ? Module.NONE : module;
                rebuildScreen();
            }));
    }

    private void drawColumn(DrawContext context, Layout layout, ColumnLayout columnLayout, ColumnGroup group) {
        HuzzUi.drawPanel(
            context,
            columnLayout.left(),
            layout.top(),
            columnLayout.right(),
            layout.top() + COLUMN_HEADER_HEIGHT + 4,
            4,
            HuzzUi.withAlpha(HuzzUi.PANEL_BACKGROUND_ALT, 214),
            HuzzUi.withAlpha(HuzzUi.TEXT_SECONDARY, 90)
        );
        HuzzUi.drawHeaderBar(
            context,
            columnLayout.left() + 1,
            layout.top() + 1,
            columnLayout.right() - 1,
            layout.top() + COLUMN_HEADER_HEIGHT + 1,
            HuzzUi.withAlpha(HuzzUi.PANEL_OUTLINE, 118)
        );
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(group.title().toUpperCase(Locale.ROOT)), columnLayout.centerX(), layout.top() + 5, HuzzUi.TEXT_PRIMARY);

        int moduleTop = columnLayout.bodyTop() - scrollOffset;
        for (Module module : group.modules()) {
            if (isVisible(layout, moduleTop, moduleHeight(module))) {
                drawModule(context, columnLayout, module, moduleTop);
            }
            moduleTop += moduleHeight(module) + MODULE_GAP;
        }
    }

    private void drawModule(DrawContext context, ColumnLayout columnLayout, Module module, int top) {
        int bottom = top + moduleHeight(module);
        int accent = accentColor(module);
        HuzzUi.drawPanel(
            context,
            columnLayout.innerLeft(),
            top,
            columnLayout.innerRight(),
            bottom,
            4,
            HuzzUi.withAlpha(moduleFillColor(module), 228),
            HuzzUi.withAlpha(HuzzUi.TEXT_SECONDARY, 88)
        );
        HuzzUi.drawHeaderBar(context, columnLayout.innerLeft() + 1, top + 1, columnLayout.innerRight() - 1, top + MODULE_HEADER_HEIGHT, HuzzUi.withAlpha(accent, 118));
        context.drawTextWithShadow(textRenderer, Text.literal(isCollapsed(module) ? "+" : "-"), columnLayout.innerLeft() + 4, top + 4, HuzzUi.TEXT_PRIMARY);
        context.drawTextWithShadow(textRenderer, Text.literal(moduleTitle(module).toUpperCase(Locale.ROOT)), columnLayout.innerLeft() + 12, top + 4, HuzzUi.TEXT_PRIMARY);

        int liveCount = liveCount(module);
        if (liveCount >= 0) {
            int chipRight = columnLayout.innerRight() - TOGGLE_WIDTH - 8;
            HuzzUi.drawChip(context, chipRight - 18, top + 2, chipRight, top + 14, liveCount > 0);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(Integer.toString(liveCount)), chipRight - 9, top + 5, HuzzUi.TEXT_PRIMARY);
        }

        if (isCollapsed(module)) {
            return;
        }

        switch (module) {
            case BLOCK_ESP, STORAGE_ESP -> {
                boolean blockEsp = module == Module.BLOCK_ESP;
                boolean tracers = blockEsp ? workingCopy.isBlockEspTracers() : workingCopy.isStorageEspTracers();
                List<String> targets = blockEsp ? workingCopy.getBlockEspTargets() : workingCopy.getStorageEspTargets();
                drawOptionRow(context, columnLayout, top, 0, "Tracer", tracers ? "On" : "Off", tracers);
                drawOptionRow(context, columnLayout, top, 1, "Bind", keybindLabel(module, listeningForKeybind == module), true);
                drawInputRow(context, columnLayout, top, 2, "Add");
                if (targets.isEmpty()) {
                    drawOptionRow(context, columnLayout, top, 3, "List", "<none>", false);
                } else {
                    for (int index = 0; index < targets.size(); index++) {
                        drawOptionRow(context, columnLayout, top, 3 + index, index == 0 ? "List" : "", targets.get(index), false);
                    }
                }
            }
            case STASH_FINDER -> drawOptionRow(context, columnLayout, top, 0, "Count", Integer.toString(workingCopy.getStashFinderThreshold()), workingCopy.isStashFinderEnabled());
            case HOLE_ESP -> {
                drawOptionRow(context, columnLayout, top, 0, "Type", "1x1 + 1x3 / 5+", workingCopy.isHoleEspEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Scope", "loaded chunks", true);
            }
            case BEDROCK_HOLE_ESP -> {
                drawOptionRow(context, columnLayout, top, 0, "Type", "bedrock 2x1x1", workingCopy.isBedrockHoleEspEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Scope", "loaded chunks", true);
            }
            case BREAK_PROGRESS -> drawOptionRow(context, columnLayout, top, 0, "Bar", "above crosshair", workingCopy.isBreakProgressEnabled());
            case BLOCK_NOTIFIER -> {
                List<String> targets = workingCopy.getBlockNotifierTargets();
                drawOptionRow(context, columnLayout, top, 0, "Range", workingCopy.getBlockEspRangeBlocks() + "b", true);
                drawInputRow(context, columnLayout, top, 1, "Add");
                if (targets.isEmpty()) {
                    drawOptionRow(context, columnLayout, top, 2, "List", "<none>", false);
                } else {
                    for (int index = 0; index < targets.size(); index++) {
                        drawOptionRow(context, columnLayout, top, 2 + index, index == 0 ? "List" : "", targets.get(index), false);
                    }
                }
            }
            case CHAT_MACRO -> {
                List<HuzzConfig.ChatMacroEntry> macros = workingCopy.getChatMacros();
                drawInputRow(context, columnLayout, top, 0, "Msg");
                drawOptionRow(context, columnLayout, top, 1, "Bind", listeningForChatMacroKey
                    ? "Press key"
                    : (chatMacroDraftKeyCode == HuzzConfig.NO_KEY
                        ? "None"
                        : InputUtil.Type.KEYSYM.createFromCode(chatMacroDraftKeyCode).getLocalizedText().getString()), true);
                if (macros.isEmpty()) {
                    drawOptionRow(context, columnLayout, top, 2, "List", "<none>", false);
                } else {
                    for (int index = 0; index < macros.size(); index++) {
                        HuzzConfig.ChatMacroEntry entry = macros.get(index);
                        drawOptionRow(context, columnLayout, top, 2 + index, index == 0 ? "List" : "",
                            keyName(entry.getKeyCode()) + " " + entry.getMessage(), false);
                    }
                }
            }
            case PLAYER_NOTIFIER -> drawOptionRow(context, columnLayout, top, 0, "Toast", "on players", workingCopy.isPlayerNotifierEnabled());
            case AUTO_DISCONNECT -> drawOptionRow(context, columnLayout, top, 0, "Leave", "on players", workingCopy.isAutoDisconnectEnabled());
            case PLAYER_ESP -> drawOptionRow(context, columnLayout, top, 0, "Draw", "box+trace", workingCopy.isPlayerEspEnabled());
            case MOB_ESP -> {
                drawOptionRow(context, columnLayout, top, 0, "Draw", "box+trace", workingCopy.isMobEspEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Tracer", workingCopy.isMobEspTracers() ? "On" : "Off", workingCopy.isMobEspTracers());
            }
            case AIM_FOV -> drawOptionRow(context, columnLayout, top, 0, "Radius", workingCopy.getAimFovRadius() + "px", workingCopy.isAimFovEnabled());
            case SWING_SPEED -> drawOptionRow(context, columnLayout, top, 0, "Multi", formatMultiplier(workingCopy.getSwingSpeedMultiplier()), workingCopy.isSwingSpeedEnabled());
            case DOUBLE_CLICK -> drawOptionRow(context, columnLayout, top, 0, "Delay", workingCopy.getDoubleClickDelayMs() + "ms", workingCopy.isDoubleClickEnabled());
            case FREE_CAM -> {
                drawOptionRow(context, columnLayout, top, 0, "Speed", workingCopy.getFreeCamSpeed() + "x", workingCopy.isFreeCamEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Bind", keybindLabel(module, listeningForKeybind == module), true);
            }
            case AUTO_REPLENISH -> drawOptionRow(context, columnLayout, top, 0, "Fill", "same block", workingCopy.isAutoReplenishEnabled());
            case AUTO_BRIDGE -> drawOptionRow(context, columnLayout, top, 0, "Mode", "back+sneak+use", workingCopy.isAutoBridgeEnabled());
            case TUNNEL_BASE_FINDER -> drawOptionRow(context, columnLayout, top, 0, "Mode", workingCopy.getTunnelBaseFinderMode().label(), workingCopy.isTunnelBaseFinderEnabled());
            case AIM_ASSIST -> {
                drawOptionRow(context, columnLayout, top, 0, "Slot", workingCopy.getAimAssistSlot() == 0 ? "Any" : Integer.toString(workingCopy.getAimAssistSlot()), workingCopy.isAimAssistEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Target", workingCopy.getAimAssistTarget().label(), true);
                drawOptionRow(context, columnLayout, top, 2, "Power", formatPercent(workingCopy.getAimAssistStrength()), true);
                drawOptionRow(context, columnLayout, top, 3, "Visible", workingCopy.isAimAssistVisibleOnly() ? "Only" : "Any", workingCopy.isAimAssistVisibleOnly());
            }
            case AUTO_TOOL -> drawOptionRow(context, columnLayout, top, 0, "Pick", "best hotbar", workingCopy.isAutoToolEnabled());
            case AUTO_EAT -> drawOptionRow(context, columnLayout, top, 0, "When", "<= 6 hunger", workingCopy.isAutoEatEnabled());
            case FREE_LOOK, ITEM_NAMETAGS -> drawOptionRow(context, columnLayout, top, 0, "Bind", keybindLabel(module, listeningForKeybind == module), true);
            case FAST_USE -> drawOptionRow(context, columnLayout, top, 0, "Cooldown", workingCopy.getFastUseCooldownTicks() + "t", workingCopy.isFastUseEnabled());
            case FULL_BRIGHT -> {
                drawOptionRow(context, columnLayout, top, 0, "Mode", workingCopy.getFullBrightMethod() == HuzzConfig.FullBrightMethod.GAMMA ? "Gamma" : "NVis", workingCopy.isFullBrightEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Fade", workingCopy.isFullBrightFade() ? "On" : "Off", workingCopy.isFullBrightFade());
                drawOptionRow(context, columnLayout, top, 2, "Gamma", formatPercent(workingCopy.getFullBrightDefaultGamma()), true);
                drawOptionRow(context, columnLayout, top, 3, "Bind", keybindLabel(module, listeningForKeybind == module), true);
            }
            case CUSTOM_FOV -> drawOptionRow(context, columnLayout, top, 0, "FOV", "Hover slider", true);
            case MOTION_BLUR -> {
                drawOptionRow(context, columnLayout, top, 0, "State", workingCopy.isMotionBlurEnabled() ? "On" : "Off", workingCopy.isMotionBlurEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Frames", Integer.toString(workingCopy.getMotionBlurFrames()), true);
            }
            case TIME_CHANGER -> drawOptionRow(context, columnLayout, top, 0, "Time", formatTicks(workingCopy.getTimeChangerValue()), workingCopy.isTimeChangerEnabled());
            case NO_RENDER -> {
                drawOptionRow(context, columnLayout, top, 0, "Weather", workingCopy.isNoRenderWeather() ? "Off" : "On", workingCopy.isNoRenderWeather());
                drawOptionRow(context, columnLayout, top, 1, "Particle", workingCopy.isNoRenderParticles() ? "Off" : "On", workingCopy.isNoRenderParticles());
                drawOptionRow(context, columnLayout, top, 2, "OW Fog", workingCopy.isNoRenderOverworldFog() ? "Off" : "On", workingCopy.isNoRenderOverworldFog());
                drawOptionRow(context, columnLayout, top, 3, "Chunk", workingCopy.isNoRenderChunkCulling() ? "Off" : "On", workingCopy.isNoRenderChunkCulling());
            }
            case BLOCK_SELECT -> {
                drawOptionRow(context, columnLayout, top, 0, "Mode", workingCopy.getBlockSelectionMode().label(), workingCopy.getBlockSelectionMode() != HuzzConfig.BlockSelectionMode.OFF);
                drawOptionRow(context, columnLayout, top, 1, "Color", workingCopy.getBlockSelectionColor().label(), true);
            }
            case FAKE_MEDIA -> drawOptionRow(context, columnLayout, top, 0, "Prefix", "dark blue +", workingCopy.isFakeMediaEnabled());
            case FAKE_PAY -> drawOptionRow(context, columnLayout, top, 0, "Cmd", "/pay name 1k", workingCopy.isFakePayEnabled());
            case FAKE_STATS -> {
                drawOptionRow(context, columnLayout, top, 0, "Mode", workingCopy.getFakeStatsMode() == HuzzConfig.FakeStatsMode.EDIT_ALL ? "Edit" : "Live", true);
                if (workingCopy.getFakeStatsMode() == HuzzConfig.FakeStatsMode.EDIT_ALL) {
                    drawInputRow(context, columnLayout, top, 1, "Title");
                    drawInputRow(context, columnLayout, top, 2, "Money");
                    drawInputRow(context, columnLayout, top, 3, "Shards");
                    drawInputRow(context, columnLayout, top, 4, "Kills");
                    drawInputRow(context, columnLayout, top, 5, "Deaths");
                    drawInputRow(context, columnLayout, top, 6, "Keyall");
                    drawInputRow(context, columnLayout, top, 7, "Play");
                    drawInputRow(context, columnLayout, top, 8, "Team");
                    drawInputRow(context, columnLayout, top, 9, "Region");
                } else {
                    drawOptionRow(context, columnLayout, top, 1, "Money", "live - pay", false);
                }
            }
            case AUTO_TOTEM -> drawOptionRow(context, columnLayout, top, 0, "Delay", workingCopy.getAutoTotemDelayMs() + "ms", workingCopy.isAutoTotemEnabled());
            case AUTO_ROCKET -> drawOptionRow(context, columnLayout, top, 0, "Delay", formatDurationMs(workingCopy.getAutoRocketDelayMs()), workingCopy.isAutoRocketEnabled());
            case AUTO_CRYSTAL -> drawOptionRow(context, columnLayout, top, 0, "Delay", workingCopy.getAutoCrystalDelayMs() + "ms", workingCopy.isAutoCrystalEnabled());
            case NAME_PROTECT -> drawInputRow(context, columnLayout, top, 0, "Name");
            case SKIN_PROTECT -> drawInputRow(context, columnLayout, top, 0, "User");
            case FAKE_SPAWNER -> drawInputRow(context, columnLayout, top, 0, "Block");
            case FAKE_ELYTRA -> drawInputRow(context, columnLayout, top, 0, "Item");
            case BEDROCK_PROTECT -> drawOptionRow(context, columnLayout, top, 0, "Texture", "netherite", workingCopy.isBedrockProtectEnabled());
            case AUTO_RECONNECT -> {
                drawInputRow(context, columnLayout, top, 0, "IP");
                drawOptionRow(context, columnLayout, top, 1, "Delay", formatDurationMs(workingCopy.getAutoReconnectDelayMs()), true);
            }
            case SILENT_SET_HOME -> drawOptionRow(context, columnLayout, top, 0, "Cmd", "/sethome 1", workingCopy.isSilentSetHomeEnabled());
            case DUAL_ARM_ANIMATION -> drawOptionRow(context, columnLayout, top, 0, "Anim", "dual swing", workingCopy.isDualArmAnimationEnabled());
            case HUD -> {
                drawOptionRow(context, columnLayout, top, 0, "FPS", workingCopy.isHudFpsEnabled() ? "Shown" : "Hidden", workingCopy.isHudFpsEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Map", workingCopy.isHudMinimapEnabled() ? "Shown" : "Hidden", workingCopy.isHudMinimapEnabled());
                drawOptionRow(context, columnLayout, top, 2, "Clock", workingCopy.isHudClockEnabled() ? "Shown" : "Hidden", workingCopy.isHudClockEnabled());
                drawOptionRow(context, columnLayout, top, 3, "Armor", workingCopy.isHudArmorEnabled() ? "Shown" : "Hidden", workingCopy.isHudArmorEnabled());
            }
            case CRAFTER_MACRO -> {
                drawOptionRow(context, columnLayout, top, 0, "Top", crafterRowValue(0), workingCopy.isCrafterMacroEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Mid", crafterRowValue(1), workingCopy.isCrafterMacroEnabled());
                drawOptionRow(context, columnLayout, top, 2, "Bot", crafterRowValue(2), workingCopy.isCrafterMacroEnabled());
            }
            case NONE -> {
            }
        }

        if (needsAutoKeybindRow(module)) {
            drawOptionRow(context, columnLayout, top, optionCount(module) - 1, "Bind", keybindLabel(module, listeningForKeybind == module), true);
        }
    }

    private void drawOptionRow(DrawContext context, ColumnLayout columnLayout, int moduleTop, int rowIndex, String label, String value, boolean accent) {
        int top = rowTop(moduleTop, rowIndex);
        int left = columnLayout.innerLeft() + 2;
        int right = columnLayout.innerRight() - 2;
        HuzzUi.drawRoundedRect(context, left, top, right, top + ROW_HEIGHT, HuzzUi.withAlpha(0xFF000000, 54), 2);
        int valueLeft = left + 4;
        if (!label.isEmpty()) {
            String labelText = label.toUpperCase(Locale.ROOT);
            context.drawText(textRenderer, Text.literal(labelText), left + 4, top + 4, HuzzUi.TEXT_SECONDARY, false);
            valueLeft += textRenderer.getWidth(labelText) + 10;
        }
        context.drawText(textRenderer, Text.literal(clipValue(value).toUpperCase(Locale.ROOT)), valueLeft, top + 4, accent ? HuzzUi.TEXT_PRIMARY : HuzzUi.TEXT_MUTED, false);
    }

    private void drawInputRow(DrawContext context, ColumnLayout columnLayout, int moduleTop, int rowIndex, String label) {
        int top = rowTop(moduleTop, rowIndex);
        int left = columnLayout.innerLeft() + 2;
        int right = columnLayout.innerRight() - 2;
        HuzzUi.drawRoundedRect(context, left, top, right, top + ROW_HEIGHT, HuzzUi.withAlpha(0xFF000000, 54), 2);
        context.drawText(textRenderer, Text.literal(label.toUpperCase(Locale.ROOT)), left + 4, top + 4, HuzzUi.TEXT_SECONDARY, false);
    }

    private void captureDrafts() {
        if (blockEspField != null) {
            blockEspDraft = blockEspField.getText();
        }
        if (storageEspField != null) {
            storageEspDraft = storageEspField.getText();
        }
        if (blockNotifierField != null) {
            blockNotifierDraft = blockNotifierField.getText();
        }
        if (fakeSpawnerField != null) {
            fakeSpawnerDraft = fakeSpawnerField.getText();
        }
        if (fakeElytraField != null) {
            fakeElytraDraft = fakeElytraField.getText();
        }
        if (nameProtectField != null) {
            nameProtectDraft = nameProtectField.getText();
        }
        if (skinProtectField != null) {
            skinProtectDraft = skinProtectField.getText();
        }
        if (chatMacroField != null) {
            chatMacroDraft = chatMacroField.getText();
        }
        if (autoReconnectField != null) {
            autoReconnectDraft = autoReconnectField.getText();
        }
        if (fakeStatsTitleField != null) {
            fakeStatsTitleDraft = fakeStatsTitleField.getText();
        }
        if (fakeStatsMoneyField != null) {
            fakeStatsMoneyDraft = fakeStatsMoneyField.getText();
        }
        if (fakeStatsShardsField != null) {
            fakeStatsShardsDraft = fakeStatsShardsField.getText();
        }
        if (fakeStatsKillsField != null) {
            fakeStatsKillsDraft = fakeStatsKillsField.getText();
        }
        if (fakeStatsDeathsField != null) {
            fakeStatsDeathsDraft = fakeStatsDeathsField.getText();
        }
        if (fakeStatsKeyallField != null) {
            fakeStatsKeyallDraft = fakeStatsKeyallField.getText();
        }
        if (fakeStatsPlaytimeField != null) {
            fakeStatsPlaytimeDraft = fakeStatsPlaytimeField.getText();
        }
        if (fakeStatsTeamField != null) {
            fakeStatsTeamDraft = fakeStatsTeamField.getText();
        }
        if (fakeStatsRegionField != null) {
            fakeStatsRegionDraft = fakeStatsRegionField.getText();
        }

        workingCopy.setNameProtectName(nameProtectDraft);
        workingCopy.setSkinProtectName(skinProtectDraft);
        workingCopy.setFakeSpawnerBlockId(fakeSpawnerDraft);
        workingCopy.setFakeElytraItemId(fakeElytraDraft);
        workingCopy.setFakeStatsTitle(fakeStatsTitleDraft);
        workingCopy.setFakeStatsMoney(fakeStatsMoneyDraft);
        workingCopy.setFakeStatsShards(fakeStatsShardsDraft);
        workingCopy.setFakeStatsKills(fakeStatsKillsDraft);
        workingCopy.setFakeStatsDeaths(fakeStatsDeathsDraft);
        workingCopy.setFakeStatsKeyall(fakeStatsKeyallDraft);
        workingCopy.setFakeStatsPlaytime(fakeStatsPlaytimeDraft);
        workingCopy.setFakeStatsTeam(fakeStatsTeamDraft);
        workingCopy.setFakeStatsRegion(fakeStatsRegionDraft);
        workingCopy.setAutoReconnectAddress(autoReconnectDraft);
    }

    private void saveWorkingCopy() {
        captureDrafts();
        HuzzClient.getConfigManager().getConfig().copyFrom(workingCopy);
        HuzzClient.getConfigManager().save();
    }

    private void rebuildScreen() {
        captureDrafts();
        if (client != null) {
            init(width, height);
        }
    }

    private void activatePanic() {
        captureDrafts();
        workingCopy.disableAllModules();
        if (client != null) {
            HuzzClient.getChatCommandController().triggerPanic(client);
            close();
        } else {
            saveWorkingCopy();
        }
    }

    private void resetDraftsFromWorkingCopy() {
        fakeSpawnerDraft = workingCopy.getFakeSpawnerBlockId();
        fakeElytraDraft = workingCopy.getFakeElytraItemId();
        nameProtectDraft = workingCopy.getNameProtectName();
        skinProtectDraft = workingCopy.getSkinProtectName();
        chatMacroDraft = "";
        chatMacroDraftKeyCode = HuzzConfig.NO_KEY;
        autoReconnectDraft = workingCopy.getAutoReconnectAddress();
        fakeStatsTitleDraft = workingCopy.getFakeStatsTitle();
        fakeStatsMoneyDraft = workingCopy.getFakeStatsMoney();
        fakeStatsShardsDraft = workingCopy.getFakeStatsShards();
        fakeStatsKillsDraft = workingCopy.getFakeStatsKills();
        fakeStatsDeathsDraft = workingCopy.getFakeStatsDeaths();
        fakeStatsKeyallDraft = workingCopy.getFakeStatsKeyall();
        fakeStatsPlaytimeDraft = workingCopy.getFakeStatsPlaytime();
        fakeStatsTeamDraft = workingCopy.getFakeStatsTeam();
        fakeStatsRegionDraft = workingCopy.getFakeStatsRegion();
    }

    private void clearTextFieldRefs() {
        blockEspField = null;
        storageEspField = null;
        blockNotifierField = null;
        fakeSpawnerField = null;
        fakeElytraField = null;
        nameProtectField = null;
        skinProtectField = null;
        chatMacroField = null;
        autoReconnectField = null;
        fakeStatsTitleField = null;
        fakeStatsMoneyField = null;
        fakeStatsShardsField = null;
        fakeStatsKillsField = null;
        fakeStatsDeathsField = null;
        fakeStatsKeyallField = null;
        fakeStatsPlaytimeField = null;
        fakeStatsTeamField = null;
        fakeStatsRegionField = null;
    }

    private void toggleModule(Module module) {
        switch (module) {
            case BLOCK_ESP -> workingCopy.setBlockEspEnabled(!workingCopy.isBlockEspEnabled());
            case STORAGE_ESP -> workingCopy.setStorageEspEnabled(!workingCopy.isStorageEspEnabled());
            case HOLE_ESP -> workingCopy.setHoleEspEnabled(!workingCopy.isHoleEspEnabled());
            case BEDROCK_HOLE_ESP -> workingCopy.setBedrockHoleEspEnabled(!workingCopy.isBedrockHoleEspEnabled());
            case BREAK_PROGRESS -> workingCopy.setBreakProgressEnabled(!workingCopy.isBreakProgressEnabled());
            case STASH_FINDER -> workingCopy.setStashFinderEnabled(!workingCopy.isStashFinderEnabled());
            case BLOCK_NOTIFIER -> workingCopy.setBlockNotifierEnabled(!workingCopy.isBlockNotifierEnabled());
            case CHAT_MACRO -> workingCopy.setChatMacroEnabled(!workingCopy.isChatMacroEnabled());
            case PLAYER_NOTIFIER -> workingCopy.setPlayerNotifierEnabled(!workingCopy.isPlayerNotifierEnabled());
            case AUTO_DISCONNECT -> workingCopy.setAutoDisconnectEnabled(!workingCopy.isAutoDisconnectEnabled());
            case PLAYER_ESP -> workingCopy.setPlayerEspEnabled(!workingCopy.isPlayerEspEnabled());
            case MOB_ESP -> workingCopy.setMobEspEnabled(!workingCopy.isMobEspEnabled());
            case SWING_SPEED -> workingCopy.setSwingSpeedEnabled(!workingCopy.isSwingSpeedEnabled());
            case FREE_CAM -> workingCopy.setFreeCamEnabled(!workingCopy.isFreeCamEnabled());
            case FREE_LOOK -> workingCopy.setFreeLookEnabled(!workingCopy.isFreeLookEnabled());
            case AUTO_REPLENISH -> workingCopy.setAutoReplenishEnabled(!workingCopy.isAutoReplenishEnabled());
            case AUTO_BRIDGE -> workingCopy.setAutoBridgeEnabled(!workingCopy.isAutoBridgeEnabled());
            case TUNNEL_BASE_FINDER -> workingCopy.setTunnelBaseFinderEnabled(!workingCopy.isTunnelBaseFinderEnabled());
            case AUTO_TOOL -> workingCopy.setAutoToolEnabled(!workingCopy.isAutoToolEnabled());
            case AUTO_CRYSTAL -> workingCopy.setAutoCrystalEnabled(!workingCopy.isAutoCrystalEnabled());
            case AUTO_EAT -> workingCopy.setAutoEatEnabled(!workingCopy.isAutoEatEnabled());
            case AIM_ASSIST -> workingCopy.setAimAssistEnabled(!workingCopy.isAimAssistEnabled());
            case AIM_FOV -> workingCopy.setAimFovEnabled(!workingCopy.isAimFovEnabled());
            case FAST_USE -> workingCopy.setFastUseEnabled(!workingCopy.isFastUseEnabled());
            case CUSTOM_FOV -> workingCopy.setCustomFovEnabled(!workingCopy.isCustomFovEnabled());
            case FULL_BRIGHT -> workingCopy.setFullBrightEnabled(!workingCopy.isFullBrightEnabled());
            case MOTION_BLUR -> workingCopy.setMotionBlurEnabled(!workingCopy.isMotionBlurEnabled());
            case TIME_CHANGER -> workingCopy.setTimeChangerEnabled(!workingCopy.isTimeChangerEnabled());
            case NO_RENDER -> workingCopy.setNoRenderEnabled(!workingCopy.isNoRenderEnabled());
            case BLOCK_SELECT -> workingCopy.setBlockSelectionMode(
                workingCopy.getBlockSelectionMode() == HuzzConfig.BlockSelectionMode.OFF
                    ? HuzzConfig.BlockSelectionMode.FILLED
                    : HuzzConfig.BlockSelectionMode.OFF);
            case ITEM_NAMETAGS -> workingCopy.setItemNametagsEnabled(!workingCopy.isItemNametagsEnabled());
            case FAKE_MEDIA -> workingCopy.setFakeMediaEnabled(!workingCopy.isFakeMediaEnabled());
            case FAKE_PAY -> workingCopy.setFakePayEnabled(!workingCopy.isFakePayEnabled());
            case FAKE_STATS -> workingCopy.setFakeStatsEnabled(!workingCopy.isFakeStatsEnabled());
            case AUTO_TOTEM -> workingCopy.setAutoTotemEnabled(!workingCopy.isAutoTotemEnabled());
            case AUTO_ROCKET -> workingCopy.setAutoRocketEnabled(!workingCopy.isAutoRocketEnabled());
            case NAME_PROTECT -> workingCopy.setNameProtectEnabled(!workingCopy.isNameProtectEnabled());
            case SKIN_PROTECT -> workingCopy.setSkinProtectEnabled(!workingCopy.isSkinProtectEnabled());
            case AUTO_RECONNECT -> workingCopy.setAutoReconnectEnabled(!workingCopy.isAutoReconnectEnabled());
            case FAKE_SPAWNER -> workingCopy.setFakeSpawnerEnabled(!workingCopy.isFakeSpawnerEnabled());
            case FAKE_ELYTRA -> workingCopy.setFakeElytraEnabled(!workingCopy.isFakeElytraEnabled());
            case BEDROCK_PROTECT -> workingCopy.setBedrockProtectEnabled(!workingCopy.isBedrockProtectEnabled());
            case SILENT_SET_HOME -> workingCopy.setSilentSetHomeEnabled(!workingCopy.isSilentSetHomeEnabled());
            case DOUBLE_CLICK -> workingCopy.setDoubleClickEnabled(!workingCopy.isDoubleClickEnabled());
            case DUAL_ARM_ANIMATION -> workingCopy.setDualArmAnimationEnabled(!workingCopy.isDualArmAnimationEnabled());
            case HUD -> workingCopy.setHudEnabled(!workingCopy.isHudEnabled());
            case CRAFTER_MACRO -> workingCopy.setCrafterMacroEnabled(!workingCopy.isCrafterMacroEnabled());
            case NONE -> {
            }
        }
    }

    private boolean isEnabled(Module module) {
        return switch (module) {
            case BLOCK_ESP -> workingCopy.isBlockEspEnabled();
            case STORAGE_ESP -> workingCopy.isStorageEspEnabled();
            case HOLE_ESP -> workingCopy.isHoleEspEnabled();
            case BEDROCK_HOLE_ESP -> workingCopy.isBedrockHoleEspEnabled();
            case BREAK_PROGRESS -> workingCopy.isBreakProgressEnabled();
            case STASH_FINDER -> workingCopy.isStashFinderEnabled();
            case BLOCK_NOTIFIER -> workingCopy.isBlockNotifierEnabled();
            case CHAT_MACRO -> workingCopy.isChatMacroEnabled();
            case PLAYER_NOTIFIER -> workingCopy.isPlayerNotifierEnabled();
            case AUTO_DISCONNECT -> workingCopy.isAutoDisconnectEnabled();
            case PLAYER_ESP -> workingCopy.isPlayerEspEnabled();
            case MOB_ESP -> workingCopy.isMobEspEnabled();
            case SWING_SPEED -> workingCopy.isSwingSpeedEnabled();
            case FREE_CAM -> workingCopy.isFreeCamEnabled();
            case FREE_LOOK -> workingCopy.isFreeLookEnabled();
            case AUTO_REPLENISH -> workingCopy.isAutoReplenishEnabled();
            case AUTO_BRIDGE -> workingCopy.isAutoBridgeEnabled();
            case TUNNEL_BASE_FINDER -> workingCopy.isTunnelBaseFinderEnabled();
            case AUTO_TOOL -> workingCopy.isAutoToolEnabled();
            case AUTO_CRYSTAL -> workingCopy.isAutoCrystalEnabled();
            case AUTO_EAT -> workingCopy.isAutoEatEnabled();
            case AIM_ASSIST -> workingCopy.isAimAssistEnabled();
            case AIM_FOV -> workingCopy.isAimFovEnabled();
            case FAST_USE -> workingCopy.isFastUseEnabled();
            case CUSTOM_FOV -> workingCopy.isCustomFovEnabled();
            case FULL_BRIGHT -> workingCopy.isFullBrightEnabled();
            case MOTION_BLUR -> workingCopy.isMotionBlurEnabled();
            case TIME_CHANGER -> workingCopy.isTimeChangerEnabled();
            case NO_RENDER -> workingCopy.isNoRenderEnabled();
            case BLOCK_SELECT -> workingCopy.getBlockSelectionMode() != HuzzConfig.BlockSelectionMode.OFF;
            case ITEM_NAMETAGS -> workingCopy.isItemNametagsEnabled();
            case FAKE_MEDIA -> workingCopy.isFakeMediaEnabled();
            case FAKE_PAY -> workingCopy.isFakePayEnabled();
            case FAKE_STATS -> workingCopy.isFakeStatsEnabled();
            case AUTO_TOTEM -> workingCopy.isAutoTotemEnabled();
            case AUTO_ROCKET -> workingCopy.isAutoRocketEnabled();
            case NAME_PROTECT -> workingCopy.isNameProtectEnabled();
            case SKIN_PROTECT -> workingCopy.isSkinProtectEnabled();
            case AUTO_RECONNECT -> workingCopy.isAutoReconnectEnabled();
            case FAKE_SPAWNER -> workingCopy.isFakeSpawnerEnabled();
            case FAKE_ELYTRA -> workingCopy.isFakeElytraEnabled();
            case BEDROCK_PROTECT -> workingCopy.isBedrockProtectEnabled();
            case SILENT_SET_HOME -> workingCopy.isSilentSetHomeEnabled();
            case DOUBLE_CLICK -> workingCopy.isDoubleClickEnabled();
            case DUAL_ARM_ANIMATION -> workingCopy.isDualArmAnimationEnabled();
            case HUD -> workingCopy.isHudEnabled();
            case CRAFTER_MACRO -> workingCopy.isCrafterMacroEnabled();
            case NONE -> false;
        };
    }

    private void setModuleKeyCode(Module module, int keyCode) {
        HuzzConfig.ModuleKey moduleKey = moduleKey(module);
        if (moduleKey != null) {
            workingCopy.setModuleKeyCode(moduleKey, keyCode);
        }
    }

    private int moduleKeyCode(Module module) {
        HuzzConfig.ModuleKey moduleKey = moduleKey(module);
        return moduleKey == null ? HuzzConfig.NO_KEY : workingCopy.getModuleKeyCode(moduleKey);
    }

    private String keybindLabel(Module module, boolean listening) {
        if (listening) {
            return "Press key";
        }
        int keyCode = moduleKeyCode(module);
        return keyCode == HuzzConfig.NO_KEY ? "None" : InputUtil.Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString();
    }

    private int moduleHeight(Module module) {
        if (isCollapsed(module)) {
            return MODULE_HEADER_HEIGHT + 4;
        }
        return MODULE_HEADER_HEIGHT + 4 + optionBlockHeight(optionCount(module)) + 4;
    }

    private int optionCount(Module module) {
        int baseRows = switch (module) {
            case BLOCK_ESP -> 4 + Math.max(0, workingCopy.getBlockEspTargets().size() - 1);
            case HOLE_ESP, BEDROCK_HOLE_ESP -> 2;
            case BREAK_PROGRESS, PLAYER_NOTIFIER, AUTO_DISCONNECT, PLAYER_ESP, AIM_FOV, SWING_SPEED, AUTO_REPLENISH,
                AUTO_BRIDGE, TUNNEL_BASE_FINDER, AUTO_TOOL, AUTO_EAT, FAKE_MEDIA, FAKE_PAY, FAST_USE, AUTO_TOTEM, AUTO_ROCKET, NAME_PROTECT,
                SKIN_PROTECT, BEDROCK_PROTECT, SILENT_SET_HOME, DUAL_ARM_ANIMATION -> 1;
            case MOB_ESP -> 2;
            case STORAGE_ESP -> 4 + Math.max(0, workingCopy.getStorageEspTargets().size() - 1);
            case AUTO_CRYSTAL -> 1;
            case AIM_ASSIST -> 4;
            case STASH_FINDER -> 1;
            case AUTO_RECONNECT -> 2;
            case BLOCK_NOTIFIER -> 3 + Math.max(0, workingCopy.getBlockNotifierTargets().size() - 1);
            case CHAT_MACRO -> 3 + Math.max(0, workingCopy.getChatMacros().size() - 1);
            case FREE_CAM, FAKE_STATS -> workingCopy.getFakeStatsMode() == HuzzConfig.FakeStatsMode.EDIT_ALL && module == Module.FAKE_STATS ? 10 : 2;
            case FREE_LOOK, ITEM_NAMETAGS -> 1;
            case CUSTOM_FOV, FAKE_SPAWNER, FAKE_ELYTRA, DOUBLE_CLICK -> 1;
            case FULL_BRIGHT -> 4;
            case MOTION_BLUR, BLOCK_SELECT -> 2;
            case TIME_CHANGER -> 1;
            case NO_RENDER -> 4;
            case HUD -> 4;
            case CRAFTER_MACRO -> 3;
            case NONE -> 0;
        };
        return baseRows + (needsAutoKeybindRow(module) ? 1 : 0);
    }

    private boolean needsAutoKeybindRow(Module module) {
        return module != Module.NONE && module != Module.BLOCK_ESP && module != Module.STORAGE_ESP && module != Module.FREE_CAM
            && module != Module.FREE_LOOK && module != Module.FULL_BRIGHT && module != Module.ITEM_NAMETAGS;
    }

    private HuzzConfig.ModuleKey moduleKey(Module module) {
        return switch (module) {
            case BLOCK_ESP -> HuzzConfig.ModuleKey.BLOCK_ESP;
            case STORAGE_ESP -> HuzzConfig.ModuleKey.STORAGE_ESP;
            case HOLE_ESP -> HuzzConfig.ModuleKey.HOLE_ESP;
            case BEDROCK_HOLE_ESP -> HuzzConfig.ModuleKey.BEDROCK_HOLE_ESP;
            case BREAK_PROGRESS -> HuzzConfig.ModuleKey.BREAK_PROGRESS;
            case STASH_FINDER -> HuzzConfig.ModuleKey.STASH_FINDER;
            case BLOCK_NOTIFIER -> HuzzConfig.ModuleKey.BLOCK_NOTIFIER;
            case CHAT_MACRO -> HuzzConfig.ModuleKey.CHAT_MACRO;
            case PLAYER_NOTIFIER -> HuzzConfig.ModuleKey.PLAYER_NOTIFIER;
            case AUTO_DISCONNECT -> HuzzConfig.ModuleKey.AUTO_DISCONNECT;
            case PLAYER_ESP -> HuzzConfig.ModuleKey.PLAYER_ESP;
            case MOB_ESP -> HuzzConfig.ModuleKey.MOB_ESP;
            case SWING_SPEED -> HuzzConfig.ModuleKey.SWING_SPEED;
            case FREE_CAM -> HuzzConfig.ModuleKey.FREE_CAM;
            case FREE_LOOK -> HuzzConfig.ModuleKey.FREE_LOOK;
            case AUTO_REPLENISH -> HuzzConfig.ModuleKey.AUTO_REPLENISH;
            case AUTO_BRIDGE -> HuzzConfig.ModuleKey.AUTO_BRIDGE;
            case TUNNEL_BASE_FINDER -> HuzzConfig.ModuleKey.TUNNEL_BASE_FINDER;
            case AUTO_TOOL -> HuzzConfig.ModuleKey.AUTO_TOOL;
            case AUTO_CRYSTAL -> HuzzConfig.ModuleKey.AUTO_CRYSTAL;
            case AUTO_EAT -> HuzzConfig.ModuleKey.AUTO_EAT;
            case AIM_ASSIST -> HuzzConfig.ModuleKey.AIM_ASSIST;
            case AIM_FOV -> HuzzConfig.ModuleKey.AIM_FOV;
            case FAST_USE -> HuzzConfig.ModuleKey.FAST_USE;
            case CUSTOM_FOV -> HuzzConfig.ModuleKey.CUSTOM_FOV;
            case FULL_BRIGHT -> HuzzConfig.ModuleKey.FULL_BRIGHT;
            case MOTION_BLUR -> HuzzConfig.ModuleKey.MOTION_BLUR;
            case TIME_CHANGER -> HuzzConfig.ModuleKey.TIME_CHANGER;
            case NO_RENDER -> HuzzConfig.ModuleKey.NO_RENDER;
            case BLOCK_SELECT -> HuzzConfig.ModuleKey.BLOCK_SELECT;
            case ITEM_NAMETAGS -> HuzzConfig.ModuleKey.ITEM_NAMETAGS;
            case FAKE_MEDIA -> HuzzConfig.ModuleKey.FAKE_MEDIA;
            case FAKE_PAY -> HuzzConfig.ModuleKey.FAKE_PAY;
            case FAKE_STATS -> HuzzConfig.ModuleKey.FAKE_STATS;
            case AUTO_TOTEM -> HuzzConfig.ModuleKey.AUTO_TOTEM;
            case AUTO_ROCKET -> HuzzConfig.ModuleKey.AUTO_ROCKET;
            case NAME_PROTECT -> HuzzConfig.ModuleKey.NAME_PROTECT;
            case SKIN_PROTECT -> HuzzConfig.ModuleKey.SKIN_PROTECT;
            case AUTO_RECONNECT -> HuzzConfig.ModuleKey.AUTO_RECONNECT;
            case HUD -> HuzzConfig.ModuleKey.HUD;
            case CRAFTER_MACRO -> HuzzConfig.ModuleKey.CRAFTER_MACRO;
            case FAKE_SPAWNER -> HuzzConfig.ModuleKey.FAKE_SPAWNER;
            case FAKE_ELYTRA -> HuzzConfig.ModuleKey.FAKE_ELYTRA;
            case BEDROCK_PROTECT -> HuzzConfig.ModuleKey.BEDROCK_PROTECT;
            case SILENT_SET_HOME -> HuzzConfig.ModuleKey.SILENT_SET_HOME;
            case DOUBLE_CLICK -> HuzzConfig.ModuleKey.DOUBLE_CLICK;
            case DUAL_ARM_ANIMATION -> HuzzConfig.ModuleKey.DUAL_ARM_ANIMATION;
            case NONE -> null;
        };
    }

    private int optionBlockHeight(int rowCount) {
        return rowCount <= 0 ? 0 : rowCount * ROW_HEIGHT + (rowCount - 1) * ROW_GAP;
    }

    private int rowTop(int moduleTop, int rowIndex) {
        return moduleTop + MODULE_HEADER_HEIGHT + 3 + rowIndex * (ROW_HEIGHT + ROW_GAP);
    }

    private int maxScroll(Layout layout) {
        int maxHeight = 0;
        for (ColumnGroup group : COLUMNS) {
            int height = COLUMN_HEADER_HEIGHT + 6;
            for (Module module : group.modules()) {
                height += moduleHeight(module) + MODULE_GAP;
            }
            maxHeight = Math.max(maxHeight, height);
        }
        return Math.max(0, maxHeight - layout.viewportHeight());
    }

    private boolean isCollapsed(Module module) {
        return collapsedModules.contains(module);
    }

    private Module moduleAtHeader(Layout layout, double mouseX, double mouseY) {
        for (int columnIndex = 0; columnIndex < COLUMNS.size(); columnIndex++) {
            ColumnLayout columnLayout = layout.columns().get(columnIndex);
            int moduleTop = columnLayout.bodyTop() - scrollOffset;
            for (Module module : COLUMNS.get(columnIndex).modules()) {
                if (isInside(columnLayout.innerLeft() + 2, moduleTop + 1, columnLayout.innerRight() - TOGGLE_WIDTH - 12, moduleTop + MODULE_HEADER_HEIGHT, mouseX, mouseY)) {
                    return module;
                }
                moduleTop += moduleHeight(module) + MODULE_GAP;
            }
        }
        return Module.NONE;
    }

    private boolean isVisible(Layout layout, int top, int height) {
        return top + height >= layout.viewportTop() && top <= layout.viewportBottom();
    }

    private Layout layout() {
        List<Integer> columnWidths = buildColumnWidths();
        int actualWidth = columnWidths.stream().mapToInt(Integer::intValue).sum() + COLUMN_GAP * (COLUMNS.size() - 1);
        int left = SCREEN_MARGIN;
        int top = SCREEN_MARGIN;
        int bottom = height - 10;
        return new Layout(left, top, Math.min(width - SCREEN_MARGIN, left + actualWidth), bottom, buildColumns(left, top, columnWidths));
    }

    private List<Integer> buildColumnWidths() {
        ArrayList<Integer> widths = new ArrayList<>(COLUMNS.size());
        for (ColumnGroup group : COLUMNS) {
            int widestText = textRenderer == null ? 0 : textRenderer.getWidth(group.title().toUpperCase(Locale.ROOT));
            for (Module module : group.modules()) {
                widestText = Math.max(widestText, textRenderer == null ? 0 : textRenderer.getWidth(moduleTitle(module).toUpperCase(Locale.ROOT)));
            }
            widths.add(Math.max(176, widestText + 88));
        }
        return List.copyOf(widths);
    }

    private List<ColumnLayout> buildColumns(int left, int top, List<Integer> columnWidths) {
        ArrayList<ColumnLayout> columns = new ArrayList<>(COLUMNS.size());
        int currentLeft = left;
        for (int index = 0; index < COLUMNS.size(); index++) {
            int columnWidth = columnWidths.get(index);
            int columnLeft = currentLeft;
            columns.add(new ColumnLayout(columnLeft, columnLeft + columnWidth, top));
            currentLeft += columnWidth + COLUMN_GAP;
        }
        return List.copyOf(columns);
    }

    private String footerMessage() {
        if (listeningForKeybind != Module.NONE) {
            return "Press a key for " + moduleTitle(listeningForKeybind) + " | ESC clears";
        }
        if (listeningForChatMacroKey) {
            return "Press a key for Chat Macro | ESC clears";
        }
        return "ESC saves and closes";
    }

    private String moduleTitle(Module module) {
        return switch (module) {
            case BLOCK_ESP -> "Block ESP";
            case STORAGE_ESP -> "Storage ESP";
            case HOLE_ESP -> "Hole ESP";
            case BEDROCK_HOLE_ESP -> "Bedrock Hole ESP";
            case BREAK_PROGRESS -> "Break Bar";
            case STASH_FINDER -> "Stash Finder";
            case BLOCK_NOTIFIER -> "Block Notify";
            case CHAT_MACRO -> "Chat Macro";
            case PLAYER_NOTIFIER -> "Player Notify";
            case AUTO_DISCONNECT -> "Auto DC";
            case PLAYER_ESP -> "Player ESP";
            case MOB_ESP -> "Mob ESP";
            case AIM_FOV -> "Aim FOV";
            case SWING_SPEED -> "Swing Speed";
            case FREE_CAM -> "Free Cam";
            case FREE_LOOK -> "Free Look";
            case AUTO_REPLENISH -> "Auto Refill";
            case AUTO_BRIDGE -> "Auto Bridge";
            case TUNNEL_BASE_FINDER -> "Tunnel Finder";
            case AIM_ASSIST -> "Aim Assist";
            case AUTO_TOOL -> "Auto Tool";
            case AUTO_CRYSTAL -> "Auto Crystal";
            case AUTO_EAT -> "Auto Eat";
            case FAST_USE -> "Fast Use";
            case CUSTOM_FOV -> "Custom FOV";
            case FULL_BRIGHT -> "Full Bright";
            case MOTION_BLUR -> "Motion Blur";
            case TIME_CHANGER -> "Time Changer";
            case NO_RENDER -> "No Render";
            case BLOCK_SELECT -> "Block Select";
            case ITEM_NAMETAGS -> "Item Tags";
            case FAKE_MEDIA -> "Fake Media";
            case FAKE_PAY -> "Fake Pay";
            case FAKE_STATS -> "Fake Stats";
            case AUTO_TOTEM -> "Auto Totem";
            case AUTO_ROCKET -> "Auto Rocket";
            case NAME_PROTECT -> "Name Protect";
            case SKIN_PROTECT -> "Skin Protect";
            case AUTO_RECONNECT -> "Auto Rejoin";
            case FAKE_SPAWNER -> "Fake Spawner";
            case FAKE_ELYTRA -> "Fake Elytra";
            case BEDROCK_PROTECT -> "Bedrock Protect";
            case SILENT_SET_HOME -> "Silent Home";
            case DOUBLE_CLICK -> "Double Click";
            case DUAL_ARM_ANIMATION -> "67";
            case HUD -> "HUD";
            case CRAFTER_MACRO -> "Crafter";
            case NONE -> "";
        };
    }

    private int liveCount(Module module) {
        return switch (module) {
            case BLOCK_ESP -> HuzzClient.getBlockEspTracker().getHighlightedBlocks().size();
            case STORAGE_ESP -> HuzzClient.getStorageEspTracker().getHighlightedBlocks().size();
            case HOLE_ESP -> HuzzClient.getHoleEspTracker().getHighlightedHoles().size();
            case BEDROCK_HOLE_ESP -> HuzzClient.getBedrockHoleEspTracker().getHighlightedHoles().size();
            case CHAT_MACRO -> workingCopy.getChatMacros().size();
            case PLAYER_NOTIFIER, AUTO_DISCONNECT, PLAYER_ESP -> client != null && client.world != null ? Math.max(0, client.world.getPlayers().size() - 1) : -1;
            case MOB_ESP -> {
                if (client == null || client.world == null) {
                    yield -1;
                }
                int mobs = 0;
                for (var entity : client.world.getEntities()) {
                    if (entity instanceof net.minecraft.entity.mob.MobEntity) {
                        mobs++;
                    }
                }
                yield mobs;
            }
            case BLOCK_NOTIFIER -> workingCopy.getBlockNotifierTargets().size();
            case CRAFTER_MACRO -> Integer.bitCount(workingCopy.getCrafterMacroMask());
            case BREAK_PROGRESS, AIM_FOV, SWING_SPEED, FREE_CAM, FREE_LOOK, AUTO_REPLENISH, AUTO_BRIDGE, AIM_ASSIST, AUTO_TOOL, AUTO_CRYSTAL, FAST_USE,
                STASH_FINDER, CUSTOM_FOV, FULL_BRIGHT, MOTION_BLUR, TIME_CHANGER, NO_RENDER, BLOCK_SELECT, ITEM_NAMETAGS, FAKE_MEDIA, FAKE_PAY, FAKE_STATS, AUTO_TOTEM, AUTO_ROCKET,
                NAME_PROTECT, SKIN_PROTECT, AUTO_RECONNECT, FAKE_SPAWNER, FAKE_ELYTRA, BEDROCK_PROTECT, SILENT_SET_HOME, DOUBLE_CLICK, DUAL_ARM_ANIMATION, TUNNEL_BASE_FINDER, AUTO_EAT, HUD, NONE -> -1;
        };
    }

    private int accentColor(Module module) {
        return switch (module) {
            case BLOCK_ESP -> 0xFFC89263;
            case STORAGE_ESP -> 0xFF82C98D;
            case HOLE_ESP -> 0xFFFF6677;
            case BEDROCK_HOLE_ESP -> 0xFFF4F4F4;
            case BREAK_PROGRESS -> 0xFF7EF5B4;
            case STASH_FINDER -> 0xFF68D789;
            case BLOCK_NOTIFIER -> 0xFFFFB96B;
            case CHAT_MACRO -> 0xFF7BD1FF;
            case PLAYER_NOTIFIER -> 0xFF71A8FF;
            case AUTO_DISCONNECT -> 0xFFFF7E7E;
            case PLAYER_ESP -> 0xFF72B4FF;
            case MOB_ESP -> 0xFF77E67A;
            case AIM_FOV -> 0xFFFF74C8;
            case SWING_SPEED -> 0xFF7FD5FF;
            case FREE_CAM -> 0xFF71A8FF;
            case FREE_LOOK -> 0xFF618AFF;
            case AUTO_REPLENISH -> 0xFFF3D77D;
            case AUTO_BRIDGE -> 0xFFFFA36D;
            case TUNNEL_BASE_FINDER -> 0xFFFFCF6B;
            case AIM_ASSIST -> 0xFFFF9B74;
            case AUTO_TOOL -> 0xFFD9F06E;
            case AUTO_CRYSTAL -> 0xFFFF74C8;
            case AUTO_EAT -> 0xFFFFD97E;
            case FAST_USE -> 0xFF65C3FF;
            case CUSTOM_FOV -> 0xFF9BC5FF;
            case FULL_BRIGHT -> 0xFFD6C26B;
            case MOTION_BLUR -> 0xFFA778FF;
            case TIME_CHANGER -> 0xFF7ED8FF;
            case NO_RENDER -> 0xFF95B7FF;
            case BLOCK_SELECT -> 0xFF56D9FF;
            case ITEM_NAMETAGS -> 0xFFB37BFF;
            case FAKE_MEDIA -> 0xFF7DDBFF;
            case FAKE_PAY -> 0xFF7DFFB1;
            case FAKE_STATS -> 0xFF4DD4FF;
            case AUTO_TOTEM -> 0xFFF4D37E;
            case AUTO_ROCKET -> 0xFFFFA85C;
            case NAME_PROTECT -> 0xFFDF8DFF;
            case SKIN_PROTECT -> 0xFFFF9CBE;
            case AUTO_RECONNECT -> 0xFF76E0A8;
            case FAKE_SPAWNER -> 0xFF8E71FF;
            case FAKE_ELYTRA -> 0xFF7FD7C2;
            case BEDROCK_PROTECT -> 0xFFB9BDC6;
            case SILENT_SET_HOME -> 0xFF8FE07A;
            case DOUBLE_CLICK -> 0xFFFFB978;
            case DUAL_ARM_ANIMATION -> 0xFF7DB7FF;
            case HUD -> 0xFF93D7FF;
            case CRAFTER_MACRO -> 0xFFFFC56B;
            case NONE -> HuzzUi.TEXT_SECONDARY;
        };
    }

    private int moduleFillColor(Module module) {
        return switch (module) {
            case BLOCK_ESP -> 0xB226211A;
            case STORAGE_ESP -> 0xB218211A;
            case HOLE_ESP -> 0xB229171C;
            case BEDROCK_HOLE_ESP -> 0xB22B2B2B;
            case BREAK_PROGRESS -> 0xB218241E;
            case STASH_FINDER -> 0xB218231C;
            case BLOCK_NOTIFIER -> 0xB2272117;
            case CHAT_MACRO -> 0xB2172227;
            case PLAYER_NOTIFIER -> 0xB2181F29;
            case AUTO_DISCONNECT -> 0xB2281719;
            case PLAYER_ESP -> 0xB2171E28;
            case MOB_ESP -> 0xB217261B;
            case AIM_FOV -> 0xB2281723;
            case SWING_SPEED -> 0xB2172229;
            case FREE_CAM -> 0xB2181C24;
            case FREE_LOOK -> 0xB2171A27;
            case AUTO_REPLENISH -> 0xB2262217;
            case AUTO_BRIDGE -> 0xB2281E17;
            case TUNNEL_BASE_FINDER -> 0xB2282217;
            case AIM_ASSIST -> 0xB2281B17;
            case AUTO_TOOL -> 0xB2232817;
            case AUTO_CRYSTAL -> 0xB2281723;
            case AUTO_EAT -> 0xB2262217;
            case FAST_USE -> 0xB2172228;
            case CUSTOM_FOV -> 0xB2172030;
            case FULL_BRIGHT -> 0xB2262318;
            case MOTION_BLUR -> 0xB2211A28;
            case TIME_CHANGER -> 0xB217232B;
            case NO_RENDER -> 0xB2192030;
            case BLOCK_SELECT -> 0xB2172328;
            case ITEM_NAMETAGS -> 0xB2211A28;
            case FAKE_MEDIA -> 0xB2182227;
            case FAKE_PAY -> 0xB217241D;
            case FAKE_STATS -> 0xB2172328;
            case AUTO_TOTEM -> 0xB2262217;
            case AUTO_ROCKET -> 0xB2281F17;
            case NAME_PROTECT -> 0xB2231927;
            case SKIN_PROTECT -> 0xB2271921;
            case AUTO_RECONNECT -> 0xB218241F;
            case FAKE_SPAWNER -> 0xB2201B29;
            case FAKE_ELYTRA -> 0xB2182522;
            case BEDROCK_PROTECT -> 0xB2232427;
            case SILENT_SET_HOME -> 0xB218241B;
            case DOUBLE_CLICK -> 0xB2282117;
            case DUAL_ARM_ANIMATION -> 0xB2172030;
            case HUD -> 0xB2172128;
            case CRAFTER_MACRO -> 0xB2272119;
            case NONE -> HuzzUi.PANEL_BACKGROUND_ALT;
        };
    }

    private String crafterRowValue(int row) {
        StringBuilder builder = new StringBuilder(5);
        for (int col = 0; col < 3; col++) {
            if (col > 0) {
                builder.append(' ');
            }
            builder.append(workingCopy.isCrafterSlotSelected(row * 3 + col) ? 'X' : '.');
        }
        return builder.toString();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String formatDurationMs(int durationMs) {
        return String.format(Locale.ROOT, "%.1fs", durationMs / 1000.0D);
    }

    private static String formatMultiplier(double value) {
        return String.format(Locale.ROOT, "%.2fx", value);
    }

    private static String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", value * 100.0D);
    }

    private static String formatTicks(long value) {
        return Long.toString(value) + "t";
    }

    private static String keyName(int keyCode) {
        return keyCode == HuzzConfig.NO_KEY
            ? "None"
            : InputUtil.Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString();
    }

    private static String clipValue(String value) {
        return value.length() > 12 ? value.substring(0, 11) + "..." : value;
    }

    private static HuzzConfig.HighlightColor nextHighlightColor(HuzzConfig.HighlightColor color) {
        HuzzConfig.HighlightColor[] values = HuzzConfig.HighlightColor.values();
        return values[(color.ordinal() + 1) % values.length];
    }

    private static HuzzConfig.HighlightColor previousHighlightColor(HuzzConfig.HighlightColor color) {
        HuzzConfig.HighlightColor[] values = HuzzConfig.HighlightColor.values();
        return values[Math.floorMod(color.ordinal() - 1, values.length)];
    }

    private static boolean isInside(int left, int top, int right, int bottom, double mouseX, double mouseY) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    private enum Module {
        BLOCK_ESP,
        STORAGE_ESP,
        HOLE_ESP,
        BEDROCK_HOLE_ESP,
        BREAK_PROGRESS,
        STASH_FINDER,
        BLOCK_NOTIFIER,
        CHAT_MACRO,
        PLAYER_NOTIFIER,
        AUTO_DISCONNECT,
        PLAYER_ESP,
        MOB_ESP,
        AIM_FOV,
        SWING_SPEED,
        FREE_CAM,
        FREE_LOOK,
        AUTO_REPLENISH,
        AUTO_BRIDGE,
        TUNNEL_BASE_FINDER,
        AIM_ASSIST,
        AUTO_TOOL,
        AUTO_CRYSTAL,
        AUTO_EAT,
        FAST_USE,
        CUSTOM_FOV,
        FULL_BRIGHT,
        MOTION_BLUR,
        TIME_CHANGER,
        NO_RENDER,
        BLOCK_SELECT,
        ITEM_NAMETAGS,
        FAKE_MEDIA,
        FAKE_PAY,
        FAKE_STATS,
        AUTO_TOTEM,
        AUTO_ROCKET,
        NAME_PROTECT,
        SKIN_PROTECT,
        AUTO_RECONNECT,
        FAKE_SPAWNER,
        FAKE_ELYTRA,
        BEDROCK_PROTECT,
        SILENT_SET_HOME,
        DOUBLE_CLICK,
        DUAL_ARM_ANIMATION,
        HUD,
        CRAFTER_MACRO,
        NONE
    }

    private record ColumnGroup(String title, List<Module> modules) {
    }

    private record Layout(int left, int top, int right, int bottom, List<ColumnLayout> columns) {
        int viewportTop() { return top + COLUMN_HEADER_HEIGHT + 4; }
        int viewportBottom() { return bottom - FOOTER_HEIGHT; }
        int viewportHeight() { return viewportBottom() - viewportTop(); }
    }

    private record ColumnLayout(int left, int right, int top) {
        int centerX() { return (left + right) / 2; }
        int innerLeft() { return left + 3; }
        int innerRight() { return right - 3; }
        int innerWidth() { return innerRight() - innerLeft(); }
        int bodyTop() { return top + COLUMN_HEADER_HEIGHT + 6; }
    }
}
