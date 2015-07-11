package com.itv.thingtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.itv.thingtracker.models.Product;
import com.itv.thingtracker.utilities.CameraPreview;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

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

        String line = null;
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

    private class APIRequestTask extends AsyncTask<String, Void, String>
    {
        protected String doInBackground( String... id )
        {
            try
            {
                URL url = new URL( "http://192.168.1.10:3000/products/" + id[0] );
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                InputStream in = new BufferedInputStream( connection.getInputStream() );

                return convertStreamToString( in );
            }
            catch ( Exception e )
            {
                String error = e.getMessage();
            }

            return null;
        }

        protected void onPostExecute( String result )
        {
            //final TextView outputView = (TextView)findViewById( R.id.requestOutput );
            JSONObject jObject;
            Product product;

            try
            {
                jObject = new JSONObject( result );
                product = new Product( jObject.getString( "id" ), jObject.getString( "name" ) );

                String message = new String();
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

                //outputView.setText( product.getName() );
            }
            catch ( Exception e )
            {
                String error = e.getMessage();
            }
        }
    }
}
