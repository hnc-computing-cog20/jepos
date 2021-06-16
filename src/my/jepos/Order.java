/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.jepos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author john
 */

// Holds a total of an order
public class Order {
    private int id;
    private Staff handler; // The member of staff who took the order
    private ArrayList<String> text;
    private LinkedHashMap<Product, Integer> products;
    private Date date;
    
    public Order(Staff handler, Date date)
    { // Constructor: Initialise member objects and add table header.
        this.text = new ArrayList<String>();
        this.text.add("ITEM\tQTY\tPRICE\tTOTAL\n");
        this.products = new LinkedHashMap<Product, Integer>(); // product, quantity
        this.handler = handler;
        this.date = date;
    }
    
    public ArrayList<String> getText()
    { // Get the entire output (product list + header), in text format.
        return this.text;
    }
    
    public void setNewOrderId() throws SQLException
    { // Set the id (primary key) of this order. This is for new orders (not existing orders!).
        Database.initStatement();
        // Get the last id in the orders table
        ResultSet rs = Database.getStatement().executeQuery("SELECT id FROM orders WHERE ID="
                + "(SELECT MAX(id) FROM orders)"
                );
        while(rs.next())
        {
            // This id is last id + 1
            this.id = rs.getInt("ID") + 1;
        }
        Database.close();
    }
    public void setId(int id)
    { // Set the id (primary key) of this order. This is for existing orders (not new orders!).
        this.id = id;
    }
    public int getId()
    { // Only works after setId() or setNewOrderId() have been called, of course.
        return this.id;
    }
    
    public void setHandler(Staff handler)
    {
        this.handler = handler;
    }
    public Staff getHandler()
    {
        return this.handler;
    }
    
    public void setDate(Date date)
    {
        this.date = date;
    }
    public Date getDate()
    {
        return this.date;
    }
    
    public void resetText()
    { // Clear the text and re-add the headers
        this.text.clear();
        this.text.add("ITEM\tQTY\tPRICE\tTOTAL\n");
    }
    
    public LinkedHashMap<Product, Integer> getProducts()
    { // Get the list of products in the sales total
        return this.products;
    }
    public void setProducts(LinkedHashMap<Product, Integer> products)
    { // Set products without adding each item one by one. Called when constructing an order from the database.
        this.products = products;
    }
    
    public void addText(Product item, int qty)
    { // Add a row of text for an item (product).
        this.text.add(item.getName()
                + "\t" + qty
                + "\t" + new DecimalFormat("£0.00").format(item.getPrice()) // Truncate, add pound sign, and trailing zeroes.
                + "\t" + new DecimalFormat("£0.00").format(item.getPrice()*qty) // Ditto...
                + "\n"
        );
    }
    
    public void addItem(Product item, int qty)
    {
        this.products.put(item, qty);
    }
    public void removeItem(Product item)
    {
        this.products.remove(item);
        this.resetText();
        this.populateList();
    }
    public void removeAllItems()
    {
        this.products.clear();
        this.resetText();
        /*
        for(Map.Entry<Product, Integer> i : this.products.entrySet())
        {
            removeItem(i.getKey());
        }*/
    }
    
    public boolean itemExists(Product item)
    { // Check if an item is already in the order
        if(this.products.containsKey(item))
        { // Yes it is
            return true;
        }
        else
        { // No it isn't
            return false;
        }
    }
    
    public Product getLastItem()
    { // Get the last item in the item list
        int lastIndex = products.size()-1; // Last item is size - 1
        Set productsSet = products.entrySet(); // Convert products to a Set
        Map.Entry<Product, Integer> lastElement; // Hold the element
        lastElement = (new ArrayList<Map.Entry<Product, Integer>>(productsSet)).get(lastIndex); // Set element to item at index
        
        Product product = lastElement.getKey();
        return product;
    }
    
    public Product getItemByIndex(int index)
    { // Get an item in the item list by its index
        Set productsSet = products.entrySet(); // Similar to getLastItem()...
        Map.Entry<Product, Integer> elementAtIndex;
        elementAtIndex = (new ArrayList<Map.Entry<Product, Integer>>(productsSet)).get(index);
        
        Product product = elementAtIndex.getKey();
        return product;
    }
    
    public OrderItem getItem(Product item)
    { // Return one item from products as an OrderItem
        OrderItem orderItem = new OrderItem(
                item,
                this.products.get(item)
        );
        return orderItem;
    }
    
    public double getTotal()
    { // Get the total price of the order
        double total = 0;

        // Iterate through the item list (products)
        for(Map.Entry<Product, Integer> i : products.entrySet())
        {
            int qty = i.getValue();
            if(i.getValue() == null)
            { // Set qty to 0 if none given
                qty = 0;
            }
            else
            { // Set qty to stored qty
                qty = i.getValue();
            }
            // Calculate total price
            total += i.getKey().getPrice() * qty;
        }
        return total;
    }
    
    public void populateList()
    { // Add each line of text to this.text
        this.resetText(); // Reset the text to avoid duplicate entries
        // Iterate through the item list (products)
        for(Map.Entry<Product, Integer> i : products.entrySet())
        {
            this.addText(i.getKey(), i.getValue());
        }    
    }
    
    public static ArrayList<Order> getAllOrders() throws SQLException
    { // Pull all orders from the database
        ArrayList<Order> orders = new ArrayList<Order>();
        
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM orders");
        while(rs.next())
        {
            orders.add( Order.getOrderById(rs.getInt("ID")) );
        }
        Database.close();
        
        return orders;
    }
    
    public static Order getOrderById(int id) throws SQLException
    { // Get one order from the database by its id
        Staff handler = null;
        LinkedHashMap<Product, Integer> items = new LinkedHashMap<Product, Integer>();
        Date date = new Date();
        //Calendar calendar = new GregorianCalendar(); // DEBUG
        
        Database.initStatement();
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM orders WHERE id="+id);
        while(rs.next())
        {
            // Get handler
            handler = Staff.getByUsername( rs.getString("HANDLER") );
            // Parse items
            items = parseAllItemsFromDb( rs.getString("ITEMS") );
            // Get date (Unix epoch)
            date.setTime( (long)rs.getInt("DATE") * 1000 );
            //calendar.setTime(date); // DEBUG
        }
        Database.close();
        
        // Construct Order
        Order order = new Order(handler, date);
        // Set id
        order.setId(id);
        // Add items
        order.setProducts(items);
        
        return order;
    }
    
    public static Order getLastOrder() throws SQLException
    {
        Order order = null;
        Database.initStatement();
        // Get the last id in the orders table
        ResultSet rs = Database.getStatement().executeQuery("SELECT * FROM orders WHERE id="
                + "(SELECT MAX(id) FROM orders)");
        while(rs.next())
        {
            order = Order.getOrderById( rs.getInt("ID") );
        }
        Database.close();
        
        return order;
    }
    
    public static LinkedHashMap<Product, Integer> parseAllItemsFromDb(String itemsString)
    { // Parse each item from the items field in the database.
        // Database format: ITEM_ID,QUANTITY;ITEM_ID,QUANTITY;ITEM_ID,QUANTITY; ...
        ArrayList<String> itemsList = new ArrayList<String>(); // List for each entry delimited by the semi-colons.
        LinkedHashMap<Product, Integer> items = new LinkedHashMap<Product, Integer>();
        int index = 0;
        
        while(itemsString.indexOf(';') != -1)
        { // Loop through itemsString until it finds no more semi-colons (indexOf() returns -1)
            String itemEntry = itemsString.substring(index, itemsString.indexOf(';'));
            itemsString = itemsString.substring(itemsString.indexOf(';')+1);
            //System.out.println("DEBUG: "+itemEntry+"\tITEMSSTRING: "+itemsString);
            itemsList.add(itemEntry);
        }
        
        for(String i : itemsList)
        { // Loop through itemsList and parse each item
            //System.out.println("DEBUG2: " + i);
            OrderItem item = Order.parseItemFromDb(i);
            items.put(item.getProduct(), item.getQuantity());
        }

        //System.out.println(items.toString()); // DEBUG
        return items;
    }
    
    public static OrderItem parseItemFromDb(String itemEntry)
    { // Return one entry for the products ArrayList, parsed from the database.
        // Database format: ITEM_ID,QUANTITY;ITEM_ID,QUANTITY;ITEM_ID,QUANTITY; ...
        // Get item by ITEM_ID for Product object, then get QUANTITY as Integer.
        int id = Integer.parseInt( itemEntry.substring(0, itemEntry.indexOf(',')) );
        int quantity = Integer.parseInt( itemEntry.substring(itemEntry.indexOf(',')+1, itemEntry.length()) );
        Product product = null;
        try
        {
            product = Inventory.getById(id);
        } catch (SQLException ex) {
            Logger.getLogger(ui.class.getName()).log(Level.SEVERE, null, ex);
        }
        OrderItem item = new OrderItem(product, quantity);
        return item;
    }
    
    public String getItemsString()
    { // Create a suitable string for the "ITEMS" column of the database, from products.
        // Format: ID,QUANTITY;ID,QUANTITY;ID,QUANTITY; ...
        String itemsString = "";
        for(Map.Entry<Product, Integer> i : this.products.entrySet())
        {
            itemsString += i.getKey().getId() + "," + i.getValue() + ";";
        }
        return itemsString;
    }
    
    public void save() throws SQLException
    { // Write this order to the database
        // Format: ID,HANDLER,ITEMS,DATE
        Database.initStatement();
        Database.getStatement().executeUpdate(
                "INSERT INTO orders VALUES("
                + this.id + ","
                + "\""+this.handler.getUsername()+"\"" + ","
                + "\""+this.getItemsString()+"\"" + "," // Get a string suitable for the items column
                + (this.date.getTime()/1000) + ");" // Divide date.getTime() by 1000 to get Unix time
        );
        Database.close();
        
        System.out.println(
                "["+this.date+"] "
                + "user " + "\""+this.handler.getUsername()+"\" "
                + "saved order (ID: "+this.id+") "
                + "to the database."
        );
    }
}

class OrderItem
{ // Simple class to hold one Product and one integer (quantity). Saves me using a one-element Hashmap.
    private Product product;
    private int quantity;
    
    public OrderItem(Product product, int quantity)
    {
        this.product = product;
        this.quantity = quantity;
    }
    
    public Product getProduct() { return this.product; }
    public void setProduct(Product product) { this.product = product; }
    
    public int getQuantity() { return this.quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
