package dev.bunghole.votefly;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {

    private final String url;

    public DatabaseManager(String url) {
        this.url = url;
    }

    public void initialize() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS vote_fly_times (uuid TEXT PRIMARY KEY, expiry_time BIGINT)")) {
            stmt.executeUpdate();
        }
    }

    public void saveVoteFlyTime(UUID uuid, long expiryTime) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement("REPLACE INTO vote_fly_times (uuid, expiry_time) VALUES (?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setLong(2, expiryTime);
            stmt.executeUpdate();
        }
    }

    public Long loadVoteFlyTime(UUID uuid) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement("SELECT expiry_time FROM vote_fly_times WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("expiry_time");
                }
            }
        }
        return null;
    }
}
