package com.itv.thingtracker.models;

/**
 * Created by ryan on 7/10/15.
 */
public class Product
{
    private final String Id;
    private final String Name;

    public Product()
    {
        Id = null;
        Name = null;
    }

    public Product( String id, String name )
    {
        Id = id;
        Name = name;
    }

    public String getId()
    {
        return Id;
    }

    public String getName()
    {
        return Name;
    }
}
