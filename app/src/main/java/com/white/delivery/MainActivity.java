package com.white.delivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.white.delivery.ui.ViewProfile.ViewProfile;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;

public class MainActivity extends AppCompatActivity{

    private AppBarConfiguration mAppBarConfiguration;
    File f1;
    View headerview;
    public static UserData static_userData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_settings, R.id.nav_help,R.id.nav_terms,R.id.nav_offers,R.id.nav_driver,R.id.nav_details)
                .setDrawerLayout(drawer)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id=menuItem.getItemId();
            //it's possible to do more actions on several items, if there is a large amount of items I prefer switch(){case} instead of if()
            if (id==R.id.nav_share){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,"www.google.com");
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent,"share via"));
            }
            if(id == R.id.nav_terms){

            }
            if(id == R.id.nav_logout){
                //Toast.makeText(this,"LOGOUT",Toast.LENGTH_SHORT).show();
                LogoutPopup logoutPopup = new LogoutPopup();
                logoutPopup.show(getSupportFragmentManager(),"logout");
            }
            //This is for maintaining the behavior of the Navigation view
            NavigationUI.onNavDestinationSelected(menuItem,navController);
            //This is for closing the drawer after acting on it
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
        headerview = navigationView.getHeaderView(0);
        LinearLayout header = headerview.findViewById(R.id.header);
        headerview.setOnClickListener(v -> {
            Fragment fr = new ViewProfile();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.nav_default_pop_enter_anim,R.anim.nav_default_pop_exit_anim);
            transaction.replace(R.id.drawer_layout,fr);
            transaction.addToBackStack(null);
            transaction.commit();
            drawer.closeDrawer(GravityCompat.START);
        });
        getUserData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void getUserData(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("phone",MODE_PRIVATE);

        try {
            String phone = sharedPreferences.getString("phone","");
            DocumentReference documentReference = firebaseFirestore.collection("USERS").document(phone);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    UserData userData = documentSnapshot.toObject(UserData.class);
                    if(userData != null){
                        static_userData = userData;
                        ImageView imageView = headerview.findViewById(R.id.imageView);
                        TextView textView = headerview.findViewById(R.id.name_tv);
                        try{
                            textView.setText(userData.getName());
                            Glide.with(getApplicationContext())
                                    .load(userData.getUrl())
                                    .circleCrop()
                                    .placeholder(R.drawable.profile)
                                    .into(imageView);
                        }catch (Exception e){}
                    }
                }
            });
        }catch (Exception e){

        }
    }

}
