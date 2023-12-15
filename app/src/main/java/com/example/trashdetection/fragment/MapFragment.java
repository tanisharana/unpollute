package com.example.trashdetection.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.trashdetection.R;
import com.example.trashdetection.model.CaptionInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    ArrayList<WeightedLatLng> listData = new ArrayList<WeightedLatLng>();
    private double longitude, latitude;

    ProgressBar progressLoadOnMap;
    public MapFragment() {
        // Required empty public constructor
    }

    public MapFragment(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressLoadOnMap = view.findViewById(R.id.progressLoadOnMap);

        progressLoadOnMap.setVisibility(View.VISIBLE);
        try {
            listData = generateHeatMapData();
            System.out.println("A listData: "  + listData);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

//        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        ArrayList<WeightedLatLng> data = null;
        data = listData;
        progressLoadOnMap.setVisibility(View.GONE);
        System.out.println("F listData: "  + listData);

        HeatmapTileProvider heatMapProvider = new HeatmapTileProvider.Builder()
                .weightedData(data) // load our weighted data
                .radius(50) // optional, in pixels, can be anything between 20 and 50
                .maxIntensity(1000.0) // set the maximum intensity
                .build();

        googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatMapProvider));

        System.out.println("M La : " + latitude);
        System.out.println("M Lo : " + longitude);
        LatLng indiaLatLng = new LatLng(latitude, longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(indiaLatLng, 14f));

    }

    private ArrayList<WeightedLatLng> generateHeatMapData() throws JSONException {
        ArrayList<WeightedLatLng> data = new ArrayList<WeightedLatLng>();

        System.out.println("Call function");

        DatabaseReference  reference = FirebaseDatabase.getInstance().getReference("caption_image");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                System.out.println("Success data change: " + dataSnapshot);
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    CaptionInfo captionInfo = snap.getValue(CaptionInfo.class);

                    System.out.println("lat: " + captionInfo.getLatitude());
                    System.out.println("long: " + captionInfo.getLongitude());
                    double lat = Double.parseDouble(captionInfo.getLatitude());
                    double lon = Double.parseDouble(captionInfo.getLongitude());
                    double density = Double.parseDouble(captionInfo.getDensity());

                    if (density != 0.0) {
                        WeightedLatLng weightedLatLng = new WeightedLatLng(new LatLng(lat, lon), density);
                        data.add(weightedLatLng);
                    }
                }

                System.out.println("data: " + data);

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
                        mapFragment.getMapAsync(MapFragment.this);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error data change: " + error.getMessage());
            }
        });


//        JSONArray jsonData = getJsonDataFromAsset("district_data.json");
//        if(jsonData != null) {
//            for (int i = 0; i < jsonData.length(); i++) {
//                JSONObject entry = jsonData.getJSONObject(i);
//                double lat = entry.getDouble("lat");
//                double lon = entry.getDouble("lon");
//                double density = entry.getDouble("density");
//
//                if (density != 0.0) {
//                    WeightedLatLng weightedLatLng = new WeightedLatLng(new LatLng(lat, lon), density);
//                    data.add(weightedLatLng);
//                }
//
//            }
//        }

        listData = data;
        return data;
    }

    private JSONArray getJsonDataFromAsset(String fileName) {
        try {
            InputStream is = getActivity().getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonString = new String(buffer, "UTF-8");

            return new JSONArray(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}