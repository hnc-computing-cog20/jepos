/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.jepos;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author john
 */

// Holds information for members of staff
public class Staff {
    
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private ROLE role;
    private static Database db;
    private static Statement stmt;
    
    public Staff(int id, String firstName, String lastName,
                String username, String password, ROLE role)
    { // Produce full Staff object for writing to db or displaying info
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    public Staff(int id, String username, String password, ROLE role)
    { // Produce minimal Staff object for authentication
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }
    public void setNewId() throws SQLException
    { // Set the id (primary key) of this Staff member. This is for new Staff (not existing ones!).
        // Similar to Order.setNewOrderID() btw.
        Database.initStatement();
        // Get the last id in the orders table
        ResultSet rs = Database.getStatement().executeQuery("SELECT id FROM staff WHERE ID="
                + "(SELECT MAX(id) FROM staff)"
                );
        while(rs.next())
        {
            // This id is last id + 1
            this.id = rs.getInt("ID") + 1;
        }
        Database.close();
    }
    
    public String getUsername() { return this.username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return this.password; }
    public void setPassword(String password) { this.password = password; }
    
    public ROLE getRole() { return this.role; }
    public void setRole(ROLE role) { this.role = role; }
    
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getFirstName() { return this.firstName; }
    
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getLastName() { return this.lastName; }
    
    public String generateUsername() throws SQLException
    { // Generate a username from first initial, surname, and random num (10+99)
        String generatedUsername;
        generatedUsername = this.firstName.substring(0, 1) + this.lastName;
        
        Random rand = new Random();
        
        // Loop round if the username happens to already exist
        do
        {
            generatedUsername = generatedUsername + (rand.nextInt(90) + 10);
        
            generatedUsername = generatedUsername.toLowerCase();
        }while( userExists(generatedUsername) );
        
        return generatedUsername;
    }
    
    public static boolean userExists(String username) throws SQLException
    { // Check that a given username exists in the database
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM staff WHERE LOWER(username)="
                + "\"" + username.toLowerCase() + "\""
        );
        if(rs.next())
        { // Username exists in the database
            Database.close();
            return true;
        }
        else
        { // Username does not exist in the database
            Database.close();
            return false;
        }
    }
    
    public static boolean authenticate(String username, String password) throws SQLException
    { // Authenticate a username/password combination against the database
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT password FROM staff WHERE UPPER(username)="
                +"\"" + username.toUpperCase() + "\"");
        while(rs.next())
        {
            if(password.equals(rs.getString("PASSWORD")))
            { // Given password matches the one in database
                Database.close();
                System.out.println("Authentication success!");
                return true;
            }
            else
            { // Password incorrect
                Database.close();
                System.out.println("Authentication failed because password is incorrect!");
                return false;
            }
        }
        // I guess this could happen if the user didn't exist...
        Database.close();
        return false;
    }
    
    public static Staff getByUsername(String username) throws SQLException
    {
        Staff staff = null;
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM staff WHERE UPPER(username)="
                + "\"" + username.toUpperCase() + "\"");
        while(rs.next())
        {
            staff = new Staff(
                    rs.getInt("ID"),
                    rs.getString("FIRSTNAME"),
                    rs.getString("SURNAME"),
                    rs.getString("USERNAME"),
                    rs.getString("PASSWORD"),
                    ROLE.values()[rs.getInt("ROLE")]
                    );
        }
        Database.close();
        return staff;
    }
    
    public static ArrayList<Staff> getAllStaff() throws SQLException
    { // Get a list of all staff
        ArrayList<Staff> list = new ArrayList<Staff>();
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM staff");
        while(rs.next())
        {
            Staff staff = new Staff(
                    rs.getInt("ID"),
                    rs.getString("FIRSTNAME"),
                    rs.getString("SURNAME"),
                    rs.getString("USERNAME"),
                    rs.getString("PASSWORD"),
                    ROLE.values()[rs.getInt("ROLE")]
                    );
            list.add(staff);
        }
        Database.close();
        return list;
    }
    
    public static int getTotalCustServed(String staffUsername) throws SQLException
    { // Get the total amount of orders this member of staff has handled
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM orders WHERE UPPER(handler)="
                + "\""+staffUsername+"\";"
                );
        int total = 0;
        while(rs.next())
        {
            total++;
        }
        Database.close();
        return total;
    }
    
    public static double getTotalSalesTakings(String staffUsername) throws SQLException
    { // Get the total amount of money that has been made from sales
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT items FROM orders WHERE UPPER(handler)="
                + "\""+staffUsername+"\";"
                );
        double total = 0;
        // Key = item, Value = quantity
        LinkedHashMap<Product, Integer> singleOrderItems = new LinkedHashMap<Product, Integer>();
        while(rs.next())
        {
            singleOrderItems = Order.parseAllItemsFromDb( rs.getString("ITEMS") );
            for( Map.Entry<Product, Integer> i : singleOrderItems.entrySet() )
            { // Loop through the items in this order
                // total += Price of item * quantity
                total += i.getKey().getPrice() * i.getValue();
            }
            singleOrderItems.clear(); // Clear for next iteration
        }
        Database.close();
        return total;
    }
    
    public static double getAvgSaleCost(String staffUsername) throws SQLException
    { // Get the average total cost of sales by this staff member
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT items FROM orders WHERE UPPER(handler)="
                + "\""+staffUsername+"\";"
                );
        double avg = 0;
        double total = 0;
        int count = 0; // Could also just call getTotalCustServed() instead of counting, but this keeps db calls down
        // Key = item, Value = quantity
        LinkedHashMap<Product, Integer> singleOrderItems = new LinkedHashMap<Product, Integer>();
        while(rs.next())
        {
            singleOrderItems = Order.parseAllItemsFromDb( rs.getString("ITEMS") );
            for( Map.Entry<Product, Integer> i : singleOrderItems.entrySet() )
            { // Loop through the items in this order
                // total += Price of item * quantity
                total += i.getKey().getPrice() * i.getValue();
            }
            singleOrderItems.clear(); // Clear for next iteration
            count++;
        }
        Database.close();
        avg = total / count;
        return avg;
    }
    
    public static double getMinSaleCost(String staffUsername) throws SQLException
    { // Get the minimum total cost from all sales by this staff member
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT items FROM orders WHERE UPPER(handler)="
                + "\""+staffUsername+"\";"
                );
        double min = 0;
        double singleOrderTotal = 0;
        ArrayList<Double> totals = new ArrayList<Double>();
        // Key = item, Value = quantity
        LinkedHashMap<Product, Integer> singleOrderItems = new LinkedHashMap<Product, Integer>();
        while(rs.next())
        {
            singleOrderItems = Order.parseAllItemsFromDb( rs.getString("ITEMS") );
            for( Map.Entry<Product, Integer> i : singleOrderItems.entrySet() )
            { // Loop through the items in this order
                // add the totals of each order to totals
                singleOrderTotal += i.getKey().getPrice() * i.getValue();
            }
            singleOrderItems.clear(); // Clear for next iteration
            totals.add(singleOrderTotal);
            singleOrderTotal = 0;
        }
        Database.close();
        
        // Now to get the minimum
        min = totals.get(0);
        for(double i : totals)
        {
            if(i < min) { min = i; } // If current num is less than min, min = current num.
        }
        
        return min;
    }
    
    public static double getMaxSaleCost(String staffUsername) throws SQLException
    { // Get the maximum total cost from all sales by this staff member
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT items FROM orders WHERE UPPER(handler)="
                + "\""+staffUsername+"\";"
                );
        double max = 0;
        double singleOrderTotal = 0;
        ArrayList<Double> totals = new ArrayList<Double>();
        // Key = item, Value = quantity
        LinkedHashMap<Product, Integer> singleOrderItems = new LinkedHashMap<Product, Integer>();
        while(rs.next())
        {
            singleOrderItems = Order.parseAllItemsFromDb( rs.getString("ITEMS") );
            for( Map.Entry<Product, Integer> i : singleOrderItems.entrySet() )
            { // Loop through the items in this order
                // add the totals of each order to totals
                singleOrderTotal += i.getKey().getPrice() * i.getValue();
            }
            singleOrderItems.clear(); // Clear for next iteration
            totals.add(singleOrderTotal);
            singleOrderTotal = 0;
        }
        Database.close();
        
        // Now to get the maximum
        max = totals.get(0);
        for(double i : totals)
        {
            if(i > max) { max = i; } // If current num is greater than max, max = current num.
        }
        
        return max;
    }
    
    public void save() throws SQLException
    { // Save staff member to the database
        // Should actually check by ID but doesn't matter
        if( userExists(this.username) )
        { // Staff member exists in the database - we're updating an existing record.
            System.out.println("Staff member exists, updating existing record...");
            saveExistingStaff();
        }
        else
        { // Staff member doesn't exist - we're created a new record.
            System.out.println("Staff member doesn't exist, creating new record...");
            saveNewStaff();
        }
        
        System.out.println(
                "User ID " + this.id + " \""+this.username+"\""
                + " written to the database!"
        );
    }
    
    private void saveExistingStaff() throws SQLException
    { // Update an existing staff member
        Database.initStatement();
        Database.getStatement().executeUpdate(
                "UPDATE staff SET "
                + "id=" + this.id + ","
                + "firstname=" + "\""+this.firstName+"\"" + ","
                + "surname=" + "\""+this.lastName+"\"" + ","
                + "username=" + "\""+this.username+"\"" + ","
                + "password=" + "\""+this.password+"\"" + ","
                + "role=" + this.role.ordinal() + " "
                + "WHERE LOWER(username)="
                + "\""+this.username.toLowerCase()+"\"" + ";"
        );
        
        Database.close();
    }
    private void saveNewStaff() throws SQLException
    { // Write a new staff member
        Database.initStatement();
        Database.getStatement().executeUpdate(
                "INSERT INTO staff VALUES("
                + this.id + ","
                + "\""+this.firstName+"\"" + ","
                + "\""+this.lastName+"\"" + ","
                + "\""+this.username+"\"" + ","
                + "\""+this.password+"\"" + ","
                + this.role.ordinal() + ");"
        );
        Database.close();
    }
    
    public void delete() throws SQLException
    { // Delete a staff member from the database
        // Check that record exists first
        if( userExists(this.username) )
        { // Record exists
            Database.initStatement();
            Database.getStatement().executeUpdate(
                    "DELETE FROM staff WHERE LOWER(username)="
                    + "\""+this.username.toLowerCase()+"\"" + ";"
            );
            Database.close();
            System.out.println("Deleted user \""+this.username+"\" from the database!");
        }
        else
        { // Record does not exist, probably an unsaved Staff object, abort.
            System.out.println("Record does not exist for \""+this.username+"\", nothing was deleted.");
        }
    }
}
