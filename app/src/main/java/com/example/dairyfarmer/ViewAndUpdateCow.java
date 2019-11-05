package com.example.dairyfarmer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.dairyfarmer.RegisterCow.Database_Path;

public class ViewAndUpdateCow extends AppCompatActivity {

    //Image request code
    private int PICK_IMAGE_REQUEST = 1;
    //Context context;
    EditText cowNameUpdate,tagIdUpdate, dateOfBUpdate, cowDetailsUpdate;
    Spinner cowOrBullUpdate;
    ImageView cowImage;
    RecyclerViewAdapter X;
    Button updateCowDetailsBtn, takePicture, galleryChoose;
    // Folder path for Firebase Storage.
    String Storage_Path = "All_Image_Uploads/";
    private boolean imageChange = false;
    //List<Cow> MainImageUploadInfoList;

    ProgressDialog progressDialog ;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    // Creating URI.
    Uri FilePathUri;
    //Bitmap to get image from gallery
    private Bitmap bitmap;
    private String pictureImagePath = "";
    Uri photoURI;
    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;

    String childPath ;
    DatabaseReference setDetails, myref;
    private FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase database;Integer position;
    Cow UploadInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_and_update_cow);
        Intent intent = getIntent();
        position = intent.getIntExtra("number",0);

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(ViewAndUpdateCow.this);

        //Toast.makeText(getApplicationContext(), "number "+position, Toast.LENGTH_LONG).show();
       // onBindViewHolder(position);
        galleryChoose = findViewById(R.id.galleryChoose);
        updateCowDetailsBtn = findViewById(R.id.updateCowDetailsBtn);
        takePicture =findViewById(R.id.takePicture);

        cowNameUpdate = findViewById(R.id.cowNameUpdate);
        cowImage = findViewById(R.id.CowImageView);
        tagIdUpdate = findViewById(R.id.tagIdUpdate);
        dateOfBUpdate = findViewById(R.id.dateOfBUpdate);
        cowDetailsUpdate = findViewById(R.id.cowDetailsUpdate);
        cowOrBullUpdate = findViewById(R.id.cowOrBullUpdate);

        storageReference = FirebaseStorage.getInstance().getReference();
        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);

        //create a list of items for the spinner.
        String[] items = new String[]{"Cow", "Bull"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        cowOrBullUpdate.setAdapter(adapter);


        UploadInfo = X.MainImageUploadInfoList.get(position);
        cowDetailsUpdate.setText((UploadInfo.getCowDetails()));
        cowNameUpdate.setText(UploadInfo.getCowName());
        tagIdUpdate.setText(UploadInfo.getTagId());
        dateOfBUpdate.setText(UploadInfo.getDateOfB());
        SettingsActivity.setSpinText(cowOrBullUpdate,UploadInfo.getCowOrBull());

        childPath = UploadInfo.getDate();
        //Loading image from Glide library.
        Glide.with(X.context).load(UploadInfo.getImageURL()).into(cowImage);



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
                popUpConfirmation();
            }
        });
    }
    private void updateCowDetails(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String fullEmail = user.getEmail();

        String[] splitEmail = fullEmail.split("@");
        final String email = splitEmail[0];

        childPath = UploadInfo.getDate();

        final String cowNameUpdateID = cowNameUpdate.getText().toString();
        final String tagIdUpdateID = tagIdUpdate.getText().toString();
        final String dateOfBUpdateID = dateOfBUpdate.getText().toString();
        final String cowDetailsUpdateID = cowDetailsUpdate.getText().toString();
        //final String phoneID = phoneUpdate.getText().toString();
        //final String usernameID = usernameUpdate.getText().toString();
        final String cowOrBullUpdateID = cowOrBullUpdate.getSelectedItem().toString();

        if (FilePathUri != null || cowImage.getDrawable() != null) {
            // Setting progressDialog Title.
            progressDialog.setTitle("Data is Updating...");

            // Showing progressDialog.
            progressDialog.show();

            database = FirebaseDatabase.getInstance();
            myref = database.getReference();
            myref.child("All_Image_Uploads_Database").child(email).child(childPath).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    // Adding image upload id s child element into databaseReference.
                    dataSnapshot.getRef().child("cowOrBull").setValue(cowOrBullUpdateID);
                    dataSnapshot.getRef().child("dateOfB").setValue(dateOfBUpdateID);
                    dataSnapshot.getRef().child("details").setValue(cowDetailsUpdateID);
                    dataSnapshot.getRef().child("cowName").setValue(cowNameUpdateID);
                    dataSnapshot.getRef().child("image").setValue(cowNameUpdateID);
                    dataSnapshot.getRef().child("tagId").setValue(tagIdUpdateID);
                    // Showing toast message after done uploading.
                    Toast.makeText(getApplicationContext(), "Data Updated Successfully ", Toast.LENGTH_LONG).show();
                    }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // Creating second StorageReference.
            if(imageChange==true){
                final StorageReference storageReference2nd = storageReference.child(Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));
            //}
            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference2nd.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onSuccess(Uri uri) {
                           databaseReference.child(email).child(childPath).child("imageUrl").setValue(uri.toString());
                           databaseReference.child(email).child(childPath).child("url").setValue(uri.toString());
                        }
                    });
                }
            })// If something goes wrong .
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Hiding the progressDialog.
                            progressDialog.dismiss();
                            // Showing exception erro message.
                            Toast.makeText(ViewAndUpdateCow.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    // On progress change upload time.
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            // Setting progressDialog Title.
                            progressDialog.setTitle("Image is Uploading...");

                        }
                    });
            }
            Intent intent = new Intent(ViewAndUpdateCow.this, HomePage .class);
            startActivity(intent);

        }
        else {

            Toast.makeText(ViewAndUpdateCow.this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();
        }
    }

    public void popUpConfirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to Update the cow details?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                updateCowDetails();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    private void dispatchTakePictureIntent() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp+ ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;

        File file = new File(pictureImagePath);
        photoURI = FileProvider.getUriForFile(getApplicationContext(), "com.example.android.fileprovider", file);
        //Uri outputFileUri = Uri.fromFile(file);
        Uri outputFileUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.android.fileprovider", file);

        imageChange = true;
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, 123);
    }
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");

        imageChange = true;
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
        }else if (requestCode == 123) {
            File imgFile = new  File(pictureImagePath);
            if(imgFile.exists()){
                FilePathUri = Uri.parse(pictureImagePath);
                FilePathUri = photoURI;
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                //ImageView myImage = (ImageView) findViewById(R.id.imageviewTest);
                cowImage.setImageBitmap(myBitmap);

            }
        }
        else{
            System.out.println("error" );
        }
    }
}
