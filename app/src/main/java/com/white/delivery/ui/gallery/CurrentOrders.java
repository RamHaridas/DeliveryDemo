package com.white.delivery.ui.gallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.white.delivery.OrderData;
import com.white.delivery.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class CurrentOrders extends Fragment {
    View root;
    CollectionReference collectionReference;
    FirebaseFirestore firebaseFirestore;
    SharedPreferences sharedPreferences;
    List<OrderData> orderDataList;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_current_orders, container, false);
        orderDataList = new ArrayList<OrderData>();
        sharedPreferences = getActivity().getSharedPreferences("phone", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone","");
        firebaseFirestore = FirebaseFirestore.getInstance();
        try {
            collectionReference = firebaseFirestore.collection("USERS").document(phone).collection("ORDER");
            getData();
        }catch (Exception e){
            Toast.makeText(getContext(),"ERROR",Toast.LENGTH_SHORT).show();
        }

        return root;
    }
    public void getData(){
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    OrderData orderData = documentSnapshot.toObject(OrderData.class);
                    if(orderData != null){
                        if(!orderData.isCompleted()){
                            orderDataList.add(orderData);
                        }
                    }
                }
                RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recyclerView_co);
                ListAdapterOP adapter = new ListAdapterOP(orderDataList);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(adapter);
                if(orderDataList.isEmpty()){
                    TextView textView = root.findViewById(R.id.text31);
                    textView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
