package com.tradergame;

import com.tradergame.dao.DatabaseConnection;
import com.tradergame.dao.TradingDAO;
import com.tradergame.service.MarketSimulator;

import org.jfree.chart.axis.LogAxis;
import java.text.NumberFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TraderUI extends JFrame {

    final private int playerId;
    final private String username;

    private java.util.List<String> activeHeadlines = new java.util.ArrayList<>();
    private int tickerIndex = 0;

    private final TradingDAO tradingDAO = new TradingDAO();

    private JLabel balanceLabel, investedLabel, pnlLabel, realizedPnlLabel, newsLabel;
    private JTable marketTable, portfolioTable, historyTable;
    private DefaultTableModel marketModel, portfolioModel, historyModel;
    private JComboBox<String> commodityDropdown;
    private JTextField quantityField;
    private Map<Integer, TimeSeries> chartSeriesMap = new HashMap<>();

    private MarketSimulator simulator;
    private Timer refreshTimer, tickerTimer;

    public TraderUI(int loggedInPlayerId, String loggedInUsername) {

        this.playerId = loggedInPlayerId;
        this.username = loggedInUsername;

        setTitle("Global Commodity Trader Simulator");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Base layout

        simulator = new MarketSimulator();
        new Thread(simulator).start();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel dashboardPanel = buildDashboardTab();
        JPanel historyPanel = buildHistoryTab();

        tabbedPane.addTab("Trading Dashboard", dashboardPanel);
        tabbedPane.addTab("Transaction History", historyPanel);

        add(tabbedPane, BorderLayout.CENTER);

        refreshTimer = new Timer(2000, e -> refreshData());
        refreshTimer.start();

        tickerTimer = new Timer(4000, e -> {
            if (activeHeadlines.isEmpty()) return;

            String headline = activeHeadlines.get(tickerIndex);
            newsLabel.setText(headline);

            // Visual cue based on content
            boolean isBreaking = headline.contains("BREAKING")
                    || headline.contains("CRISIS")
                    || headline.contains("ALERT");

            newsLabel.getParent().setBackground(
                    isBreaking ? new Color(185, 28, 28)   // Deep red
                            : new Color(51, 65, 85)    // Calm slate
            );

            // Advance to next headline, wrap around
            tickerIndex = (tickerIndex + 1) % activeHeadlines.size();
        });
        tickerTimer.start();

        refreshData();
    }

    private void applyDarkThemeToTable(JTable table, JScrollPane scrollPane) {

        table.setBackground(new Color(30, 41, 59));
        table.setForeground(new Color(241, 245, 249));
        table.setGridColor(new Color(71, 85, 105));
        table.setSelectionBackground(new Color(56, 189, 248));
        table.setSelectionForeground(Color.BLACK);
        table.setRowHeight(35);
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));

        table.getTableHeader().setBackground(new Color(15, 23, 42)); // Deeper navy for the header
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        table.getTableHeader().setOpaque(false);

        scrollPane.getViewport().setBackground(new Color(30, 41, 59));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105), 1));

        centerTableData(table);
    }

    private JPanel buildDashboardTab() {
        JPanel dashboard = new JPanel(new BorderLayout(10, 10));

        JPanel topContainer = new JPanel(new BorderLayout());

        JPanel statsPanel = new JPanel(new GridLayout(1, 4));
        statsPanel.setBackground(new Color(40, 44, 52));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        balanceLabel = new JLabel("Cash: $0.00", SwingConstants.CENTER);
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        investedLabel = new JLabel("Invested: $0.00", SwingConstants.CENTER);
        investedLabel.setForeground(Color.LIGHT_GRAY);
        investedLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        pnlLabel = new JLabel("Unrealized P/L: $0.00", SwingConstants.CENTER);
        pnlLabel.setForeground(Color.WHITE);
        pnlLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        realizedPnlLabel = new JLabel("Lifetime Profit: $0.00", SwingConstants.CENTER);
        realizedPnlLabel.setForeground(new Color(241, 196, 15));
        realizedPnlLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        statsPanel.add(balanceLabel);
        statsPanel.add(investedLabel);
        statsPanel.add(pnlLabel);
        statsPanel.add(realizedPnlLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        actionPanel.setBackground(new Color(40, 44, 52));

        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setBackground(new Color(220, 38, 38)); // Clean Red
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> performLogout());
        actionPanel.add(logoutButton);

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.add(statsPanel, BorderLayout.CENTER);
        headerBar.add(actionPanel, BorderLayout.EAST);

        JPanel newsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        newsPanel.setBackground(new Color(220, 38, 38)); // Alert Red
        newsLabel = new JLabel("MARKET OPEN: Fetching latest news...");
        newsLabel.setForeground(Color.WHITE);
        newsLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        newsPanel.add(newsLabel);

        topContainer.add(headerBar, BorderLayout.CENTER);
        topContainer.add(newsPanel, BorderLayout.SOUTH);

        dashboard.add(topContainer, BorderLayout.NORTH);


        String[] marketCols = {"ID", "Asset", "Live Price"};
        marketModel = new DefaultTableModel(marketCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        marketTable = new JTable(marketModel);
        marketTable.setRowHeight(25);
        centerTableData(marketTable); // Align text to center
        JScrollPane marketScroll = new JScrollPane(marketTable);
        marketScroll.setBorder(BorderFactory.createTitledBorder("Live Market"));

        applyDarkThemeToTable(marketTable, marketScroll);

        String[] portCols = {"Asset", "Qty", "Bought at Price", "Live Price", "Total Value", "P/L"};
        portfolioModel = new DefaultTableModel(portCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        portfolioTable = new JTable(portfolioModel);
        portfolioTable.setRowHeight(25);
        centerTableData(portfolioTable); // Align text to center
        JScrollPane portfolioScroll = new JScrollPane(portfolioTable);
        portfolioScroll.setBorder(BorderFactory.createTitledBorder("Your Portfolio"));
        applyDarkThemeToTable(portfolioTable, portfolioScroll);

        JSplitPane tablesSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, marketScroll, portfolioScroll);
        tablesSplit.setResizeWeight(0.3);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT commodity_id, name FROM Commodity");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                TimeSeries series = new TimeSeries(rs.getString("name"));
                chartSeriesMap.put(rs.getInt("commodity_id"), series);
                dataset.addSeries(series);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Live Asset Price History", "Time", "Price ($)", dataset, true, true, false);
        applyDarkThemeToChart(chart);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity instanceof LegendItemEntity) {
                    LegendItemEntity legendEntity = (LegendItemEntity) entity;
                    String commodityName = (String) legendEntity.getSeriesKey();

                    showSingleCommodityChart(commodityName);
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {

            }
        });
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablesSplit, chartPanel);
        mainSplit.setResizeWeight(0.5);
        dashboard.add(mainSplit, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 20)); // Increased padding
        bottomPanel.setBackground(new Color(15, 23, 42)); // Match the dark theme

        Font massiveFont = new Font("SansSerif", Font.BOLD, 22);

        JLabel commLabel = new JLabel("Commodity:");
        commLabel.setForeground(Color.WHITE);
        commLabel.setFont(massiveFont);

        commodityDropdown = new JComboBox<>();
        commodityDropdown.setFont(new Font("SansSerif", Font.BOLD, 20));
        commodityDropdown.setPreferredSize(new Dimension(250, 50)); // Make the dropdown much wider and taller

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT commodity_id, name FROM Commodity ORDER BY commodity_id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                commodityDropdown.addItem(rs.getInt("commodity_id") + " - " + rs.getString("name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        JLabel qtyLabel = new JLabel("Qty:");
        qtyLabel.setForeground(Color.WHITE);
        qtyLabel.setFont(massiveFont);

        quantityField = new JTextField(5);
        quantityField.setText("10");
        quantityField.setFont(massiveFont);
        quantityField.setHorizontalAlignment(JTextField.CENTER);
        quantityField.setPreferredSize(new Dimension(120, 50)); // Make the text box taller

        JButton buyButton = new JButton("BUY");
        buyButton.setBackground(new Color(16, 185, 129)); // Emerald Green
        buyButton.setForeground(Color.WHITE);
        buyButton.setFont(massiveFont);
        buyButton.setPreferredSize(new Dimension(160, 55));
        buyButton.setFocusPainted(false);
        buyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton sellButton = new JButton("SELL");
        sellButton.setBackground(new Color(220, 38, 38)); // Crimson Red
        sellButton.setForeground(Color.WHITE);
        sellButton.setFont(massiveFont);
        sellButton.setPreferredSize(new Dimension(160, 55));
        sellButton.setFocusPainted(false);
        sellButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buyButton.addActionListener(e -> executeTrade(true));
        sellButton.addActionListener(e -> executeTrade(false));

        bottomPanel.add(commLabel);
        bottomPanel.add(commodityDropdown);
        bottomPanel.add(qtyLabel);
        bottomPanel.add(quantityField);
        bottomPanel.add(buyButton);
        bottomPanel.add(sellButton);

        dashboard.add(bottomPanel, BorderLayout.SOUTH);

        return dashboard;
    }

    private void applyDarkThemeToChart(JFreeChart chart) {

        chart.setBackgroundPaint(new Color(30, 41, 59));

        if (chart.getTitle() != null) chart.getTitle().setPaint(Color.WHITE);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(30, 41, 59));
            chart.getLegend().setItemPaint(Color.LIGHT_GRAY);
        }

        org.jfree.chart.plot.XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(15, 23, 42)); // Deep dark background for the graph
        plot.setDomainGridlinePaint(new Color(71, 85, 105)); // Subtle gray vertical gridlines
        plot.setRangeGridlinePaint(new Color(71, 85, 105));  // Subtle gray horizontal gridlines

        plot.getDomainAxis().setTickLabelPaint(Color.LIGHT_GRAY);
        plot.getDomainAxis().setLabelPaint(Color.WHITE);

        LogAxis logYAxis = new LogAxis("Price (Log Scale)");
        logYAxis.setNumberFormatOverride(NumberFormat.getCurrencyInstance()); // Keep the $ format
        logYAxis.setTickLabelPaint(Color.LIGHT_GRAY);
        logYAxis.setLabelPaint(Color.WHITE);
        plot.setRangeAxis(logYAxis);

        org.jfree.chart.renderer.xy.XYItemRenderer renderer = plot.getRenderer();
        for (int i = 0; i < 20; i++) {
            // 3.0f makes the line 3 pixels thick (default is 1.0f)
            renderer.setSeriesStroke(i, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
    }

    private void performLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Logout Confirm",
                JOptionPane.YES_NO_OPTION);
        if (tickerTimer != null) tickerTimer.stop();
        if (choice == JOptionPane.YES_OPTION) {
            if (simulator != null) simulator.stopSimulator();
            if (refreshTimer != null) refreshTimer.stop();

            this.dispose();

            SwingUtilities.invokeLater(() -> {
                LoginUI loginWindow = new LoginUI();
                loginWindow.setLocationRelativeTo(null);
                loginWindow.setVisible(true);
            });
        }
    }

    private void showSingleCommodityChart(String commodityName) {
        TimeSeries selectedSeries = null;

        for (TimeSeries series : chartSeriesMap.values()) {
            if (series.getKey().equals(commodityName)) {
                selectedSeries = series;
                break;
            }
        }

        if (selectedSeries != null) {
            TimeSeriesCollection singleDataset = new TimeSeriesCollection();
            singleDataset.addSeries(selectedSeries);

            JFreeChart singleChart = ChartFactory.createTimeSeriesChart(
                    commodityName,
                    "Time",
                    "Price ($)",
                    singleDataset,
                    false,
                    true,
                    false
            );

            applyDarkThemeToChart(singleChart);

            singleChart.getXYPlot().getRenderer().setSeriesPaint(0, new Color(56, 189, 248));
            singleChart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(4.0f));

            ChartPanel singleChartPanel = new ChartPanel(singleChart);
            singleChartPanel.setPreferredSize(new Dimension(600, 400));

            JDialog dialog = new JDialog(this, commodityName + " Live Chart", false); // 'false' lets you keep interacting with the main app
            dialog.add(singleChartPanel);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
    }

    private JPanel buildHistoryTab() {
        JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
        historyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] historyCols = {"Transaction ID", "Asset", "Type", "Quantity", "Price Executed", "Total Value", "Realized P/L", "Date & Time"};
        historyModel = new DefaultTableModel(historyCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(30);
        historyTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        centerTableData(historyTable);

        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Complete Ledger"));
        applyDarkThemeToTable(historyTable, historyScroll);

        historyPanel.add(historyScroll, BorderLayout.CENTER);
        return historyPanel;
    }


    private void centerTableData(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private void executeTrade(boolean isBuy) {
        try {
            int commodityId = Integer.parseInt(commodityDropdown.getSelectedItem().toString().split(" - ")[0]);
            int quantity = Integer.parseInt(quantityField.getText().trim());

            boolean success = isBuy ? tradingDAO.buyCommodity(playerId, commodityId, quantity)
                    : tradingDAO.sellCommodity(playerId, commodityId, quantity);
            if (success) refreshData();
            else JOptionPane.showMessageDialog(this, "Trade failed. Check balance or inventory.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid quantity.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void refreshData() {

        try (Connection conn = DatabaseConnection.getConnection()) {
            Second currentSecond = new Second();

            PreparedStatement newsStmt = conn.prepareStatement(
                    "SELECT headline FROM Market_Events ORDER BY last_triggered DESC LIMIT 1"
            );
            ResultSet newsRs = newsStmt.executeQuery();

            if (newsRs.next()) {
                String latestNews = newsRs.getString("headline");
                newsLabel.setText(latestNews);

                // Visual cue based on content
                boolean isBreaking = latestNews.contains("BREAKING")
                        || latestNews.contains("CRISIS")
                        || latestNews.contains("ALERT");

                newsLabel.getParent().setBackground(
                        isBreaking ? new Color(185, 28, 28)
                                : new Color(51, 65, 85)
                );
            }

            PreparedStatement cashStmt = conn.prepareStatement("SELECT cash_balance FROM Player WHERE player_id = ?");
            cashStmt.setInt(1, playerId);
            ResultSet cashRs = cashStmt.executeQuery();
            if (cashRs.next()) {
                balanceLabel.setText(String.format("Cash: $%,.2f", cashRs.getDouble("cash_balance")));
            }

            PreparedStatement marketStmt = conn.prepareStatement("SELECT commodity_id, name, current_price FROM Commodity");
            ResultSet marketRs = marketStmt.executeQuery();
            marketModel.setRowCount(0);

            while (marketRs.next()) {
                int id = marketRs.getInt("commodity_id");
                String name = marketRs.getString("name");
                double price = marketRs.getDouble("current_price");

                marketModel.addRow(new Object[]{ id, name, String.format("$%,.2f", price) });

                if (chartSeriesMap.containsKey(id)) {
                    chartSeriesMap.get(id).addOrUpdate(currentSecond, price);
                }
            }

            updatePortfolioData(conn);

            updateHistoryData(conn);

        } catch (SQLException e) {
            System.err.println("Refresh Cycle Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePortfolioData(Connection conn) throws SQLException {
        String query = "SELECT c.name, p.quantity, p.average_buy_price, c.current_price " +
                "FROM Portfolio p JOIN Commodity c ON p.commodity_id = c.commodity_id " +
                "WHERE p.player_id = ?";

        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, playerId);
        ResultSet rs = ps.executeQuery();

        portfolioModel.setRowCount(0);
        double totalInvested = 0;
        double totalCurrentValue = 0;

        while (rs.next()) {
            String name = rs.getString(1);
            int qty = rs.getInt(2);
            double avgBuy = rs.getDouble(3);
            double livePrice = rs.getDouble(4);

            double costBasis = qty * avgBuy;
            double currentValue = qty * livePrice;
            double pnl = currentValue - costBasis;

            totalInvested += costBasis;
            totalCurrentValue += currentValue;

            String pnlStr = String.format("%s$%,.2f", (pnl >= 0 ? "+" : "-"), Math.abs(pnl));

            portfolioModel.addRow(new Object[]{
                    name, qty,
                    String.format("$%,.2f", avgBuy),
                    String.format("$%,.2f", livePrice),
                    String.format("$%,.2f", currentValue),
                    pnlStr
            });
        }

        investedLabel.setText(String.format("Invested: $%,.2f", totalInvested));
        double unrealizedPnL = totalCurrentValue - totalInvested;

        String upnlStr = String.format("Unrealized P/L: %s$%,.2f", (unrealizedPnL >= 0 ? "+" : "-"), Math.abs(unrealizedPnL));
        pnlLabel.setText(upnlStr);

        pnlLabel.setForeground(unrealizedPnL >= 0 ? new Color(52, 211, 153) : new Color(248, 113, 113));

    }

    private void updateHistoryData(Connection conn) throws SQLException {
        String query = "SELECT t.transaction_id, c.name, t.transaction_type, t.quantity, t.price_per_unit, t.realized_profit, t.transaction_date " +
                "FROM Transaction_History t JOIN Commodity c ON t.commodity_id = c.commodity_id " +
                "WHERE t.player_id = ? ORDER BY t.transaction_date DESC";

        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, playerId);
        ResultSet rs = ps.executeQuery();

        historyModel.setRowCount(0);
        double lifetimeProfit = 0;

        while (rs.next()) {
            double profit = rs.getDouble("realized_profit");
            lifetimeProfit += profit;

            String type = rs.getString("transaction_type");
            String profStr = type.equals("SELL") ? String.format("%s$%,.2f", (profit >= 0 ? "+" : "-"), Math.abs(profit)) : "-";

            int qty = rs.getInt("quantity");
            double price = rs.getDouble("price_per_unit");
            double totalValue = qty * price;

            historyModel.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getString("name"),
                    type,
                    qty,
                    String.format("$%,.2f", price),
                    String.format("$%,.2f", totalValue),
                    profStr,
                    rs.getString("transaction_date").substring(0, 19)
            });
        }

        String lifeStr = String.format("Lifetime Profit: %s$%,.2f", (lifetimeProfit >= 0 ? "+" : "-"), Math.abs(lifetimeProfit));
        realizedPnlLabel.setText(lifeStr);

        if (lifetimeProfit > 0) {
            realizedPnlLabel.setForeground(new Color(52, 211, 153));
        } else if (lifetimeProfit < 0) {
            realizedPnlLabel.setForeground(new Color(248, 113, 113));
        }
        else realizedPnlLabel.setForeground(new Color(250, 204, 21));
    }
}