package com.itv.thingtracker.app;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.itv.thingtracker.models.Product;
import com.itv.thingtracker.utils.API;


public class CreateProductActivity extends ActionBarActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_product_create );

        final TextView idView = (TextView)findViewById( R.id.idView );
        final EditText nameInput = (EditText)findViewById( R.id.nameInput );
        final Button createButton = (Button)findViewById( R.id.createButton );

        Bundle extras = getIntent().getExtras();
        final String productId = extras.getString( "ID" );

        idView.setText( productId );

        createButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                new APICreateProductTask().execute( new Product( productId, nameInput.getText().toString() ) );
            }
        } );
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_product_create, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if ( id == R.id.action_settings )
        {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    private class APICreateProductTask extends AsyncTask<Product, Void, Integer>
    {
        protected Integer doInBackground( Product... product )
        {
            return API.CreateProduct( product[0] );
        }

        protected void onPostExecute( Integer result )
        {
            String text;

            switch ( result )
            {
                case -1:
                    text = "An application error occurred! <-1>";
                    break;
                case 200:
                    text = "Product successfully added!";
                    break;
                default:
                    text = String.format( "Server error %d", result );
                    break;
            }

            final TextView resultsView = (TextView)findViewById( R.id.resultsView );
            resultsView.setText( text );
        }
    }
}
