package com.itv.thingtracker.models;

import com.itv.thingtracker.utils.API;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Product implements Serializable
{
    private final String Id;
    private String Name;

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

    public void setName( String name )
    {
        Name = name;
    }

    public JSONObject toJSON()
    {
        JSONObject jObject = new JSONObject();

        try
        {
            jObject.put( API.PRODUCT_FIELD_ID, getId() );
            jObject.put( API.PRODUCT_FIELD_NAME, getName() );
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
            return null;
        }

        return jObject;
    }
}
