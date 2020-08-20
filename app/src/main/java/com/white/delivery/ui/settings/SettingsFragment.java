package com.white.delivery.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.white.delivery.R;

public class SettingsFragment extends Fragment {
    View view;
    LinearLayout change;
    NavController navcont;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        change=view.findViewById(R.id.change_password);
        navcont= NavHostFragment.findNavController(this);
        change.setOnClickListener(v -> {
/*            ChangePassword nextFrag= new ChangePassword();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.drawer_layout, nextFrag, "ChangePassword")
                    .addToBackStack(null)
                    .commit();*/
            navcont.navigate(R.id.action_nav_settings_to_nav_change_password);
        });
        return view;
    }
}
