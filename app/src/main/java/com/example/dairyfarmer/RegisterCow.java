package com.example.dairyfarmer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class RegisterCow extends AppCompatActivity {

    //Image request code
    private int PICK_IMAGE_REQUEST = 3;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    final int REQUEST_PERMISSION_CODE = 1000;

    File mPhotoFile;
    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;

    //Bitmap to get image from gallery
    private Bitmap bitmap;

    //Uri to store the image uri
    private Uri filePath;

    private Button buttonChoose, takePhoto;
    private Button buttonUpload;
    private ImageView imageView;
    private EditText editText, cowName, tagId, cowDOB;
    private Spinner cowOrBull;

    String currentPhotoPath;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_TAKE_PHOTO = 42;

    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;


    // Image request code for onActivityResult() .
    int Image_Request_Code = 7;



    // Folder path for Firebase Storage.
    String Storage_Path = "All_Image_Uploads/";

    // Root Database Name for Firebase Database.
    public static final String Database_Path = "All_Image_Uploads_Database";


    // Creating URI.
    Uri FilePathUri;

    ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_cow);

        requestStoragePermission();

        cowOrBull = findViewById(R.id.cowOrBull);
        //create a list of items for the spinner.
        String[] items = new String[]{"Cow", "Bull"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        cowOrBull.setAdapter(adapter);

        buttonChoose = findViewById(R.id.chooseImage);
        buttonUpload = findViewById(R.id.uploadCow);
        takePhoto = findViewById(R.id.takePhoto);
        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.editText);
        cowName = findViewById(R.id.cowName);
        cowDOB = findViewById(R.id.cowDOB);
        tagId = findViewById(R.id.tagId);

        // Assigning Id to ProgressDialog.
        progressDialog = new ProgressDialog(RegisterCow.this);

        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().getReference();

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }});
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cowBirth = cowDOB.getText().toString();
                boolean isDateValid = false;
                isDateValid = validateJavaDate(cowBirth);
                System.out.println("oooooooooooooooooooooooooooopppppppppppppppppppppppppppppp  "+isDateValid );
                if(cowBirth.isEmpty()){
                    cowDOB.setError("Date Empty");
                    cowDOB.requestFocus();
                }else if(!isDateValid){
                    cowDOB.setError("Please check the date mm/dd/yyyy");
                    cowDOB.requestFocus();
                }
                else if(isDateValid) {
                    // Calling method to upload selected image on Firebase storage.
                    popUpConfirmation();
                }
            }
        });


    }
    public void popUpConfirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to Register the Cow?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                UploadImageFileToFirebaseStorage();
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
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    private String pictureImagePath = "";
    Uri photoURI;
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
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(cameraIntent, 123);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public static boolean validateJavaDate(String strDate)
    {
        /* Check if date is 'null' */
        if (strDate.trim().equals(""))
        {
            return false;
        }
        /* Date is not 'null' */
        else
        {
            /*
             * Set preferred date format,
             * For example MM-dd-yyyy, MM.dd.yyyy,dd.MM.yyyy etc.*/
            SimpleDateFormat sdfrmt = new SimpleDateFormat("dd/MM/yyyy");
            sdfrmt.setLenient(false);
            /* Create Date object
             * parse the string into date
             */
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());


            try
            {
                Date now = sdf.parse(currentDateandTime);
                Date given = sdf.parse(strDate);

                if(now.before(given)){
                    return false;
                }
                /*if(now.after(given)){

                System.out.println(strDate+" is valid date format");
                }*/

                Date javaDate = sdfrmt.parse(strDate);
                System.out.println(strDate+" is valid date format");
            }
            /* Date format is invalid */
            catch (ParseException e)
            {
                System.out.println(strDate+" is Invalid Date format");
                return false;
            }
            /* Return true if date format is valid */
            return true;
        }
    }
    //method to show file chooser
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
                imageView.setImageBitmap(bitmap);
                buttonChoose.setText("Image Selected");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if(data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
            }
        }else if (requestCode == 123) {
            File imgFile = new  File(pictureImagePath);
            if(imgFile.exists()){
                FilePathUri = Uri.parse(pictureImagePath);
                FilePathUri = photoURI;
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                //ImageView myImage = (ImageView) findViewById(R.id.imageviewTest);
                imageView.setImageBitmap(myBitmap);

            }
        }
        else{
            System.out.println("error" );
        }
    }


    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    public void UploadImageFileToFirebaseStorage() {


        final String cowOrBullId = cowOrBull.getSelectedItem().toString();
        final String editTextId = editText.getText().toString();
        final String cowNameId = cowName.getText().toString();
        final String tagIdID = tagId.getText().toString();
        final String cowDOBId = cowDOB.getText().toString();

        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {
            // Setting progressDialog Title.
            progressDialog.setTitle("Image is Uploading...");

            // Showing progressDialog.
            progressDialog.show();

            // Creating second StorageReference.
            final StorageReference storageReference2nd = storageReference.child(Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri));
            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference2nd.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onSuccess(Uri uri) {
                           // Log.d(TAG, "onSuccess: uri= "+ uri.toString());

                            // Getting image name from EditText and store into string variable.
                            String TempImageName = cowName.getText().toString().trim();

                            // Hiding the progressDialog after done uploading.
                            progressDialog.dismiss();

                            // Showing toast message after done uploading.
                            Toast.makeText(getApplicationContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();

                            startActivity(new Intent(RegisterCow.this, HomePage.class));

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String fullEmail = user.getEmail();

                            // Getting image upload ID.
                            //String ImageUploadId = databaseReference.push().getKey();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                            String currentDateandTime = sdf.format(new Date());
                            String[] splitEmail = fullEmail.split("@");
                            final String email = splitEmail[0];

                            @SuppressWarnings("VisibleForTests")
                            Cow imageUploadInfo = new Cow(TempImageName, tagIdID,cowOrBullId,cowDOBId,editTextId,cowNameId,uri.toString(),currentDateandTime);

                            // Adding image upload id s child element into databaseReference.
                            databaseReference.child(email).child(currentDateandTime).setValue(imageUploadInfo);
                            Intent intent = new Intent(RegisterCow.this, HomePage .class);
                            intent.putExtra("email",email);
                            startActivity(intent);
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
                            Toast.makeText(RegisterCow.this, exception.getMessage(), Toast.LENGTH_LONG).show();
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
        else {

            Toast.makeText(RegisterCow.this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();

        }
    }
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }


    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera_result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && camera_result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                REQUEST_PERMISSION_CODE);
    }





}
