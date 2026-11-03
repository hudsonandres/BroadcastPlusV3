package Dev.ScalerGames.BroadcastPlus.Commands.Broadcasting;

import Dev.ScalerGames.BroadcastPlus.Commands.CommandCheck;
import Dev.ScalerGames.BroadcastPlus.Files.Gui;
import Dev.ScalerGames.BroadcastPlus.Files.Lang;
import Dev.ScalerGames.BroadcastPlus.Files.Engine;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Methods.Advancement;
import Dev.ScalerGames.BroadcastPlus.Methods.BroadcastMethods;
import Dev.ScalerGames.BroadcastPlus.Methods.Features;
import Dev.ScalerGames.BroadcastPlus.Methods.Gui.GuiCreator;
import Dev.ScalerGames.BroadcastPlus.Redis.RedisMessage;
import Dev.ScalerGames.BroadcastPlus.Utils.Format;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Broadcast implements CommandExecutor, TabCompleter {

    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("broadcast") || label.equalsIgnoreCase("announce")) {
            if (CommandCheck.execute(s, "bp.broadcast", false)) {

                // --- Parse optional --server <name> prefix ---
                // Usage with server: /broadcast --server <serverName> <method> [args...]
                // Usage global:      /broadcast <method> [args...]
                String targetServer = null;
                int offset = 0;

                if (args.length >= 2 && args[0].equalsIgnoreCase("--server")) {
                    targetServer = args[1];
                    offset = 2; // skip '--server <name>'
                }

                // After stripping the optional prefix, we need at least method + 1 arg
                // (except for some methods that only need the method itself)
                String[] effectiveArgs = Arrays.copyOfRange(args, offset, args.length);

                if (effectiveArgs.length >= 2) {

                    if (effectiveArgs[0].equalsIgnoreCase("chat")) {
                        String chatMsg = Messages.broadcastMSG(effectiveArgs, 1);
                        if (shouldExecuteLocally(targetServer)) {
                            Bukkit.getOnlinePlayers().forEach(p -> Features.broadcastChat(chatMsg, p));
                        }
                        redisPublish("chat", chatMsg, "", targetServer);
                    }

                    else if (effectiveArgs[0].equalsIgnoreCase("bar")) {
                        if (effectiveArgs.length >= 3) {
                            if (CommandCheck.isInt(effectiveArgs[1])) {
                                String barMsg = Messages.stringJoin(effectiveArgs, 2);
                                int timing = Integer.parseInt(effectiveArgs[1]);
                                if (shouldExecuteLocally(targetServer)) {
                                    Bukkit.getOnlinePlayers().forEach(player -> BroadcastMethods.sendActionBar(player, barMsg, timing));
                                }
                                redisPublish("bar", barMsg, String.valueOf(timing), targetServer);
                            } else {
                                Messages.prefix(s, "&cInvalid Timing");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-bar-usage"));
                        }
                    }

                    else if (effectiveArgs[0].equalsIgnoreCase("title")) {
                        String titleMsg = Messages.stringJoin(effectiveArgs, 1);
                        if (shouldExecuteLocally(targetServer)) {
                            Bukkit.getOnlinePlayers().forEach(player -> BroadcastMethods.sendTitle(player, titleMsg));
                        }
                        redisPublish("title", titleMsg, "", targetServer);
                    }

                    else if (effectiveArgs[0].equalsIgnoreCase("gui")) {
                        if (effectiveArgs.length == 2 && Objects.requireNonNull(Gui.getGuiConfig().getConfigurationSection("Menus")).getKeys(false).contains(effectiveArgs[1])) {
                            if (shouldExecuteLocally(targetServer)) {
                                Bukkit.getOnlinePlayers().forEach(player -> GuiCreator.generate(player, effectiveArgs[1]));
                            }
                            // GUI is not synchronized via Redis (it depends on local state).
                        } else {
                            Messages.prefix(s, "&cInvalid GUI Name");
                        }
                    }

                    else if (effectiveArgs[0].equalsIgnoreCase("boss")) {
                        if (effectiveArgs.length == 2) {
                            if (Main.getInstance().getConfig().contains("Presets." + effectiveArgs[1] + ".boss")) {
                                int t = Main.getInstance().getConfig().getInt("Presets." + effectiveArgs[1] + ".boss.time");
                                String c = Main.getInstance().getConfig().getString("Presets." + effectiveArgs[1] + ".boss.color");
                                String st = Main.getInstance().getConfig().getString("Presets." + effectiveArgs[1] + ".boss.style");
                                String tx = Main.getInstance().getConfig().getString("Presets." + effectiveArgs[1] + ".boss.text");
                                if (shouldExecuteLocally(targetServer)) {
                                    Main.bar.createBar(t, c, st, tx);
                                    Bukkit.getOnlinePlayers().forEach(player -> Main.bar.addPlayer(player));
                                }
                                redisPublish("boss", tx, t + "|" + c + "|" + st, targetServer);
                            } else {
                                Messages.prefix(s, "&cInvalid Preset Name");
                            }
                        }
                        else if (effectiveArgs.length >= 5) {
                            if (CommandCheck.isInt(effectiveArgs[1])) {
                                String bossMsg = Messages.stringJoin(effectiveArgs, 4);
                                if (shouldExecuteLocally(targetServer)) {
                                    Main.bar.createBar(Integer.parseInt(effectiveArgs[1]), effectiveArgs[2], effectiveArgs[3], bossMsg);
                                    Bukkit.getOnlinePlayers().forEach(player -> Main.bar.addPlayer(player));
                                }
                                redisPublish("boss", bossMsg, effectiveArgs[1] + "|" + effectiveArgs[2] + "|" + effectiveArgs[3], targetServer);
                            } else {
                                Messages.prefix(s, "&cInvalid Timing");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-boss-usage"));
                        }
                    }

                    else if (effectiveArgs[0].equalsIgnoreCase("advancement")) {
                        if (effectiveArgs.length >= 3) {
                            if (Material.matchMaterial(effectiveArgs[1]) != null) {
                                String advMsg = Messages.stringJoin(effectiveArgs, 3);
                                if (shouldExecuteLocally(targetServer)) {
                                    Bukkit.getOnlinePlayers().forEach(player -> Advancement.display(player, effectiveArgs[1].toLowerCase(), Advancement.Style.valueOf(effectiveArgs[2]), Format.placeholder(player, advMsg)));
                                }
                                redisPublish("advancement", advMsg, effectiveArgs[1].toLowerCase() + "|" + effectiveArgs[2], targetServer);
                            } else {
                                Messages.prefix(s, "&cInvalid Item");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-advancement-usage"));
                        }
                    }

                } else {
                    Messages.prefix(s, Lang.getLangConfig().getString("broadcast-usage"));
                }
            }
        }
        return false;
    }

    List<String> method = Arrays.asList("chat", "title", "bar", "gui", "boss", "advancement");
    List<String> colors = Arrays.asList("BLUE", "GREEN", "PINK", "PURPLE", "RED", "WHITE", "YELLOW");
    List<String> styles = Arrays.asList("SOLID", "SEGMENTED_6", "SEGMENTED_10", "SEGMENTED_12", "SEGMENTED_20");
    List<String> advanceStyles = Arrays.asList("GOAL", "TASK", "CHALLENGE");

    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {
        // Detect if we are in "--server <name> ..." mode
        int offset = 0;
        if (args.length >= 1 && args[0].equalsIgnoreCase("--server")) {
            // args[1] would be server name (free text), args[2] would be method
            if (args.length == 1) return List.of("--server");
            if (args.length == 2) return List.of("<server-name>");
            offset = 2;
        }

        String[] effectiveArgs = Arrays.copyOfRange(args, offset, args.length);

        if (effectiveArgs.length == 0) {
            return List.of("--server", "chat", "title", "bar", "gui", "boss", "advancement");
        }

        if (effectiveArgs.length == 1) {
            List<String> result = new ArrayList<>();
            // Also offer --server at position 1 (if offset == 0)
            if (offset == 0 && "--server".startsWith(effectiveArgs[0].toLowerCase())) {
                result.add("--server");
            }
            method.forEach(a -> {
                if (a.toLowerCase().startsWith(effectiveArgs[0].toLowerCase()))
                    result.add(a);
            });
            return result;
        }

        if (effectiveArgs[0].equalsIgnoreCase("boss")) {
            List<String> cResult = new ArrayList<>();
            if (effectiveArgs.length == 3) {
                colors.forEach(color -> {
                    if (color.toLowerCase().startsWith(effectiveArgs[2].toLowerCase()))
                        cResult.add(color);
                });
                return cResult;
            }

            List<String> sResult = new ArrayList<>();
            if (effectiveArgs.length == 4) {
                styles.forEach(style -> {
                    if (style.toLowerCase().startsWith(effectiveArgs[3].toLowerCase()))
                        sResult.add(style);
                });
                return sResult;
            }
        }

        if (effectiveArgs[0].equalsIgnoreCase("advancement")) {
           List<String> sResult = new ArrayList<>();
           if (effectiveArgs.length == 3) {
               advanceStyles.forEach(style -> {
                   if (style.toLowerCase().startsWith(effectiveArgs[2].toLowerCase()))
                       sResult.add(style);
               });
               return sResult;
           }
        }

        return null;
    }

    /**
     * Publishes a broadcast to Redis.
     *
     * @param type         Broadcast type (chat, title, bar, etc.)
     * @param message      The message content
     * @param extra        Extra parameters (timing, color, etc.)
     * @param targetServer The target server name, or null for a global broadcast
     */
    /**
     * Returns true if the broadcast should be executed on this server:
     * - targetServer is null/empty → global broadcast, always execute locally.
     * - targetServer is set → execute locally only if this server is in the target list.
     */
    private boolean shouldExecuteLocally(String targetServer) {
        if (targetServer == null || targetServer.isEmpty()) return true;
        String thisServer = Engine.getEngineConfig().getString("Redis.server-name", "default");
        return Arrays.stream(targetServer.split(","))
                .map(String::trim)
                .anyMatch(t -> t.equalsIgnoreCase(thisServer));
    }

    private void redisPublish(String type, String message, String extra, String targetServer) {
        if (Main.redis != null && Main.redis.isEnabled()) {
            String server = Engine.getEngineConfig().getString("Redis.server-name", "default");
            Main.redis.publish(new RedisMessage(type, message, extra, server, targetServer));
        }
    }

    private String getItemKey(Material material) {
        if(material.isBlock()){
            String id = material.getKey().getKey();

            return "block.minecraft."+id;
        } else if(material.isItem()){
            String id = material.getKey().getKey();

            return "item.minecraft."+id;
        }
        return "block.minecraft.dirt";
    }

}
