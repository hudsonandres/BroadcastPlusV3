package Dev.ScalerGames.BroadcastPlus.Commands.Broadcasting;

import Dev.ScalerGames.BroadcastPlus.Commands.CommandCheck;
import Dev.ScalerGames.BroadcastPlus.Files.Gui;
import Dev.ScalerGames.BroadcastPlus.Files.Lang;
import Dev.ScalerGames.BroadcastPlus.Files.Engine;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Methods.BroadcastMethods;
import Dev.ScalerGames.BroadcastPlus.Methods.Features;
import Dev.ScalerGames.BroadcastPlus.Methods.Gui.GuiCreator;
import Dev.ScalerGames.BroadcastPlus.Redis.RedisMessage;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PermBroadcast implements CommandExecutor, TabCompleter {

    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("permbroadcast") || label.equalsIgnoreCase("pb")) {
            if (CommandCheck.execute(s, "bp.permbroadcast", false)) {
                if (args.length >= 3) {

                    if (args[0].equalsIgnoreCase("chat")) {
                        String chatMsg = Messages.broadcastMSG(args, 2);
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            if (player.hasPermission(args[1])) {
                                Features.broadcastChat(chatMsg, player);
                            }
                        });
                        redisPublish("chat", chatMsg, "");
                    }

                    else if (args[0].equalsIgnoreCase("bar")) {
                        if (args.length >= 4) {
                            if (CommandCheck.isInt(args[2])) {
                                String barMsg = Messages.stringJoin(args, 3);
                                int timing = Integer.parseInt(args[3]);
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.hasPermission(args[1])) {
                                        BroadcastMethods.sendActionBar(player, barMsg, timing);
                                    }
                                });
                                redisPublish("bar", barMsg, String.valueOf(timing));
                            } else {
                                Messages.prefix(s, "&cInvalid Timing");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("perm-broadcast-bar-usage"));
                        }
                    }

                    else if (args[0].equalsIgnoreCase("title")) {
                        String titleMsg = Messages.stringJoin(args, 2);
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            if (player.hasPermission(args[1])) {
                                BroadcastMethods.sendTitle(player, titleMsg);
                            }
                        });
                        redisPublish("title", titleMsg, "");
                    }

                    else if (args[0].equalsIgnoreCase("gui")) {
                        if (args.length == 3 && Objects.requireNonNull(Gui.getGuiConfig().getConfigurationSection("Menus")).getKeys(false).contains(args[2])) {
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (player.hasPermission(args[1])) {
                                    GuiCreator.generate(player, args[2]);
                                }
                            });
                        } else {
                            Messages.prefix(s, "&cInvalid GUI Name");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("boss")) {
                        if (args.length == 3) {
                            if (Main.getInstance().getConfig().contains("Presets." + args[2] + ".boss")) {
                                int t = Main.getInstance().getConfig().getInt("Presets." + args[2] + ".boss.time");
                                String c = Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.color");
                                String st = Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.style");
                                String tx = Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.text");
                                Main.bar.createBar(t, c, st, tx);
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.hasPermission(args[1])) {
                                        Main.bar.addPlayer(player);
                                    }
                                });
                                redisPublish("boss", tx, t + "|" + c + "|" + st);
                            } else {
                                Messages.prefix(s, "&cInvalid Preset Name");
                            }
                        }
                         else if (args.length >= 6 && CommandCheck.isInt(args[2])) {
                            String bossMsg = Messages.stringJoin(args, 5);
                            Main.bar.createBar(Integer.parseInt(args[2]), args[3], args[4], bossMsg);
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (player.hasPermission(args[1])) {
                                    Main.bar.addPlayer(player);
                                }
                            });
                            redisPublish("boss", bossMsg, args[2] + "|" + args[3] + "|" + args[4]);
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("perm-broadcast-boss-usage"));
                        }
                    }

                    else {
                        Messages.prefix(s, Lang.getLangConfig().getString("broadcast-options"));
                    }

                } else {
                    Messages.prefix(s, Lang.getLangConfig().getString("perm-broadcast-usage"));
                }
            }
        }
        return false;
    }

    List<String> method = Arrays.asList("chat", "title", "bar", "gui", "boss");
    List<String> colors = Arrays.asList("BLUE", "GREEN", "PINK", "PURPLE", "RED", "WHITE", "YELLOW");
    List<String> styles = Arrays.asList("SOLID", "SEGMENTED_6", "SEGMENTED_10", "SEGMENTED_12", "SEGMENTED_20");

    public List<String> onTabComplete (@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {

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

    private void redisPublish(String type, String message, String extra) {
        if (Main.redis != null && Main.redis.isEnabled()) {
            String server = Engine.getEngineConfig().getString("Redis.server-name", "default");
            Main.redis.publish(new RedisMessage(type, message, extra, server));
        }
    }

}
