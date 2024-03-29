package com.abuzz.amap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.abuzz.amaphelper.AMapViewInstance;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  {

    private class corner{
        double x;
        double y;
        double lat;
        double lon;

        corner(double x, double y, double lat, double lon ) {
            this.x = x;
            this.y = y;
            this.lat = lat;
            this.lon = lon;
        }
    }
    private String url = "https://map.dev.abuzz.tech/amap/embedex.php?site=DIFC_POC&mobile=true&apiKey=9BC813867BC6838AEFB07AD15B262EBD";

    private int cur_zone = 0;
    private double[] lonlat_transf = new double[]{ 1.0, 0.0, 0.0,
                                                   0.0, 0.0, 1.0,
                                                   0.0, 0.0, 0.0,
                                                   0.0, 1.0, 0.0 };

    public static final int REQUEST_PERMISSIONS = 1;
    public static String[] PERMISSIONS_ALL = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public IALocationManager mIALocationManager;
    public IALocationListener mIALocationListener;
    public corner topLeft;
    public corner bottomRight;

    public double[] coordinates = new double[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,PERMISSIONS_ALL,REQUEST_PERMISSIONS);
        setContentView(R.layout.activity_main);


        topLeft = new corner(-5835,46063,25.21577789088,55.27965678284);
        bottomRight = new corner(22481, 17722, 25.21318461787,55.28253282524);

        coordinates = latlngToXY(25.214441, 55.281049);

        mIALocationManager = IALocationManager.create(this);

        mIALocationListener = new IALocationListener() {
            @Override
            public void onLocationChanged(IALocation iaLocation) {

                log("The current lat-lon-alt are as follows "+ iaLocation.getLatitude()+" "+iaLocation.getLongitude()+" "+iaLocation.getAltitude());
                coordinates = latlngToXY(iaLocation.getLatitude(), iaLocation.getLongitude());
                Toast.makeText(getApplicationContext(),iaLocation.getLatitude()+" "+iaLocation.getLongitude()+" "+iaLocation.getAltitude(),Toast.LENGTH_SHORT)
                              .show();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                log(s+i);

            }
        };

        AMapViewInstance.getInstance().setContext(this);
        AMapViewInstance.getInstance().setUrl(url);
        AMapViewInstance.getInstance().setLonLatTransf(lonlat_transf);
        AMapViewInstance.getInstance().setQrScan(false);
        AMapViewInstance.getInstance().setSpeechRec(false);
        AMapViewInstance.getInstance().setSharePoi(false);
        AMapViewInstance.getInstance().setPoiDetails(false);

        findViewById(R.id.load_map).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AMapViewInstance.getInstance().show();
            }
        });

        findViewById(R.id.simulate_curloc).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startLocationSimulationLoop();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIALocationManager.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(),mIALocationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
      //  mIALocationManager.removeLocationUpdates(mIALocationListener);
        mIALocationManager.requestLocationUpdates(IALocationRequest.create(),mIALocationListener);

    }

    // FAKE simulation of user moving



    private double[] latlngToXY(double lat, double lng){
        double perX = (lng - topLeft.lon)/(bottomRight.lon - topLeft.lon);
        double perY = (lat - topLeft.lat)/(bottomRight.lat - topLeft.lat);

        coordinates[0] = topLeft.x + (bottomRight.x - topLeft.x) * perX  ; //lon
        coordinates[1] = topLeft.y + (bottomRight.y - topLeft.y) * perY  ; //lat

       log(coordinates[0] + "  " + coordinates[1]);
        return coordinates;
    }


    private void startLocationSimulationLoop() {

        Runnable positionWebViewRunnable = () -> {

                AMapViewInstance.getInstance().getAmap().setLonLatCurrentPosition(
                        coordinates[0],
                        coordinates[1],
                        0.0,
                        cur_zone, null);

                log(coordinates[0] + "thread" + coordinates[1]);

            };

            TimerTask positionTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (AMapViewInstance.getInstance().getWebView() != null) {
                        AMapViewInstance.getInstance().getWebView().post(positionWebViewRunnable);
                    }
                }
            };

            new Timer().scheduleAtFixedRate(positionTimerTask, 0, 200);

            Toast.makeText(this, "Location Simulation started", Toast.LENGTH_SHORT).show();
    }


    private void log(Object object){
        System.out.println("FATAL::: "+object);
    }
}

