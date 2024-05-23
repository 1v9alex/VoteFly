package dev.bunghole.votefly;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public class VoteFly extends JavaPlugin {

    private VoteFlyManager voteFlyManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("VoteFly has been enabled!");

        // Load configuration
        saveDefaultConfig();

        // Ensure data folder exists
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Initialize database
        String dbType = getConfig().getString("Database.Type");
        String url = "jdbc:sqlite:" + new File(getDataFolder(), getConfig().getString("Database.SQLite.FileName"));
        if (dbType.equalsIgnoreCase("MYSQL")) {
            url = "jdbc:mysql://" + getConfig().getString("Database.MySQL.Host") + "/" + getConfig().getString("Database.MySQL.Database") + getConfig().getString("Database.MySQL.Options");
        }

        try {
            DatabaseManager databaseManager = new DatabaseManager(url);
            databaseManager.initialize();

            // Initialize VoteFlyManager
            voteFlyManager = new VoteFlyManager(databaseManager);

            // Register commands
            this.getCommand("votefly").setExecutor(new VoteFlyCommand(this, voteFlyManager));

            // Schedule reminders
            scheduleReminders();

            // Schedule periodic saves
            schedulePeriodicSaves();

        } catch (SQLException e) {
            getLogger().severe("Failed to initialize database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        voteFlyManager.saveAll();
        getLogger().info("VoteFly has been disabled!");
    }

    private void scheduleReminders() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (voteFlyManager.isVoteFlyEnabled(player)) {
                    long remainingTime = voteFlyManager.getRemainingVoteFlyTime(player);
                    if (remainingTime == 270 || remainingTime == 180 || remainingTime == 60 || remainingTime == 30) {
                        player.sendMessage(ChatColor.YELLOW + "You have " + formatTime(remainingTime) + " of vote-fly time left.");
                    }
                    if (remainingTime <= 0) {
                        handleVoteFlyOff(player);
                        player.sendMessage(ChatColor.RED + "Your vote-fly time has expired.");
                    }
                }
            }
        }, 0L, 20L); // Schedule to run every second
    }

    private void schedulePeriodicSaves() {
        int saveInterval = getConfig().getInt("Database.Auto_Save_Interval");
        if (saveInterval > 0) {
            Bukkit.getScheduler().runTaskTimer(this, voteFlyManager::saveAll, saveInterval * 60L * 20L, saveInterval * 60L * 20L);
        }
    }

    private void handleVoteFlyOff(Player player) {
        player.setFlying(false);
        player.setAllowFlight(false);
        voteFlyManager.setVoteFlyEnabled(player, false);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%dm %ds", minutes, remainingSeconds);
    }
}
