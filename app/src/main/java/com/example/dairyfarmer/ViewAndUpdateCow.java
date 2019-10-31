package com.example.dairyfarmer;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ViewAndUpdateCow extends AppCompatActivity {

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;
    //Context context;
    EditText cowNameUpdate,tagIdUpdate, dateOfBUpdate, cowDetailsUpdate;
    Spinner cowOrBullUpdate;
    ImageView cowImage;
    RecyclerViewAdapter X;
    Button updateCowDetailsBtn, takePicture, galleryChoose;
    //List<Cow> MainImageUploadInfoList;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    // Creating URI.
    Uri FilePathUri;
    //Bitmap to get image from gallery
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_and_update_cow);
        Intent intent = getIntent();
        Integer position = intent.getIntExtra("number",0);

        //Toast.makeText(getApplicationContext(), "number "+position, Toast.LENGTH_LONG).show();
        onBindViewHolder(position);
        galleryChoose = findViewById(R.id.galleryChoose);
        updateCowDetailsBtn = findViewById(R.id.updateCowDetailsBtn);
        takePicture =findViewById(R.id.takePicture);
        galleryChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }});
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        updateCowDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCowDetails();
            }
        });
    }
    private void updateCowDetails(){

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File dir = new File(Environment.getExternalStorageDirectory().toString() + "/myApp/Pictures") ;
            dir.mkdirs();
            String path = dir.toString();
            File imageFile = new File(path,System.currentTimeMillis() + ".jpg" );

            //File imageFile = new File(Environment.getExternalStorageDirectory(), "myApp/Pictures/" + timeStamp + ".jpg");
            Uri uri = Uri.fromFile(imageFile);
            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            takePictureIntent.putExtra("return-data", true);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            FilePathUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);
                cowImage.setImageBitmap(bitmap);
                galleryChoose.setText("Image Selected");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            FilePathUri = data.getData();

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), FilePathUri);
            cowImage.setImageBitmap(imageBitmap);
        }
        else{
            System.out.println("error" );
        }
    }
    public void onBindViewHolder(final int position) {
        cowNameUpdate = findViewById(R.id.cowNameUpdate);
        cowImage = findViewById(R.id.CowImageView);
        tagIdUpdate = findViewById(R.id.tagIdUpdate);
        dateOfBUpdate = findViewById(R.id.dateOfBUpdate);
        cowDetailsUpdate = findViewById(R.id.cowDetailsUpdate);
        cowOrBullUpdate = findViewById(R.id.cowOrBullUpdate);

        //create a list of items for the spinner.
        String[] items = new String[]{"Cow", "Bull"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        cowOrBullUpdate.setAdapter(adapter);


        Cow UploadInfo = X.MainImageUploadInfoList.get(position);
        cowDetailsUpdate.setText((UploadInfo.getCowDetails()));
        cowNameUpdate.setText(UploadInfo.getCowName());
        tagIdUpdate.setText(UploadInfo.getTagId());
        dateOfBUpdate.setText(UploadInfo.getDateOfB());
        SettingsActivity.setSpinText(cowOrBullUpdate,UploadInfo.getCowOrBull());

        //Loading image from Glide library.
        Glide.with(X.context).load(UploadInfo.getImageURL()).into(cowImage);

    }
}
