package Dev.ScalerGames.BroadcastPlus;

import Dev.ScalerGames.BroadcastPlus.Commands.AutoBroadcastCMD;
import Dev.ScalerGames.BroadcastPlus.Commands.BroadcastPlusCMD;
import Dev.ScalerGames.BroadcastPlus.Commands.Broadcasting.*;
import Dev.ScalerGames.BroadcastPlus.Files.Config;
import Dev.ScalerGames.BroadcastPlus.Files.Data;
import Dev.ScalerGames.BroadcastPlus.Files.Gui;
import Dev.ScalerGames.BroadcastPlus.Files.Lang;
import Dev.ScalerGames.BroadcastPlus.Commands.ScheduleBroadcastCMD;
import Dev.ScalerGames.BroadcastPlus.Methods.AutoBroadcast;
import Dev.ScalerGames.BroadcastPlus.Methods.BossBar;
import Dev.ScalerGames.BroadcastPlus.Methods.ScheduledBroadcast;
import Dev.ScalerGames.BroadcastPlus.Methods.Gui.GuiListener;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import Dev.ScalerGames.BroadcastPlus.Utils.Metrics;
import Dev.ScalerGames.BroadcastPlus.Utils.Placeholders;
import Dev.ScalerGames.BroadcastPlus.Utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin implements Listener {

    public static Main plugin;
    public static BossBar bar;
    public final Map<UUID, Boolean> autoBroadcast = new HashMap<>();
    public static AutoBroadcast ab;
    public static ScheduledBroadcast scheduledBroadcast;

    @Override
    public void onEnable() {
        plugin = this;
        enableFiles();
        enableCommands();
        enableListeners();
        enablePlugins();
        bar = new BossBar(this);
        ab = new AutoBroadcast(this);
        ab.autoMessage();
        scheduledBroadcast = new ScheduledBroadcast(this);
        scheduledBroadcast.start();
        new Metrics(this, 17055);
        updateChecker();
        retrieveData();
    }

    @Override
    public void onDisable() {
        List<String> storage = Data.getDataConfig().getStringList("auto-broadcast");
        Main.plugin.autoBroadcast.forEach((player, option) -> storage.add(player + ":" + option));
        Data.getDataConfig().set("auto-broadcast", storage);
        Data.saveData();
    }

    public static Main getInstance() {return plugin;}

    public void enableCommands() {
        Objects.requireNonNull(getCommand("broadcast")).setExecutor(new Broadcast());
        Objects.requireNonNull(getCommand("broadcast")).setTabCompleter(new Broadcast());
        Objects.requireNonNull(getCommand("broadcastworld")).setExecutor(new BroadcastWorld());
        Objects.requireNonNull(getCommand("broadcastworld")).setTabCompleter(new BroadcastWorld());
        Objects.requireNonNull(getCommand("localbroadcast")).setExecutor(new LocalBroadcast());
        Objects.requireNonNull(getCommand("localbroadcast")).setTabCompleter(new LocalBroadcast());
        Objects.requireNonNull(getCommand("broadcastplus")).setExecutor(new BroadcastPlusCMD());
        Objects.requireNonNull(getCommand("broadcastplus")).setTabCompleter(new BroadcastPlusCMD());
        Objects.requireNonNull(getCommand("randombroadcast")).setExecutor(new RandomBroadcast());
        Objects.requireNonNull(getCommand("randombroadcast")).setTabCompleter(new Broadcast());
        Objects.requireNonNull(getCommand("permbroadcast")).setExecutor(new PermBroadcast());
        Objects.requireNonNull(getCommand("permbroadcast")).setTabCompleter(new PermBroadcast());
        Objects.requireNonNull(getCommand("practicebroadcast")).setExecutor(new PracticeBroadcast());
        Objects.requireNonNull(getCommand("practicebroadcast")).setTabCompleter(new PracticeBroadcast());
        Objects.requireNonNull(getCommand("autobroadcast")).setExecutor(new AutoBroadcastCMD());
        Objects.requireNonNull(getCommand("schedulebroadcast")).setExecutor(new ScheduleBroadcastCMD());
        Objects.requireNonNull(getCommand("schedulebroadcast")).setTabCompleter(new ScheduleBroadcastCMD());
    }

    public void enableFiles() {
        Config.enableConfig();
        Lang.saveDefaultLang();
        Gui.saveDefaultGui();
        Data.saveDefaultData();
    }

    public void enablePlugins() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getPluginManager().registerEvents(this, this);
            new Placeholders().register();
            Messages.logger("&2Successfully hooked into PlaceholderAPI");
        }
    }

    public void updateChecker() {
        new UpdateChecker(this, 87816).getVersion(version -> {
            if (!getInstance().getDescription().getVersion().equalsIgnoreCase(version)) {
                Messages.logger("&6There is a new update on Spigot");
            } else {
                Messages.logger("&2You are using the latest version of BroadcastPlus");
            }
        });
    }

    public void retrieveData() {
        try {
            Data.getDataConfig().getStringList("auto-broadcast").forEach(settings -> {
                String[] split = settings.split(":"); UUID player = UUID.fromString(split[0]); Boolean option = Boolean.valueOf(split[1]);
                Main.plugin.autoBroadcast.put(player, option);
            });
        } catch (Exception ex) {
            Messages.logger("&4Auto broadcast settings failed to load!");
        }
    }

    public void enableListeners() {
        Bukkit.getPluginManager().registerEvents(new GuiListener(), this);
    }

}
