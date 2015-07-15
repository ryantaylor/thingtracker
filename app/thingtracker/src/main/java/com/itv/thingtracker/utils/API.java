package com.itv.thingtracker.utils;

import com.itv.thingtracker.exceptions.APIResultException;
import com.itv.thingtracker.models.Product;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public final class API
{
    // URLs
    public static final String URL_PRODUCT_GET = "http://192.168.1.10:3000/products/";
    public static final String URL_PRODUCT_POST = "http://192.168.1.10:3000/create";

    // API field names
    public static final String PRODUCT_FIELD_ID = "id";
    public static final String PRODUCT_FIELD_NAME = "name";

    private API()
    {
    }

    public static APIResult<Product> GetProduct( String id )
    {
        URL url;
        HttpURLConnection connection;
        InputStream in;
        JSONObject jObject;

        APIResult<Product> productResult = new APIResult<Product>( id, null );

        try
        {
            url = new URL( URL_PRODUCT_GET + id );
            connection = (HttpURLConnection)url.openConnection();
        }
        catch ( MalformedURLException e1 )
        {
            e1.printStackTrace();
            return productResult;
        }
        catch ( IOException e2 )
        {
            e2.printStackTrace();
            return productResult;
        }

        try
        {
            in = new BufferedInputStream( connection.getInputStream() );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            connection.disconnect();
            return productResult;
        }

        jObject = Utils.ConvertStreamToJSON( in );

        if ( jObject != null )
        {
            if ( jObject.length() > 0 )
            {
                try
                {
                    productResult.setResult( new Product( jObject.getString( PRODUCT_FIELD_ID ), jObject.getString( PRODUCT_FIELD_NAME ) ) );
                }
                catch ( JSONException e )
                {
                    e.printStackTrace();
                }
            }
        }

        try
        {
            in.close();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            connection.disconnect();
        }

        return productResult;
    }

    public static int CreateProduct( Product product )
    {
        URL url;
        HttpURLConnection connection;
        OutputStreamWriter out;
        JSONObject jObject;
        int response = -1;

        jObject = product.toJSON();

        if ( jObject == null )
            return response;
        else
        {
            try
            {
                url = new URL( URL_PRODUCT_POST );
                connection = (HttpURLConnection)url.openConnection();
            }
            catch ( MalformedURLException e1 )
            {
                e1.printStackTrace();
                return response;
            }
            catch ( IOException e2 )
            {
                e2.printStackTrace();
                return response;
            }

            connection.setDoOutput( true );
            connection.setDoInput( true );
            connection.setRequestProperty( "Content-Type", "application/json" );

            try
            {
                connection.setRequestMethod( "POST" );
            }
            catch ( ProtocolException e )
            {
                e.printStackTrace();
                connection.disconnect();
                return response;
            }

            try
            {
                out = new OutputStreamWriter( connection.getOutputStream() );

                out.write( jObject.toString() );
                out.flush();
                out.close();

                response = connection.getResponseCode();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
            finally
            {
                connection.disconnect();
            }

            return response;
        }
    }
}
