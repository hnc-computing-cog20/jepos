/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.jepos;

import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 *
 * @author john
 */

// Interface for accessing the store's inventory via the database
public class Inventory {
    private ArrayList<Product> products; // TODO: Should probably delete
    
    public Inventory() throws SQLException
    {
        this.products = new ArrayList<Product>();
    }
    
    public void addProduct(Product product)
    { // TODO: Change this to add to the database.
        this.products.add(product);
    }
    
    public void printProducts()
    { // DELETE: Obsolete.
        for(Product i : this.products)
        {
            System.out.println(i.getId()+"\t"+i.getName()+"\t"+i.getPrice());
        }
    }
    
    public static Product getByName(String name) throws SQLException
    { // Find and return a Product object by its name
        Product product = null;
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM inventory WHERE UPPER(name)="+"\""+name+"\"");
        // Known bug: If two items have same name, will return first result and not complain.
        // Should throw warning if more than one item with same name were found.
        while(rs.next())
        {
            product = new Product(
            rs.getInt("ID"),
            rs.getString("NAME"),
            rs.getDouble("PRICE")
            );
        }
        Database.close();
        return product;
    }
    
    public static Product getById(int id) throws SQLException
    { // Find and return a Product object by its id
        Product product = null;
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM inventory WHERE id="+"\""+id+"\"");
        while(rs.next())
        {
            product = new Product(
            rs.getInt("ID"),
            rs.getString("NAME"),
            rs.getDouble("PRICE")
            );
        }
        Database.close();
        return product;
    }
    
    public static ArrayList<Product> getAllProducts() throws SQLException
    { // Return an ArrayList of ALL products in the inventory for easy iteration
        // This code is almost identical to getByName... DRY!
        ArrayList<Product> list = new ArrayList<Product>();
        Product product;
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM inventory");
        while(rs.next())
        {
            product = new Product(
            rs.getInt("ID"),
            rs.getString("NAME"),
            rs.getDouble("PRICE")
            );
            list.add(product);
        }
        Database.close();
        return list;
    }
}
