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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,PERMISSIONS_ALL,REQUEST_PERMISSIONS);
        setContentView(R.layout.activity_main);

        mIALocationManager = IALocationManager.create(this);

        mIALocationListener = new IALocationListener() {
            @Override
            public void onLocationChanged(IALocation iaLocation) {
                //getLat() getLon() function returns double.
                log("The current lat-lon is "+ iaLocation.getLatitude()+" "+iaLocation.getLongitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

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
    private int cur_step = 0;
    private double[] from_pos = new double[]{ 8543, 32373, -200 };
    private double[] to_pos = new double[]{ 10125, 34942, -200 };
    private boolean simulationStarted = false;

    private void startLocationSimulationLoop() {
        if(!simulationStarted) {
            simulationStarted = true;

            Runnable positionWebViewRunnable = () -> {
                AMapViewInstance.getInstance().getAmap().setLonLatCurrentPosition(
                        from_pos[0] + cur_step*(to_pos[0] - from_pos[0])/40,
                        from_pos[1] + cur_step*(to_pos[1] - from_pos[1])/40,
                        from_pos[2] + cur_step*(to_pos[2] - from_pos[2])/40,
                        cur_zone, null);
                cur_step = (cur_step + 1) % 40;
            };

            TimerTask positionTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (AMapViewInstance.getInstance().getWebView() != null) {
                        AMapViewInstance.getInstance().getWebView().post(positionWebViewRunnable);
                    }
                }
            };

            new Timer().scheduleAtFixedRate(positionTimerTask, 1000, 1000);

            Toast.makeText(this, "Location Simulation started", Toast.LENGTH_SHORT).show();
        }
    }

    private void log(Object object){
        System.out.println("FATAL::: "+object);
    }
}