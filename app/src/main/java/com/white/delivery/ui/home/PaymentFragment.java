package com.white.delivery.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.white.delivery.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PaymentFragment extends Fragment {
    View root;
    Button done_bt;
    NavController navCont;
    Double total;
    TextView total_tv;
    StorageReference storageReference;
    String name;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_payment, container, false);

        storageReference = FirebaseStorage.getInstance().getReference().child("ORDER_IMAGES");
        Bundle args = getArguments();
        String time = args.getString("time","");
        String mobile = args.getString("mobile","");
        name = mobile+time+"url1";
        Log.i("MOBILE",name);
        done_bt=root.findViewById(R.id.payment_done);
        navCont= NavHostFragment.findNavController(this);
        total=Double.parseDouble(getArguments().getString("cost"));
        total_tv=root.findViewById(R.id.totalpayment);
        total_tv.setText(String.format("Total: %s", total + 20));
        done_bt.setOnClickListener(v -> navCont.navigate(R.id.action_paymentFragment_to_nav_home));
        /*OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navCont.navigate(R.id.action_paymentFragment_to_nav_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);*/
        return root;
    }

}
