# 📈 Global Commodity Trading Simulator

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Swing](https://img.shields.io/badge/Java_Swing-007396?style=for-the-badge&logo=java&logoColor=white)

A high-performance, multithreaded desktop trading simulator built with Java and MySQL. This application mimics a real-world financial terminal, featuring a live market engine, dynamic news events, and real-time portfolio tracking.

## ✨ Key Features

* **Real-Time Market Engine:** A background simulator thread runs concurrently with the UI, recalculating market prices via JDBC batch processing every few seconds.
* **Dynamic News & Events:** Global events (like pipeline leaks or tech breakthroughs) are pulled from the database, instantly impacting specific commodity prices and altering the UI's news ticker.
* **Algorithmic Market "Gravity":** Built-in mean-reversion logic prevents hyperinflation, ensuring assets normalize after massive crashes or spikes.
* **Advanced Charting:** Integrates **JFreeChart** with a custom dark-mode theme and logarithmic Y-axis scaling to visualize volatile assets alongside high-value commodities simultaneously.
* **Comprehensive Portfolio Management:** Instantly calculates Unrealized P/L based on live prices, tracks total investment, and maintains a permanent, immutable ledger of all historical trades and Lifetime Profit.
* **Modern Dark UI:** Completely customized Java Swing components, featuring responsive action buttons, custom table cell rendering, and professional color palettes.

## 🛠️ Tech Stack

* **Language:** Java (JDK 11+)
* **GUI Framework:** Java Swing / AWT
* **Database:** MySQL
* **Libraries:**
  * `mysql-connector-java` (JDBC)
  * `jfreechart` (Data Visualization)

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK) 11** or higher installed.
* **MySQL Server** running locally or in the cloud.
* Your preferred Java IDE (IntelliJ IDEA, Eclipse, VS Code).

### 1. Clone the repository
```bash
git clone https://github.com/geckoFooBar/commodity-trader-simulator.git
cd Global-Commodity-Trader
```
### 2. Database Setup and Schema
```
CREATE DATABASE IF NOT EXISTS trader_game;
USE trader_game;

CREATE TABLE Player (
    player_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    cash_balance DECIMAL(15, 2) DEFAULT 25000.00
);

CREATE TABLE Commodity (
    commodity_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    current_price DECIMAL(15, 2) NOT NULL
);

CREATE TABLE Portfolio (
    player_id INT,
    commodity_id INT,
    quantity INT NOT NULL DEFAULT 0,
    average_buy_price DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (player_id, commodity_id),
    FOREIGN KEY (player_id) REFERENCES Player(player_id),
    FOREIGN KEY (commodity_id) REFERENCES Commodity(commodity_id)
);

CREATE TABLE Transaction_History (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT,
    commodity_id INT,
    transaction_type ENUM('BUY', 'SELL') NOT NULL,
    quantity INT NOT NULL,
    price_per_unit DECIMAL(15, 2) NOT NULL,
    realized_profit DECIMAL(15, 2) DEFAULT 0.00,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES Player(player_id),
    FOREIGN KEY (commodity_id) REFERENCES Commodity(commodity_id)
);

CREATE TABLE Market_Events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    headline VARCHAR(255) NOT NULL,
    target_commodity_id INT,
    price_multiplier DECIMAL(5, 2),
    last_triggered TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_commodity_id) REFERENCES Commodity(commodity_id)
);

INSERT INTO Commodity (name, current_price) VALUES 
('Gold', 2500.00), ('Silver', 30.00), ('Crude Oil', 80.00), 
('Platinum', 1000.00), ('Copper', 4.00), ('Wheat', 6.00), 
('Coffee', 2.50), ('Natural Gas', 3.00), ('Lithium', 150.00), 
('Uranium', 85.00), ('Corn', 5.20), ('Palladium', 1400.00);

INSERT INTO Market_Events (headline, target_commodity_id, price_multiplier) VALUES 
('HISTORIC CRASH: OPEC completely dissolves; nations flood the market with infinite Crude Oil!', 3, 0.20),
('TECH MANIA: Solid-state battery breakthrough sends Lithium demand into overdrive.', 9, 1.60),
('ENERGY CRISIS: Nations pivot aggressively to nuclear; Uranium reserves instantly depleted.', 10, 1.55),
('WEATHER ALERT: Historic droughts across the Midwest devastate global Corn yields.', 11, 1.45),
('ASTEROID MINING: First successful space retrieval brings 10,000 tons of Gold to Earth. Market collapses!', 1, 0.25),
('MARKET UPDATE: Standard trading volume. Prices fluctuating normally.', 1, 1.00);

INSERT INTO Player (username, password, cash_balance) VALUES ('testuser', 'password123', 50000.00);
```

## 3. Configure Database Credentials
### Navigate to DatabaseConnection.java (or your DAO configuration class) in the source code. Update the JDBC URL, username, and password to match your local MySQL configuration:

```
public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/trader_game"; // Used MySQL as DB
    private static final String USER = "user";          // Change to your MySQL username
    private static final String PASSWORD = "password";  // Change to your MySQL password
}
```

## 4. Build and Run
+ Through IDE: Open the project in IntelliJ IDEA/Eclipse, and run the Main.java file. Personally I would use IntelliJ.
+ Through Executable: If you have compiled the project using a tool like Launch4j, simply double-click the GlobalCommodityTrader.exe file.
+ Log In: Use the default credentials created in the SQL script (testuser / password123) to access the dashboard.
