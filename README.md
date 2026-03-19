# 📈 Global Commodity Trader Pro

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Swing](https://img.shields.io/badge/Java_Swing-007396?style=for-the-badge&logo=java&logoColor=white)

A high-performance, multithreaded desktop trading simulator built with Java and MySQL. This application mimics a real-world financial terminal, featuring a live market engine, dynamic news events, and real-time portfolio tracking.

## 📸 Previews

> **Note:** *Update these placeholder links with actual screenshots of your application from an `assets/` folder in your repository.*

| Live Trading Dashboard | Complete Transaction Ledger |
| :---: | :---: |
| ![Dashboard](placeholder-link-to-dashboard-screenshot.png) | ![Ledger](placeholder-link-to-ledger-screenshot.png) |

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
* **Libraries:** * `mysql-connector-java` (JDBC)
  * `jfreechart` (Data Visualization)

---

## 🚀 Getting Started

### Prerequisites
* **Java Development Kit (JDK) 11** or higher installed.
* **MySQL Server** running locally or in the cloud.
* Your preferred Java IDE (IntelliJ IDEA, Eclipse, VS Code).

### 1. Clone the repository
```bash
git clone https://github.com/YourUsername/Global-Commodity-Trader.git
cd Global-Commodity-Trader
