package com.white.delivery.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.white.delivery.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

import com.google.firebase.firestore.GeoPoint;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    TextView pick, drop;
    Button search_button;
    LinearLayout source, dest;
    GoogleMap mMap;
    LinearLayout scooter, van;
    int select;
    Location currLoc;
    double lat_a, long_a;
    FusedLocationProviderClient fusedLocationProviderClient;
    View view;
    MapView mMapView;
    CheckBox van_check, scooter_check;
    Boolean packtype;
    public static GeoPoint pick_geoPoint, drop_geoPoint;
    private Marker start, end;
    private GeoApiContext mGAPI;
    MarkerOptions src_markerOptions, des_markerOptions;
    Double distancepoly;
    private static final int MULTIPLE_PERMISSIONS = 10;
    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int REQUEST_LOCATION = 101;
    public NavController navCont;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        navCont = NavHostFragment.findNavController(this);
        Places.initialize(view.getContext(), "AIzaSyBvc5Uu-Wegy176lKthjwNpRfq27mEKsCM");
        PlacesClient placesClient = Places.createClient(getContext());
        pick = view.findViewById(R.id.pickup);
        drop = view.findViewById(R.id.drop);
        scooter = view.findViewById(R.id.scooter);
        van = view.findViewById(R.id.van);
        search_button = view.findViewById(R.id.rc);
        van_check = view.findViewById(R.id.van_check);
        scooter_check = view.findViewById(R.id.scooter_check);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(view.getContext());
        source = view.findViewById(R.id.source);
        dest = view.findViewById(R.id.dest);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //mMapView.getMapAsync(HomeFragment.this);
        Handler handler = new Handler();
        checkPermissions();
        handler.postDelayed(() -> {
            select = 0;
            mMapView = view.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(HomeFragment.this);
            mMapView.onResume();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(view.getContext());
            fetchLastLocation();
        }, 500);
        scooter.setOnClickListener(v -> {
            if (!scooter_check.isChecked()) {
                scooter_check.setChecked(true);
                if (van_check.isChecked()) {
                    van_check.setChecked(false);
                }
            } else {
                scooter_check.setChecked(false);
                van_check.setChecked(true);
            }
            packtype = true;
        });
        van.setOnClickListener(v -> {
            if (!van_check.isChecked()) {
                van_check.setChecked(true);
                if (scooter_check.isChecked()) {
                    scooter_check.setChecked(false);
                }
            } else {
                van_check.setChecked(false);
                scooter_check.setChecked(true);
            }
            packtype = false;
        });
        source.setOnClickListener(v -> openMapFragment(v, "source"));
        dest.setOnClickListener(v -> openMapFragment(v, "dest"));
        search_button.setOnClickListener(this::openOrderFragment);
        Bundle args = getArguments();
        if (args == null) {
            MapFragment.sourcelatLng = new LatLng(0, 0);
            MapFragment.deslatLng = new LatLng(0, 0);
        }
        return view;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }
        googleMap.setMyLocationEnabled(true);
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 500, 10, 0);
        if(lat_a != 0.0 && long_a != 0.0) {
            LatLng latLng = new LatLng(lat_a, long_a);
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location").position(latLng)
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_bike));
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            //googleMap.clear();
            //printAddress(lat_a, long_a);
            //googleMap.addMarker(markerOptions);
        }
        if(MapFragment.sourcelatLng.latitude != 0.0 && MapFragment.sourcelatLng.longitude != 0.0) {
            loadLocalDataSource();
        }
        if(MapFragment.deslatLng.latitude != 0.0 && MapFragment.deslatLng.longitude != 0.0) {
            loadLocalDataDestination();
        }
        if(mGAPI==null) {
            mGAPI = new GeoApiContext.Builder().apiKey(getString(R.string.map_key)).build();
        }
        addRouteOnCreate();
    }
    private void fetchLastLocation() {
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if(location != null){
                currLoc = location;
                lat_a = location.getLatitude();
                long_a = location.getLongitude();
                //Toast.makeText(getContext(),currLoc.toString(),Toast.LENGTH_LONG).show();
                Log.i("Location",location.toString());
                MapView mv =view.findViewById(R.id.mapView);
                mv.getMapAsync(HomeFragment.this);
            }
        });
    }
    private  void checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(getContext(),p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : permissionsList) {
                        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                            permissionsDenied += "\n" + per;
                        }
                    }
                }
                return;
            }
        }
    }
    public void openMapFragment(View view,String name){
        Bundle args = new Bundle();
        fetchLastLocation();
        args.putString("name",name);
        args.putDouble("lat",lat_a);
        args.putDouble("long",long_a);
        navCont.navigate(R.id.action_nav_home_to_mapFragment,args);
    }
    public void printAddress(double lat, double lng){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        Log.e("latitude", "latitude--" + lat);
        try {
            Log.e("latitude", "inside latitude--" + lat);
            addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                pick.setText(address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context,int vectorId){
        Drawable drawable = ContextCompat.getDrawable(context,vectorId);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);

    }
    public void loadLocalDataSource(){
        if(MapFragment.sourcelatLng.latitude != 0.0){
            if(MapFragment.sourcelatLng.longitude != 0.0){
                LatLng latLng = MapFragment.sourcelatLng;
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location").position(latLng)
                        .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_green_dot));
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                src_markerOptions = markerOptions;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                //googleMap.clear();
                SharedPreferences sharedPreferences = getContext().getSharedPreferences("local",0);
                pick.setText(sharedPreferences.getString("srcadd","Pickup Location"));
                mMap.addMarker(markerOptions);
            }
        }
    }
    public void loadLocalDataDestination(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("local",0);

        if(MapFragment.deslatLng.latitude != 0.0){
            if(MapFragment.deslatLng.longitude != 0.0){
                LatLng latLng = MapFragment.deslatLng;
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Drop Location").position(latLng)
                        .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_red_dot));
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                des_markerOptions = markerOptions;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                //googleMap.clear();
                drop.setText(sharedPreferences.getString("desadd","Drop Location"));
                mMap.addMarker(markerOptions);

            }
        }
    }
    public void openOrderFragment(View view){
        /*Fragment orderDetailsFragmentFragment = new OrderDetailsFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.nav_default_pop_enter_anim,R.anim.nav_default_pop_exit_anim);
        transaction.replace(R.id.drawer_layout,orderDetailsFragmentFragment);
        transaction.addToBackStack(null);
        transaction.commit();*/
        //OR
        Bundle arg=new Bundle();
        if(src_markerOptions==null){
            Toast.makeText(getContext(),"Enter start location",Toast.LENGTH_SHORT).show();
            return;
        }
        if(des_markerOptions==null){
            Toast.makeText(getContext(),"Enter Destination location",Toast.LENGTH_SHORT).show();
            return;
        }
        if(packtype==null){
            Toast.makeText(getContext(),"Please select any one",Toast.LENGTH_SHORT).show();
            return;
        }else if(packtype){
            arg.putString("packageType","Scooter");
        }
        else{
            arg.putString("packageType","Van");
        }
        if (distancepoly==null){
            Toast.makeText(getContext(), "something went wrong try again...", Toast.LENGTH_SHORT).show();
            return;
        }
        arg.putString("distance",distancepoly.toString());
        navCont.navigate(R.id.action_nav_home_to_orderDetailsFragment,arg);
    }
    private void calculateDirections(){
        Log.d(Constraints.TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                end.getPosition().latitude,
                end.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGAPI);

        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        start.getPosition().latitude,
                        start.getPosition().longitude
                )
        );
        Log.d(Constraints.TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(Constraints.TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(Constraints.TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(Constraints.TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(Constraints.TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                distancepoly = Double.parseDouble(result.routes[0].legs[0].distance.toString().substring(0,2));
                Log.e(TAG,"dis "+distancepoly);
                addPolylinesToMap(result);
            }
            @Override
            public void onFailure(Throwable e) {
                Log.e(Constraints.TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );
            }
        });
    }
    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(Constraints.TAG, "run: result routes: " + result.routes.length);
                for(DirectionsRoute route: result.routes){
                    Log.d(Constraints.TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();
                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){
//                        Log.d(TAG, "run: latlng: " + latLng.toString());
                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                    polyline.setClickable(true);
                }
            }
        });
    }
    public void addRouteOnCreate(){
        if(MapFragment.sourcelatLng.latitude != 0.0 && MapFragment.sourcelatLng.longitude != 0.0){
            if(MapFragment.deslatLng.latitude != 0.0 && MapFragment.deslatLng.longitude != 0.0){
                LatLng src = MapFragment.sourcelatLng;
                LatLng des = MapFragment.deslatLng;
                start = mMap.addMarker(src_markerOptions);
                end = mMap.addMarker(des_markerOptions);
                pick_geoPoint = new GeoPoint(start.getPosition().latitude,start.getPosition().longitude);
                drop_geoPoint = new GeoPoint(end.getPosition().latitude,end.getPosition().longitude);
                calculateDirections();
            }
        }
    }

}
