package es.uvigo.esei.dai.hybridserver.dao;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class HTMLDaoDB implements HTMLDao {
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    public HTMLDaoDB(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;

        // Crear tabla si no existe, ajustándose a la especificación exacta
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS HTML (
                    uuid CHAR(36) PRIMARY KEY, 
                    content TEXT NOT NULL
                )
                """);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    @Override
    public void savePage(String uuid, String htmlContent) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "REPLACE INTO HTML(uuid, content) VALUES (?, ?)")) {
            stmt.setString(1, uuid);
            stmt.setString(2, htmlContent);
            stmt.executeUpdate();
        }
    }

    @Override
    public String getPage(String uuid) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT content FROM HTML WHERE uuid = ?")) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("content");
                }
                return null;
            }
        }
    }

    @Override
    public boolean deletePage(String uuid) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM HTML WHERE uuid = ?")) {
            stmt.setString(1, uuid);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Set<String> listPages() throws Exception {
        Set<String> uuids = new HashSet<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT uuid FROM HTML");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                uuids.add(rs.getString("uuid"));
            }
        }
        return uuids;
    }
}