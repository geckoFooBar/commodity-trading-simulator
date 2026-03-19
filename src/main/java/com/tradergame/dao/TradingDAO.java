package com.tradergame.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TradingDAO {

    public boolean buyCommodity(int playerId, int commodityId, int quantity) throws SQLException {

        Connection conn = DatabaseConnection.getConnection();

        String checkPriceQuery = "SELECT current_price FROM Commodity WHERE commodity_id = ?";
        String checkCashQuery = "SELECT cash_balance FROM Player WHERE player_id = ?";
        String updateCashQuery = "UPDATE Player SET cash_balance = cash_balance - ? WHERE player_id = ?";
        
        // Using UPSERT (Insert if new, Update if already owns) - MySQL syntax
        String upsertPortfolioQuery = "INSERT INTO Portfolio (player_id, commodity_id, quantity, average_buy_price) " +
                                      "VALUES (?, ?, ?, ?) " +
                                      "ON DUPLICATE KEY UPDATE " +
                                      "average_buy_price = ((average_buy_price * quantity) + (? * ?)) / (quantity + ?), " +
                                      "quantity = quantity + ?";
                                      
        String logTransactionQuery = "INSERT INTO Transaction_History (player_id, commodity_id, transaction_type, quantity, price_per_unit) VALUES (?, ?, 'BUY', ?, ?)";

        try {

            conn.setAutoCommit(false); 

            PreparedStatement priceStmt = conn.prepareStatement(checkPriceQuery);
            priceStmt.setInt(1, commodityId);
            ResultSet priceRs = priceStmt.executeQuery();
            if (!priceRs.next()) throw new SQLException("Commodity not found.");
            double currentPrice = priceRs.getDouble("current_price");
            double totalCost = currentPrice * quantity;

            // 3. Check player's cash
            PreparedStatement cashStmt = conn.prepareStatement(checkCashQuery);
            cashStmt.setInt(1, playerId);
            ResultSet cashRs = cashStmt.executeQuery();
            if (!cashRs.next()) throw new SQLException("Player not found.");
            double cashBalance = cashRs.getDouble("cash_balance");
            
            if (cashBalance < totalCost) {
                System.out.println("Insufficient funds!");
                conn.rollback(); // Cancel transaction
                return false;
            }

            // 4. Deduct Cash
            PreparedStatement updateCashStmt = conn.prepareStatement(updateCashQuery);
            updateCashStmt.setDouble(1, totalCost);
            updateCashStmt.setInt(2, playerId);
            updateCashStmt.executeUpdate();

            // 5. Update Portfolio
            PreparedStatement portfolioStmt = conn.prepareStatement(upsertPortfolioQuery);
            portfolioStmt.setInt(1, playerId);
            portfolioStmt.setInt(2, commodityId);
            portfolioStmt.setInt(3, quantity);
            portfolioStmt.setDouble(4, currentPrice);
            // Parameters for ON DUPLICATE KEY UPDATE math
            portfolioStmt.setDouble(5, currentPrice);
            portfolioStmt.setInt(6, quantity);
            portfolioStmt.setInt(7, quantity);
            portfolioStmt.setInt(8, quantity);
            portfolioStmt.executeUpdate();

            // 6. Log Transaction
            PreparedStatement logStmt = conn.prepareStatement(logTransactionQuery);
            logStmt.setInt(1, playerId);
            logStmt.setInt(2, commodityId);
            logStmt.setInt(3, quantity);
            logStmt.setDouble(4, currentPrice);
            logStmt.executeUpdate();

            // 7. COMMIT TRANSACTION (Everything succeeded!)
            conn.commit();
            System.out.println("Successfully bought " + quantity + " units!");
            return true;

        } catch (SQLException e) {
            try {
                System.err.println("Transaction failed. Rolling back changes...");
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                // Reset auto-commit to true for other standard queries
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean sellCommodity(int playerId, int commodityId, int quantityToSell) {

        String checkPortfolioQuery = "SELECT quantity, average_buy_price FROM Portfolio WHERE player_id = ? AND commodity_id = ?";
        String checkPriceQuery = "SELECT current_price FROM Commodity WHERE commodity_id = ?";
        String updateCashQuery = "UPDATE Player SET cash_balance = cash_balance + ? WHERE player_id = ?";
        String updatePortfolioQuery = "UPDATE Portfolio SET quantity = quantity - ? WHERE player_id = ? AND commodity_id = ?";

        String logTransactionQuery = "INSERT INTO Transaction_History (player_id, commodity_id, transaction_type, quantity, price_per_unit, realized_profit) VALUES (?, ?, 'SELL', ?, ?, ?)";

        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Check Portfolio & Get Buy Price
            PreparedStatement portfolioCheckStmt = conn.prepareStatement(checkPortfolioQuery);
            portfolioCheckStmt.setInt(1, playerId);
            portfolioCheckStmt.setInt(2, commodityId);
            ResultSet portfolioRs = portfolioCheckStmt.executeQuery();

            if (!portfolioRs.next()) {
                System.out.println("You don't own any of this commodity!");
                conn.rollback(); return false;
            }

            int currentOwned = portfolioRs.getInt("quantity");
            double averageBuyPrice = portfolioRs.getDouble("average_buy_price"); // <-- Captured!

            if (currentOwned < quantityToSell) {
                System.out.println("Insufficient quantity.");
                conn.rollback(); return false;
            }

            // 2. Get Live Market Price
            PreparedStatement priceStmt = conn.prepareStatement(checkPriceQuery);
            priceStmt.setInt(1, commodityId);
            ResultSet priceRs = priceStmt.executeQuery();
            if (!priceRs.next()) throw new SQLException("Commodity not found.");

            double currentPrice = priceRs.getDouble("current_price");
            double totalRevenue = currentPrice * quantityToSell;

            // 3. Calculate Realized Profit!
            double realizedProfit = (currentPrice - averageBuyPrice) * quantityToSell;

            // 4. Update Cash
            PreparedStatement updateCashStmt = conn.prepareStatement(updateCashQuery);
            updateCashStmt.setDouble(1, totalRevenue);
            updateCashStmt.setInt(2, playerId);
            updateCashStmt.executeUpdate();

            // 5. Update Portfolio
            PreparedStatement updatePortfolioStmt = conn.prepareStatement(updatePortfolioQuery);
            updatePortfolioStmt.setInt(1, quantityToSell);
            updatePortfolioStmt.setInt(2, playerId);
            updatePortfolioStmt.setInt(3, commodityId);
            updatePortfolioStmt.executeUpdate();

            // 6. Log Transaction with Profit
            PreparedStatement logStmt = conn.prepareStatement(logTransactionQuery);
            logStmt.setInt(1, playerId);
            logStmt.setInt(2, commodityId);
            logStmt.setInt(3, quantityToSell);
            logStmt.setDouble(4, currentPrice);
            logStmt.setDouble(5, realizedProfit); // <-- Injected!
            logStmt.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try {
                Connection conn = DatabaseConnection.getConnection();
                if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}