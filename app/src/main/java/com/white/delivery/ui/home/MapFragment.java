package com.white.delivery.ui.home;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.white.delivery.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    LinearLayout select_add;
    public static LatLng sourcelatLng;
    public static LatLng deslatLng;
    String add;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    double lat,lng;
    private String mParam1;
    private String mParam2;
    String name;
    GoogleMap mMap;
    View view;
    MapView mapView;
    TextView searchView;
    RelativeLayout address;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public NavController navCont;

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, container, false);

        navCont = NavHostFragment.findNavController(this);
        select_add = view.findViewById(R.id.select_address);
        address = view.findViewById(R.id.address);
        searchView = view.findViewById(R.id.search);
        Places.initialize(view.getContext(), "AIzaSyDVpo7xTz7wk5tS4JMMRSXFwbu_6iZho-o");
        PlacesClient placesClient = Places.createClient(getContext());
        mapView = view.findViewById(R.id.mapView2);
        mapView.getMapAsync(MapFragment.this);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bundle args = getArguments();
        lat = args.getDouble("lat",0.0);
        lng = args.getDouble("long",0.0);
        name = args.getString("name","default");
        sharedPreferences = getContext().getSharedPreferences("local",0);
        editor = sharedPreferences.edit();
        searchView.setText("Getting address....");
        source_autocomplete();
        select_add.setOnClickListener(v -> {
            if(name.equals("source")){
                editor.putString("slat",String.valueOf(lat));
                editor.apply();
                editor.putString("slong",String.valueOf(lng));
                editor.apply();
                editor.putString("srcadd",add);
                editor.apply();
                sourcelatLng = new LatLng(lat,lng);
            }else if(name.equals("dest")){
                editor.putString("dlat",String.valueOf(lat));
                editor.apply();
                editor.putString("dlong",String.valueOf(lng));
                editor.apply();
                editor.putString("desadd",add);
                editor.apply();
                deslatLng = new LatLng(lat,lng);
            }
            printAddress(lat,lng);
            closeMapFragment(v,name);
        });
        printAddress(lat,lng);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final LatLng latLng = new LatLng(lat,lng);
        try {
            googleMap.setMyLocationEnabled(true);
        }catch(Exception e){}
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 500, 10, 0);
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.setOnCameraIdleListener(() -> {
            LatLng mid = mMap.getCameraPosition().target;
            Log.i("Location", mid.latitude +","+ mid.longitude);
            printAddress(mid.latitude,mid.longitude);
            lat = mid.latitude;
            lng = mid.longitude;
        });
    }
    public void source_autocomplete(){
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(lat-0.02, lng-0.02),
                new LatLng(lat+0.02, lng+0.02)));
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                Log.i(TAG, "LATLNG"+place.getName()+","+place.getLatLng());
                addSourceMarker(place.getLatLng());
                searchView.setText(place.getAddress().toString());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }
    public void addSourceMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location").position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        //mMap.addMarker(markerOptions);
    }
    public void printAddress(double lat, double lng){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        //lat = location.getLatitude();
        //lng = location.getLongitude();

        Log.e("latitude", "latitude--" + lat);

        try {
            Log.e("latitude", "inside latitude--" + lat);
            addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                /*String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();*/
                add = address;
                searchView.setText(address);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void closeMapFragment(View view,String a){
        Bundle args = new Bundle();
        args.putDouble("lat",lat);
        args.putDouble("long",lng);
        args.putString("name",a);
        args.putString("add",add);
/*        Fragment homeFragment = new HomeFragment();
        homeFragment.setArguments(args);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.nav_default_pop_enter_anim,R.anim.nav_default_pop_exit_anim);
        transaction.replace(getId(),homeFragment);
        transaction.addToBackStack(null);
        transaction.commit();*/
        navCont.navigate(R.id.action_mapFragment_to_nav_home,args);
    }
}
