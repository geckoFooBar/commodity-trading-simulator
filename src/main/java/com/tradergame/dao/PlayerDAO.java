package com.tradergame.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerDAO {

    public int authenticateUser(String username, String password) {
        String query = "SELECT player_id FROM Player WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("player_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Login failed
    }

    // Registers a new user and gives them starting cash
    public boolean registerUser(String username, String password) {
        String query = "INSERT INTO Player (username, password, cash_balance) VALUES (?, ?, 100000.00)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Usually fails if the username already exists (UNIQUE constraint)
            return false;
        }
    }
}