package com.example.dairyfarmer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.app.ProgressDialog;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomePage extends AppCompatActivity {
    private TextView numCattle,welcomeUser;
    private ProgressBar progressBar;
    private static final String TAG = "HomePage";
    private FirebaseAuth auth;

    // Creating DatabaseReference.
    DatabaseReference databaseReference;
    DatabaseReference getUserName;

    // Creating RecyclerView.
    RecyclerView recyclerView;

    // Creating RecyclerView.Adapter.
    RecyclerView.Adapter adapter ;

    // Creating Progress dialog
    ProgressDialog progressDialog;

    // Creating List of ImageUploadInfo class.
    List<Cow> list = new ArrayList<>();
    List<User> getUserDetails = new ArrayList<>();
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            Intent intent = new Intent(HomePage.this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }
        else if(id == R.id.about){
            Intent intent = new Intent(HomePage.this, About.class);
            startActivity(intent);
            return true;

        }else if(id == R.id.logout){
            logout();
            return true;
        }else if(id == R.id.passUpdate){
            ResetPassword();
            return true;
        }else if(id ==R.id.registerCow){
            Intent intent = new Intent(HomePage.this, RegisterCow.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(HomePage.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void ResetPassword(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        final String email = user.getEmail();
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to reset your password?\nReset Instructions will be sent to this email address "+ email);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog

                progressBar.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(HomePage.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                    logout();
                                } else {
                                    Toast.makeText(HomePage.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        });

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        //return true;
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            //registerCow = findViewById(R.id.registerCow);
            welcomeUser = findViewById(R.id.welcomeUser);
            numCattle = findViewById(R.id.numOfCattle);
            //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


            String fullEmail = user.getEmail();

            String[] splitEmail = fullEmail.split("@");
            final String email = splitEmail[0];

            // Assign id to RecyclerView.
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

            // Setting RecyclerView size true.
            recyclerView.setHasFixedSize(true);

            // Setting RecyclerView layout as LinearLayout.
            recyclerView.setLayoutManager(new LinearLayoutManager(HomePage.this));

            // Assign activity this to progress dialog.
            progressDialog = new ProgressDialog(HomePage.this);

            // Setting up message in Progress dialog.
            progressDialog.setMessage("Loading Images From Firebase.");

            // Showing progress dialog.
            progressDialog.show();

            //String path to username
            String toUsername = "users/"+email;
            getUserName = FirebaseDatabase.getInstance().getReference(toUsername);
            getUserName.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User current = dataSnapshot.getValue(User.class);
                    getUserDetails.add(current);
                    User me = getUserDetails.get(0);
                    welcomeUser.setText("");
                    welcomeUser.append("Welcome "+me.getUsername());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            // Setting up Firebase image upload folder path in databaseReference.
            // The path is already defined in MainActivity.
            //

            String toDatabase = RegisterCow.Database_Path+"/"+email;
            System.out.println(toDatabase);
            databaseReference = FirebaseDatabase.getInstance().getReference(toDatabase);

            // Adding Add Value Event Listener to databaseReference.
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    int number = 0;
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                        Cow imageUploadInfo = postSnapshot.getValue(Cow.class);

                        list.add(imageUploadInfo);


                    }
                    number = (int) snapshot.getChildrenCount();
                    numCattle.setText("");
                    numCattle.setText(number+" Cattles");
                    adapter = new RecyclerViewAdapter(getApplicationContext(), list);
                    recyclerView.setAdapter(adapter);

                    // Hiding the progress dialog.
                    progressDialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    // Hiding the progress dialog.
                    progressDialog.dismiss();

                }
            });
        } else {
            Intent i = new Intent(HomePage.this,LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }


    }

}