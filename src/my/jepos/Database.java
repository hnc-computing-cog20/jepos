/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.jepos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author john
 */

// Contains everything required for accessing a database
// Can be used statically or as an instance.
public class Database {
    private static String driver = "jdbc:sqlite:";
    private static String dbName = "db/database.db";
    private static String conURL = driver + dbName;
    private static Connection con;
    private static Statement stmt;
    
    public Database() throws SQLException
    { // If instantiated, initStatement() is not required before ResultSet queries.
        this.driver = "jdbc:sqlite:";
        this.dbName = "db/database.db"; // May need to reverse slashes for Windows LOL
        this.conURL = driver + dbName;
        this.con = DriverManager.getConnection(conURL);
        this.stmt = this.con.createStatement();
        
        System.out.println("Connection to SQLite database "+dbName+" established!");
    }
    
    static { // Old code, DELETE
        /*try {
            // Not sure if this is a good idea but...
            //Database.con = DriverManager.getConnection(conURL);
            //Database.stmt = con.createStatement();
            System.out.println("(static) Connection to SQLite database db/database.db established!");
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
    }
    
    public static Statement initStatement() throws SQLException
    { // Prepare stmt before initialising any ResultSet
        Database.con = DriverManager.getConnection(conURL);
        Database.stmt = con.createStatement();
        System.out.println("(static) Connection to SQLite database db/database.db established!");
        return stmt;
    }
    
    public static Connection getCon()
    {
        return Database.con;
    }
    
    public static Statement getStatement()
    {
        return Database.stmt;
    }
    
    public static void close() throws SQLException
    { // Close the connection. Always call this once the database is no longer required!
        Database.con.close();
        System.out.println("Database connection closed.");
    }
    
    public static void clearDatabase() throws SQLException
    { // Delete all entries in the database
        Database.initStatement();
        Database.getStatement().executeUpdate("DELETE FROM staff;");
        Database.getStatement().executeUpdate("DELETE FROM orders;");
        Database.getStatement().executeUpdate("DELETE FROM inventory;");
        Database.close();
    }
    
    public static void fillTestDatabase() throws SQLException
    { // Fills the database with some values for testing (to make it easy for Asmat)
        Database.initStatement();
        // Add the staff
        Database.getStatement().executeUpdate("INSERT INTO staff VALUES "
                + "(0,\"Stephen\",\"Staffington\",\"stephen\",\"ste7\",0),"
                + "(1,\"Maximus\",\"de Managero\",\"max\",\"max2\",1),"
                + "(2,\"Timothy\",\"Technicius\",\"tim\",\"tim5\",2);"
        );
        // Add the inventory
        Database.getStatement().executeUpdate("INSERT INTO inventory VALUES "
                + "(0,\"Apple\",0.6),"
                + "(1,\"Banana\",0.72),"
                + "(2,\"Egg\",0.8),"
                + "(3,\"Bread\",1.15),"
                + "(4,\"Onion\",0.9),"
                + "(5,\"Dog\",150.0),"
                + "(6,\"Milk\",1.3),"
                + "(7,\"Meme\",13.37),"
                + "(8,\"BigTV\",199.99);"
        );
        // Add an order for each user (or the Reports view won't work)
        Database.getStatement().executeUpdate("INSERT INTO orders VALUES "
                + "(0,\"stephen\",\"0,1;\",1623788155),"
                + "(1,\"max\",\"0,2;\",1623788155),"
                + "(2,\"tim\",\"0,3;\",1623788155);"
        );
        Database.close();
    }
    
    public String testFunc() throws SQLException
    { // DEBUG: Makes sure we can actually pull data from the database
        String result = "";
        Statement stmt = this.con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM staff");
        while (rs.next())
                {
                    result += rs.getString("USERNAME") + ":"
                            + rs.getString("PASSWORD") + "\n";
                }
        return result;
    }
}
