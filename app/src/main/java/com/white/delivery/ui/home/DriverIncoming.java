package com.white.delivery.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.white.delivery.DriverData;
import com.white.delivery.OnlineDrivers;
import com.white.delivery.OrderData;
import com.white.delivery.R;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class DriverIncoming extends Fragment implements OnMapReadyCallback {

    View view;
    MapView mapView;
    GoogleMap googleMap;
    Bundle args;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    TextView name,number;
    ImageView imageView;
    public static String type_temp;
    private float start_rotation;
    ListenerRegistration listenerRegistration;
    PicassoMarker marker;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_driver_incoming, container, false);
        imageView = view.findViewById(R.id.driver);
        name = view.findViewById(R.id.driver_name);
        number = view.findViewById(R.id.driver_number);
        args = getArguments();

        firebaseFirestore = FirebaseFirestore.getInstance();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView = view.findViewById(R.id.driver_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(DriverIncoming.this);
        mapView.onResume();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavHostFragment.findNavController(DriverIncoming.this).navigate(R.id.action_nav_driver_to_nav_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        getDriverData();
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public void getDriverData(){
        String phone = args.getString("driver");
        if(phone == null){
            Log.d(TAG,"PHONE IS NULL");
            return;
        }
        documentReference = firebaseFirestore.collection("DRIVERS").document(phone);
        try{
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    DriverData driverData = documentSnapshot.toObject(DriverData.class);
                    if(driverData != null){
                        name.setText(driverData.getName());
                        number.setText(driverData.getPhone());
                        Picasso.with(getContext())
                                .load(driverData.getProfile_url())
                                .fit()
                                .centerCrop()
                                .into(imageView);
                        getDriverLocation(driverData.getVehicle_type(),driverData.getPhone());
                    }
                }
            });
        }catch(Exception e){

        }
    }
    public void getDriverLocation(String type,String phone){
        String name = "ONLINE_"+type+"_DRIVERS";
        type_temp = name;
        Log.i("TYPE",type_temp);
        DocumentReference dref = firebaseFirestore.collection(name).document(phone);
        try {
            dref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    OnlineDrivers onlineDrivers = documentSnapshot.toObject(OnlineDrivers.class);
                    GeoPoint geoPoint = onlineDrivers.getGeoPoint();
                    if(geoPoint == null){
                        return;
                    }
                    LatLng latLng = new LatLng(geoPoint.getLatitude(),geoPoint.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    marker = new PicassoMarker(googleMap.addMarker(new MarkerOptions().position(latLng)));
                    Picasso.with(view.getContext()).load(R.mipmap.car).resize( 50,  50)
                            .into(marker);
                    locationListener(name,phone);
                }
            });
        }catch (Exception e){

        }
    }



    public void moveVechile(final Marker myMarker, final Location finalPosition) {

        final LatLng startPosition = myMarker.getPosition();

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 3000;
        final boolean hideMarker = false;

        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;

            @Override
            public void run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude * (1 - t) + (finalPosition.getLatitude()) * t,
                        startPosition.longitude * (1 - t) + (finalPosition.getLongitude()) * t);
                myMarker.setPosition(currentPosition);
                // myMarker.setRotation(finalPosition.getBearing());


                // Repeat till progress is completeelse
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                    // handler.postDelayed(this, 100);
                } else {
                    if (hideMarker) {
                        myMarker.setVisible(false);
                    } else {
                        myMarker.setVisible(true);
                    }
                }
            }
        });


    }


    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;


                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                start_rotation = -rot > 180 ? rot / 2 : rot;
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

   public void locationListener(String name,String number){
        DocumentReference dref = firebaseFirestore.collection(name).document(number);
        try{
            dref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e != null){
                        return;
                    }
                    if(documentSnapshot.exists()){
                        OnlineDrivers onlineDrivers = documentSnapshot.toObject(OnlineDrivers.class);
                        if(onlineDrivers != null) {
                            Log.i("LOC", onlineDrivers.getGeoPoint().toString());
                            Location location = new Location("");
                            location.setLatitude(onlineDrivers.getGeoPoint().getLatitude());
                            location.setLongitude(onlineDrivers.getGeoPoint().getLongitude());
                            moveVechile(marker.getmMarker(),location);
                            rotateMarker(marker.getmMarker(),location.getBearing(),start_rotation);
                            try {
                                getOrderData();
                            }catch (Exception ex){}
                        }else{
                            Log.i("LOC", "EMPTY");
                        }
                    }
                }
            });
        }catch (Exception e){

        }
   }

   public void getOrderData(){
        String name = args.getString("order");
        try {
            if (name.isEmpty()) {
                return;
            }
        }catch(Exception e){return;}
        String phone = this.getActivity().getSharedPreferences("phone", Context.MODE_PRIVATE)
                            .getString("phone","");
        DocumentReference dref = firebaseFirestore.collection("USERS").document(phone)
                                    .collection("ORDER").document(name);
        try{
            dref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    OrderData orderData = documentSnapshot.toObject(OrderData.class);
                    if(orderData != null){
                        try {
                            addDropMarker(orderData.getDrop_location());
                            addPickMarker(orderData.getPickup_location());
                        }catch (Exception e){}
                    }
                }
            });
        }catch (Exception e){

        }
   }

    public void addPickMarker(GeoPoint geoPoint){
        try {
            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Pickup Point")
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_place_green));

            googleMap.addMarker(markerOptions);
        }catch (Exception e){}
    }
    public void addDropMarker(GeoPoint geoPoint){
        try {
            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Drop Point")
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_place_red));

            googleMap.addMarker(markerOptions);
        }catch (Exception e){}
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorId){
        Drawable drawable = ContextCompat.getDrawable(context,vectorId);
        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onStart() {
        super.onStart();
        String name = args.getString("order");
        String phone = this.getActivity().getSharedPreferences("phone", Context.MODE_PRIVATE)
                .getString("phone","");
        try {
            DocumentReference dref = firebaseFirestore.collection("USERS").document(phone)
                    .collection("ORDER").document(name);
            dref.addSnapshotListener(this.getActivity(),new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e != null){
                        return;
                    }
                    if(documentSnapshot.exists()){
                        OrderData orderData = documentSnapshot.toObject(OrderData.class);
                        try{
                            if(orderData.isCompleted()){
                                NavController navController = NavHostFragment.findNavController(DriverIncoming.this);
                                navController.navigate(R.id.to_complete);
                            }
                        }catch (Exception ex){

                        }
                    }
                }
            });

        }catch (Exception e){}
    }
}
