package com.itv.thingtracker.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.itv.thingtracker.models.Product;
import com.itv.thingtracker.utils.API;
import com.itv.thingtracker.utils.APIResult;


public class MainActivity extends ActionBarActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        IntentIntegrator integrator = new IntentIntegrator( this );
        integrator.setCaptureActivity( CaptureActivityFullSensor.class );
        integrator.setOrientationLocked( false );
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        IntentResult result = IntentIntegrator.parseActivityResult( requestCode, resultCode, data );
        Toast toast;

        if ( result != null )
        {
            if ( result.getContents() == null )
            {
                Log.d( "MainActivity", "Cancelled scan" );
                toast = Toast.makeText( this, "Cancelled", Toast.LENGTH_LONG );
                toast.show();
            }
            else
            {
                Log.d( "MainActivity", "Scanned" );
                toast = Toast.makeText( this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG );
                toast.setGravity( Gravity.BOTTOM, 0, 0 );
                toast.show();

                new APIRequestTask().execute( result.getContents() );
            }
        }
        else
        {
            Log.d( "MainActivity", "Weird" );
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult( requestCode, resultCode, data );
        }
    }

    private class APIRequestTask extends AsyncTask<String, Void, APIResult<Product>>
    {
        protected APIResult<Product> doInBackground( String... id )
        {
            return API.GetProduct( id[0] );
        }

        protected void onPostExecute( APIResult<Product> result )
        {
            Product product;

            product = result.getResult();

            if ( product == null )
            {
                Intent intent = new Intent( getBaseContext(), CreateProductActivity.class );
                intent.putExtra( "ID", result.getId() );
                startActivity( intent );
            }
            else
            {
                Intent intent = new Intent( getBaseContext(), ViewProductActivity.class );
                intent.putExtra( "PRODUCT", product );
                startActivity( intent );
            }
        }
    }
}