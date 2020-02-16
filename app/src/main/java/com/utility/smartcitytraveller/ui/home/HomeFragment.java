package com.utility.smartcitytraveller.ui.home;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.utility.smartcitytraveller.MainActivity;
import com.utility.smartcitytraveller.MapsActivity;
import com.utility.smartcitytraveller.R;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.siegmar.fastcsv.reader.CsvParser;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

public class HomeFragment extends Fragment {


    MapView mMapView;
    private static GoogleMap googleMapS;
    public static Location lastLocation=null;
    static Marker lastLocationMarker = null;
    static FloatingActionButton fabNavigation;
    static FloatingActionButton fabMyLocation;
    static FloatingActionButton fabAllFocus;
    static Marker navigateTo = null;
    static ArrayList<Marker> allMarkers = new ArrayList<>();
    public static String whatNext = "petrolPumps.csv";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        fabNavigation = root.findViewById(R.id.fb_direction);
        fabMyLocation = root.findViewById(R.id.fb_current_location);
        fabAllFocus = root.findViewById(R.id.fb_all_focus);

        mMapView = (MapView) root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);


        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        fabNavigation.setVisibility(View.GONE);
        navigateTo = null;

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMapS = googleMap;
                updateUserLocation(lastLocation);
                Log.e("InsideAsyncMap", whatNext);
                if (whatNext == null) {
                    readPetrolPumps();
                } else {
                    readX(whatNext);
                }

                googleMapS.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        fabNavigation.setVisibility(View.VISIBLE);
                        navigateTo = marker;
                        marker.showInfoWindow();
                        lastLocationMarker.showInfoWindow();
                        return false;
                    }
                });
            }
        });

        fabNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + lastLocation.getLatitude() + "," + lastLocation.getLongitude() + "&daddr=" + navigateTo.getPosition().latitude + "," + navigateTo.getPosition().longitude));
                startActivity(intent);
                fabNavigation.setVisibility(View.GONE);
            }
        });

        fabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = plotLastLocation();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng).zoom(10).build();
                googleMapS.moveCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                fabNavigation.setVisibility(View.GONE);
            }
        });

        fabAllFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allFocus();
                fabNavigation.setVisibility(View.GONE);
            }
        });
        // Perform any camera updates here
        return root;
    }



    public static void updateUserLocation(Location location) {
        fabNavigation.setVisibility(View.GONE);
//        googleMapS.clear();
        if (location != null)
            lastLocation = location;
        else {
            Log.e("Inside update location", "location is null here");
            return;
        }
        if (lastLocation == null || lastLocation != location) {
            // create marker
            LatLng latLng = plotLastLocation();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(12).build();
            googleMapS.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

        } else {
            if (lastLocation == location) {
                Log.e("HomeFragment", "last location  same as current");
            } else {
                Log.e("HomeFragment", "last location  is null");
            }

        }
        readX(whatNext);

    }

    public static LatLng plotLastLocation() {
        if (lastLocationMarker != null) {
            lastLocationMarker.remove();
        }

        LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(latLng).title("Your Location");
        // Changing marker icon
        marker.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        // adding marker
        lastLocationMarker = googleMapS.addMarker(marker);
        lastLocationMarker.showInfoWindow();
        return latLng;
    }


    public static void readX(String X) {
        if (googleMapS == null || fabNavigation ==null) {
            Log.e("return", "Prematirely");
            return;
        }
        fabNavigation.setVisibility(View.GONE);
        googleMapS.clear();
        if (lastLocation != null) {
            plotLastLocation();
            HashMap<CsvRow,Float> allLocations = new HashMap<>();
            allMarkers = new ArrayList<>();
            File file = new File(MapsActivity.ROOT_PATH_FOR_SAVING_TRIP_FOLDERS, X);
            CsvReader csvReader = new CsvReader();
            try (CsvParser csvParser = csvReader.parse(file, StandardCharsets.UTF_8)) {
                CsvRow row;
                while ((row = csvParser.nextRow()) != null) {
                    if (!row.getField(1).equals("latitude")) {
                        Double lat = Double.parseDouble(row.getField(1));
                        Double lon = Double.parseDouble(row.getField(2));
                        float[] dist = new float[1];
                        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), lat, lon, dist);
                        allLocations.put(row, (Float)dist[0]);

                        System.out.println("First column of line: " + row.getField(0));
                        System.out.println("Distance: " + dist[0]);
                    }
                }

                allLocations = sortByValues(allLocations);
                Set set = allLocations.entrySet();
                Iterator iterator = set.iterator();
                int loc = 0;
                while(iterator.hasNext()) {
                    if (loc == 10) {
                        break;
                    }
                    Map.Entry me = (Map.Entry)iterator.next();
                    CsvRow row1 = (CsvRow) me.getKey();
                    Double lat = Double.parseDouble(row1.getField(1));
                    Double lon = Double.parseDouble(row1.getField(2));
                    LatLng latLng = new LatLng(lat, lon);
                    MarkerOptions marker = new MarkerOptions().position(latLng).title(row1.getField(0));


                    // Changing marker icon
                    marker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

                    allMarkers.add(googleMapS.addMarker(marker));
                    System.out.print(me.getKey() + ": ");
                    System.out.println(me.getValue());
                    loc +=1;
                }

                allFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e("InsideReadX", "last location is null");
        }
    }

    public static void allFocus() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : allMarkers) {
            builder.include(marker.getPosition());
        }
        builder.include(lastLocationMarker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 90; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMapS.animateCamera(cu);
        lastLocationMarker.showInfoWindow();
    }

    public static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }





    public static void readPetrolPumps() {
        fabNavigation.setVisibility(View.GONE);
        googleMapS.clear();
        if (lastLocation != null) {
            plotLastLocation();
            HashMap<CsvRow,Float> allLocations = new HashMap<>();
            allMarkers = new ArrayList<>();
            File file = new File(MapsActivity.ROOT_PATH_FOR_SAVING_TRIP_FOLDERS, "petrolPumps.csv");
            CsvReader csvReader = new CsvReader();
            try (CsvParser csvParser = csvReader.parse(file, StandardCharsets.UTF_8)) {
                CsvRow row;
                while ((row = csvParser.nextRow()) != null) {
                    if (!row.getField(1).equals("latitude")) {
                        Double lat = Double.parseDouble(row.getField(1));
                        Double lon = Double.parseDouble(row.getField(2));
                        float[] dist = new float[1];
                        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), lat, lon, dist);
                        allLocations.put(row, (Float)dist[0]);

                        System.out.println("First column of line: " + row.getField(0));
                        System.out.println("Distance: " + dist[0]);
                    }
                }

                allLocations = sortByValues(allLocations);
                Set set = allLocations.entrySet();
                Iterator iterator = set.iterator();
                int loc = 0;
                while(iterator.hasNext()) {
                    if (loc == 10) {
                        break;
                    }
                    Map.Entry me = (Map.Entry)iterator.next();
                    CsvRow row1 = (CsvRow) me.getKey();
                    Double lat = Double.parseDouble(row1.getField(1));
                    Double lon = Double.parseDouble(row1.getField(2));
                    LatLng latLng = new LatLng(lat, lon);
                    MarkerOptions marker = new MarkerOptions().position(latLng).title(row1.getField(0));


                    // Changing marker icon
                    marker.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

                    allMarkers.add(googleMapS.addMarker(marker));
                    System.out.print(me.getKey() + ": ");
                    System.out.println(me.getValue());
                    loc +=1;
                }

                allFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}