package Dev.ScalerGames.BroadcastPlus.Commands.Broadcasting;

import Dev.ScalerGames.BroadcastPlus.Commands.CommandCheck;
import Dev.ScalerGames.BroadcastPlus.Files.Lang;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Methods.BroadcastMethods;
import Dev.ScalerGames.BroadcastPlus.Methods.Features;
import Dev.ScalerGames.BroadcastPlus.Methods.Gui.GuiCreator;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PracticeBroadcast implements CommandExecutor, TabCompleter {

    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("practicebroadcast")) {
            if (CommandCheck.execute(s, "bp.practicebroadcast", true)) {
                if (args.length >= 2) {
                    Player p = (Player) s;
                    if (args[0].equalsIgnoreCase("chat")) {
                        Features.broadcastChat(Messages.broadcastMSG(args, 1), p);
                    }

                    else if (args[0].equalsIgnoreCase("bar")) {
                        if (args.length >= 3 && CommandCheck.isInt(args[1])) {
                            BroadcastMethods.sendActionBar(p, Messages.stringJoin(args, 2), Integer.parseInt(args[1]));
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-bar-usage"));
                        }
                    }

                    else if (args[0].equalsIgnoreCase("title")) {
                        BroadcastMethods.sendTitle(p, Messages.stringJoin(args, 1));
                    }

                    else if (args[0].equalsIgnoreCase("gui")) {
                        if (args.length == 2 && Main.getInstance().getConfig().contains("Menus." + args[1])) {
                            GuiCreator.generate(p, args[1]);
                        } else {
                            Messages.prefix(s, "&cInvalid GUI Name");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("boss")) {
                        if (args.length == 2) {
                            if (Main.getInstance().getConfig().contains("Presets." + args[1] + ".boss")) {
                                org.bukkit.boss.BossBar createdBar = Main.bar.createBar(
                                        Main.getInstance().getConfig().getInt("Presets." + args[1] + ".boss.time"),
                                        Main.getInstance().getConfig().getString("Presets." + args[1] + ".boss.color"),
                                        Main.getInstance().getConfig().getString("Presets." + args[1] + ".boss.style"),
                                        Main.getInstance().getConfig().getString("Presets." + args[1] + ".boss.text"));
                                createdBar.addPlayer(p);
                            } else {
                                Messages.prefix(s, "&cInvalid Preset Name");
                            }
                        }
                        else if (args.length >= 5) {
                            if (CommandCheck.isInt(args[1])) {
                                org.bukkit.boss.BossBar createdBar = Main.bar.createBar(
                                        Integer.parseInt(args[1]), args[2], args[3], Messages.stringJoin(args, 4));
                                createdBar.addPlayer(p);
                            } else {
                                Messages.prefix(s, "&cInvalid Timing");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-boss-usage"));
                        }
                    }

                } else {
                    Messages.prefix(s, Lang.getLangConfig().getString("broadcast-usage"));
                }
            }
        }
        return false;
    }

    List<String> method = Arrays.asList("chat", "title", "bar", "gui", "boss");
    List<String> colors = Arrays.asList("BLUE", "GREEN", "PINK", "PURPLE", "RED", "WHITE", "YELLOW");
    List<String> styles = Arrays.asList("SOLID", "SEGMENTED_6", "SEGMENTED_10", "SEGMENTED_12", "SEGMENTED_20");

    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            method.forEach(a -> {
                if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(a);
            });
            return result;
        }

        if (args[0].equalsIgnoreCase("boss")) {
            List<String> cResult = new ArrayList<>();
            if (args.length == 3) {
                colors.forEach(color -> {
                    if (color.toLowerCase().startsWith(args[2].toLowerCase()))
                        cResult.add(color);
                });
                return cResult;
            }

            List<String> sResult = new ArrayList<>();
            if (args.length == 4) {
                styles.forEach(style -> {
                    if (style.toLowerCase().startsWith(args[3].toLowerCase()))
                        sResult.add(style);
                });
                return sResult;
            }

        }

        return null;
    }

}
