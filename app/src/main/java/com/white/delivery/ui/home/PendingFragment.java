package com.white.delivery.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.white.delivery.OnlineDrivers;
import com.white.delivery.OrderData;
import com.white.delivery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;


public class PendingFragment extends Fragment{

    public static volatile boolean isTrue = false;
    View view;
    //WebView webView;
    String packtype;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;
    DocumentReference documentReference, dref;
    List<OnlineDrivers> onlineDriversList;
    SharedPreferences sharedPreferences;
    Button cancel;
    public volatile String phone_no;
    public static boolean assigned;
    public volatile boolean flag = false;
    Handler mainHandler;
    public NavController navCont;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_pending, container, false);
        assigned = false;
        /*GifImageView gifImageView = (GifImageView) view.findViewById(R.id.gif);
        gifImageView.setGifImageResource(R.drawable.search);*/
        /*ImageView imageView = view.findViewById(R.id.gif);
        Glide.with(this).load(R.drawable.search).into(imageView);*/



        onlineDriversList = new ArrayList<>();
        sharedPreferences = this.getActivity().getSharedPreferences("phone", Context.MODE_PRIVATE);
        phone_no = sharedPreferences.getString("phone", "");
        firebaseFirestore = FirebaseFirestore.getInstance();
        //webView = view.findViewById(R.id.gif);
        cancel = view.findViewById(R.id.cancel_bt);
        navCont = NavHostFragment.findNavController(this);
        Bundle args = getArguments();
        packtype = args.getString("packtype", "");
        Log.i("TYPE", packtype);
        //WebSettings webSettings = webView.getSettings();
        //webSettings.setJavaScriptEnabled(true);
        //String file = "file:android_asset/search.gif";
        //webView.loadUrl(file);
        mainHandler = new Handler();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            ExampleRunnable exampleRunnable = new ExampleRunnable(packtype);
            //exampleRunnable.run();
            new Thread(exampleRunnable).start();
            listener();
        }, 2000);
        cancel.setOnClickListener(v -> navCont.navigate(R.id.action_nav_pending_to_nav_home));
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(getContext(),"Please Wait",Toast.LENGTH_SHORT).show();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        return view;
    }

    class ExampleRunnable implements Runnable {
        String packtype;
        FirebaseFirestore firebaseFirestore;
        DocumentReference documentReference, dref, dref_temp;
        CollectionReference collectionReference;
        String name;

        ExampleRunnable(String packtype) {
            this.packtype = packtype;
            name = "ONLINE_" + packtype + "_DRIVERS";
            firebaseFirestore = FirebaseFirestore.getInstance();
            documentReference = firebaseFirestore.collection("USERS").document(phone_no);
            dref_temp = firebaseFirestore.collection("USERS").document(phone_no);
            collectionReference = firebaseFirestore.collection(name);
        }

        @Override
        public void run() {
            try {

                collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot post : queryDocumentSnapshots) {
                        if (flag) {
                            Toast.makeText(getContext(), "DRIVER ALLOCATED", Toast.LENGTH_LONG).show();
                            return;
                        }
                        OnlineDrivers onlineDrivers = post.toObject(OnlineDrivers.class);
                        documentReference.collection("ORDER")
                                .document(OrderDetailsFragment.order_name).get().addOnSuccessListener(documentSnapshot -> {
                            OrderData orderData = documentSnapshot.toObject(OrderData.class);
                            if(orderData == null){
                                orderData = OrderDetailsFragment.orderData_per;
                            }
                            if (orderData != null || OrderDetailsFragment.orderData_per != null) {
                                if (getDistance(onlineDrivers.getGeoPoint(), orderData.getPickup_location()) <= 3000.00) {
                                    Log.e("YUP", "REACHED HERE");
                                    dref = firebaseFirestore.collection("DRIVERS")
                                            .document(onlineDrivers.getNumber());
                                    dref.collection("ORDERS")
                                            .document(orderData.getDate() + "+" + orderData.getTime())
                                            .set(orderData);
                                    try {
                                        Thread.sleep(10000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    OrderData finalOrderData = orderData;
                                    dref_temp.collection("ORDER")
                                            .document(OrderDetailsFragment.order_name)
                                            .get().addOnSuccessListener(documentSnapshot1 -> {
                                        OrderData orderData1 = documentSnapshot1.toObject(OrderData.class);
                                        if(orderData1 == null){
                                            orderData1.setAssigned(isIsTrue());
                                        }
                                        try {
                                            Log.e("FLAG", String.valueOf(orderData1.isAssigned()));
                                            if (orderData1.isAssigned()) {
                                                flag = true;
                                                Log.e("FLAG", String.valueOf(orderData1.isAssigned()));
                                                Toast.makeText(getContext(), "DRIVER ALLOCATED", Toast.LENGTH_LONG).show();
                                                NavController navController = NavHostFragment.findNavController(PendingFragment.this);
                                                Bundle args = new Bundle();
                                                args.putString("driver", onlineDrivers.getNumber());
                                                args.putString("order", documentSnapshot1.getId());
                                                try {
                                                    navController.navigate(R.id.action_pending_to_driver, args);
                                                } catch (Exception e) {
                                                    Fragment fragment = new DriverIncoming();
                                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                                                    fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
                                                            .addToBackStack(null)
                                                            .commit();
                                                }
                                                return;
                                            } else if (!orderData1.isAssigned()) {
                                                Log.e("DELETED", "DELETED");
                                                dref.collection("ORDERS")
                                                        .document(finalOrderData.getDate() + "+" + finalOrderData.getTime())
                                                        .delete();

                                            }
                                        }catch (Exception e){}
                                    });
                                }
                            }
                        });
                    }
                });
            } catch (Exception e) {

            }
        }

        public double getDistance(GeoPoint pick, GeoPoint driver) {

            double lat1, lon1, lat2, lon2;
            lat1 = pick.getLatitude();
            lon1 = pick.getLongitude();
            lat2 = driver.getLatitude();
            lon2 = driver.getLongitude();
            Location loc1, loc2;
            loc1 = new Location("");
            loc1.setLatitude(lat1);
            loc1.setLongitude(lon1);
            loc2 = new Location("");
            loc2.setLatitude(lat2);
            loc2.setLongitude(lon2);
            return loc1.distanceTo(loc2);

        }
    }

    public void listener(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        try {
            DocumentReference documentReference = firebaseFirestore.collection("USERS").document(phone_no);

            documentReference.collection("ORDER").document(OrderDetailsFragment.order_name).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e != null){
                        Log.i("Exception","Error");
                        return;
                    }
                    if(documentSnapshot.exists()){
                        OrderData orderData = documentSnapshot.toObject(OrderData.class);
                        try {
                            setIsTrue(orderData.isAssigned());
                        }catch (Exception ex){Log.i("Exception","ErrorNull");}
                    }
                }
            });
        }catch (Exception e){}
    }

    public static void setIsTrue(boolean isTrue) {
        PendingFragment.isTrue = isTrue;
    }

    public static boolean isIsTrue() {
        return isTrue;
    }
}
