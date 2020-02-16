package com.utility.smartcitytraveller;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.utility.smartcitytraveller.ui.home.HomeFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static java.lang.Math.round;

public class MapsActivity extends AppCompatActivity {

    FrameLayout flSos;
    LinearLayout llPolice;
    LinearLayout llHospital;
    LinearLayout llGarage;
    LinearLayout llClose;
    private FusedLocationProviderClient mFusedLocationClient;
    public static final String ROOT_FOLDER_NAME = "SmartCityTraveller";
    public static final String ROOT_PATH_FOR_SAVING_TRIP_FOLDERS = Environment.getExternalStorageDirectory() + File.separator + ROOT_FOLDER_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        flSos = findViewById(R.id.fl_sos);
        flSos.setVisibility(View.GONE);
        llPolice = findViewById(R.id.ll_police);
        llPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEmergencyContactInfo("police.csv");
                compileSms();
                flSos.setVisibility(View.GONE);
            }
        });
        llHospital = findViewById(R.id.ll_hospital);
        llHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEmergencyContactInfo("hospital.csv");
                compileSms();
                flSos.setVisibility(View.GONE);
            }
        });
        llGarage = findViewById(R.id.ll_garage);
        llGarage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEmergencyContactInfo("garage.csv");
                compileSms();
                flSos.setVisibility(View.GONE);
            }
        });
        llClose = findViewById(R.id.ll_close);
        llClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flSos.setVisibility(View.GONE);
            }
        });

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.new_logo);
        getSupportActionBar().setHideOffset(0);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#000000'>Smart City Traveller</font>"));
//        getSupportActionBar().setTitle("Smart City Traveller");


        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                Log.e("Navigation", destination.getLabel() + "");
                if (destination.getLabel().equals("Hospital")) {
                    HomeFragment.whatNext = "hospital.csv";
                    HomeFragment.readX("hospital.csv");
                } else if(destination.getLabel().equals("Garage")) {
                    HomeFragment.whatNext = "garage.csv";
                    HomeFragment.readX("garage.csv");
                } else if(destination.getLabel().equals("Petrol pump")) {
                    HomeFragment.whatNext = "petrolPumps.csv";
                    HomeFragment.readX("petrolPumps.csv");
                } else if(destination.getLabel().equals("Police")) {
                    HomeFragment.whatNext = "police.csv";
                    HomeFragment.readX("police.csv");
                }
            }
        });
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        101);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }


        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2*1000,
                1200, mLocationListener);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            HomeFragment.updateUserLocation(location);
                        }
                    }
                });

    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            Log.e("Location Info", location.getLatitude() + "," + location.getLongitude());
            HomeFragment.updateUserLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            markLoggedOut();
        } else if (id == R.id.action_profile) {
            goToProfileActivity();
        } else if (id == R.id.action_sos) {
            flSos.setVisibility(View.VISIBLE);
//            compileSms();
        }
        return super.onOptionsItemSelected(item);
    }


    private void sendSMS(String phoneNumber, String message)
    {
        Intent pInten = new Intent(this, ProfileActivity.class);
        pInten.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                pInten, 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "Message sent to " + phoneNumber + "!!", Toast.LENGTH_LONG).show();
    }

    public void markLoggedOut() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.is_logged_in), false);
        editor.commit();

        editor.putString("name_1", null);
        editor.putString("name_2", null);
        editor.putString("name_3", null);

        editor.putString("number_1", null);
        editor.putString("number_2", null);
        editor.putString("number_3", null);

        editor.putString("Your_Name", null);

        editor.commit();

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        boolean status = sharedPref.getBoolean( getString(R.string.is_logged_in), false);
        Log.e("LogInStatus", status + "");
        goToLoginInActivity();
    }

    public void goToLoginInActivity() {
        Intent mapsScreen = new Intent(MapsActivity.this, LoginActivity.class);
        mapsScreen.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mapsScreen);
    }

    public void goToProfileActivity() {
        Intent mapsScreen = new Intent(MapsActivity.this, ProfileActivity.class);
//        mapsScreen.addFlags(FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mapsScreen);
    }


    public void compileSms() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String name1 = sharedPref.getString( "name_1", null);
        String name2 = sharedPref.getString( "name_2", null);
        String name3 = sharedPref.getString( "name_3", null);

        String num1 = sharedPref.getString( "number_1", null);
        String num2 = sharedPref.getString( "number_2", null);
        String num3 = sharedPref.getString( "number_3", null);

        String myName = sharedPref.getString( "Your_Name", null);

        String SMS_CONTENT = "Your Friend, ";
        if (myName != null) {
            SMS_CONTENT = SMS_CONTENT + myName;
        }

        SMS_CONTENT = SMS_CONTENT + " , at location: ";

        String location = "http://maps.google.com/maps?daddr=" + HomeFragment.lastLocation.getLatitude() + "," + HomeFragment.lastLocation.getLongitude();// + "&daddr=null,null"

        SMS_CONTENT = SMS_CONTENT + location;

        SMS_CONTENT = SMS_CONTENT + " needs help.";

        if (num1 != null) {
            sendSMS(num1, SMS_CONTENT);
        }

        if (num2 != null) {
            sendSMS(num2, SMS_CONTENT);
        }

        if (num3 != null) {
            sendSMS(num3, SMS_CONTENT);
        }

    }



    public void getEmergencyContactInfo(String fileName) {
        HashMap<CsvRow,Float> allLocations = new HashMap<>();
        File file = new File(MapsActivity.ROOT_PATH_FOR_SAVING_TRIP_FOLDERS, fileName);
        CsvReader csvReader = new CsvReader();
        try (CsvParser csvParser = csvReader.parse(file, StandardCharsets.UTF_8)) {
            CsvRow row;
            while ((row = csvParser.nextRow()) != null) {
                if (!row.getField(1).equals("latitude")) {
                    Double lat = Double.parseDouble(row.getField(1));
                    Double lon = Double.parseDouble(row.getField(2));
                    float[] dist = new float[1];
                    Location.distanceBetween(HomeFragment.lastLocation.getLatitude(), HomeFragment.lastLocation.getLongitude(), lat, lon, dist);
                    allLocations.put(row, (Float)dist[0]);

                    System.out.println("First column of line: " + row.getField(0));
                    System.out.println("Distance: " + dist[0]);
                }
            }

            allLocations = HomeFragment.sortByValues(allLocations);
            Set set = allLocations.entrySet();
            Iterator iterator = set.iterator();
            int loc = 0;

            while(iterator.hasNext()) {
                if (loc == 2) {
                    break;
                }
                Map.Entry me = (Map.Entry)iterator.next();
                CsvRow row1 = (CsvRow) me.getKey();
                Double lat = Double.parseDouble(row1.getField(1));
                Double lon = Double.parseDouble(row1.getField(2));

                compileSmsViaPhone(row1.getField(3), row1.getField(0), (Float)me.getValue());
                System.out.print(me.getKey() + ": ");
                System.out.println(me.getValue());
                loc +=1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void compileSmsViaPhone(String Phone, String Name, Float distance){

        Log.e("CLIENT MESSAGE", "SENT!!!!!!!!!!!!" + Name +  "-" +distance );
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String myName = sharedPref.getString( "Your_Name", null);
        String SMS_CONTENT = Name + ": Someone needs help at " + round(distance/1000.0) + "Km. Your Client, ";
        if (myName != null) {
            SMS_CONTENT = SMS_CONTENT + myName;
        }

        SMS_CONTENT = SMS_CONTENT + " , at location: ";

        String location = "http://maps.google.com/maps?daddr=" + HomeFragment.lastLocation.getLatitude() + "," + HomeFragment.lastLocation.getLongitude();// + "&daddr=null,null"

        SMS_CONTENT = SMS_CONTENT + location;

        SMS_CONTENT = SMS_CONTENT + " needs help.";

        sendSMS("0"+Phone, SMS_CONTENT);

        Log.e("CLIENT MESSAGE", "SENT!!!!!!!!!!!!");
    }
}
