package com.tradergame.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/db_name"; // By default MySQL uses port 3006
    private static final String USER = "user"; // Change it to your MySQL username
    private static final String PASSWORD = "password"; // Change it to your MySQL password

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
