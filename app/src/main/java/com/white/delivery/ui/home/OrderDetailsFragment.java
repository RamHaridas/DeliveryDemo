package com.white.delivery.ui.home;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.white.delivery.OrderData;
import com.white.delivery.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;


public class OrderDetailsFragment extends Fragment implements Dialog_Get_Image.onPhotoSelectedListener,Dialog_Get_Image.MyDialogCloseListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2  = "param2";
    View root;
    public static OrderData orderData_per;
    int number_of_images=0;
    Switch remove_switch;
    ImageView[] imageViews;
    int removed_images[];
    int number_images_removed=0;
    boolean remove_state=false;
    Uri imageuri=null;
    Bitmap imagebitmap=null;
    ImageContainer ic[];
    Dialog_Get_Image dgi;
    RelativeLayout rdate,rtime;
    TextView tv_date,tv_time;
    Button b_confirm_order;
    String packType;
    EditText et_esWeight,et_esdimen;
    Double distance;
    public NavController navCont;
    Double cost=0.0;
    SharedPreferences sharedPreferences;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private static final int MAX=3;
    private static final int REQUEST_CODE=11;
    Uri imguri1, imguri2, imguri3, imguri4;
    Bitmap imgbit1,imgbit2,imgbit3,imgbit4;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;
    DocumentReference documentReference;
    String phone_no;
    EditText description, weight,dimension,receiver_name,rec_no,rec_add;
    TextView date,time;
    StorageReference storageReference;
    String uni_time;
    private static String name;
    String desc,weight1,dimen,date1,time1,rec_name,recadd,recno;
    public static String order_name;
    String hour, min;
    Calendar selected_cal;
    public OrderDetailsFragment() {
        // Required empty public constructor
    }
    public static OrderDetailsFragment newInstance(String param1, String param2) {
        OrderDetailsFragment fragment = new OrderDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_order_details, container, false);

        order_name = "";
        storageReference = FirebaseStorage.getInstance().getReference().child("ORDER_IMAGES");
        sharedPreferences = this.getActivity().getSharedPreferences("phone", Context.MODE_PRIVATE);
        phone_no = sharedPreferences.getString("phone","");
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("USERS");
        documentReference = firebaseFirestore.collection("USERS").document(phone_no);
        navCont = NavHostFragment.findNavController(this);
        packType = getArguments().getString("packageType");
        distance = Double.parseDouble(getArguments().getString("distance"));

        description = root.findViewById(R.id.description_et);
        et_esdimen = root.findViewById(R.id.dimen_et);
        et_esWeight = root.findViewById(R.id.weight_et);
        tv_date = root.findViewById(R.id.date_tv);
        tv_time = root.findViewById(R.id.time_tv);
        receiver_name = root.findViewById(R.id.rname_et);
        rec_no = root.findViewById(R.id.rmobile_et);
        rec_add = root.findViewById(R.id.raddress);

        Toast.makeText(getContext(),packType,Toast.LENGTH_SHORT).show();
        imageViews=new CustomImageView[5];
        removed_images=new int[5];
        ic=new ImageContainer[4];
        verifyPermissions();
        imageViews[0] = root.findViewById(R.id.img1);
        imageViews[1] = root.findViewById(R.id.img2);
        imageViews[2] = root.findViewById(R.id.img3);
        imageViews[3] = root.findViewById(R.id.img4);
        imageViews[4] = root.findViewById(R.id.add);
        remove_switch=root.findViewById(R.id.remove_switch);
        b_confirm_order=root.findViewById(R.id.confirm_booking_bt);

        remove_switch.setOnClickListener(v -> removeSwitchImage());
        imageViews[0].setOnClickListener(v -> removeImage(0));
        imageViews[1].setOnClickListener(v -> removeImage(1));
        imageViews[2].setOnClickListener(v -> removeImage(2));
        imageViews[3].setOnClickListener(v -> removeImage(3));
        imageViews[4].setOnClickListener(v -> {
            if (!remove_state) {
                addImage();
            }
        });
        rdate=root.findViewById(R.id.date_rl);
        rtime=root.findViewById(R.id.time_rl);
        hour=String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        if (hour.length()==1){
            hour="0"+hour;
        }
        min=String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));
        if (min.length()==1){
            min="0"+min;
        }
        tv_date.setText(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"-"+Calendar.getInstance().get(Calendar.MONTH)+"-"+Calendar.getInstance().get(Calendar.YEAR));
        tv_time.setText(String.format("%s:%s", hour, min));
        rdate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, monthOfYear, dayOfMonth) -> tv_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year), mYear, mMonth, mDay);
            c.add(Calendar.DAY_OF_MONTH, 0); // subtract 2 years from now
            datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            c.add(Calendar.DAY_OF_MONTH, 5); // add 4 years to min date to have 2 years after now
            datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            datePickerDialog.show();
        });
        rtime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    selected_cal = Calendar.getInstance();
                    selected_cal.set(Calendar.HOUR_OF_DAY,hourOfDay);
                    selected_cal.set(Calendar.MINUTE,minute);
                    selected_cal.set(Calendar.SECOND,0);
                    tv_time.setText(hourOfDay+":"+minute);
                }
            },mHour,mMinute,false);
            timePickerDialog.show();
        });
        b_confirm_order.setOnClickListener(v ->confirmOrder(v));

        getImageUrl("+91942250885914:23url1");
        return root;
    }
    @Override
    public void getImagePath(Uri imagePath) {
        imageuri=imagePath;
        imagebitmap=null;
    }
    @Override
    public void getImageBitmap(Bitmap bitmap) {
        imagebitmap=bitmap;
        imageuri=null;
    }
    @Override
    public void handleDialogClose(int num) {
        num = number_of_images;
        if(number_images_removed<=0){
            if (num==0) {
                Log.e("Image Adder","----"+num);
                if(imagebitmap!=null){
                    imageViews[num].setImageBitmap(imagebitmap);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder","----"+num);
                    imgbit1 = imagebitmap;
                    number_of_images=number_of_images+1;
                }
                else if(imageuri!=null){
                    imageViews[num].setImageURI(imageuri);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("xImage Adder","----"+num);
                    imguri1 = imageuri;
                    number_of_images=number_of_images+1;
                }

            }
            else if(num==1){
                Log.e("Image Adder","----"+num);
                if(imagebitmap!=null) {
                    imageViews[num].setImageBitmap(imagebitmap);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder","----"+num);
                    imgbit2 = imagebitmap;
                    number_of_images=number_of_images+1;
                }
                else if(imageuri!=null){
                    imageViews[num].setImageURI(imageuri);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder","----"+num);
                    imguri2 = imageuri;
                    number_of_images=number_of_images+1;
                }

            }
            else if(num==2){
                Log.e("Image Adder","----"+num);
                if(imagebitmap!=null){
                    imageViews[num].setImageBitmap(imagebitmap);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder","----"+num);
                    imgbit3 = imagebitmap;
                    number_of_images=number_of_images+1;
                }
                else if(imageuri!=null){
                    imageViews[num].setImageURI(imageuri);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder","----"+num);
                    imguri3 = imageuri;
                    number_of_images=number_of_images+1;
                }
            }
            else if(num==3){
                Log.e("Image Adder","----"+num);
                if(imagebitmap!=null) {
                    imageViews[num].setImageBitmap(imagebitmap);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder","----"+num);
                    number_of_images=number_of_images+1;
                    imageViews[4].setVisibility(View.GONE);
                    imgbit4 = imagebitmap;
                    number_images_removed=0;
                }
                else if(imageuri!=null) {
                    imageViews[num].setImageURI(imageuri);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder","----"+num);
                    number_of_images=number_of_images+1;
                    imageViews[4].setVisibility(View.GONE);
                    imguri4 = imageuri;
                    number_images_removed=0;
                }

            }
        }else if(number_images_removed>=1) {

            num=removed_images[number_images_removed];
            number_images_removed = number_images_removed - 1;
            if (num == 0) {
                Log.e("Image Adder", "----" + num);
                if (imagebitmap != null) {
                    imageViews[num].setImageBitmap(imagebitmap);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder", "----" + num);
                    imgbit1 = imagebitmap;
                    number_of_images = number_of_images + 1;
                }
                else if (imageuri != null) {
                    imageViews[num].setImageURI(imageuri);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder", "----" + num);
                    imguri1 = imageuri;
                    number_of_images = number_of_images + 1;
                }
            }
            else if (num == 1) {
                Log.e("Image Adder", "----" + num);
                if (imagebitmap != null) {
                    imageViews[num].setImageBitmap(imagebitmap);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder", "----" + num);
                    imgbit2 = imagebitmap;
                    number_of_images = number_of_images + 1;
                }
                else if (imageuri != null) {
                    imageViews[num].setImageURI(imageuri);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder", "----" + num);
                    imguri2 = imageuri;
                    number_of_images = number_of_images + 1;
                }
            }
            else if (num == 2) {
                Log.e("Image Adder", "----" + num);
                if (imagebitmap != null) {
                    imageViews[num].setImageBitmap(imagebitmap);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder", "----" + num);
                    imgbit3 = imagebitmap;
                    number_of_images = number_of_images + 1;
                }
                else if (imageuri != null) {
                    imageViews[num].setImageURI(imageuri);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder", "----" + num);
                    imguri3 = imageuri;
                    number_of_images = number_of_images + 1;
                }
            }
            else if (num == 3) {
                Log.e("Image Adder", "----" + num);
                if (imagebitmap != null) {
                    imageViews[num].setImageBitmap(imagebitmap);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder", "----" + num);
                    imgbit4 = imagebitmap;
                    number_of_images = number_of_images + 1;
                }
                else if (imageuri != null) {
                    imageViews[num].setImageURI(imageuri);
                    Log.e("Image Adder", "----" + num);
                    imageViews[num].setVisibility(View.VISIBLE);
                    Log.e("Image Adder", "----" + num);
                    imguri4 = imageuri;
                    number_of_images = number_of_images + 1;
                }
            }
            if(number_of_images>=3 && number_images_removed==0){
                imageViews[4].setVisibility(View.GONE);
                number_images_removed = 0;
            }
        }
        imagebitmap=null;
        imageuri=null;
    }
    private void addImage(){
        int curr=number_of_images;
        if(curr>=1){
            imageViews[4].setVisibility(View.GONE);
            number_images_removed=0;
            Toast.makeText(getContext(), "Image limit reached", Toast.LENGTH_LONG).show();
            return;
        }
        dgi=new Dialog_Get_Image();
        dgi.setTargetFragment(OrderDetailsFragment.this,curr);
        Bundle args=new Bundle();
        args.putInt("curr",curr);
        dgi.setArguments(args);
        dgi.show(getParentFragmentManager(),getString(R.string.dialog_Get_Image));
    }
    private void removeSwitchImage(){
        if(!remove_state){
            if(number_of_images==0){
                remove_switch.setChecked(false);
                Toast.makeText(getContext(),"No Images to remove",Toast.LENGTH_SHORT).show();
            }
            else{
                remove_state=true;
                Toast.makeText(getContext(),"Click on Image you want to remove ",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            remove_state=false;
        }

    }
    private void removeImage(int num){
        if(remove_state){
            imageViews[num].setVisibility(View.GONE);
            imageViews[num].setImageDrawable(null);
            number_images_removed++;
            removed_images[number_images_removed]=num;
            remove_switch.setChecked(false);
            remove_state=false;
            number_of_images--;
            imageViews[4].setVisibility(View.VISIBLE);
        }
    }
    void verifyPermissions(){
        String[] permissions={Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        if((ContextCompat.checkSelfPermission(getContext(),permissions[0])== PackageManager.PERMISSION_GRANTED) &&  (ContextCompat.checkSelfPermission(getContext(),
                permissions[1])== PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(getContext(),
                permissions[2])== PackageManager.PERMISSION_GRANTED){


        }else{
            ActivityCompat.requestPermissions(getActivity(),permissions,REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        verifyPermissions();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public void confirmOrder(View view){
        if(et_esWeight.getText().toString().equals("")){
            Toast.makeText(getContext(),"the weight must be between 1 to 10kgs for bike",Toast.LENGTH_SHORT).show();
            return;
        }
        if (et_esdimen.getText().toString().equals("")){
            Toast.makeText(getContext(),"the dimen must be between 0 to 1sqft for bike",Toast.LENGTH_SHORT).show();
            return;
        }
        if(packType.equals("Scooter")){
            if(10.0<Double.parseDouble(et_esWeight.getText().toString()) ||Double.parseDouble(et_esWeight.getText().toString())<1.0 || et_esWeight.getText().toString().equals("")){
                Toast.makeText(getContext(),"the weight must be between 1 to 10kgs for bike",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if(packType.equals("Van")){
            if(100.0<Double.parseDouble(et_esWeight.getText().toString()) || Double.parseDouble(et_esWeight.getText().toString())<10.0){
                Toast.makeText(getContext(),"the weight must be between 10 to 100kgs for van",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if(packType.equals("Scooter")){
            if(1.0<Double.parseDouble(et_esdimen.getText().toString()) ||Double.parseDouble(et_esdimen.getText().toString())<0.0 || et_esdimen.getText().toString().equals("")){
                Toast.makeText(getContext(),"the dimen must be between 0 to 1sqft for bike",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if(packType.equals("Van")){
            if(2.0<Double.parseDouble(et_esdimen.getText().toString()) || Double.parseDouble(et_esdimen.getText().toString())<1.0 || et_esdimen.getText().toString().equals("")){
                Toast.makeText(getContext(),"the weight must be between 1 to 2sqft for van",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if(packType.equals("Scooter")){
            cost=cost+30;
        }
        else{
            cost=cost+50;
        }
        uploadOrderData();
    }
    public void uploadOrderData(){
        desc = description.getText().toString().trim();
        weight1 = et_esWeight.getText().toString().trim();
        dimen = et_esdimen.getText().toString().trim();
        date1 = tv_date.getText().toString().trim();
        time1 = tv_time.getText().toString().trim();
        uni_time = tv_time.getText().toString().trim();
        rec_name = receiver_name.getText().toString().trim();
        recadd = rec_add.getText().toString().trim();
        recno = rec_no.getText().toString().trim();
        if(desc.isEmpty()){
            description.setError("Cannot be empty");
            description.requestFocus();
            return;
        }else if(weight1.isEmpty()){
            et_esWeight.setError("Cannot be empty");
            et_esWeight.requestFocus();
            return;
        }else if(dimen.isEmpty()){
            et_esdimen.setError("Cannot be empty");
            et_esdimen.requestFocus();
            return;
        }else if (date1.isEmpty()){
            tv_date.setError("Cannot be empty");
            tv_date.requestFocus();
            return;
        }else if(time1.isEmpty()){
            tv_time.setError("Cannot be empty");
            tv_time.requestFocus();
            return;
        }else if(rec_name.isEmpty()){
            receiver_name.setError("Cannot be empty");
            receiver_name.requestFocus();
            return;
        }else if(recadd.isEmpty()){
            rec_add.setError("Cannot be empty");
            rec_add.requestFocus();
            return;
        }else if(recno.isEmpty()){
            rec_no.setError("Cannot be empty");
            rec_no.requestFocus();
            return;
        }else if(imgbit1 == null && imguri1 == null){
            Toast.makeText(getContext(),"Please add at least one images",Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            Calendar datetime = Calendar.getInstance();
            if(selected_cal.getTimeInMillis() < datetime.getTimeInMillis()){
                Toast.makeText(getContext(),"Invalid Time",Toast.LENGTH_SHORT).show();
                return;
            }
        }catch (NullPointerException e){
            Toast.makeText(getContext(),"Invalid Time",Toast.LENGTH_SHORT).show();
            return;
        }

        order_name = date1+"+"+time1;

        Log.e("Firebase data","///////////////////////////////////////////////////////////////////////////");

        if(imguri1 != null){
            uploadFile(imguri1,0);
        }else if(imgbit1 != null){
            uploadBitmap(imgbit1,0);
        }
        /*if(imguri2 != null){
            uploadFile(imguri2,1);
        }else if(imgbit2 != null){
            uploadBitmap(imgbit2,1);
        }

        if(imguri3 != null){
            uploadFile(imguri3,2);
        }else if(imgbit3 != null){
            uploadBitmap(imgbit3,2);
        }

        if(imguri4 != null){
            uploadFile(imguri4,3);
        }else if(imgbit4 != null){
            uploadBitmap(imgbit4,3);
        }*/

        cost=cost+(distance*10);
        Toast.makeText(getContext(),"Total cost "+cost,Toast.LENGTH_SHORT).show();
        Bundle args=new Bundle();
        args.putString("cost",cost.toString());
        args.putString("mobile",phone_no);
        args.putString("time",time1);
        args.putString("packtype",packType);
        navCont.navigate(R.id.action_orderDetailsFragment_to_pendingFragment,args);
    }
    public void uploadFile(Uri uri,final int num){
        final String time = String.valueOf(System.currentTimeMillis());
        final String ext = getFileExtension(uri);
        if(num == 0){
            name = phone_no+uni_time+"url1";
        }else if(num == 1){
            name = phone_no+uni_time+"url2";
        }else if(num == 2){
            name = phone_no+uni_time+"url3";
        }else if(num == 3){
            name = phone_no+uni_time+"url4";
        }
        StorageReference storageReference1 = storageReference.child(name);
        UploadTask uploadTask = storageReference1.putFile(uri);

        uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.child(name).getDownloadUrl()
                .addOnCompleteListener(task -> {

                    OrderData orderData = new OrderData();
                    orderData.setDesc(desc);
                    orderData.setDimen(dimen);
                    orderData.setDate(date1);
                    orderData.setMobile_number(recno);
                    orderData.setRec_name(rec_name);
                    orderData.setTime(time1);
                    orderData.setWeight(weight1);
                    orderData.setRec_address(recadd);
                    orderData.setUser(phone_no);
                    orderData.setUrl1(task.getResult().toString());
                    orderData.setAssigned(false);
                    orderData.setCompleted(false);
                    orderData.setPickup_location(HomeFragment.pick_geoPoint);
                    orderData.setDrop_location(HomeFragment.drop_geoPoint);
                    orderData_per = orderData;
                    documentReference.collection("ORDER").document(order_name).set(orderData);
        })).addOnFailureListener(e -> Toast.makeText(getContext(),"ERROR IN UPLOAD: "+e.getMessage(),Toast.LENGTH_SHORT).show())
                .addOnProgressListener(taskSnapshot -> {
            double p = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
            //progressBar.setProgress((int)p);
        });
    }
    public void uploadBitmap(Bitmap bitmap,final int num){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] bytes = baos.toByteArray();
        final String time = String.valueOf(System.currentTimeMillis());
        final String ext = "jpeg";
        if(num == 0){
            name = phone_no+uni_time+"url1";
        }else if(num == 1){
            name = phone_no+uni_time+"url2";
        }else if(num == 2){
            name = phone_no+uni_time+"url3";
        }else if(num == 3){
            name = phone_no+uni_time+"url4";
        }
        StorageReference storageReference1 = storageReference.child(name);
        UploadTask uploadTask = storageReference1.putBytes(bytes);

        uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.child(name).getDownloadUrl().addOnCompleteListener(task -> {
            OrderData orderData = new OrderData();
            orderData.setDesc(desc);
            orderData.setDimen(dimen);
            orderData.setDate(date1);
            orderData.setMobile_number(recno);
            orderData.setRec_name(rec_name);
            orderData.setTime(time1);
            orderData.setWeight(weight1);
            orderData.setUrl1(task.getResult().toString());
            orderData.setRec_address(recadd);
            orderData.setUser(phone_no);
            orderData.setAssigned(false);
            orderData.setCompleted(false);
            orderData.setPickup_location(HomeFragment.pick_geoPoint);
            orderData.setDrop_location(HomeFragment.drop_geoPoint);
            orderData_per = orderData;
            documentReference.collection("ORDER").document(order_name).set(orderData);


        })).addOnFailureListener(e -> Toast.makeText(getContext(),"ERROR IN UPLOAD: "+e.getMessage(),Toast.LENGTH_SHORT).show()).addOnProgressListener(taskSnapshot -> {
            double p = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
            //progressBar.setProgress((int)p);
        });
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = this.getActivity().getContentResolver();
        MimeTypeMap mimeTypeInfo = MimeTypeMap.getSingleton();
        return  mimeTypeInfo.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    public void getImageUrl(String name){

        storageReference.child(name).getDownloadUrl().addOnSuccessListener(uri -> Log.i("URL",uri.toString()))
                .addOnFailureListener(e -> Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show());
    }
}
