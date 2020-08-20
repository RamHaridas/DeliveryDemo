package com.white.delivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistryActivity extends AppCompatActivity implements Dialog_Reg_Get_Image.MyDialogCloseListener,Dialog_Reg_Get_Image.onPhotoSelectedListener{


    Button btn;
    ProgressBar progressBar;
    ImageView profile_iv;
    Dialog_Reg_Get_Image dgi;
    Bitmap imagebitmap;
    Uri imageuri;
    TextInputEditText name,email,phone,pass,conf_pass;
    CountryCodePicker countryCodePicker;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    String cont;
    Uri profile_uri;
    Bitmap profile_bit;
    UserData userData;
    String naam;
    String ema;
    String phoney;
    String pas;
    String cpass;
    private static String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry);
        userData = new UserData();
        progressBar = findViewById(R.id.progress);
        storageReference = FirebaseStorage.getInstance().getReference().child("USER");
        firebaseFirestore = FirebaseFirestore.getInstance();
        countryCodePicker = findViewById(R.id.ccp);
        name = findViewById(R.id.fullname);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        pass = findViewById(R.id.password);
        conf_pass = findViewById(R.id.confirm_password);

        profile_iv = findViewById(R.id.profile_image);
        btn = findViewById(R.id.register);
        profile_iv.setOnClickListener(v -> {
            dgi=new Dialog_Reg_Get_Image();
            dgi.show(getSupportFragmentManager(),"Dialog_Reg_Image");
        });

        btn.setOnClickListener(v -> check(v));
    }

    private void check(View v) {

        naam = name.getText().toString().trim();
        ema = email.getText().toString().trim();
        phoney = countryCodePicker.getDefaultCountryCodeWithPlus() + phone.getText().toString().trim();
        pas = pass.getText().toString().trim();
        cpass = conf_pass.getText().toString().trim();
        cont = phoney;
        if(naam.isEmpty()){
            name.setError("Cannot be empty");
            name.requestFocus();
            return;
        }else if(!isEmailValid(ema)){
            email.setError("Invalid format");
            email.requestFocus();
            return;
        }else if(phone.getText().toString().isEmpty() && phoney.length() < 13){
            phone.setError("Invalid Contact number");
            phone.requestFocus();
            return;
        }else if(pas.isEmpty()){
            pass.setError("Cannot be empty");
            pass.requestFocus();
            return;
        }else if(!cpass.equals(pas)){
            conf_pass.setError("Password does not matches");
            conf_pass.requestFocus();
            return;
        }else if(profile_bit == null && profile_uri == null){
            Toast.makeText(RegistryActivity.this,"Please Upload your Pic",Toast.LENGTH_SHORT).show();
            return;
        }
        if(profile_uri != null){
            uploadFile(profile_uri);
        }else if(profile_bit != null){
            uploadBitmap(profile_bit);
        }


        progressBar.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            progressBar.setVisibility(View.INVISIBLE);
            startActivity(new Intent(RegistryActivity.this,User.class));
        },2500);
    }

    @Override
    public void getImagePath(Uri imagePath) {
        imageuri = imagePath;
        imagebitmap = null;
    }
    @Override
    public void getImageBitmap(Bitmap bitmap) {
        imagebitmap = bitmap;
        imageuri = null;
    }

    @Override
    public void handleDialogClose(int num) {
        if(imagebitmap!=null) {
            profile_iv.setImageDrawable(null);
            profile_iv.setImageBitmap(imagebitmap);
            profile_bit = imagebitmap;
            Log.e("Image Adder", "----" +1);
        }
        else if(imageuri!=null) {
            profile_iv.setImageDrawable(null);
            profile_iv.setImageURI(imageuri);
            profile_uri = imageuri;
            Log.e("Image Adder", "----" + 2);
        }
        else{
            Toast.makeText(this,"No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void uploadFile(Uri uri){
        final String time = String.valueOf(System.currentTimeMillis());
        final String ext = getFileExtension(uri);
        StorageReference storageReference1 = storageReference.child(time+"."+ext);
        UploadTask uploadTask = storageReference1.putFile(uri);

        uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.child(time+"."+ext).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    url = task.getResult().toString();
                    userData.setUrl(url);
                    userData.setName(naam);
                    userData.setEmail(ema);
                    userData.setMobile(phoney);
                    userData.setPass(pas);
                    firebaseFirestore.collection("USERS").document(phoney).set(userData)
                            .addOnSuccessListener(aVoid -> Toast.makeText(RegistryActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(RegistryActivity.this,"Registration Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show());
                    Log.i("URL",url);
                })).addOnFailureListener(e -> Toast.makeText(RegistryActivity.this,"ERROR IN UPLOAD: "+e.getMessage(),Toast.LENGTH_SHORT).show())
                .addOnProgressListener(taskSnapshot -> {
            double p = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
            //progressBar.setProgress((int)p);
        });
    }

    public void uploadBitmap(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] bytes = baos.toByteArray();
        final String time = String.valueOf(System.currentTimeMillis());
        final String ext = "jpeg";
        StorageReference storageReference1 = storageReference.child(time+"."+ext);
        UploadTask uploadTask = storageReference1.putBytes(bytes);
        uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.child(time+"."+ext)
                .getDownloadUrl().addOnCompleteListener(task -> {
            url = task.getResult().toString();
            userData.setUrl(url);
            userData.setName(naam);
            userData.setEmail(ema);
            userData.setMobile(phoney);
            userData.setPass(pas);
            firebaseFirestore.collection("USERS").document(phoney).set(userData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(RegistryActivity.this,"Registration Successfull",Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(RegistryActivity.this,"Registration Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show());
            Log.i("URL",url);
        })).addOnFailureListener(e -> Toast.makeText(RegistryActivity.this,"ERROR IN UPLOAD: "+e.getMessage(),Toast.LENGTH_SHORT).show()).addOnProgressListener(taskSnapshot -> {
            double p = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
            //progressBar.setProgress((int)p);
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeInfo = MimeTypeMap.getSingleton();
        return  mimeTypeInfo.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}
