package com.itv.thingtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.itv.thingtracker.models.Product;
import com.itv.thingtracker.utilities.CameraPreview;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

public class ScannerActivity extends Activity
{
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    //TextView scanText;
    //Button scanButton;
    EditText input;
    FrameLayout preview;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    AlertDialog.Builder alertBuilder;

    static
    {
        System.loadLibrary( "iconv" );
    }

    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.main );

        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

        alertBuilder = new AlertDialog.Builder( this );

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig( 0, Config.X_DENSITY, 3 );
        scanner.setConfig( 0, Config.Y_DENSITY, 3 );

        //mPreview = new CameraPreview( this, mCamera, previewCb, autoFocusCB );
        //preview = (FrameLayout)findViewById( R.id.scannerLayout );
        //preview.addView( mPreview );

//        scanButton.setOnClickListener( new View.OnClickListener()
//        {
//            public void onClick( View v )
//            {
//                if ( barcodeScanned )
//                {
//                    barcodeScanned = false;
//                    scanText.setText( "Scanning..." );
//                    mCamera.setPreviewCallback( previewCb );
//                    mCamera.startPreview();
//                    previewing = true;
//                    mCamera.autoFocus( autoFocusCB );
//                }
//            }
//        } );
    }

    public void onPause()
    {
        super.onPause();
        releaseCamera();
    }

    public void onResume()
    {
        super.onResume();
        if ( mCamera == null )
            mCamera = getCameraInstance();

        mPreview = new CameraPreview( this, mCamera, previewCb, autoFocusCB );
        preview = (FrameLayout)findViewById( R.id.scannerLayout );
        preview.addView( mPreview );
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance()
    {
        Camera c = null;
        try
        {
            c = Camera.open();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return c;
    }

    private void releaseCamera()
    {
        if ( mCamera != null )
        {
            previewing = false;
            mCamera.setPreviewCallback( null );
            mCamera.release();
            mCamera = null;
            mPreview.getHolder().removeCallback( mPreview );
            preview.removeView( mPreview );
        }
    }

    private void resetCamera()
    {
        if ( barcodeScanned )
        {
            barcodeScanned = false;
            mCamera.setPreviewCallback( previewCb );
            mCamera.startPreview();
            previewing = true;
            mCamera.autoFocus( autoFocusCB );
        }
    }

    private Runnable doAutoFocus = new Runnable()
    {
        public void run()
        {
            if ( previewing )
                mCamera.autoFocus( autoFocusCB );
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback()
    {
        public void onPreviewFrame( byte[] data, Camera camera )
        {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image( size.width, size.height, "Y800" );
            barcode.setData( data );

            int result = scanner.scanImage( barcode );

            if ( result != 0 )
            {
                previewing = false;
                mCamera.setPreviewCallback( null );
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for ( Symbol sym : syms )
                {
                    //scanText.setText( "barcode result " + sym.getData() );
                    barcodeScanned = true;
                    new APIRequestTask().execute( sym.getData() );
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback()
    {
        public void onAutoFocus( boolean success, Camera camera )
        {
            autoFocusHandler.postDelayed( doAutoFocus, 1000 );
        }
    };

    /**
     * Called when the activity is first created.
     */
    /*@Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );

        final Button requestButton = (Button)findViewById( R.id.requestButton );

        requestButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                new APIRequestTask().execute( "http://192.168.1.10:3000/products/1" );
            }
        } );
    }*/
    private String convertStreamToString( InputStream is )
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

    private class APIRequestTask extends AsyncTask<String, Void, String[]>
    {
        protected String[] doInBackground( String... id )
        {
            URL url;
            HttpURLConnection connection;
            InputStream in;
            String[] result;

            try
            {
                result = new String[2];

                url = new URL( "http://192.168.1.10:3000/products/" + id[0] );
                connection = (HttpURLConnection)url.openConnection();
                in = new BufferedInputStream( connection.getInputStream() );

                result[0] = id[0];
                result[1] = convertStreamToString( in );

                connection.disconnect();

                return result;
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                //connection.disconnect();
            }

            return null;
        }

        // result[0] = UPC ID
        // result[1] = JSON from server
        //
        protected void onPostExecute( String[] result )
        {
            //final TextView outputView = (TextView)findViewById( R.id.requestOutput );
            JSONObject jObject;
            Product product;
            String message;

            try
            {
                jObject = new JSONObject( result[1] );

                // An empty JSON object means we don't have this product in the db
                //
                if ( jObject.length() == 0 )
                {
                    input = new EditText( getBaseContext() );
                    input.setText( "" );
                    input.setInputType( InputType.TYPE_CLASS_TEXT );

                    message = String.format( "ID: %s\nThis item does not exist. Create?", result[0] );
                    alertBuilder.setMessage( message );
                    alertBuilder.setCancelable( true );
                    alertBuilder.setView( input );
                    alertBuilder.setPositiveButton( "Create", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int i )
                        {
                            JSONObject jObject;

                            try
                            {
                                jObject = new JSONObject();

                                jObject.put( "id", result[0] );
                                jObject.put( "name", input.getText() );

                                new APICreateProductTask().execute( jObject );
                            }
                            catch ( Exception e )
                            {
                                e.printStackTrace();
                                //connection.disconnect();
                            }

                            resetCamera();
                            dialogInterface.cancel();
                        }
                    } );

                    alertBuilder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int i )
                        {
                            resetCamera();
                            dialogInterface.cancel();
                        }
                    } );

                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }
                else
                {
                    product = new Product( jObject.getString( "id" ), jObject.getString( "name" ) );

                    message = String.format( "ID: %s\nName: %s", product.getId(), product.getName() );
                    alertBuilder.setMessage( message );
                    alertBuilder.setCancelable( false );
                    alertBuilder.setPositiveButton( "OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialogInterface, int i )
                        {
                            resetCamera();
                            dialogInterface.cancel();
                        }
                    } );

                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                }

                //outputView.setText( product.getName() );
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
    }

    private class APICreateProductTask extends AsyncTask<JSONObject, Void, Void>
    {
        protected Void doInBackground( JSONObject... jObject )
        {
            URL url;
            HttpURLConnection connection;
            OutputStreamWriter out;

            try
            {
                url = new URL( "http://192.168.1.10:3000/create" );
                connection = (HttpURLConnection)url.openConnection();
                connection.setDoOutput( true );
                connection.setDoInput( true );
                connection.setRequestProperty( "Content-Type", "application/json" );
                connection.setRequestMethod( "POST" );

                out = new OutputStreamWriter( connection.getOutputStream() );

                out.write( jObject[0].toString() );
                out.flush();
                out.close();

                int status = connection.getResponseCode();

                System.out.println( status );

                //out = new BufferedWriter( new OutputStreamWriter( connection.getOutputStream() ) );

                //out.write( jObject[0].toString() );
                //out.write( "test" );
                //out.flush();
                //out.close();

                /*OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                writer.write(jObject[0].toString());
                if(writer != null){
                    writer.flush();
                    writer.close();
                }

                Log.d( "thingtracker", "test log" );

                int status = connection.getResponseCode();
                status = connection.getResponseCode();
                Log.d( "thingtracker", String.format("%d", status) );*/
                //InputStream in = connection.getInputStream();
                //System.out.println( convertStreamToString( in ) );

                //connection.disconnect();
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                //connection.disconnect();
            }

            return null;
        }
    }
}
