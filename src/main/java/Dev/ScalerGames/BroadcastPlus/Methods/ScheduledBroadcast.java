package Dev.ScalerGames.BroadcastPlus.Methods;

import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Utils.Format;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Handles clock-based scheduled broadcasts.
 * Each schedule fires once per day at a configured HH:mm time
 * using the global timezone defined in config.yml.
 */
public class ScheduledBroadcast {

    private final Main plugin;

    /** Tracks the per-schedule message index for "order" method */
    private final Map<String, Integer> messageIndex = new HashMap<>();

    /**
     * Tracks which schedules have already fired in the current minute.
     * Cleared automatically when the minute changes, preventing duplicate sends
     * within the same 30-second polling interval.
     */
    private final Set<String> firedThisMinute = new HashSet<>();

    /** The last minute string we observed — used to detect minute rollovers */
    private String lastMinute = "";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public ScheduledBroadcast(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts the repeating task that polls every 30 seconds (600 ticks).
     * Only runs if ScheduledBroadcast.enabled is true in config.yml.
     */
    public void start() {
        if (!Main.getInstance().getConfig().getBoolean("ScheduledBroadcast.enabled")) {
            return;
        }

        // Validate the configured timezone before starting
        String tzId = Main.getInstance().getConfig().getString("ScheduledBroadcast.timezone", "UTC");
        ZoneId zoneId;
        try {
            zoneId = ZoneId.of(tzId);
        } catch (Exception e) {
            Messages.logger("&4Invalid timezone '" + tzId + "' in ScheduledBroadcast.timezone! Defaulting to UTC.");
            zoneId = ZoneId.of("UTC");
        }

        final ZoneId finalZoneId = zoneId;
        Messages.logger("&2Scheduled broadcasts started using timezone: &f" + finalZoneId.getId());

        // Poll every 30 seconds (600 ticks)
        Main.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(
                Main.getInstance(),
                () -> tick(finalZoneId),
                0L,
                600L
        );
    }

    /**
     * Called every 30 seconds. Checks all configured schedules against the current time.
     *
     * @param zoneId the timezone to evaluate the current time in
     */
    private void tick(ZoneId zoneId) {
        ConfigurationSection schedulesSection = Main.getInstance().getConfig()
                .getConfigurationSection("ScheduledBroadcast.schedules");

        if (schedulesSection == null) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(zoneId);
        String currentMinute = now.format(TIME_FORMATTER);

        // When the minute rolls over, reset the fired-set so schedules can fire again next day
        if (!currentMinute.equals(lastMinute)) {
            firedThisMinute.clear();
            lastMinute = currentMinute;
        }

        // Evaluate each schedule
        for (String scheduleName : schedulesSection.getKeys(false)) {
            ConfigurationSection schedule = schedulesSection.getConfigurationSection(scheduleName);
            if (schedule == null) continue;

            String targetTime = schedule.getString("time", "");

            // Fire only if time matches and hasn't already fired this minute
            if (currentMinute.equals(targetTime) && !firedThisMinute.contains(scheduleName)) {
                firedThisMinute.add(scheduleName);
                fireSchedule(scheduleName, schedule);
            }
        }
    }

    /**
     * Sends the broadcast for a single schedule to all online players.
     *
     * @param scheduleName the schedule key (used for logging and index tracking)
     * @param schedule     the config section for this schedule
     */
    private void fireSchedule(String scheduleName, ConfigurationSection schedule) {
        List<String> messages = schedule.getStringList("messages");
        if (messages.isEmpty()) return;

        String method = schedule.getString("method", "order");
        String rawMsg;

        if (method.equalsIgnoreCase("order")) {
            int index = messageIndex.getOrDefault(scheduleName, 0);
            rawMsg = messages.get(index);
            // Advance index, wrapping around when the list ends
            messageIndex.put(scheduleName, (index + 1) % messages.size());
        } else {
            // Random selection
            rawMsg = messages.get(new Random().nextInt(messages.size()));
        }

        final String msg = Messages.autoBroadcastMSG(rawMsg);

        // Log to console
        Bukkit.getConsoleSender().sendMessage(Format.color(msg));

        // Send to all online players, respecting their auto-broadcast opt-out preference
        Bukkit.getOnlinePlayers().forEach(player -> {
            Boolean optedIn = Main.plugin.autoBroadcast.get(player.getUniqueId());
            if (optedIn == null || optedIn) {
                Features.broadcastChat(msg, player);
            }
        });

        Messages.logger("&7Fired scheduled broadcast: &e" + scheduleName
                + " &7at &f" + Objects.requireNonNull(schedule.getString("time")));
    }

}
