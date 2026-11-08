package Dev.ScalerGames.BroadcastPlus.Commands.Broadcasting;

import Dev.ScalerGames.BroadcastPlus.Commands.CommandCheck;
import Dev.ScalerGames.BroadcastPlus.Files.Gui;
import Dev.ScalerGames.BroadcastPlus.Files.Lang;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Methods.Advancement;
import Dev.ScalerGames.BroadcastPlus.Methods.BroadcastMethods;
import Dev.ScalerGames.BroadcastPlus.Methods.Features;
import Dev.ScalerGames.BroadcastPlus.Methods.Gui.GuiCreator;
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

public class BroadcastWorld implements CommandExecutor, TabCompleter {

    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("broadcastworld") || label.equalsIgnoreCase("bw")) {
            if (CommandCheck.execute(s, "bp.broadcasworld", false)) {
                if (args.length >= 2) {

                    if (args[0].equalsIgnoreCase("chat")) {
                        if (Bukkit.getWorld(args[1]) != null) {
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (player.getWorld().getName().equalsIgnoreCase(args[1])) {
                                    Features.broadcastChat(Messages.broadcastMSG(args, 2), player);
                                }
                            });
                        } else {
                            Messages.prefix(s, "&cInvalid world name");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("bar")) {
                        if (args.length >= 3) {
                            if (CommandCheck.isInt(args[2])) {
                                if (Bukkit.getWorld(args[1]) != null) {
                                    Bukkit.getOnlinePlayers().forEach(player -> {
                                        if (player.getWorld().getName().equalsIgnoreCase(args[1])) {
                                            BroadcastMethods.sendActionBar(player, Messages.stringJoin(args, 3), Integer.parseInt(args[2]));
                                        }
                                    });
                                } else {
                                    Messages.prefix(s, "&cInvalid world name");
                                }
                            } else {
                                Messages.prefix(s, "&cInvalid Time");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-world-bar-usage"));
                        }
                    }

                    else if (args[0].equalsIgnoreCase("title")) {
                        if (Bukkit.getWorld(args[1]) != null) {
                            Bukkit.getOnlinePlayers().forEach(player -> {
                                if (player.getWorld().getName().equalsIgnoreCase(args[1])) {
                                    BroadcastMethods.sendTitle(player, Messages.stringJoin(args, 2));
                                }
                            });
                        } else {
                            Messages.prefix(s, "&cInvalid world name");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("gui")) {
                        if (args.length == 3 && Objects.requireNonNull(Gui.getGuiConfig().getConfigurationSection("Menus")).getKeys(false).contains(args[2])) {
                            if (Bukkit.getWorld(args[1]) != null) {
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.getWorld().getName().equalsIgnoreCase(args[1])) {
                                        GuiCreator.generate(player, args[2]);
                                    }
                                });
                            } else {
                                Messages.prefix(s, "&cInvalid world name");
                            }
                        } else {
                            Messages.prefix(s, "&cInvalid GUI name");
                        }
                    }

                    else if (args[0].equalsIgnoreCase("boss")) {
                        if (args.length == 3) {
                            if (Main.getInstance().getConfig().contains("Presets." + args[2] + ".boss")) {
                                org.bukkit.boss.BossBar createdBar = Main.bar.createBar(
                                        Main.getInstance().getConfig().getInt("Presets." + args[2] + ".boss.time"),
                                        Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.color"),
                                        Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.style"),
                                        Main.getInstance().getConfig().getString("Presets." + args[2] + ".boss.text"));
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.getWorld().getName().equalsIgnoreCase(args[1])) {
                                        createdBar.addPlayer(player);
                                    }
                                });
                            } else {
                                Messages.prefix(s, "&cInvalid Prefix Name");
                            }
                        }
                        else if (args.length >= 6 && CommandCheck.isInt(args[2])) {
                            if (Bukkit.getWorld(args[1]) != null) {
                                org.bukkit.boss.BossBar createdBar = Main.bar.createBar(Integer.parseInt(args[2]), args[3], args[4], Messages.stringJoin(args, 5));
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.getWorld().getName().equalsIgnoreCase(args[1])) {
                                        createdBar.addPlayer(player);
                                    }
                                });
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-world-boss-usage"));
                        }
                    }

                    else if(args[0].equalsIgnoreCase("advancement")) {
                        if (args.length >= 4) {
                            if (Bukkit.getWorld(args[1]) == null) {
                                Messages.prefix(s, "&cInvalid World");
                                return false;
                            }

                            if (Material.matchMaterial(args[2]) != null) {
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    if (player.getWorld().getName().equalsIgnoreCase(args[1])) {
                                        Advancement.display(player, args[2].toLowerCase(), Advancement.Style.valueOf(args[3]), Format.placeholder(player, Messages.stringJoin(args, 4)));
                                    }
                                });
                            } else {
                                Messages.prefix(s, "&cInvalid Item");
                            }
                        } else {
                            Messages.prefix(s, Lang.getLangConfig().getString("broadcast-advancement-usage"));
                        }
                    }

                    else {
                        Messages.prefix(s, Lang.getLangConfig().getString("broadcast-world-usage"));
                    }

                } else {
                    Messages.prefix(s, Lang.getLangConfig().getString("broadcast-world-usage"));
                }
            }
        }
        return false;
    }

    List<String> method = Arrays.asList("chat", "title", "bar", "gui", "boss", "advancement");
    List<String> worlds = new ArrayList<>();
    List<String> colors = Arrays.asList("BLUE", "GREEN", "PINK", "PURPLE", "RED", "WHITE", "YELLOW");
    List<String> styles = Arrays.asList("SOLID", "SEGMENTED_6", "SEGMENTED_10", "SEGMENTED_12", "SEGMENTED_20");
    List<String> advanceStyles = Arrays.asList("GOAL", "TASK", "CHALLENGE");

    public List<String> onTabComplete (@NotNull CommandSender s, @NotNull Command cmd, @NotNull String label, String[] args) {

        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            method.forEach(m -> {
                if (m.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(m);
            });
            return result;
        }

        // Always rebuild the world list to reflect worlds loaded/unloaded since last tab-complete
        worlds.clear();
        Bukkit.getWorlds().forEach(world -> worlds.add(world.getName()));

        List<String> wResult = new ArrayList<>();
        if (args.length == 2) {
            worlds.forEach(w -> {
                if (w.toLowerCase().startsWith(args[1].toLowerCase()))
                    wResult.add(w);
            });
            return wResult;
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

        if (args[0].equalsIgnoreCase("advancement")) {
            List<String> sResult = new ArrayList<>();
            if (args.length == 4) {
                advanceStyles.forEach(style -> {
                    if (style.toLowerCase().startsWith(args[3].toLowerCase()))
                        sResult.add(style);
                });
                return sResult;
            }
        }

        return null;
    }

}
