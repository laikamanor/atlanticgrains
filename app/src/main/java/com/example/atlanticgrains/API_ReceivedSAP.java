package com.example.atlanticgrains;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class API_ReceivedSAP extends AppCompatActivity {
    Button btnLocaton,btnCamera;
    TextView textView1, textView2, textView3, textView4,textView5;
    FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_p_i__received_s_a_p);

        btnLocaton = findViewById(R.id.btnLocation);
        btnCamera = findViewById(R.id.btnCamera);
        textView1 = findViewById(R.id.text_view1);
        textView2 = findViewById(R.id.text_view2);
        textView3 = findViewById(R.id.text_view3);
        textView4 = findViewById(R.id.text_view4);
        textView5 = findViewById(R.id.text_view5);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        btnLocaton.setOnClickListener(view -> {
            if(ActivityCompat.checkSelfPermission(API_ReceivedSAP.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(API_ReceivedSAP.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                getLocation();
            }else{
                ActivityCompat.requestPermissions(API_ReceivedSAP.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
            }
        });

//        btnCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(ActivityCompat.checkSelfPermission(API_ReceivedSAP.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
//                    getLocation();
//                }else{
//                    ActivityCompat.requestPermissions(API_ReceivedSAP.this,new String[]{Manifest.permission.CAMERA},44);
//
//                }
//            }
//        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1]== PackageManager.PERMISSION_GRANTED)){
            getLocation();
        }else{
            Toast.makeText(getBaseContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    public void getLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if(location != null){
                        textView1.setText(Html.fromHtml("<font color='#6200EE'><b>Latitude: </b><br></font>" + location.getLatitude()));

                        textView2.setText(Html.fromHtml("<font color='#6200EE'><b>Longtitude: </b><br></font>" + location.getLongitude()));
                    }else{
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        LocationCallback locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                                textView1.setText(Html.fromHtml("<font color='#6200EE'><b>Latitude: </b><br></font>" + location1.getLatitude()));

                                textView2.setText(Html.fromHtml("<font color='#6200EE'><b>Longtitude: </b><br></font>" + location1.getLongitude()));

                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                }
            });
        }else{
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
}