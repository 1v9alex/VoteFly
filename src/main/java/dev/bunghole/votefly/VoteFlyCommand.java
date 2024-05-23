package dev.bunghole.votefly;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class VoteFlyCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final VoteFlyManager voteFlyManager;
    private Essentials essentials;

    public VoteFlyCommand(JavaPlugin plugin, VoteFlyManager voteFlyManager) {
        this.plugin = plugin;
        this.voteFlyManager = voteFlyManager;
        this.essentials = (Essentials) plugin.getServer().getPluginManager().getPlugin("Essentials");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                handleVoteFlyToggle((Player) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            }
        } else {
            switch (args[0].toLowerCase()) {
                case "check":
                    if (args.length == 1) {
                        if (sender instanceof Player) {
                            handleVoteFlyCheck((Player) sender);
                        } else {
                            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                        }
                    } else if (args.length == 2 && sender.hasPermission("votefly.mod")) {
                        handleVoteFlyCheck(sender, args[1]);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Unknown command. Use /votefly [check|on|off|toggle]");
                    }
                    break;
                case "on":
                    if (sender instanceof Player) {
                        handleVoteFlyOn((Player) sender);
                    } else {
                        sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    }
                    break;
                case "off":
                    if (sender instanceof Player) {
                        handleVoteFlyOff((Player) sender);
                    } else {
                        sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    }
                    break;
                case "toggle":
                    if (sender instanceof Player) {
                        handleVoteFlyToggle((Player) sender);
                    } else {
                        sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    }
                    break;
                case "fix":
                    if (sender.hasPermission("votefly.admin")) {
                        if (args.length == 2) {
                            handleVoteFlyFix(sender, args[1]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /votefly fix [player|all]");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    }
                    break;
                case "wipe":
                    if (sender.hasPermission("votefly.admin")) {
                        if (args.length == 2) {
                            handleVoteFlyWipe(sender, args[1]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /votefly wipe [player|all]");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    }
                    break;
                case "add":
                    if (sender.hasPermission("votefly.admin")) {
                        if (args.length >= 3) {
                            handleVoteFlyAdd(sender, args[1], args[2]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /votefly add [player|all] [time m time s]");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    }
                    break;
                case "remove":
                    if (sender.hasPermission("votefly.admin")) {
                        if (args.length >= 3) {
                            handleVoteFlyRemove(sender, args[1], args[2]);
                        } else {
                            sender.sendMessage(ChatColor.RED + "Usage: /votefly remove [player|all] [time m time s]");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown command. Use /votefly [check|on|off|toggle]");
                    break;
            }
        }
        return true;
    }

    private void handleVoteFlyToggle(Player player) {
        if (voteFlyManager.hasVoteFlyTime(player)) {
            if (player.isFlying()) {
                player.setFlying(false);
                player.setAllowFlight(false);
                player.sendMessage(ChatColor.YELLOW + "Vote fly turned off, time remaining: " + formatTime(voteFlyManager.getRemainingVoteFlyTime(player)));
            } else {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendMessage(ChatColor.YELLOW + "Vote fly turned on, time remaining: " + formatTime(voteFlyManager.getRemainingVoteFlyTime(player)));
            }
        } else {
            player.sendMessage(ChatColor.RED + "You do not have any vote-fly time right now.");
        }
    }

    private void handleVoteFlyCheck(Player player) {
        if (voteFlyManager.hasVoteFlyTime(player)) {
            player.sendMessage(ChatColor.GREEN + "You have " + formatTime(voteFlyManager.getRemainingVoteFlyTime(player)) + " of vote-fly time left.");
        } else {
            player.sendMessage(ChatColor.RED + "Your vote-fly time has expired.");
        }
    }

    private void handleVoteFlyCheck(CommandSender sender, String target) {
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer != null) {
            long remainingTime = voteFlyManager.getRemainingVoteFlyTime(targetPlayer);
            sender.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " has " + formatTime(remainingTime) + " of vote-fly time left.");
        } else {
            sender.sendMessage(ChatColor.RED + "Player not found.");
        }
    }

    private void handleVoteFlyFix(CommandSender sender, String target) {
        if (target.equalsIgnoreCase("all")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                voteFlyManager.fixVoteFlyTime(player.getUniqueId());
            }
            sender.sendMessage(ChatColor.GREEN + "Fixed vote-fly times for all players.");
        } else {
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null) {
                voteFlyManager.fixVoteFlyTime(targetPlayer.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Fixed vote-fly time for " + targetPlayer.getName() + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            }
        }
    }

    private void handleVoteFlyWipe(CommandSender sender, String target) {
        if (target.equalsIgnoreCase("all")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                voteFlyManager.wipeVoteFlyTime(player.getUniqueId());
            }
            sender.sendMessage(ChatColor.GREEN + "Wiped vote-fly times for all players.");
        } else {
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null) {
                voteFlyManager.wipeVoteFlyTime(targetPlayer.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + "Wiped vote-fly time for " + targetPlayer.getName() + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            }
        }
    }

    private void handleVoteFlyAdd(CommandSender sender, String target, String time) {
        long timeInSeconds = parseTime(time);
        if (timeInSeconds <= 0) {
            sender.sendMessage(ChatColor.RED + "Invalid time format. Use [time m time s].");
            return;
        }

        if (target.equalsIgnoreCase("all")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                voteFlyManager.addVoteFlyTime(player, timeInSeconds);
            }
            sender.sendMessage(ChatColor.GREEN + "Added " + formatTime(timeInSeconds) + " of vote-fly time to all players.");
        } else {
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null) {
                voteFlyManager.addVoteFlyTime(targetPlayer, timeInSeconds);
                sender.sendMessage(ChatColor.GREEN + "Added " + formatTime(timeInSeconds) + " of vote-fly time to " + targetPlayer.getName() + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            }
        }
    }


    private void handleVoteFlyRemove(CommandSender sender, String target, String time) {
        long timeInSeconds = parseTime(time);
        if (timeInSeconds <= 0) {
            sender.sendMessage(ChatColor.RED + "Invalid time format. Use [time m time s].");
            return;
        }

        if (target.equalsIgnoreCase("all")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                voteFlyManager.removeVoteFlyTime(player, timeInSeconds);
            }
            sender.sendMessage(ChatColor.GREEN + "Removed " + formatTime(timeInSeconds) + " of vote-fly time from all players.");
        } else {
            Player targetPlayer = Bukkit.getPlayer(target);
            if (targetPlayer != null) {
                voteFlyManager.removeVoteFlyTime(targetPlayer, timeInSeconds);
                sender.sendMessage(ChatColor.GREEN + "Removed " + formatTime(timeInSeconds) + " of vote-fly time from " + targetPlayer.getName() + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            }
        }
    }

    private long parseTime(String time) {
        String[] parts = time.split(" ");
        long totalSeconds = 0;
        for (String part : parts) {
            if (part.endsWith("m")) {
                totalSeconds += Integer.parseInt(part.replace("m", "")) * 60;
            } else if (part.endsWith("s")) {
                totalSeconds += Integer.parseInt(part.replace("s", ""));
            }
        }
        return totalSeconds;
    }

    private void handleVoteFlyOn(Player player) {
        if (voteFlyManager.hasVoteFlyTime(player)) {
            player.setAllowFlight(true);
            player.setFlying(true);
            player.sendMessage(ChatColor.YELLOW + "Vote fly turned on, time remaining: " + formatTime(voteFlyManager.getRemainingVoteFlyTime(player)));
        } else {
            player.sendMessage(ChatColor.RED + "You do not have any vote-fly time right now.");
        }
    }

    private void handleVoteFlyOff(Player player) {
        if (voteFlyManager.hasVoteFlyTime(player)) {
            player.setFlying(false);
            player.setAllowFlight(false);
            player.sendMessage(ChatColor.YELLOW + "Vote fly turned off, time remaining: " + formatTime(voteFlyManager.getRemainingVoteFlyTime(player)));
        } else {
            player.sendMessage(ChatColor.RED + "You do not have any vote-fly time right now.");
        }
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%dm %ds", minutes, remainingSeconds);
    }
}
