package com.itv.thingtracker.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class Utils
{
    private Utils() {}

    public static String ConvertStreamToString( InputStream is )
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
        StringBuilder sb = new StringBuilder();

        String line;
        try
        {
            while ( ( line = reader.readLine() ) != null )
            {
                sb.append( line ).append( '\n' );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static JSONObject ConvertStreamToJSON( InputStream in )
    {
        try
        {
            return new JSONObject( ConvertStreamToString( in ) );
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
            return null;
        }
    }
}
