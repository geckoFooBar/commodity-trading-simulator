package com.tradergame.service;

import com.tradergame.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class MarketSimulator implements Runnable {

    private volatile boolean running = true;
    private final Random random = new Random();
    private int cycleCount = 0; // Tracks how many times the loop has run

    public void stopSimulator() {
        this.running = false;
    }

    @Override
    public void run() {
        System.out.println("[Market Simulator] Online.");

        while (running) {
            try {
                Thread.sleep(10000);
                if (!running) break;

                cycleCount++;

                if (cycleCount % 4 == 0) {
                    triggerNewsEvent();
                } else {
                    fluctuatePrices();
                    publishNews(11);
                }

            } catch (InterruptedException e) {
                System.out.println("[Market Simulator] Shutting down...");
            }
        }
    }

    private void fluctuatePrices() {
        String selectQuery = "SELECT commodity_id, current_price FROM Commodity";
        String updateQuery = "UPDATE Commodity SET current_price = ? WHERE commodity_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement getStmt = conn.prepareStatement(selectQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
             ResultSet rs = getStmt.executeQuery()) {

            conn.setAutoCommit(false);

            while (rs.next()) {
                int id = rs.getInt("commodity_id");
                double currentPrice = rs.getDouble("current_price");
                double shiftMultiplier;

                if (currentPrice > 10000) {
                    shiftMultiplier = 0.70 + (random.nextDouble() * 0.20);
                } else if (currentPrice < 2) {
                    shiftMultiplier = 1.10 + (random.nextDouble() * 0.20);
                } else {
                    shiftMultiplier = 0.95 + (random.nextDouble() * 0.10);
                }

                double newPrice = currentPrice * shiftMultiplier;

                if (newPrice > 80000) newPrice = 80000;
                if (newPrice < 0.50) newPrice = 0.50;

                updateStmt.setDouble(1, newPrice);
                updateStmt.setInt(2, id);
                updateStmt.addBatch();
            }

            updateStmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void triggerNewsEvent() {
        // Fetch a random event (excluding our 'Standard' baseline event which we'll say is ID 11)
        String fetchEventQuery = "SELECT event_id, target_commodity_id, price_multiplier FROM Market_Events WHERE price_multiplier != 1.00 ORDER BY RAND() LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement eventStmt = conn.prepareStatement(fetchEventQuery);
             ResultSet rs = eventStmt.executeQuery()) {

            if (rs.next()) {
                int eventId = rs.getInt("event_id");
                int targetId = rs.getInt("target_commodity_id");
                double multiplier = rs.getDouble("price_multiplier");

                PreparedStatement swingStmt = conn.prepareStatement(
                        "UPDATE Commodity SET current_price = current_price * ? WHERE commodity_id = ?"
                );
                swingStmt.setDouble(1, multiplier);
                swingStmt.setInt(2, targetId);
                swingStmt.executeUpdate();

                // 2. "Stamp" this event so the UI knows it is active
                publishNews(eventId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void publishNews(int eventId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE Market_Events SET last_triggered = CURRENT_TIMESTAMP WHERE event_id = ?")) {
            stmt.setInt(1, eventId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}