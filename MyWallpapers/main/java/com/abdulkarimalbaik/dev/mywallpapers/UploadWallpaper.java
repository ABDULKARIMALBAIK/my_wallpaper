package com.abdulkarimalbaik.dev.mywallpapers;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.abdulkarimalbaik.dev.mywallpapers.Common.Common;
import com.abdulkarimalbaik.dev.mywallpapers.Database.DataSource.RecentsRepository;
import com.abdulkarimalbaik.dev.mywallpapers.Database.LocalDatabase.LocalDatabase;
import com.abdulkarimalbaik.dev.mywallpapers.Database.LocalDatabase.RecentsDataSource;
import com.abdulkarimalbaik.dev.mywallpapers.Database.ModelDB.Recents;
import com.abdulkarimalbaik.dev.mywallpapers.Model.AzureComputerVision.ComputerVision;
import com.abdulkarimalbaik.dev.mywallpapers.Model.AzureComputerVision.URLUpload;
import com.abdulkarimalbaik.dev.mywallpapers.Model.CategoryItem;
import com.abdulkarimalbaik.dev.mywallpapers.Model.WallpaperItem;
import com.abdulkarimalbaik.dev.mywallpapers.Remote.IComputerVision;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadWallpaper extends AppCompatActivity {

    private static final int GALLERY_PERMISSION_CODE_REQUEST = 1000;
    private static final int PICK_IMAGE_REQUEST = 1111;

    ImageView imaagePreview;
    Button btnGallery , btnUpload , btnSubmit;
    MaterialSpinner spinner;

    //Firebase Storage
    FirebaseStorage storage;
    StorageReference storageReference;

    IComputerVision mService;

    private String category_id_selected = "" , directionUrl = "" , nameOfFile = "";
    private Uri filePath;
    Map<String , String> spinnerData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_wallpaper);

        mService = Common.getComputerVisionAPI();

        //Init Firebase
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init Views
        imaagePreview = (ImageView)findViewById(R.id.imagePreview);
        btnGallery = (Button)findViewById(R.id.btnGallery);
        btnUpload = (Button)findViewById(R.id.btnUpload);
        spinner = (MaterialSpinner)findViewById(R.id.spinner);

        loadCategoryToSpinner();

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(UploadWallpaper.this , android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(UploadWallpaper.this , android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= 23)
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE} , GALLERY_PERMISSION_CODE_REQUEST);
                }
                else {

                    chooseImage();
                }

            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (spinner.getSelectedIndex() == 0)
                    Toast.makeText(UploadWallpaper.this, "Please choose category", Toast.LENGTH_SHORT).show();
                else
                    uploadImage();
            }
        });

        btnSubmit = (Button)findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectionToInternet(UploadWallpaper.this))
                    detectAdultContent(directionUrl);
                else
                    Toast.makeText(UploadWallpaper.this, "Please check your connection by internet !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void detectAdultContent(final String directionUrl) {

        if (directionUrl.isEmpty())
            Toast.makeText(this, "Image not uploaded !", Toast.LENGTH_SHORT).show();
        else {

            final AlertDialog dialog = new SpotsDialog(this);
            dialog.show();
            dialog.setMessage("Analyzing Image...");

            mService.analyzeImage(Common.getAPIAdultEndPoint() , new URLUpload(directionUrl))
                    .enqueue(new Callback<ComputerVision>() {
                        @Override
                        public void onResponse(Call<ComputerVision> call, Response<ComputerVision> response) {
                            
                            if(response.isSuccessful()){
                                
                                if (!response.body().getAdult().isAdultContent()){
                                    
                                    //If image contain adult content
                                    //we will save it to our background gallery
                                    dialog.dismiss();
                                    saveImageInDatabase(category_id_selected , directionUrl);
                                    Toast.makeText(UploadWallpaper.this, "Success", Toast.LENGTH_SHORT).show();
                                }
                                else{

                                    //If url is adult content , we will delete it from Firebase Storage
                                    dialog.dismiss();
                                    deleteFileFromStorage(nameOfFile);
                                }
                            }
                            else {

                                //Here Azure Service was stop , so we can't detect if image contain Adult content
                                //However, we will save
                                dialog.dismiss();
                                saveImageInDatabase(category_id_selected , directionUrl);
                                Toast.makeText(UploadWallpaper.this, "Success without detecting adult content", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ComputerVision> call, Throwable t) {
                            Toast.makeText(UploadWallpaper.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    private void deleteFileFromStorage(String nameOfFile) {

        storageReference.child(new StringBuilder("..................").append(nameOfFile).toString())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(UploadWallpaper.this, "Your image is adult content and will be deleted !", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage() {

        if (filePath != null){

            final AlertDialog dialog = new SpotsDialog(this);
            dialog.show();
            dialog.setCancelable(false);
            dialog.setMessage("Please waiting...");

            //We want name of image to delete it
            nameOfFile = UUID.randomUUID().toString();

            StorageReference ref = storageReference.child(
                    new StringBuilder("................").append(nameOfFile).toString());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            dialog.dismiss();
                            directionUrl = taskSnapshot.getDownloadUrl().toString();
                            btnSubmit.setEnabled(true);
                            Toast.makeText(UploadWallpaper.this, "Now hit submit button", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(UploadWallpaper.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveImageInDatabase(String category_id_selected, String imageUrl) {

        FirebaseDatabase.getInstance()
                .getReference(Common.STR_WALLPAPER)
                .push()
                .setValue(new WallpaperItem(category_id_selected , imageUrl))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(UploadWallpaper.this, "Upload Succeed !", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadWallpaper.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void chooseImage() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent , "Select image: ") , PICK_IMAGE_REQUEST);
    }

    private void loadCategoryToSpinner() {

        FirebaseDatabase.getInstance()
                .getReference(Common.STR_CATEGORY_BACKGROUND)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                            CategoryItem item = snapshot.getValue(CategoryItem.class);
                            String key = snapshot.getKey();

                            spinnerData.put(key , item.getName());
                        }

                        //Because Material spinner will not receive hint so we need custom hint
                        Object[] valueArray = spinnerData.values().toArray();
                        List<Object> valueList = new ArrayList<>();
                        valueList.add(0 , "Category");  //add first item is hint
                        valueList.addAll(Arrays.asList(valueArray));

                        spinner.setItems(valueList);
                        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                                //When user choose category , we will get categoryId (key)
                                Object[] valueArray = spinnerData.keySet().toArray();
                                List<Object> keyList = new ArrayList<>();
                                keyList.add(0 , "Category_Key");
                                keyList.addAll(Arrays.asList(valueArray));

                                category_id_selected = keyList.get(position).toString();  //Assign key when user choose category
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case GALLERY_PERMISSION_CODE_REQUEST:{

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    chooseImage();
                else
                    Toast.makeText(this, "You can't access to gallery !", Toast.LENGTH_SHORT).show();

                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){

            filePath = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver() , filePath);
                imaagePreview.setImageBitmap(bitmap);
                btnUpload.setEnabled(true);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {

        deleteFileFromStorage(nameOfFile);
        super.onBackPressed();
    }
}
