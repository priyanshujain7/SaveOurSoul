package com.ritik.saveoursoul;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;
    private FusedLocationProviderClient fusedLocationClient;
    MediaPlayer mediaPlayer;
    List<Emergency> emergencies = new ArrayList<>();
    DatabaseReference databaseReference;
    ProgressDialog pd;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    int shakeCount = 0;
    String userId = FirebaseAuth.getInstance().getUid();







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.option_button);
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION},1);

        getData();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        /**
         * Option menu Config
         */

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(getApplicationContext(), imageView);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                         public boolean onMenuItemClick(MenuItem item) {
                                                             switch (item.getItemId()) {
                                                                 case R.id.logout:
                                                                     AuthUI.getInstance()
                                                                             .signOut(getApplicationContext())
                                                                             .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                 public void onComplete(@NonNull Task<Void> task) {
                                                                                     Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_SHORT).show();
                                                                                     startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                                                                                     finish();
                                                                                 }
                                                                             });
                                                                     return true;
                                                                 case R.id.settings:
                                                                     startActivity(new Intent(getApplicationContext(), SavedContactsActivity.class));
                                                                     finish();
                                                             }

                                                             return false;
                                                         }
                                                     }
                );
                popupMenu.inflate(R.menu.option_menu);
                popupMenu.show();
            }
        });

        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click();
            }
        });

    }

    public void getUserLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            String message = "http://maps.google.com/maps?saddr=" + lat + "," + lon;
                            for (Emergency e : emergencies) {
                                //SmsManager smsManager = SmsManager.getDefault();
                                StringBuffer smsBody = new StringBuffer();
                                smsBody.append(Uri.parse(message));
                                android.telephony.SmsManager.getDefault().sendTextMessage(e.getNumber(), null, smsBody.toString(), null, null);
                            }

                            Toast.makeText(getApplicationContext(), "Send", Toast.LENGTH_LONG).show();

                        }
                    }
                });
    }

    public void playNoise() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.noise);
        mediaPlayer.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        finish();
    }

    public void click() {
//        if (button.isEnabled()) {
        if (!emergencies.isEmpty()) {
            playNoise();
            getUserLocation();
            button.setEnabled(false);

        } else {
            Toast.makeText(getApplicationContext(), "Enter the details first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), SavedContactsActivity.class));
            finish();
        }
//        } else {
//            mediaPlayer.stop();
//            button.setEnabled(true);
//        }
    }

    public void getData() {
        emergencies.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference("Contacts").child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Emergency emergency = d.getValue(Emergency.class);
                    emergencies.add(emergency);
                }
                pd.cancel();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /*
                 * The following method, "handleShakeEvent(count):" is a stub //
                 * method you would use to setup whatever you want done once the
                 * device has been shook.
                 */

                //Toast.makeText(MainActivity.this, "onShake", Toast.LENGTH_SHORT).show();
                click();



                if(count > 12) {


                    /*******Add  Your Code Here*******/

                    // Toast.makeText(MainActivity.this, "Condition", Toast.LENGTH_SHORT).show();;


                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                        onBackPressed();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
