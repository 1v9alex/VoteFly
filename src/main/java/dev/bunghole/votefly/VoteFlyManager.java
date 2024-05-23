package dev.bunghole.votefly;

import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoteFlyManager {

    private final Map<UUID, Long> voteFlyTimes;
    private final DatabaseManager databaseManager;

    public VoteFlyManager(DatabaseManager databaseManager) {
        this.voteFlyTimes = new HashMap<>();
        this.databaseManager = databaseManager;
    }

    public void addVoteFlyTime(Player player, long timeInSeconds) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long newExpiryTime = currentTime + (timeInSeconds * 1000);

        voteFlyTimes.put(playerId, voteFlyTimes.getOrDefault(playerId, currentTime) + (timeInSeconds * 1000));
        saveVoteFlyTime(playerId);
    }

    public void removeVoteFlyTime(Player player, long timeInSeconds) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long remainingTime = voteFlyTimes.getOrDefault(playerId, currentTime) - (timeInSeconds * 1000);

        if (remainingTime <= currentTime) {
            voteFlyTimes.remove(playerId);
        } else {
            voteFlyTimes.put(playerId, remainingTime);
        }
        saveVoteFlyTime(playerId);
    }

    public long getRemainingVoteFlyTime(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        return (voteFlyTimes.getOrDefault(playerId, currentTime) - currentTime) / 1000;
    }

    public boolean hasVoteFlyTime(Player player) {
        UUID playerId = player.getUniqueId();
        return voteFlyTimes.containsKey(playerId) && getRemainingVoteFlyTime(player) > 0;
    }

    public void loadVoteFlyTime(UUID uuid) {
        try {
            Long expiryTime = databaseManager.loadVoteFlyTime(uuid);
            if (expiryTime != null) {
                voteFlyTimes.put(uuid, expiryTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveVoteFlyTime(UUID uuid) {
        Long expiryTime = voteFlyTimes.get(uuid);
        if (expiryTime != null) {
            try {
                databaseManager.saveVoteFlyTime(uuid, expiryTime);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void fixVoteFlyTime(UUID uuid) {
        Long expiryTime = voteFlyTimes.get(uuid);
        if (expiryTime != null && expiryTime < System.currentTimeMillis()) {
            voteFlyTimes.remove(uuid);
            saveVoteFlyTime(uuid);
        }
    }

    public void wipeVoteFlyTime(UUID uuid) {
        voteFlyTimes.remove(uuid);
        saveVoteFlyTime(uuid);
    }

    public void saveAll() {
        for (UUID uuid : voteFlyTimes.keySet()) {
            saveVoteFlyTime(uuid);
        }
    }
}
