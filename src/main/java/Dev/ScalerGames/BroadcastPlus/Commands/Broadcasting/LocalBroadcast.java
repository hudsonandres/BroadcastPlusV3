package Dev.ScalerGames.BroadcastPlus.Commands.Broadcasting;

import Dev.ScalerGames.BroadcastPlus.Commands.CommandCheck;
import Dev.ScalerGames.BroadcastPlus.Files.Gui;
import Dev.ScalerGames.BroadcastPlus.Files.Lang;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Methods.BroadcastMethods;
import Dev.ScalerGames.BroadcastPlus.Methods.Features;
import Dev.ScalerGames.BroadcastPlus.Methods.Gui.GuiCreator;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LocalBroadcast implements CommandExecutor, TabCompleter {

    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("localbroadcast") || label.equalsIgnoreCase("lb")) {
            // CommandCheck already ensures s is a Player when player_only=true
            if (CommandCheck.execute(s, "bp.localbroadcast", true)) {
                Player sender = (Player) s;
                if (args.length >= 3) {

                    if (args[0].equalsIgnoreCase("chat")) {
                        if (CommandCheck.isInt(args[1])) {
                            int range = Integer.parseInt(args[1]);
                            String chatMsg = Messages.broadcastMSG(args, 2);
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (player.getWorld().equals(sender.getWorld())
                                        && player.getLocation().distance(sender.getLocation()) <= range) {
                                    Features.broadcastChat(chatMsg, player);
                                }
                            });
                        } else {
                            Messages.prefix(s, "&cInvalid Distance");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("bar")) {
                        if (args.length >= 4) {
                            if (CommandCheck.isInt(args[1]) && CommandCheck.isInt(args[2])) {
                                int range = Integer.parseInt(args[1]);
                                int timing = Integer.parseInt(args[2]);
                                String barMsg = Messages.stringJoin(args, 3);
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.getWorld().equals(sender.getWorld())
                                            && player.getLocation().distance(sender.getLocation()) <= range) {
                                        BroadcastMethods.sendActionBar(player, barMsg, timing);
                                    }
                                });
                            } else {
                                Messages.prefix(s, "&cInvalid Timing(" + args[2] + ") or Region(" + args[1] + ")");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("local-broadcast-bar-usage"));
                        }
                    }

                    else if (args[0].equalsIgnoreCase("title")) {
                        if (CommandCheck.isInt(args[1])) {
                            int range = Integer.parseInt(args[1]);
                            String titleMsg = Messages.broadcastMSG(args, 2);
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (player.getWorld().equals(sender.getWorld())
                                        && player.getLocation().distance(sender.getLocation()) <= range) {
                                    BroadcastMethods.sendTitle(player, titleMsg);
                                }
                            });
                        } else {
                            Messages.prefix(s, "&cInvalid Region");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("gui")) {
                        if (args.length == 3 && Objects.requireNonNull(Gui.getGuiConfig().getConfigurationSection("Menus")).getKeys(false).contains(args[2])) {
                            if (CommandCheck.isInt(args[1])) {
                                int range = Integer.parseInt(args[1]);
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.getWorld().equals(sender.getWorld())
                                            && player.getLocation().distance(sender.getLocation()) <= range) {
                                        GuiCreator.generate(player, args[2]);
                                    }
                                });
                            } else {
                                Messages.prefix(s, "&cInvalid Region");
                            }
                        } else {
                            Messages.prefix(s, "&cInvalid GUI name");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("boss")) {
                        if (args.length == 3) {
                            if (Main.getInstance().getConfig().contains("Presets." + args[2] + ".boss")) {
                                int range = Integer.parseInt(args[1]);
                                org.bukkit.boss.BossBar createdBar = Main.bar.createBar(
                                        Main.getInstance().getConfig().getInt("Presets." + args[2] + ".boss.time"),
                                        Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.color"),
                                        Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.style"),
                                        Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.text"));
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.getWorld().equals(sender.getWorld())
                                            && player.getLocation().distance(sender.getLocation()) <= range) {
                                        createdBar.addPlayer(player);
                                    }
                                });
                            } else {
                                Messages.prefix(s, "&cInvalid Preset Name");
                            }
                        }
                        else if (args.length >= 6 && CommandCheck.isInt(args[2]) && CommandCheck.isInt(args[1])) {
                            int range = Integer.parseInt(args[1]);
                            org.bukkit.boss.BossBar createdBar = Main.bar.createBar(
                                    Integer.parseInt(args[2]), args[3], args[4], Messages.stringJoin(args, 5));
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (player.getWorld().equals(sender.getWorld())
                                        && player.getLocation().distance(sender.getLocation()) <= range) {
                                    createdBar.addPlayer(player);
                                }
                            });
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("local-broadcast-boss-usage"));
                        }
                    }

                    else {
                        Messages.prefix(s, "&cUsage: /{cmd} [chat|bar|title|boss|gui] [range] [message]".replace("{cmd}", label));
                    }

                } else {
                    Messages.prefix(s, Lang.getLangConfig().getString("local-broadcast-usage"));
                }
            }
        }
        return false;
    }

    List<String> method = Arrays.asList("chat", "title", "bar", "gui", "boss");
    List<String> colors = Arrays.asList("BLUE", "GREEN", "PINK", "PURPLE", "RED", "WHITE", "YELLOW");
    List<String> styles = Arrays.asList("SOLID", "SEGMENTED_6", "SEGMENTED_10", "SEGMENTED_12", "SEGMENTED_20");

    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            method.forEach(m -> {
                if (m.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(m);
            });
            return result;
        }

        if (args[0].equalsIgnoreCase("boss")) {
            List<String> cResult = new ArrayList<>();
            if (args.length == 4) {
                colors.forEach(c -> {
                    if (c.toLowerCase().startsWith(args[3].toLowerCase()))
                        cResult.add(c);
                });
                return cResult;
            }

            List<String> sResult = new ArrayList<>();
            if (args.length == 5) {
                styles.forEach(style -> {
                    if (style.toLowerCase().startsWith(args[4].toLowerCase()))
                        sResult.add(style);
                });
                return sResult;
            }

        }

        return null;
    }

}
