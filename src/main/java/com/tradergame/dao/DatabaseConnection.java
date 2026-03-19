package com.tradergame.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/global_commodity_trader";
    private static final String USER = "root";
    private static final String PASSWORD = "y3llow/Tee";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        //System.out.println("Database Connected!!!");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}