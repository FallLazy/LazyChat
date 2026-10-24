package dev.falllazy.lazyChat.mute.storages;

import dev.falllazy.lazyChat.LazyChat;
import dev.falllazy.lazyChat.language.LanguageManager;
import dev.falllazy.lazyChat.mute.MuteInfo;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SqliteMuteStorage implements MuteStorage {
    private final Connection connection;
    private final LazyChat plugin;
    private final LanguageManager lang;

    public SqliteMuteStorage(LazyChat plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        try {
            Class.forName("org.sqlite.JDBC");
            String dbPath = plugin.getDataFolder().getAbsolutePath() + "/data/mutes.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTableAndMigrate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SQLite storage", e);
        }
    }

    private void createTableAndMigrate() throws SQLException {
        String createSql = "CREATE TABLE IF NOT EXISTS mutes (" +
                "uuid TEXT PRIMARY KEY, " +
                "expiry INTEGER NOT NULL, " +
                "reason TEXT" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createSql);
        }

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(mutes)");
            boolean hasReason = false;
            while (rs.next()) {
                if ("reason".equalsIgnoreCase(rs.getString("name"))) {
                    hasReason = true;
                    break;
                }
            }
            if (!hasReason) {
                stmt.execute("ALTER TABLE mutes ADD COLUMN reason TEXT");
                stmt.execute("UPDATE mutes SET reason = '' WHERE reason IS NULL");
            }
        }
    }

    @Override
    public Map<UUID, MuteInfo> loadMutes() {
        Map<UUID, MuteInfo> mutes = new HashMap<>();
        String sql = "SELECT uuid, expiry, reason FROM mutes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                long expiry = rs.getLong("expiry");
                String reason = rs.getString("reason");
                if (reason == null) reason = "";
                mutes.put(uuid, new MuteInfo(expiry, reason));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load mutes from database: " + e.getMessage());
        }
        return mutes;
    }

    @Override
    public void saveMute(UUID uuid, MuteInfo info) {
        String sql = "INSERT OR REPLACE INTO mutes (uuid, expiry, reason) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setLong(2, info.expiry());
            pstmt.setString(3, info.reason());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save mute: " + e.getMessage());
        }
    }

    @Override
    public void removeMute(UUID uuid) {
        String sql = "DELETE FROM mutes WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to remove mute: " + e.getMessage());
        }
    }

    @Override
    public boolean isMuted(UUID uuid) {
        MuteInfo info = getMuteInfo(uuid);
        return info != null && System.currentTimeMillis() <= info.expiry();
    }

    @Override
    public MuteInfo getMuteInfo(UUID uuid) {
        String sql = "SELECT expiry, reason FROM mutes WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long expiry = rs.getLong("expiry");
                String reason = rs.getString("reason");
                if (reason == null) reason = "";
                return new MuteInfo(expiry, reason);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get mute info: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
        }
    }
}