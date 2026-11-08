package Dev.ScalerGames.BroadcastPlus.Methods;

import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Utils.Format;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BossBar {

    private final Main plugin;
    public int bossID = 1;

    /**
     * Keeps track of every active boss bar instance so that:
     * - Joining players (server switch) are added to all visible bars.
     * - deleteBar() can remove and cancel ALL active bars, not just the last one.
     */
    private final List<org.bukkit.boss.BossBar> activeBars = new ArrayList<>();

    /**
     * Tracks the scheduler task ID for each active bar so that deleteBar()
     * can cancel every running timer, not just the most-recently-created one.
     */
    private final List<Integer> activeTaskIDs = new ArrayList<>();

    public BossBar(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds a joining player to every currently active boss bar so they can
     * see bars that were created before they switched to this server.
     * @param p The player who just joined
     */
    public void addPlayerToActiveBars(Player p) {
        activeBars.forEach(b -> {
            if (b != null && b.isVisible()) b.addPlayer(p);
        });
    }

    /**
     * Creates a new boss bar, registers it in activeBars, starts its timer,
     * and returns the bar instance so callers can add only the intended players.
     *
     * @param time  Duration in seconds
     * @param color Bar color (BarColor name)
     * @param style Bar style (BarStyle name)
     * @param msg   Message to display
     * @return The created BossBar instance
     */
    public org.bukkit.boss.BossBar createBar(Integer time, String color, String style, String msg) {
        org.bukkit.boss.BossBar bar;
        try {
            NamespacedKey key = new NamespacedKey(Main.getInstance(), String.valueOf(bossID));
            bar = Bukkit.createBossBar(key, Format.color(msg), BarColor.valueOf(color), BarStyle.valueOf(style));
            bossID++;
        } catch (Exception e) {
            // Fallback to config defaults if the provided color/style are invalid
            BarColor fallbackColor = BarColor.valueOf(Main.getInstance().getConfig().getString("BossBar.Color", "RED"));
            BarStyle fallbackStyle = BarStyle.valueOf(Main.getInstance().getConfig().getString("BossBar.Style", "SOLID"));
            NamespacedKey key = new NamespacedKey(Main.getInstance(), String.valueOf(bossID));
            bar = Bukkit.createBossBar(key, Format.color(msg), fallbackColor, fallbackStyle);
            bossID++;
        }
        bar.setVisible(true);
        activeBars.add(bar);
        run(bar, time);
        return bar;
    }

    /**
     * Removes all active boss bars from all players and cancels every timer.
     */
    public void deleteBar() {
        for (org.bukkit.boss.BossBar b : activeBars) {
            if (b != null) {
                b.removeAll();
                b.setVisible(false);
            }
        }
        activeBars.clear();
        for (int id : activeTaskIDs) {
            Bukkit.getScheduler().cancelTask(id);
        }
        activeTaskIDs.clear();
    }

    /**
     * Adds a player to the most recently created bar.
     * Prefer using the return value of createBar() and calling addPlayer directly on it.
     * @param p The player to add
     */
    public void addPlayer(Player p) {
        if (!activeBars.isEmpty()) {
            activeBars.get(activeBars.size() - 1).addPlayer(p);
        }
    }

    /**
     * Starts the countdown animation for the given bar.
     * Each bar tracks its own task ID independently.
     *
     * @param bar        The bar to animate
     * @param timeAmount Duration in seconds
     */
    private void run(org.bukkit.boss.BossBar bar, Integer timeAmount) {
        // Wrapper to allow mutation inside the anonymous Runnable
        int[] taskIDHolder = new int[1];

        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            double progress = 1.0;
            final double step = 1.0 / timeAmount;

            @Override
            public void run() {
                if (progress <= 0) {
                    bar.removeAll();
                    bar.setVisible(false);
                    activeBars.remove(bar);
                    activeTaskIDs.remove(Integer.valueOf(taskIDHolder[0]));
                    Bukkit.getScheduler().cancelTask(taskIDHolder[0]);
                } else {
                    bar.setProgress(Math.max(0, progress));
                    progress -= step;
                }
            }
        }, 0, 20);

        taskIDHolder[0] = taskID;
        activeTaskIDs.add(taskID);
    }

}
