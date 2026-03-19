package com.tradergame.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MarketDAO {

    public void displayMarket() {
        String query = "SELECT commodity_id, name, current_price FROM Commodity";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- LIVE MARKET PRICES ---");
            System.out.printf("%-5s | %-15s | %-10s%n", "ID", "COMMODITY", "PRICE");
            System.out.println("-----------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d | %-15s | $%-10.2f%n",
                        rs.getInt("commodity_id"),
                        rs.getString("name"),
                        rs.getDouble("current_price"));
            }
            System.out.println("-----------------------------------");

        } catch (SQLException e) {
            System.err.println("Failed to fetch market prices.");
            e.printStackTrace();
        }
    }

    public void displayPortfolio(int playerId) {
        String cashQuery = "SELECT cash_balance FROM Player WHERE player_id = ?";

        // We use a JOIN here to get the actual name of the commodity, not just the ID
        String portfolioQuery = "SELECT c.name, p.quantity, p.average_buy_price " +
                "FROM Portfolio p " +
                "JOIN Commodity c ON p.commodity_id = c.commodity_id " +
                "WHERE p.player_id = ?";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement cashStmt = conn.prepareStatement(cashQuery);
            cashStmt.setInt(1, playerId);
            ResultSet cashRs = cashStmt.executeQuery();

            System.out.println("\n--- YOUR PORTFOLIO ---");
            if (cashRs.next()) {
                System.out.printf("Available Cash: $%.2f%n", cashRs.getDouble("cash_balance"));
            }

            PreparedStatement portStmt = conn.prepareStatement(portfolioQuery);
            portStmt.setInt(1, playerId);
            ResultSet portRs = portStmt.executeQuery();

            System.out.println("\nAssets Owned:");
            System.out.printf("%-15s | %-10s | %-15s%n", "COMMODITY", "QUANTITY", "AVG BUY PRICE");
            System.out.println("-------------------------------------------------");

            boolean hasAssets = false;
            while (portRs.next()) {
                hasAssets = true;
                System.out.printf("%-15s | %-10d | $%-15.2f%n",
                        portRs.getString("name"),
                        portRs.getInt("quantity"),
                        portRs.getDouble("average_buy_price"));
            }

            if (!hasAssets) {
                System.out.println("You currently do not own any commodities.");
            }
            System.out.println("-------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Failed to fetch portfolio.");
            e.printStackTrace();
        }
    }
}