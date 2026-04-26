package dev.david.huzzclient.feature;

import dev.david.huzzclient.config.HuzzConfig;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FakeStatsController {
    private static final String FAKE_OBJECTIVE_NAME = "huzzclient.fake_stats";
    private static final int MAX_LINES = 15;
    private static final int MONEY_VALUE_COLOR = 0x00FC00;
    private static final Pattern MONEY_LINE_PATTERN = Pattern.compile("(?i)(money\\s+)(\\$?-?[0-9][0-9,]*(?:\\.[0-9]+)?[kmbt]?)");

    private final HuzzConfigManager configManager;
    private final List<String> fakeScoreHolderNames = new ArrayList<>();

    private double fakePayMoneyDelta;
    private ScoreboardObjective activeOriginalObjective;
    private ScoreboardDisplaySlot activeDisplaySlot = ScoreboardDisplaySlot.SIDEBAR;

    public FakeStatsController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || !configManager.getConfig().isFakeStatsEnabled()) {
            if (!configManager.getConfig().isFakeStatsEnabled()) {
                fakePayMoneyDelta = 0.0D;
            }
        }
    }

    public void clear() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            teardownFakeSidebar(client.world.getScoreboard());
        }
        fakePayMoneyDelta = 0.0D;
        activeOriginalObjective = null;
        activeDisplaySlot = ScoreboardDisplaySlot.SIDEBAR;
    }

    public void recordFakePay(String amount) {
        HuzzConfig config = configManager.getConfig();
        if (!config.isFakeStatsEnabled()) {
            return;
        }

        fakePayMoneyDelta -= parseCompactAmount(amount);
    }

    public boolean beginRender(MinecraftClient client) {
        if (client.world == null || client.options.hudHidden) {
            return false;
        }

        HuzzConfig config = configManager.getConfig();
        Scoreboard scoreboard = client.world.getScoreboard();
        if (!config.isFakeStatsEnabled()) {
            teardownFakeSidebar(scoreboard);
            return false;
        }

        ScoreboardDisplaySlot displaySlot = resolveRenderedSidebarSlot(client, scoreboard);
        ScoreboardObjective originalObjective = scoreboard.getObjectiveForSlot(displaySlot);
        if (isFakeObjective(originalObjective)) {
            originalObjective = activeOriginalObjective;
        }

        activeOriginalObjective = originalObjective;
        activeDisplaySlot = displaySlot;
        if (config.getFakeStatsMode() == HuzzConfig.FakeStatsMode.KEEP_REAL_STATS_AND_UPDATE_MONEY) {
            teardownFakeSidebar(scoreboard);
            return false;
        }

        ScoreboardObjective fakeObjective = ensureFakeObjective(scoreboard);
        fakeObjective.setDisplayName(currentTitle(config, originalObjective));
        populateFakeObjective(scoreboard, fakeObjective, currentLines(config, originalObjective, scoreboard));
        scoreboard.setObjectiveSlot(displaySlot, fakeObjective);
        return false;
    }

    public void endRender(MinecraftClient client) {
        if (client.world == null) {
            activeOriginalObjective = null;
            activeDisplaySlot = ScoreboardDisplaySlot.SIDEBAR;
            return;
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective fakeObjective = scoreboard.getNullableObjective(FAKE_OBJECTIVE_NAME);
        if (fakeObjective != null && scoreboard.getObjectiveForSlot(activeDisplaySlot) == fakeObjective) {
            scoreboard.setObjectiveSlot(activeDisplaySlot, activeOriginalObjective);
        }
        teardownFakeSidebar(scoreboard);
        activeOriginalObjective = null;
        activeDisplaySlot = ScoreboardDisplaySlot.SIDEBAR;
    }

    public LiveSidebarEntry rewriteLiveSidebarEntry(Text renderedName, Text renderedScore) {
        if (!shouldRewriteLiveSidebar()) {
            return new LiveSidebarEntry(renderedName, renderedScore);
        }

        String nameText = renderedName.getString();
        String scoreText = renderedScore.getString();

        String adjustedName = adjustMoneyLine(nameText);
        if (!nameText.equals(adjustedName)) {
            return new LiveSidebarEntry(recolorMoneyLine(renderedName, adjustedName), renderedScore);
        }

        if (!containsMoneyLabel(nameText)) {
            return new LiveSidebarEntry(renderedName, renderedScore);
        }

        String adjustedScore = adjustStandaloneMoneyValue(scoreText);
        if (scoreText.equals(adjustedScore)) {
            return new LiveSidebarEntry(renderedName, renderedScore);
        }

        return new LiveSidebarEntry(renderedName, colorizeValue(renderedScore, adjustedScore, currentMoneyValueColor()));
    }

    private Text currentTitle(HuzzConfig config, ScoreboardObjective originalObjective) {
        if (config.getFakeStatsMode() == HuzzConfig.FakeStatsMode.EDIT_ALL) {
            return Text.literal(config.getFakeStatsTitle()).styled(style -> style.withColor(0x08C4FF));
        }
        return originalObjective == null
            ? Text.literal(config.getFakeStatsTitle()).styled(style -> style.withColor(0x08C4FF))
            : originalObjective.getDisplayName();
    }

    private List<Text> currentLines(HuzzConfig config, ScoreboardObjective originalObjective, Scoreboard scoreboard) {
        return config.getFakeStatsMode() == HuzzConfig.FakeStatsMode.EDIT_ALL
            ? buildEditableSidebar(config)
            : buildFromScoreboard(originalObjective, scoreboard);
    }

    private List<Text> buildEditableSidebar(HuzzConfig config) {
        List<Text> lines = new ArrayList<>();
        lines.add(Text.empty());
        lines.add(Text.empty()
            .append(Text.literal("$ ").styled(style -> style.withColor(0x00FC00)))
            .append(Text.literal("Money ").styled(style -> style.withColor(0xFFFFFF)))
            .append(Text.literal(adjustEditableMoney(config.getFakeStatsMoney())).styled(style -> style.withColor(currentMoneyValueColor()))));
        lines.add(Text.empty()
            .append(Text.literal("* ").styled(style -> style.withColor(0xA303F9)))
            .append(Text.literal("Shards ").styled(style -> style.withColor(0xFFFFFF)))
            .append(Text.literal(config.getFakeStatsShards()).styled(style -> style.withColor(0xA303F9))));
        lines.add(Text.empty()
            .append(Text.literal("/ ").styled(style -> style.withColor(0xFC0000)))
            .append(Text.literal("Kills ").styled(style -> style.withColor(0xFFFFFF)))
            .append(Text.literal(config.getFakeStatsKills()).styled(style -> style.withColor(0xFC0000))));
        lines.add(Text.empty()
            .append(Text.literal("x ").styled(style -> style.withColor(0xF97603)))
            .append(Text.literal("Deaths ").styled(style -> style.withColor(0xFFFFFF)))
            .append(Text.literal(config.getFakeStatsDeaths()).styled(style -> style.withColor(0xF97603))));
        lines.add(Text.empty()
            .append(Text.literal("K ").styled(style -> style.withColor(0x00A4FC)))
            .append(Text.literal("Keyall ").styled(style -> style.withColor(0xFFFFFF)))
            .append(Text.literal(config.getFakeStatsKeyall()).styled(style -> style.withColor(0x00A4FC))));
        lines.add(Text.empty()
            .append(Text.literal("P ").styled(style -> style.withColor(0xFCE300)))
            .append(Text.literal("Playtime ").styled(style -> style.withColor(0xFFFFFF)))
            .append(Text.literal(config.getFakeStatsPlaytime()).styled(style -> style.withColor(0xFCE300))));
        lines.add(Text.empty()
            .append(Text.literal("T ").styled(style -> style.withColor(0x00A4FC)))
            .append(Text.literal("Team ").styled(style -> style.withColor(0xFFFFFF)))
            .append(Text.literal(config.getFakeStatsTeam()).styled(style -> style.withColor(0x00A4FC))));
        lines.add(Text.empty());
        lines.add(Text.literal(config.getFakeStatsRegion()).styled(style -> style.withColor(0xB0B0B0)));
        return List.copyOf(lines);
    }

    private List<Text> buildFromScoreboard(ScoreboardObjective originalObjective, Scoreboard scoreboard) {
        if (originalObjective == null) {
            return List.of();
        }

        List<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(originalObjective).stream()
            .filter(entry -> !entry.hidden())
            .sorted(Comparator.<ScoreboardEntry>comparingInt(ScoreboardEntry::value).reversed().thenComparing(ScoreboardEntry::owner))
            .limit(MAX_LINES)
            .toList();
        if (entries.isEmpty()) {
            return List.of();
        }

        List<Text> lines = new ArrayList<>(entries.size());
        for (ScoreboardEntry entry : entries) {
            Text line = entry.name();
            String plain = line.getString();
            String adjusted = adjustMoneyLine(plain);
            lines.add(plain.equals(adjusted) ? line : Text.literal(adjusted));
        }
        return List.copyOf(lines);
    }

    private void populateFakeObjective(Scoreboard scoreboard, ScoreboardObjective objective, List<Text> lines) {
        clearFakeScores(scoreboard, objective);

        int count = Math.min(MAX_LINES, lines.size());
        for (int index = 0; index < count; index++) {
            String holderName = FAKE_OBJECTIVE_NAME + "." + index;
            ScoreAccess access = scoreboard.getOrCreateScore(ScoreHolder.fromName(holderName), objective);
            access.setScore(count - index);
            access.setDisplayText(lines.get(index));
            access.setNumberFormat(BlankNumberFormat.INSTANCE);
            fakeScoreHolderNames.add(holderName);
        }
    }

    private ScoreboardObjective ensureFakeObjective(Scoreboard scoreboard) {
        ScoreboardObjective objective = scoreboard.getNullableObjective(FAKE_OBJECTIVE_NAME);
        if (objective != null) {
            objective.setNumberFormat(BlankNumberFormat.INSTANCE);
            return objective;
        }

        return scoreboard.addObjective(
            FAKE_OBJECTIVE_NAME,
            ScoreboardCriterion.DUMMY,
            Text.literal("Fake Stats"),
            ScoreboardCriterion.RenderType.INTEGER,
            false,
            BlankNumberFormat.INSTANCE
        );
    }

    private void teardownFakeSidebar(Scoreboard scoreboard) {
        ScoreboardObjective fakeObjective = scoreboard.getNullableObjective(FAKE_OBJECTIVE_NAME);
        if (fakeObjective == null) {
            return;
        }

        if (scoreboard.getObjectiveForSlot(activeDisplaySlot) == fakeObjective) {
            scoreboard.setObjectiveSlot(activeDisplaySlot, activeOriginalObjective);
        }

        clearFakeScores(scoreboard, fakeObjective);
        scoreboard.removeObjective(fakeObjective);
    }

    private void clearFakeScores(Scoreboard scoreboard, ScoreboardObjective objective) {
        for (String holderName : fakeScoreHolderNames) {
            scoreboard.removeScore(ScoreHolder.fromName(holderName), objective);
        }
        fakeScoreHolderNames.clear();
    }

    private ScoreboardDisplaySlot resolveRenderedSidebarSlot(MinecraftClient client, Scoreboard scoreboard) {
        if (client.player != null) {
            Team team = scoreboard.getScoreHolderTeam(client.player.getNameForScoreboard());
            if (team != null) {
                ScoreboardDisplaySlot slot = ScoreboardDisplaySlot.fromFormatting(team.getColor());
                if (slot != null && scoreboard.getObjectiveForSlot(slot) != null) {
                    return slot;
                }
            }
        }
        return ScoreboardDisplaySlot.SIDEBAR;
    }

    private boolean shouldRewriteLiveSidebar() {
        HuzzConfig config = configManager.getConfig();
        return config.isFakeStatsEnabled() && config.getFakeStatsMode() == HuzzConfig.FakeStatsMode.KEEP_REAL_STATS_AND_UPDATE_MONEY;
    }

    private static boolean isFakeObjective(ScoreboardObjective objective) {
        return objective != null && FAKE_OBJECTIVE_NAME.equals(objective.getName());
    }

    private String adjustMoneyLine(String line) {
        Matcher matcher = MONEY_LINE_PATTERN.matcher(line);
        if (!matcher.find()) {
            return line;
        }

        double baseAmount = parseCompactAmount(matcher.group(2));
        double adjustedAmount = baseAmount + fakePayMoneyDelta;
        String replacement = formatCompactAmount(adjustedAmount);
        return line.substring(0, matcher.start(2)) + replacement + line.substring(matcher.end(2));
    }

    private String adjustEditableMoney(String configuredMoney) {
        double configuredAmount = parseCompactAmount(configuredMoney);
        if (configuredAmount == 0.0D && (configuredMoney == null || configuredMoney.isBlank() || fakePayMoneyDelta == 0.0D)) {
            return configuredMoney;
        }

        return formatCompactAmount(configuredAmount + fakePayMoneyDelta);
    }

    private String adjustStandaloneMoneyValue(String scoreText) {
        double baseAmount = parseCompactAmount(scoreText);
        if (baseAmount == 0.0D && (scoreText == null || scoreText.isBlank() || fakePayMoneyDelta == 0.0D)) {
            return scoreText;
        }
        return formatCompactAmount(baseAmount + fakePayMoneyDelta);
    }

    private static boolean containsMoneyLabel(String text) {
        return text != null && text.toLowerCase(Locale.ROOT).contains("money");
    }

    private Text recolorMoneyLine(Text source, String adjustedLine) {
        Matcher matcher = MONEY_LINE_PATTERN.matcher(adjustedLine);
        if (!matcher.find()) {
            return colorizeValue(source, adjustedLine, currentMoneyValueColor());
        }

        MutableText text = Text.empty().setStyle(source.getStyle());
        appendMoneyPrefix(text, adjustedLine.substring(0, matcher.start(2)), source);
        text.append(Text.literal(matcher.group(2)).styled(style -> style.withColor(currentMoneyValueColor())));
        text.append(Text.literal(adjustedLine.substring(matcher.end(2))).setStyle(source.getStyle()));
        return text;
    }

    private void appendMoneyPrefix(MutableText target, String prefix, Text source) {
        int index = 0;
        while (index < prefix.length()) {
            int dollarIndex = prefix.indexOf('$', index);
            if (dollarIndex < 0) {
                target.append(Text.literal(prefix.substring(index)).setStyle(source.getStyle()));
                return;
            }
            if (dollarIndex > index) {
                target.append(Text.literal(prefix.substring(index, dollarIndex)).setStyle(source.getStyle()));
            }
            target.append(Text.literal("$").styled(style -> style.withColor(currentMoneyValueColor())));
            index = dollarIndex + 1;
        }
    }

    private static Text colorizeValue(Text source, String value, int color) {
        return Text.literal(value).styled(style -> source.getStyle().withColor(color));
    }

    private int currentMoneyValueColor() {
        return MONEY_VALUE_COLOR;
    }

    private static double parseCompactAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            return 0.0D;
        }

        String amount = rawAmount.trim().replace("$", "").replace(",", "");
        if (amount.isEmpty()) {
            return 0.0D;
        }

        char suffix = Character.toUpperCase(amount.charAt(amount.length() - 1));
        double multiplier = switch (suffix) {
            case 'K' -> 1_000.0D;
            case 'M' -> 1_000_000.0D;
            case 'B' -> 1_000_000_000.0D;
            case 'T' -> 1_000_000_000_000.0D;
            default -> 1.0D;
        };

        String numericPart = multiplier == 1.0D ? amount : amount.substring(0, amount.length() - 1);
        try {
            return Double.parseDouble(numericPart) * multiplier;
        } catch (NumberFormatException exception) {
            return 0.0D;
        }
    }

    private static String formatCompactAmount(double amount) {
        double abs = Math.abs(amount);
        if (abs >= 1_000_000_000_000.0D) {
            return trimTrailingZeros(amount / 1_000_000_000_000.0D) + "T";
        }
        if (abs >= 1_000_000_000.0D) {
            return trimTrailingZeros(amount / 1_000_000_000.0D) + "B";
        }
        if (abs >= 1_000_000.0D) {
            return trimTrailingZeros(amount / 1_000_000.0D) + "M";
        }
        if (abs >= 1_000.0D) {
            return trimTrailingZeros(amount / 1_000.0D) + "K";
        }
        return trimTrailingZeros(amount);
    }

    private static String trimTrailingZeros(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        if (text.endsWith(".00")) {
            return text.substring(0, text.length() - 3);
        }
        if (text.endsWith("0")) {
            return text.substring(0, text.length() - 1);
        }
        return text;
    }

    public record LiveSidebarEntry(Text name, Text score) {
    }
}
