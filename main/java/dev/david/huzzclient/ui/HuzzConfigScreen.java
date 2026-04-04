package dev.david.huzzclient.ui;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfig;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
    private static final int COLUMN_GAP = 8;
    private static final int COLUMN_HEADER_HEIGHT = 14;
    private static final int MODULE_HEADER_HEIGHT = 16;
    private static final int ROW_HEIGHT = 16;
    private static final int ROW_GAP = 2;
    private static final int MODULE_GAP = 6;
    private static final int FOOTER_HEIGHT = 20;
    private static final int SCROLL_STEP = 18;
    private static final int TOGGLE_WIDTH = 28;
    private static final int SMALL_BUTTON_WIDTH = 28;
    private static final int KEY_BUTTON_WIDTH = 72;
    private static final int GRID_BUTTON_SIZE = 18;
    private static final int GRID_BUTTON_GAP = 4;
    private static final List<ColumnGroup> COLUMNS = List.of(
        new ColumnGroup("Render", List.of(
            Module.CHUNK_FINDER,
            Module.TIME_DEBUG,
            Module.PRIME_CHUNK_FINDER,
            Module.BLOCK_ESP,
            Module.STORAGE_ESP,
            Module.PLAYER_ESP,
            Module.FULL_BRIGHT,
            Module.ITEM_NAMETAGS,
            Module.HUD
        )),
        new ColumnGroup("Movement", List.of(
            Module.FREE_CAM,
            Module.FREE_LOOK,
            Module.FAST_USE,
            Module.AUTO_TOTEM
        )),
        new ColumnGroup("Player", List.of(
            Module.PLAYER_NOTIFIER,
            Module.FAKE_MEDIA,
            Module.FAKE_PAY,
            Module.FAKE_STATS,
            Module.NAME_PROTECT
        )),
        new ColumnGroup("Utility", List.of(
            Module.STASH_FINDER,
            Module.BLOCK_NOTIFIER,
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
    private String nameProtectDraft = "";
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
    private TextFieldWidget nameProtectField;
    private TextFieldWidget fakeStatsTitleField;
    private TextFieldWidget fakeStatsMoneyField;
    private TextFieldWidget fakeStatsShardsField;
    private TextFieldWidget fakeStatsKillsField;
    private TextFieldWidget fakeStatsDeathsField;
    private TextFieldWidget fakeStatsKeyallField;
    private TextFieldWidget fakeStatsPlaytimeField;
    private TextFieldWidget fakeStatsTeamField;
    private TextFieldWidget fakeStatsRegionField;

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
            case CHUNK_FINDER -> addChunkFinderWidgets(columnLayout, moduleTop);
            case TIME_DEBUG -> addTimeDebugWidgets(columnLayout, moduleTop);
            case BLOCK_ESP -> addEspWidgets(columnLayout, moduleTop, true);
            case STORAGE_ESP -> addEspWidgets(columnLayout, moduleTop, false);
            case STASH_FINDER -> addStashFinderWidgets(columnLayout, moduleTop);
            case BLOCK_NOTIFIER -> addBlockNotifierWidgets(columnLayout, moduleTop);
            case FREE_CAM -> addFreeCamWidgets(columnLayout, moduleTop);
            case FREE_LOOK, ITEM_NAMETAGS -> addKeyOnlyWidgets(columnLayout, moduleTop, module);
            case FAST_USE -> addFastUseWidgets(columnLayout, moduleTop);
            case FULL_BRIGHT -> addFullBrightWidgets(columnLayout, moduleTop);
            case FAKE_STATS -> addFakeStatsWidgets(columnLayout, moduleTop);
            case AUTO_TOTEM -> addAutoTotemWidgets(columnLayout, moduleTop);
            case NAME_PROTECT -> addNameProtectWidgets(columnLayout, moduleTop);
            case CRAFTER_MACRO -> addCrafterMacroWidgets(columnLayout, moduleTop);
            case PRIME_CHUNK_FINDER, PLAYER_NOTIFIER, PLAYER_ESP, FAKE_MEDIA, FAKE_PAY, HUD, NONE -> {
            }
        }
    }

    private void addChunkFinderWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-250", "+250", true,
            () -> workingCopy.setRescanRateMs(workingCopy.getRescanRateMs() - 250),
            () -> workingCopy.setRescanRateMs(workingCopy.getRescanRateMs() + 250));
        addStepButtons(columnLayout, moduleTop, 1, "-1", "+1", false,
            () -> workingCopy.setDeltaPacketThreshold(workingCopy.getDeltaPacketThreshold() - 1),
            () -> workingCopy.setDeltaPacketThreshold(workingCopy.getDeltaPacketThreshold() + 1));
        addStepButtons(columnLayout, moduleTop, 2, "-1", "+1", false,
            () -> workingCopy.setRenderHeight(workingCopy.getRenderHeight() - 1),
            () -> workingCopy.setRenderHeight(workingCopy.getRenderHeight() + 1));
        addKeybindButton(columnLayout, moduleTop, 3, Module.CHUNK_FINDER);
    }

    private void addTimeDebugWidgets(ColumnLayout columnLayout, int moduleTop) {
        int modeTop = rowTop(moduleTop, 0);
        int buttonRight = columnLayout.innerRight() - 4;
        addDrawableChild(new HuzzActionButton(buttonRight - 58, modeTop + 2, 28, 12, Text.literal("Find"),
            workingCopy.getTimeDebugMode() == HuzzConfig.TimeDebugMode.CHUNK_FINDER,
            button -> {
                workingCopy.setTimeDebugMode(HuzzConfig.TimeDebugMode.CHUNK_FINDER);
                rebuildScreen();
            }));
        addDrawableChild(new HuzzActionButton(buttonRight - 28, modeTop + 2, 28, 12, Text.literal("Heat"),
            workingCopy.getTimeDebugMode() == HuzzConfig.TimeDebugMode.HEATMAP,
            button -> {
                workingCopy.setTimeDebugMode(HuzzConfig.TimeDebugMode.HEATMAP);
                rebuildScreen();
            }));
        addStepButtons(columnLayout, moduleTop, 1, "-1s", "+1s", true,
            () -> workingCopy.setTimeDebugMarkDelayMs(workingCopy.getTimeDebugMarkDelayMs() - 1000),
            () -> workingCopy.setTimeDebugMarkDelayMs(workingCopy.getTimeDebugMarkDelayMs() + 1000));
        addKeybindButton(columnLayout, moduleTop, 2, Module.TIME_DEBUG);
    }

    private void addEspWidgets(ColumnLayout columnLayout, int moduleTop, boolean blockEsp) {
        addStepButtons(columnLayout, moduleTop, 0, "-8", "+8", true, () -> {
            if (blockEsp) {
                workingCopy.setBlockEspRangeBlocks(workingCopy.getBlockEspRangeBlocks() - 8);
            } else {
                workingCopy.setStorageEspRangeBlocks(workingCopy.getStorageEspRangeBlocks() - 8);
            }
        }, () -> {
            if (blockEsp) {
                workingCopy.setBlockEspRangeBlocks(workingCopy.getBlockEspRangeBlocks() + 8);
            } else {
                workingCopy.setStorageEspRangeBlocks(workingCopy.getStorageEspRangeBlocks() + 8);
            }
        });

        int tracerTop = rowTop(moduleTop, 1);
        boolean tracers = blockEsp ? workingCopy.isBlockEspTracers() : workingCopy.isStorageEspTracers();
        addDrawableChild(new HuzzActionButton(columnLayout.innerRight() - 32, tracerTop + 2, 28, 12, Text.literal(tracers ? "On" : "Off"), tracers, button -> {
            if (blockEsp) {
                workingCopy.setBlockEspTracers(!workingCopy.isBlockEspTracers());
            } else {
                workingCopy.setStorageEspTracers(!workingCopy.isStorageEspTracers());
            }
            rebuildScreen();
        }));

        addKeybindButton(columnLayout, moduleTop, 2, blockEsp ? Module.BLOCK_ESP : Module.STORAGE_ESP);

        int addTop = rowTop(moduleTop, 3);
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
            int targetTop = rowTop(moduleTop, 4 + index);
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

    private void addAutoTotemWidgets(ColumnLayout columnLayout, int moduleTop) {
        addStepButtons(columnLayout, moduleTop, 0, "-50", "+50", true,
            () -> workingCopy.setAutoTotemDelayMs(workingCopy.getAutoTotemDelayMs() - 50),
            () -> workingCopy.setAutoTotemDelayMs(workingCopy.getAutoTotemDelayMs() + 50));
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
            resetDraftsFromWorkingCopy();
            listeningForKeybind = Module.NONE;
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
        HuzzUi.drawPanel(context, columnLayout.left(), layout.top(), columnLayout.right(), layout.bottom(), 2, HuzzUi.PANEL_BACKGROUND, HuzzUi.withAlpha(HuzzUi.TEXT_SECONDARY, 110));
        HuzzUi.drawHeaderBar(context, columnLayout.left() + 1, layout.top() + 1, columnLayout.right() - 1, layout.top() + COLUMN_HEADER_HEIGHT, HuzzUi.PANEL_OUTLINE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(group.title()), columnLayout.centerX(), layout.top() + 4, HuzzUi.TEXT_PRIMARY);

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
        HuzzUi.drawRoundedRect(context, columnLayout.innerLeft(), top, columnLayout.innerRight(), bottom, HuzzUi.withAlpha(HuzzUi.TEXT_SECONDARY, 84), 2);
        HuzzUi.drawRoundedRect(context, columnLayout.innerLeft() + 1, top + 1, columnLayout.innerRight() - 1, bottom - 1, moduleFillColor(module), 1);
        HuzzUi.drawHeaderBar(context, columnLayout.innerLeft() + 1, top + 1, columnLayout.innerRight() - 1, top + MODULE_HEADER_HEIGHT, HuzzUi.withAlpha(accent, 128));
        context.drawTextWithShadow(textRenderer, Text.literal(isCollapsed(module) ? ">" : "v"), columnLayout.innerLeft() + 4, top + 4, HuzzUi.TEXT_PRIMARY);
        context.drawTextWithShadow(textRenderer, Text.literal(moduleTitle(module)), columnLayout.innerLeft() + 12, top + 4, HuzzUi.TEXT_PRIMARY);

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
            case CHUNK_FINDER -> {
                drawOptionRow(context, columnLayout, top, 0, "Rescan", workingCopy.getRescanRateMs() + "ms", true);
                drawOptionRow(context, columnLayout, top, 1, "Thresh", Integer.toString(workingCopy.getDeltaPacketThreshold()), workingCopy.isChunkFinderEnabled());
                drawOptionRow(context, columnLayout, top, 2, "Render", workingCopy.getRenderHeight() + "Y", false);
                drawOptionRow(context, columnLayout, top, 3, "Bind", keybindLabel(module, listeningForKeybind == module), true);
            }
            case TIME_DEBUG -> {
                drawOptionRow(context, columnLayout, top, 0, "Mode", workingCopy.getTimeDebugMode() == HuzzConfig.TimeDebugMode.HEATMAP ? "Heat" : "Find", workingCopy.isTimeDebugEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Delay", formatDurationMs(workingCopy.getTimeDebugMarkDelayMs()), true);
                drawOptionRow(context, columnLayout, top, 2, "Bind", keybindLabel(module, listeningForKeybind == module), true);
            }
            case PRIME_CHUNK_FINDER -> drawOptionRow(context, columnLayout, top, 0, "Logic", "5pkt + time", workingCopy.isPrimeChunkFinderEnabled());
            case BLOCK_ESP, STORAGE_ESP -> {
                boolean blockEsp = module == Module.BLOCK_ESP;
                int range = blockEsp ? workingCopy.getBlockEspRangeBlocks() : workingCopy.getStorageEspRangeBlocks();
                boolean tracers = blockEsp ? workingCopy.isBlockEspTracers() : workingCopy.isStorageEspTracers();
                List<String> targets = blockEsp ? workingCopy.getBlockEspTargets() : workingCopy.getStorageEspTargets();
                drawOptionRow(context, columnLayout, top, 0, "Range", range + "b", true);
                drawOptionRow(context, columnLayout, top, 1, "Tracer", tracers ? "On" : "Off", tracers);
                drawOptionRow(context, columnLayout, top, 2, "Bind", keybindLabel(module, listeningForKeybind == module), true);
                drawInputRow(context, columnLayout, top, 3, "Add");
                if (targets.isEmpty()) {
                    drawOptionRow(context, columnLayout, top, 4, "List", "<none>", false);
                } else {
                    for (int index = 0; index < targets.size(); index++) {
                        drawOptionRow(context, columnLayout, top, 4 + index, index == 0 ? "List" : "", targets.get(index), false);
                    }
                }
            }
            case STASH_FINDER -> drawOptionRow(context, columnLayout, top, 0, "Count", Integer.toString(workingCopy.getStashFinderThreshold()), workingCopy.isStashFinderEnabled());
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
            case PLAYER_NOTIFIER -> drawOptionRow(context, columnLayout, top, 0, "Toast", "on players", workingCopy.isPlayerNotifierEnabled());
            case PLAYER_ESP -> drawOptionRow(context, columnLayout, top, 0, "Draw", "box+trace", workingCopy.isPlayerEspEnabled());
            case FREE_CAM -> {
                drawOptionRow(context, columnLayout, top, 0, "Speed", workingCopy.getFreeCamSpeed() + "x", workingCopy.isFreeCamEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Bind", keybindLabel(module, listeningForKeybind == module), true);
            }
            case FREE_LOOK, ITEM_NAMETAGS -> drawOptionRow(context, columnLayout, top, 0, "Bind", keybindLabel(module, listeningForKeybind == module), true);
            case FAST_USE -> drawOptionRow(context, columnLayout, top, 0, "Cooldown", workingCopy.getFastUseCooldownTicks() + "t", workingCopy.isFastUseEnabled());
            case FULL_BRIGHT -> {
                drawOptionRow(context, columnLayout, top, 0, "Mode", workingCopy.getFullBrightMethod() == HuzzConfig.FullBrightMethod.GAMMA ? "Gamma" : "NVis", workingCopy.isFullBrightEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Fade", workingCopy.isFullBrightFade() ? "On" : "Off", workingCopy.isFullBrightFade());
                drawOptionRow(context, columnLayout, top, 2, "Gamma", formatPercent(workingCopy.getFullBrightDefaultGamma()), true);
                drawOptionRow(context, columnLayout, top, 3, "Bind", keybindLabel(module, listeningForKeybind == module), true);
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
            case NAME_PROTECT -> drawInputRow(context, columnLayout, top, 0, "Name");
            case HUD -> drawOptionRow(context, columnLayout, top, 0, "FPS", workingCopy.isFpsHudEnabled() ? "Shown" : "Hidden", workingCopy.isFpsHudEnabled());
            case CRAFTER_MACRO -> {
                drawOptionRow(context, columnLayout, top, 0, "Top", crafterRowValue(0), workingCopy.isCrafterMacroEnabled());
                drawOptionRow(context, columnLayout, top, 1, "Mid", crafterRowValue(1), workingCopy.isCrafterMacroEnabled());
                drawOptionRow(context, columnLayout, top, 2, "Bot", crafterRowValue(2), workingCopy.isCrafterMacroEnabled());
            }
            case NONE -> {
            }
        }
    }

    private void drawOptionRow(DrawContext context, ColumnLayout columnLayout, int moduleTop, int rowIndex, String label, String value, boolean accent) {
        int top = rowTop(moduleTop, rowIndex);
        int left = columnLayout.innerLeft() + 2;
        int right = columnLayout.innerRight() - 2;
        context.fill(left, top, right, top + ROW_HEIGHT, 0x44000000);
        if (!label.isEmpty()) {
            context.drawText(textRenderer, Text.literal(label), left + 4, top + 4, HuzzUi.TEXT_SECONDARY, false);
        }
        context.drawText(textRenderer, Text.literal(clipValue(value)), left + 38, top + 4, accent ? HuzzUi.TEXT_PRIMARY : HuzzUi.TEXT_MUTED, false);
    }

    private void drawInputRow(DrawContext context, ColumnLayout columnLayout, int moduleTop, int rowIndex, String label) {
        int top = rowTop(moduleTop, rowIndex);
        int left = columnLayout.innerLeft() + 2;
        int right = columnLayout.innerRight() - 2;
        context.fill(left, top, right, top + ROW_HEIGHT, 0x44000000);
        context.drawText(textRenderer, Text.literal(label), left + 4, top + 4, HuzzUi.TEXT_SECONDARY, false);
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
        if (nameProtectField != null) {
            nameProtectDraft = nameProtectField.getText();
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
        workingCopy.setFakeStatsTitle(fakeStatsTitleDraft);
        workingCopy.setFakeStatsMoney(fakeStatsMoneyDraft);
        workingCopy.setFakeStatsShards(fakeStatsShardsDraft);
        workingCopy.setFakeStatsKills(fakeStatsKillsDraft);
        workingCopy.setFakeStatsDeaths(fakeStatsDeathsDraft);
        workingCopy.setFakeStatsKeyall(fakeStatsKeyallDraft);
        workingCopy.setFakeStatsPlaytime(fakeStatsPlaytimeDraft);
        workingCopy.setFakeStatsTeam(fakeStatsTeamDraft);
        workingCopy.setFakeStatsRegion(fakeStatsRegionDraft);
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
        nameProtectDraft = workingCopy.getNameProtectName();
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
        nameProtectField = null;
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
            case CHUNK_FINDER -> workingCopy.setChunkFinderEnabled(!workingCopy.isChunkFinderEnabled());
            case TIME_DEBUG -> workingCopy.setTimeDebugEnabled(!workingCopy.isTimeDebugEnabled());
            case PRIME_CHUNK_FINDER -> workingCopy.setPrimeChunkFinderEnabled(!workingCopy.isPrimeChunkFinderEnabled());
            case BLOCK_ESP -> workingCopy.setBlockEspEnabled(!workingCopy.isBlockEspEnabled());
            case STORAGE_ESP -> workingCopy.setStorageEspEnabled(!workingCopy.isStorageEspEnabled());
            case STASH_FINDER -> workingCopy.setStashFinderEnabled(!workingCopy.isStashFinderEnabled());
            case BLOCK_NOTIFIER -> workingCopy.setBlockNotifierEnabled(!workingCopy.isBlockNotifierEnabled());
            case PLAYER_NOTIFIER -> workingCopy.setPlayerNotifierEnabled(!workingCopy.isPlayerNotifierEnabled());
            case PLAYER_ESP -> workingCopy.setPlayerEspEnabled(!workingCopy.isPlayerEspEnabled());
            case FREE_CAM -> workingCopy.setFreeCamEnabled(!workingCopy.isFreeCamEnabled());
            case FREE_LOOK -> workingCopy.setFreeLookEnabled(!workingCopy.isFreeLookEnabled());
            case FAST_USE -> workingCopy.setFastUseEnabled(!workingCopy.isFastUseEnabled());
            case FULL_BRIGHT -> workingCopy.setFullBrightEnabled(!workingCopy.isFullBrightEnabled());
            case ITEM_NAMETAGS -> workingCopy.setItemNametagsEnabled(!workingCopy.isItemNametagsEnabled());
            case FAKE_MEDIA -> workingCopy.setFakeMediaEnabled(!workingCopy.isFakeMediaEnabled());
            case FAKE_PAY -> workingCopy.setFakePayEnabled(!workingCopy.isFakePayEnabled());
            case FAKE_STATS -> workingCopy.setFakeStatsEnabled(!workingCopy.isFakeStatsEnabled());
            case AUTO_TOTEM -> workingCopy.setAutoTotemEnabled(!workingCopy.isAutoTotemEnabled());
            case NAME_PROTECT -> workingCopy.setNameProtectEnabled(!workingCopy.isNameProtectEnabled());
            case HUD -> workingCopy.setFpsHudEnabled(!workingCopy.isFpsHudEnabled());
            case CRAFTER_MACRO -> workingCopy.setCrafterMacroEnabled(!workingCopy.isCrafterMacroEnabled());
            case NONE -> {
            }
        }
    }

    private boolean isEnabled(Module module) {
        return switch (module) {
            case CHUNK_FINDER -> workingCopy.isChunkFinderEnabled();
            case TIME_DEBUG -> workingCopy.isTimeDebugEnabled();
            case PRIME_CHUNK_FINDER -> workingCopy.isPrimeChunkFinderEnabled();
            case BLOCK_ESP -> workingCopy.isBlockEspEnabled();
            case STORAGE_ESP -> workingCopy.isStorageEspEnabled();
            case STASH_FINDER -> workingCopy.isStashFinderEnabled();
            case BLOCK_NOTIFIER -> workingCopy.isBlockNotifierEnabled();
            case PLAYER_NOTIFIER -> workingCopy.isPlayerNotifierEnabled();
            case PLAYER_ESP -> workingCopy.isPlayerEspEnabled();
            case FREE_CAM -> workingCopy.isFreeCamEnabled();
            case FREE_LOOK -> workingCopy.isFreeLookEnabled();
            case FAST_USE -> workingCopy.isFastUseEnabled();
            case FULL_BRIGHT -> workingCopy.isFullBrightEnabled();
            case ITEM_NAMETAGS -> workingCopy.isItemNametagsEnabled();
            case FAKE_MEDIA -> workingCopy.isFakeMediaEnabled();
            case FAKE_PAY -> workingCopy.isFakePayEnabled();
            case FAKE_STATS -> workingCopy.isFakeStatsEnabled();
            case AUTO_TOTEM -> workingCopy.isAutoTotemEnabled();
            case NAME_PROTECT -> workingCopy.isNameProtectEnabled();
            case HUD -> workingCopy.isFpsHudEnabled();
            case CRAFTER_MACRO -> workingCopy.isCrafterMacroEnabled();
            case NONE -> false;
        };
    }

    private void setModuleKeyCode(Module module, int keyCode) {
        switch (module) {
            case CHUNK_FINDER -> workingCopy.setChunkFinderKeyCode(keyCode);
            case TIME_DEBUG -> workingCopy.setTimeDebugKeyCode(keyCode);
            case BLOCK_ESP -> workingCopy.setBlockEspKeyCode(keyCode);
            case STORAGE_ESP -> workingCopy.setStorageEspKeyCode(keyCode);
            case FREE_CAM -> workingCopy.setFreeCamKeyCode(keyCode);
            case FREE_LOOK -> workingCopy.setFreeLookKeyCode(keyCode);
            case FULL_BRIGHT -> workingCopy.setFullBrightKeyCode(keyCode);
            case ITEM_NAMETAGS -> workingCopy.setItemNametagsKeyCode(keyCode);
            case PRIME_CHUNK_FINDER, STASH_FINDER, BLOCK_NOTIFIER, PLAYER_NOTIFIER, PLAYER_ESP, FAST_USE, FAKE_MEDIA, FAKE_PAY, FAKE_STATS, AUTO_TOTEM, NAME_PROTECT, HUD, CRAFTER_MACRO, NONE -> {
            }
        }
    }

    private int moduleKeyCode(Module module) {
        return switch (module) {
            case CHUNK_FINDER -> workingCopy.getChunkFinderKeyCode();
            case TIME_DEBUG -> workingCopy.getTimeDebugKeyCode();
            case BLOCK_ESP -> workingCopy.getBlockEspKeyCode();
            case STORAGE_ESP -> workingCopy.getStorageEspKeyCode();
            case FREE_CAM -> workingCopy.getFreeCamKeyCode();
            case FREE_LOOK -> workingCopy.getFreeLookKeyCode();
            case FULL_BRIGHT -> workingCopy.getFullBrightKeyCode();
            case ITEM_NAMETAGS -> workingCopy.getItemNametagsKeyCode();
            case PRIME_CHUNK_FINDER, STASH_FINDER, BLOCK_NOTIFIER, PLAYER_NOTIFIER, PLAYER_ESP, FAST_USE, FAKE_MEDIA, FAKE_PAY, FAKE_STATS, AUTO_TOTEM, NAME_PROTECT, HUD, CRAFTER_MACRO, NONE -> HuzzConfig.NO_KEY;
        };
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
        return switch (module) {
            case CHUNK_FINDER -> 4;
            case TIME_DEBUG -> 3;
            case PRIME_CHUNK_FINDER -> 1;
            case BLOCK_ESP -> 5 + Math.max(0, workingCopy.getBlockEspTargets().size() - 1);
            case STORAGE_ESP -> 5 + Math.max(0, workingCopy.getStorageEspTargets().size() - 1);
            case STASH_FINDER -> 1;
            case BLOCK_NOTIFIER -> 3 + Math.max(0, workingCopy.getBlockNotifierTargets().size() - 1);
            case PLAYER_NOTIFIER, PLAYER_ESP, FAKE_MEDIA, FAKE_PAY, FAST_USE, AUTO_TOTEM, NAME_PROTECT, HUD -> 1;
            case FREE_CAM, FAKE_STATS -> workingCopy.getFakeStatsMode() == HuzzConfig.FakeStatsMode.EDIT_ALL && module == Module.FAKE_STATS ? 10 : 2;
            case FREE_LOOK, ITEM_NAMETAGS -> 1;
            case FULL_BRIGHT -> 4;
            case CRAFTER_MACRO -> 3;
            case NONE -> 0;
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
        int availableWidth = width - SCREEN_MARGIN * 2 - COLUMN_GAP * (COLUMNS.size() - 1);
        int columnWidth = Math.max(132, availableWidth / COLUMNS.size());
        int actualWidth = columnWidth * COLUMNS.size() + COLUMN_GAP * (COLUMNS.size() - 1);
        int left = (width - actualWidth) / 2;
        int top = 16;
        int bottom = height - 10;
        return new Layout(left, top, left + actualWidth, bottom, buildColumns(left, top, columnWidth));
    }

    private List<ColumnLayout> buildColumns(int left, int top, int columnWidth) {
        ArrayList<ColumnLayout> columns = new ArrayList<>(COLUMNS.size());
        for (int index = 0; index < COLUMNS.size(); index++) {
            int columnLeft = left + index * (columnWidth + COLUMN_GAP);
            columns.add(new ColumnLayout(columnLeft, columnLeft + columnWidth, top));
        }
        return List.copyOf(columns);
    }

    private String footerMessage() {
        if (listeningForKeybind != Module.NONE) {
            return "Press a key for " + moduleTitle(listeningForKeybind) + " | ESC clears";
        }
        return "ESC saves and closes";
    }

    private String moduleTitle(Module module) {
        return switch (module) {
            case CHUNK_FINDER -> "Chunk Finder";
            case TIME_DEBUG -> "Time Debug";
            case PRIME_CHUNK_FINDER -> "Prime Chunk";
            case BLOCK_ESP -> "Block ESP";
            case STORAGE_ESP -> "Storage ESP";
            case STASH_FINDER -> "Stash Finder";
            case BLOCK_NOTIFIER -> "Block Notify";
            case PLAYER_NOTIFIER -> "Player Notify";
            case PLAYER_ESP -> "Player ESP";
            case FREE_CAM -> "Free Cam";
            case FREE_LOOK -> "Free Look";
            case FAST_USE -> "Fast Use";
            case FULL_BRIGHT -> "Full Bright";
            case ITEM_NAMETAGS -> "Item Tags";
            case FAKE_MEDIA -> "Fake Media";
            case FAKE_PAY -> "Fake Pay";
            case FAKE_STATS -> "Fake Stats";
            case AUTO_TOTEM -> "Auto Totem";
            case NAME_PROTECT -> "Name Protect";
            case HUD -> "HUD";
            case CRAFTER_MACRO -> "Crafter";
            case NONE -> "";
        };
    }

    private int liveCount(Module module) {
        return switch (module) {
            case CHUNK_FINDER -> HuzzClient.getTracker().getMarkedChunks().size();
            case TIME_DEBUG -> workingCopy.getTimeDebugMode() == HuzzConfig.TimeDebugMode.HEATMAP
                ? HuzzClient.getTimeDebugTracker().getHeatmapChunks().size()
                : HuzzClient.getTimeDebugTracker().getMarkedChunks().size();
            case PRIME_CHUNK_FINDER -> HuzzClient.getPrimeChunkFinderTracker().getMarkedChunks().size();
            case BLOCK_ESP -> HuzzClient.getBlockEspTracker().getHighlightedBlocks().size();
            case STORAGE_ESP -> HuzzClient.getStorageEspTracker().getHighlightedBlocks().size();
            case PLAYER_NOTIFIER, PLAYER_ESP -> client != null && client.world != null ? Math.max(0, client.world.getPlayers().size() - 1) : -1;
            case BLOCK_NOTIFIER -> workingCopy.getBlockNotifierTargets().size();
            case CRAFTER_MACRO -> Integer.bitCount(workingCopy.getCrafterMacroMask());
            case FREE_CAM, FREE_LOOK, FAST_USE, STASH_FINDER, FULL_BRIGHT, ITEM_NAMETAGS, FAKE_MEDIA, FAKE_PAY, FAKE_STATS, AUTO_TOTEM, NAME_PROTECT, HUD, NONE -> -1;
        };
    }

    private int accentColor(Module module) {
        return switch (module) {
            case CHUNK_FINDER -> HuzzUi.PANEL_OUTLINE;
            case TIME_DEBUG -> HuzzUi.TIME_DEBUG_OUTLINE;
            case PRIME_CHUNK_FINDER -> 0xFF5DDB7F;
            case BLOCK_ESP -> 0xFFC89263;
            case STORAGE_ESP -> 0xFF82C98D;
            case STASH_FINDER -> 0xFF68D789;
            case BLOCK_NOTIFIER -> 0xFFFFB96B;
            case PLAYER_NOTIFIER -> 0xFF71A8FF;
            case PLAYER_ESP -> 0xFF72B4FF;
            case FREE_CAM -> 0xFF71A8FF;
            case FREE_LOOK -> 0xFF618AFF;
            case FAST_USE -> 0xFF65C3FF;
            case FULL_BRIGHT -> 0xFFD6C26B;
            case ITEM_NAMETAGS -> 0xFFB37BFF;
            case FAKE_MEDIA -> 0xFF7DDBFF;
            case FAKE_PAY -> 0xFF7DFFB1;
            case FAKE_STATS -> 0xFF4DD4FF;
            case AUTO_TOTEM -> 0xFFF4D37E;
            case NAME_PROTECT -> 0xFFDF8DFF;
            case HUD -> 0xFF93D7FF;
            case CRAFTER_MACRO -> 0xFFFFC56B;
            case NONE -> HuzzUi.TEXT_SECONDARY;
        };
    }

    private int moduleFillColor(Module module) {
        return switch (module) {
            case CHUNK_FINDER -> 0xB21B1B21;
            case TIME_DEBUG -> 0xB2191D28;
            case PRIME_CHUNK_FINDER -> 0xB1162519;
            case BLOCK_ESP -> 0xB226211A;
            case STORAGE_ESP -> 0xB218211A;
            case STASH_FINDER -> 0xB218231C;
            case BLOCK_NOTIFIER -> 0xB2272117;
            case PLAYER_NOTIFIER -> 0xB2181F29;
            case PLAYER_ESP -> 0xB2171E28;
            case FREE_CAM -> 0xB2181C24;
            case FREE_LOOK -> 0xB2171A27;
            case FAST_USE -> 0xB2172228;
            case FULL_BRIGHT -> 0xB2262318;
            case ITEM_NAMETAGS -> 0xB2211A28;
            case FAKE_MEDIA -> 0xB2182227;
            case FAKE_PAY -> 0xB217241D;
            case FAKE_STATS -> 0xB2172328;
            case AUTO_TOTEM -> 0xB2262217;
            case NAME_PROTECT -> 0xB2231927;
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

    private static String formatPercent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", value * 100.0D);
    }

    private static String clipValue(String value) {
        return value.length() > 12 ? value.substring(0, 11) + "..." : value;
    }

    private static boolean isInside(int left, int top, int right, int bottom, double mouseX, double mouseY) {
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    private enum Module {
        CHUNK_FINDER,
        TIME_DEBUG,
        PRIME_CHUNK_FINDER,
        BLOCK_ESP,
        STORAGE_ESP,
        STASH_FINDER,
        BLOCK_NOTIFIER,
        PLAYER_NOTIFIER,
        PLAYER_ESP,
        FREE_CAM,
        FREE_LOOK,
        FAST_USE,
        FULL_BRIGHT,
        ITEM_NAMETAGS,
        FAKE_MEDIA,
        FAKE_PAY,
        FAKE_STATS,
        AUTO_TOTEM,
        NAME_PROTECT,
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
