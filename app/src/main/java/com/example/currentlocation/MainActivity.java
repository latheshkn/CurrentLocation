package com.example.currentlocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {



    Button btn_ltn;
    TextView txt_show;
    private  static final long UPDATE_IN_MILL=10000;
    private  static final long FAST_IN_MILL=10000;

//    location related api

    private FusedLocationProviderClient mfusedLocationProviderClient;
    public SettingsClient msettingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentlocation;
    private  boolean requwestinglocationupdate =true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txt_show=findViewById(R.id.txt_show);

        btn_ltn=findViewById(R.id.btn_ltn);

        initial();


        btn_ltn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {

                                requwestinglocationupdate=true;

                                setlocation();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {

                                opensetting();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }


                        }).check();
            }
        });

    }



    //     initializing the api s
    public void initial(){

        mfusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(MainActivity.this);

        msettingsClient=LocationServices.getSettingsClient(this);

        locationCallback =new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);


                currentlocation=locationResult.getLastLocation();
            }
        };

        requwestinglocationupdate=false;

//        creating new location request
        locationRequest=new LocationRequest();

//        setting the updating request interval
        locationRequest.setInterval(UPDATE_IN_MILL);

//        setting the updating request interval
        locationRequest.setFastestInterval(FAST_IN_MILL);

//        setting the location accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);
        locationSettingsRequest= builder.build();



    }

    public void opensetting(){

        Intent i =new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

        Uri uri=Uri.fromParts("package", BuildConfig.APPLICATION_ID,null);
        i.setData(uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case MainActivity.RESULT_OK:
                break;

            case AppCompatActivity.RESULT_CANCELED:
                break;
        }
    }


    public  void setlocation(){

        msettingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                        mfusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

                        if (currentlocation!=null){

                            txt_show.setText("Lat"+currentlocation.getLatitude()+" " +"Lag"+currentlocation.getLongitude());
                        }

                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                int code =((ApiException)e).getStatusCode();

                switch (code){
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        ResolvableApiException eae=(ResolvableApiException)e;

                        try {
                            eae.startResolutionForResult(MainActivity.this,code);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case  LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Toast.makeText(MainActivity.this, "check your settings", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

    }
    }
