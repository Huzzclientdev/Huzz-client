package dev.david.huzzclient.feature;

import dev.david.huzzclient.HuzzClient;
import dev.david.huzzclient.config.HuzzConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class ChatCommandController {
    private static final int FAKE_PAY_ERROR_COLOR = 0xFF5555;
    private static final int PANIC_MESSAGE_COLOR = 0xFFCC66;

    private final HuzzConfigManager configManager;

    public ChatCommandController(HuzzConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean handleOutgoingMessage(MinecraftClient client, String message) {
        String normalized = message == null ? "" : message.trim();
        if (normalized.equalsIgnoreCase("panic")) {
            triggerPanic(client);
            return true;
        }
        if (normalized.startsWith("/")) {
            return handleOutgoingCommand(client, normalized.substring(1));
        }
        return false;
    }

    public boolean handleOutgoingCommand(MinecraftClient client, String command) {
        String normalized = command == null ? "" : command.trim();
        String[] parts = normalized.split("\\s+");
        if (parts.length > 0 && parts[0].equalsIgnoreCase("panic")) {
            triggerPanic(client);
            return true;
        }

        if (!configManager.getConfig().isFakePayEnabled()) {
            return false;
        }

        if (parts.length == 0 || !parts[0].equalsIgnoreCase("pay")) {
            return false;
        }

        if (parts.length != 3 || !isValidFakePayAmount(parts[2])) {
            client.inGameHud.getChatHud().addMessage(invalidFakePayMessage());
            return true;
        }

        String displayAmount = normalizeFakePayAmount(parts[2], configManager.getConfig().isFakePayUppercaseSuffix());
        HuzzClient.getFakeStatsController().recordFakePay(displayAmount);
        client.inGameHud.getChatHud().addMessage(fakePayMessage(parts[1], displayAmount));
        return true;
    }

    public void triggerPanic(MinecraftClient client) {
        configManager.getConfig().disableAllModules();
        configManager.save();
        HuzzClient.clearRuntimeState(client);
        client.inGameHud.getChatHud().addMessage(Text.literal("Panic activated. All modules disabled.").styled(style -> style.withColor(PANIC_MESSAGE_COLOR)));
    }

    private static Text fakePayMessage(String username, String amount) {
        return Text.empty()
            .append(Text.literal("You paid "))
            .append(Text.literal(username).styled(style -> style.withColor(0x00A4FC)))
            .append(Text.literal(" $" + amount).styled(style -> style.withColor(0x00FC00)))
            .append(Text.literal("."));
    }

    private static Text invalidFakePayMessage() {
        return Text.literal("That number is invalid.").styled(style -> style.withColor(FAKE_PAY_ERROR_COLOR));
    }

    private static String normalizeFakePayAmount(String amount, boolean uppercaseSuffix) {
        if (!uppercaseSuffix || amount == null) {
            return amount;
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)^(\\d+(?:\\.\\d+)?)([kmbt])$").matcher(amount);
        if (matcher.matches()) {
            return matcher.group(1) + matcher.group(2).toUpperCase(java.util.Locale.ROOT);
        }
        return amount;
    }

    private static boolean isValidFakePayAmount(String amount) {
        return amount != null && amount.matches("(?i)(?:[kmbt]\\d+(?:\\.\\d+)?|\\d+(?:\\.\\d+)?[kmbt])");
    }
}
