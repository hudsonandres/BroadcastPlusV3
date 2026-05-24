package Dev.ScalerGames.BroadcastPlus.Commands;

import Dev.ScalerGames.BroadcastPlus.Files.Data;
import Dev.ScalerGames.BroadcastPlus.Files.Gui;
import Dev.ScalerGames.BroadcastPlus.Files.Lang;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BroadcastPlusCMD implements CommandExecutor, TabCompleter {

    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("broadcastplus") || label.equalsIgnoreCase("bp")) {
            if (args.length == 0) {
                Messages.prefix(s, "&9This server is running version "
                        + Main.getInstance().getDescription().getVersion() + " of BroadcastPlus made by ScalerGames");
            }

            if (args.length >= 1) {

                if (args[0].equalsIgnoreCase("reload")) {
                    if (CommandCheck.execute(s, "bp.reload", false)) {
                        try {
                            Main.getInstance().reloadConfig();
                            Gui.reloadGui();
                            Lang.reloadLang();
                            Data.reloadData();
                            // Cancel all running tasks and restart the scheduled broadcast engine
                            Main.getInstance().getServer().getScheduler().cancelTasks(Main.getInstance());
                            Main.ab = new Dev.ScalerGames.BroadcastPlus.Methods.AutoBroadcast(Main.getInstance());
                            Main.ab.autoMessage();
                            Main.scheduledBroadcast = new Dev.ScalerGames.BroadcastPlus.Methods.ScheduledBroadcast(Main.getInstance());
                            Main.scheduledBroadcast.start();
                            Messages.logger("&2Successfully reloaded all files");
                            Messages.prefix(s, "&2Successfully reloaded all BroadcastPlus's files");
                        } catch (Exception e) {
                            Messages.logger("&4Failed to reload files");
                        }
                    }
                }

                else if (args[0].equalsIgnoreCase("version")) {
                    if (CommandCheck.execute(s, "bp.version", false)) {
                        Messages.prefix(s, "&9This server is running version "
                                + Main.getInstance().getDescription().getVersion() + " of BroadcastPlus");
                    }
                }

                else if (args[0].equalsIgnoreCase("menu")) {
                    if (CommandCheck.execute(s, "bp.menu", true)) {
                        Messages.prefix(s, "&cThe feature is not ready yet");
                    }
                }

                else if (args[0].equalsIgnoreCase("boss-clear")) {
                    if (CommandCheck.execute(s, "bp.boss-clear", false)) {
                        Main.bar.deleteBar();
                        Messages.prefix(s, "&cCleared all boss bars");
                    }
                }

                else {
                    Messages.prefix(s, "&cUsage: /" + label + " [reload|version|boss-clear]");
                }

            }

        }
        return false;
    }

    List<String> options = Arrays.asList("reload", "version", "boss-clear");
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            options.forEach(option -> {
                if (option.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(option);
            });
            return result;
        }
        return null;
    }

}
