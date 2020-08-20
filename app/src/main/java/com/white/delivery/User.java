package com.white.delivery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.hbb20.CountryCodePicker;
import java.util.ArrayList;
import java.util.List;

public class User extends AppCompatActivity {


    String[]  permissions={
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    CountryCodePicker ccp;
    Button log_in;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;
    String phone,password;
    String check;
    TextInputEditText cont,pass;
    List<String> contactList;
    DocumentReference documentReference;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    SharedPreferences islogin;
    SharedPreferences.Editor edit;
    //private static boolean isUser = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user);

        checkRequiredPermission();

        sharedPreferences = getSharedPreferences("phone",MODE_PRIVATE);
        editor = sharedPreferences.edit();
        islogin = getSharedPreferences("login",MODE_PRIVATE);
        edit = islogin.edit();
        try{
            if(islogin.getBoolean("login",false)){
                Intent intent=new Intent(this,MainActivity.class);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//Change Here
                startActivity(intent);
                finish();
            }
        }catch (Exception e){}
        check = "";
        password = "";
        phone = "";
        contactList = new ArrayList<>();
        ccp = findViewById(R.id.ccp);
        log_in = findViewById(R.id.login);
        pass = findViewById(R.id.pass);
        cont = findViewById(R.id.phone);
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("USERS");
        cont.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                phone =  ccp.getDefaultCountryCodeWithPlus() +   s.toString();
                getAllUsers();
            }
        });
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                getAllUsers();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                password = s.toString();
                checkPass();
            }
        });
        getAllUsers();
    }
public void openMain(View view) {
        if(!phone.isEmpty()){
            checkPass();
        }
        if(!contactList.contains(phone)){
            cont.setError("User not Registered");
            cont.requestFocus();
            return;
        }else if(phone.isEmpty()){
            cont.setError("Cannot be empty");
            cont.requestFocus();
            return;
        }
        else if(password.isEmpty()){
            pass.setError("Cannot be empty");
            pass.requestFocus();
            return;
        }
        else if(!check.equals(password)){
            pass.setError("Invalid Password");
            pass.requestFocus();
            return;
        }
        editor.putString("phone",phone);
        editor.apply();
        edit.putBoolean("login",true);
        edit.apply();
        Intent intent=new Intent(this,MainActivity.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//Change Here
        startActivity(intent);
        finish();
    }

    public void forgotPassword(View view) {
        /*startActivity(new Intent(this, ChangePassword.class));*/
    }

    public void openReg(View view) {
        startActivity(new Intent(this,RegistryActivity.class));
    }

    public void getAllUsers(){
        collectionReference.get().addOnSuccessListener(queryDocumentSnapshots -> {
            contactList.clear();
            for(QueryDocumentSnapshot post : queryDocumentSnapshots){
                contactList.add(post.getId());
            }
        });
    }

    public void checkPass(){
        if(phone == null){
            return;
        }
        documentReference = firebaseFirestore.collection("USERS").document(phone);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            UserData u = documentSnapshot.toObject(UserData.class);
            if(u != null && u.getName() != null) {
                check = u.getPass();
            }
        }).addOnFailureListener(e -> {

        });

    }

    public void checkRequiredPermission() {
        List<String> listNeeded= new ArrayList<>();
        for(String perm:permissions){
            if(ContextCompat.checkSelfPermission(this,perm)!= PackageManager.PERMISSION_GRANTED){
                listNeeded.add(perm);
            }
        }
        if(!listNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this,
                    listNeeded.toArray(new String[listNeeded.size()]),1);
        }
    }
}
