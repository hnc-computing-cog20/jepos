/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package my.jepos;

import java.util.Objects;

/**
 *
 * @author john
 */

// Holds one individual product
// Must be immutable. No setters! Edit by deleting and reconstructing.
public final class Product {
    private final int id;
    private final String name;
    private final double price;
    
    public Product(int id, String name, double price)
    {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    public int getId()
    {
        return this.id;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public double getPrice()
    {
        return this.price;
    }
    
    @Override
    public int hashCode()
    { // Generate hashcode. Identical instances = same hash. Required to use instance in an ArrayList
        int hash = 3;
        hash = 41 * hash + this.id;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.price) ^ (Double.doubleToLongBits(this.price) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    { // Check equality between instances of this object. Required to use instance in an ArrayList
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Product other = (Product) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
}
