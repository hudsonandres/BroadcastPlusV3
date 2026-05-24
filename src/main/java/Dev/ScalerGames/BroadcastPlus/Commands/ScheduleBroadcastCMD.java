package Dev.ScalerGames.BroadcastPlus.Commands;

import Dev.ScalerGames.BroadcastPlus.Files.Lang;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Handles the /schedulebroadcast command, which allows admins
 * to view and inspect configured scheduled broadcasts at runtime.
 *
 * Subcommands:
 *   list          — Lists all configured schedules
 *   info <name>   — Shows details of a specific schedule
 *   reload        — Reloads config and restarts the scheduler
 */
public class ScheduleBroadcastCMD implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("list", "info", "reload");

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, String label, String[] args) {
        if (!label.equalsIgnoreCase("schedulebroadcast") && !label.equalsIgnoreCase("sb")) {
            return false;
        }

        // Permission check
        if (!s.hasPermission("bp.schedulebroadcast")) {
            Messages.prefix(s, Lang.getLangConfig().getString("schedule-broadcast-permission"));
            return false;
        }

        if (args.length == 0) {
            Messages.prefix(s, Lang.getLangConfig().getString("schedule-broadcast-usage"));
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                handleList(s);
                break;
            case "info":
                if (args.length < 2) {
                    Messages.prefix(s, Lang.getLangConfig().getString("schedule-broadcast-usage"));
                } else {
                    handleInfo(s, args[1]);
                }
                break;
            case "reload":
                handleReload(s);
                break;
            default:
                Messages.prefix(s, Lang.getLangConfig().getString("schedule-broadcast-usage"));
                break;
        }

        return false;
    }

    /**
     * Lists all configured schedules with their time, timezone, and method.
     */
    private void handleList(CommandSender s) {
        if (!Main.getInstance().getConfig().getBoolean("ScheduledBroadcast.enabled")) {
            Messages.prefix(s, Lang.getLangConfig().getString("schedule-broadcast-disabled"));
            return;
        }

        ConfigurationSection schedulesSection = Main.getInstance().getConfig()
                .getConfigurationSection("ScheduledBroadcast.schedules");

        if (schedulesSection == null || schedulesSection.getKeys(false).isEmpty()) {
            Messages.prefix(s, Lang.getLangConfig().getString("schedule-broadcast-list-empty"));
            return;
        }

        String globalTimezone = Main.getInstance().getConfig().getString("ScheduledBroadcast.timezone", "UTC");
        s.sendMessage(formatColor(Lang.getLangConfig().getString("schedule-broadcast-list-header")));

        for (String name : schedulesSection.getKeys(false)) {
            ConfigurationSection schedule = schedulesSection.getConfigurationSection(name);
            if (schedule == null) continue;

            String time = schedule.getString("time", "N/A");
            String method = schedule.getString("method", "order");

            String entry = Objects.requireNonNull(
                            Lang.getLangConfig().getString("schedule-broadcast-list-entry"))
                    .replace("{name}", name)
                    .replace("{time}", time)
                    .replace("{timezone}", globalTimezone)
                    .replace("{method}", method);

            s.sendMessage(formatColor(entry));
        }
    }

    /**
     * Shows detailed info for a specific named schedule.
     *
     * @param s    the command sender
     * @param name the schedule name from config
     */
    private void handleInfo(CommandSender s, String name) {
        ConfigurationSection schedule = Main.getInstance().getConfig()
                .getConfigurationSection("ScheduledBroadcast.schedules." + name);

        if (schedule == null) {
            Messages.prefix(s, Objects.requireNonNull(
                    Lang.getLangConfig().getString("schedule-broadcast-not-found"))
                    .replace("{name}", name));
            return;
        }

        String globalTimezone = Main.getInstance().getConfig().getString("ScheduledBroadcast.timezone", "UTC");
        String time = schedule.getString("time", "N/A");
        String method = schedule.getString("method", "order");
        List<String> messages = schedule.getStringList("messages");

        s.sendMessage(formatColor(Objects.requireNonNull(
                Lang.getLangConfig().getString("schedule-broadcast-info-header"))
                .replace("{name}", name)));

        s.sendMessage(formatColor(Objects.requireNonNull(
                Lang.getLangConfig().getString("schedule-broadcast-info-time"))
                .replace("{time}", time)));

        s.sendMessage(formatColor(Objects.requireNonNull(
                Lang.getLangConfig().getString("schedule-broadcast-info-timezone"))
                .replace("{timezone}", globalTimezone)));

        s.sendMessage(formatColor(Objects.requireNonNull(
                Lang.getLangConfig().getString("schedule-broadcast-info-method"))
                .replace("{method}", method)));

        s.sendMessage(formatColor(Lang.getLangConfig().getString("schedule-broadcast-info-messages")));

        for (String msg : messages) {
            s.sendMessage(formatColor(Objects.requireNonNull(
                    Lang.getLangConfig().getString("schedule-broadcast-info-message-entry"))
                    .replace("{message}", msg)));
        }
    }

    /**
     * Reloads the config and restarts the scheduled broadcast task.
     */
    private void handleReload(CommandSender s) {
        if (!s.hasPermission("bp.reload")) {
            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-permission"));
            return;
        }
        // Reload is delegated to /bp reload; inform the user to use it
        Messages.prefix(s, "&eUse &f/bp reload &eto reload all files and restart the scheduler.");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd,
                                      @NotNull String label, String[] args) {
        if (!s.hasPermission("bp.schedulebroadcast")) return null;

        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            // Complete subcommand
            SUBCOMMANDS.forEach(sub -> {
                if (sub.startsWith(args[0].toLowerCase())) result.add(sub);
            });
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            // Complete schedule name for /schedulebroadcast info <name>
            ConfigurationSection schedulesSection = Main.getInstance().getConfig()
                    .getConfigurationSection("ScheduledBroadcast.schedules");
            if (schedulesSection != null) {
                Set<String> keys = schedulesSection.getKeys(false);
                keys.forEach(key -> {
                    if (key.startsWith(args[1].toLowerCase())) result.add(key);
                });
            }
            return result;
        }

        return null;
    }

    /** Convenience wrapper to apply color formatting to a lang string. */
    private String formatColor(String msg) {
        if (msg == null) return "";
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', msg);
    }

}
