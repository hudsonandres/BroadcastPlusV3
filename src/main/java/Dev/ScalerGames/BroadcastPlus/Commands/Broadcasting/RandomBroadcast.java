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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class RandomBroadcast implements CommandExecutor {

    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("randombroadcast") || label.equalsIgnoreCase("rb")) {
            if (CommandCheck.execute(s, "bp.randombroadcast", false)) {
                if (args.length >= 2) {

                    if (args[0].equalsIgnoreCase("chat")) {
                        Player rand = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
                        String chatMsg = Messages.broadcastMSG(args, 1);
                        Features.broadcastChat(chatMsg, rand);
                        redisPublish("chat", chatMsg, "");
                    }

                    else if (args[0].equalsIgnoreCase("bar")) {
                        if (args.length >= 3 && CommandCheck.isInt(args[1])) {
                            Player rand = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
                            String barMsg = Messages.stringJoin(args, 2);
                            int timing = Integer.parseInt(args[1]);
                            BroadcastMethods.sendActionBar(rand, barMsg, timing);
                            redisPublish("bar", barMsg, String.valueOf(timing));
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("random-broadcast-bar-usage"));
                        }
                    }

                    else if (args[0].equalsIgnoreCase("title")) {
                        Player rand = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
                        String titleMsg = Messages.stringJoin(args, 1);
                        BroadcastMethods.sendTitle(rand, titleMsg);
                        redisPublish("title", titleMsg, "");
                    }

                    else if (args[0].equalsIgnoreCase("gui")) {
                        if (args.length == 2 && Objects.requireNonNull(Gui.getGuiConfig().getConfigurationSection("Menus")).getKeys(false).contains(args[1])) {
                            Player rand = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
                            GuiCreator.generate(rand, args[1]);
                        } else {
                            Messages.prefix(s, "&cInvalid GUI Name");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("boss")) {
                        if (args.length == 2) {
                            if (Main.getInstance().getConfig().contains("Presets." + args[1] + ".boss")) {
                                int t = Main.getInstance().getConfig().getInt("Presets." + args[1] + ".boss.time");
                                String c = Main.getInstance().getConfig().getString("Presets." + args[1] + ".boss.color");
                                String st = Main.getInstance().getConfig().getString("Presets." + args[1] + ".boss.style");
                                String tx = Main.getInstance().getConfig().getString("Presets." + args[1] + ".boss.text");
                                org.bukkit.boss.BossBar createdBar = Main.bar.createBar(t, c, st, tx);
                                Player rand = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
                                createdBar.addPlayer(rand);
                                redisPublish("boss", tx, t + "|" + c + "|" + st);
                            } else {
                                Messages.prefix(s, "&cInvalid Preset Name");
                            }
                        }
                        else if (args.length >= 5) {
                            if (CommandCheck.isInt(args[1])) {
                                String bossMsg = Messages.stringJoin(args, 4);
                                org.bukkit.boss.BossBar createdBar = Main.bar.createBar(Integer.parseInt(args[1]), args[2], args[3], bossMsg);
                                Player rand = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
                                createdBar.addPlayer(rand);
                                redisPublish("boss", bossMsg, args[1] + "|" + args[2] + "|" + args[3]);
                            } else {
                                Messages.prefix(s, "&cInvalid Timing");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("random-broadcast-boss-usage"));
                        }
                    }

                    else {
                        Messages.prefix(s, Lang.getLangConfig().getString("broadcast-options"));
                    }

                } else {
                    Messages.prefix(s, Lang.getLangConfig().getString("broadcast-permission"));
                }
            }
        }
        return false;
    }

    private void redisPublish(String type, String message, String extra) {
        if (Main.redis != null && Main.redis.isEnabled()) {
            String server = Engine.getEngineConfig().getString("Redis.server-name", "default");
            Main.redis.publish(new RedisMessage(type, message, extra, server));
        }
    }

}
