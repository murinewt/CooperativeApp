package com.example.dairyfarmer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    TextView emailUpdate,usernameUpdate,firstNameUpdate,lastNameUpdate, phoneUpdate,passwordForUpdate;
    Spinner countryUpdate;
    Button updateDetails;
    List<User> getUserDetails = new ArrayList<>();
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference setDetails, myref;
    private FirebaseAuth auth;

    private ProgressBar progressBar;

    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        emailUpdate = findViewById(R.id.emailUpdate);
        usernameUpdate  = findViewById(R.id.usernameUpdate);
        firstNameUpdate =  findViewById(R.id.firstNameUpdate);
        lastNameUpdate = findViewById(R.id.lastNameUpdate);
        countryUpdate = findViewById(R.id.countryUpdate);
        phoneUpdate = findViewById(R.id.phoneUpdate);
        updateDetails = findViewById(R.id.updateDetails);
        passwordForUpdate = findViewById(R.id.passwordForUpdate);
        progressBar = findViewById(R.id.progressBar3);


        progressBar.setVisibility(View.INVISIBLE);
        //get the spinner from the xml.
        countryUpdate = findViewById(R.id.countryUpdate);
        //create a list of items for the spinner.
        String[] countries = new String[]{"Kenya","Afghanistan","Albania","Algeria","Andorra","Angola","Anguilla","Antigua &amp; Barbuda","Argentina","Armenia","Aruba","Australia","Austria","Azerbaijan","Bahamas"
                ,"Bahrain","Bangladesh","Barbados","Belarus","Belgium","Belize","Benin","Bermuda","Bhutan","Bolivia","Bosnia &amp; Herzegovina","Botswana","Brazil","British Virgin Islands"
                ,"Brunei","Bulgaria","Burkina Faso","Burundi","Cambodia","Cameroon","Canada","Cape Verde","Cayman Islands","Chad","Chile","China","Colombia","Congo","Cook Islands","Costa Rica"
                ,"Cote D Ivoire","Croatia","Cruise Ship","Cuba","Cyprus","Czech Republic","Denmark","Djibouti","Dominica","Dominican Republic","Ecuador","Egypt","El Salvador","Equatorial Guinea"
                ,"Estonia","Ethiopia","Falkland Islands","Faroe Islands","Fiji","Finland","France","French Polynesia","French West Indies","Gabon","Gambia","Georgia","Germany","Ghana"
                ,"Gibraltar","Greece","Greenland","Grenada","Guam","Guatemala","Guernsey","Guinea","Guinea Bissau","Guyana","Haiti","Honduras","Hong Kong","Hungary","Iceland","India"
                ,"Indonesia","Iran","Iraq","Ireland","Isle of Man","Israel","Italy","Jamaica","Japan","Jersey","Jordan","Kazakhstan","Kenya","Kuwait","Kyrgyz Republic","Laos","Latvia"
                ,"Lebanon","Lesotho","Liberia","Libya","Liechtenstein","Lithuania","Luxembourg","Macau","Macedonia","Madagascar","Malawi","Malaysia","Maldives","Mali","Malta","Mauritania"
                ,"Mauritius","Mexico","Moldova","Monaco","Mongolia","Montenegro","Montserrat","Morocco","Mozambique","Namibia","Nepal","Netherlands","Netherlands Antilles","New Caledonia"
                ,"New Zealand","Nicaragua","Niger","Nigeria","Norway","Oman","Pakistan","Palestine","Panama","Papua New Guinea","Paraguay","Peru","Philippines","Poland","Portugal"
                ,"Puerto Rico","Qatar","Reunion","Romania","Russia","Rwanda","Saint Pierre &amp; Miquelon","Samoa","San Marino","Satellite","Saudi Arabia","Senegal","Serbia","Seychelles"
                ,"Sierra Leone","Singapore","Slovakia","Slovenia","South Africa","South Korea","Spain","Sri Lanka","St Kitts &amp; Nevis","St Lucia","St Vincent","St. Lucia","Sudan"
                ,"Suriname","Swaziland","Sweden","Switzerland","Syria","Taiwan","Tajikistan","Tanzania","Thailand","Timor L'Este","Togo","Tonga","Trinidad &amp; Tobago","Tunisia"
                ,"Turkey","Turkmenistan","Turks &amp; Caicos","Uganda","Ukraine","United Arab Emirates","United Kingdom","United States","United States Minor Outlying Islands","Uruguay"
                ,"Uzbekistan","Venezuela","Vietnam","Virgin Islands (US)","Yemen","Zambia","Zimbabwe"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries);
        //set the spinners adapter to the previously created one.
        countryUpdate.setAdapter(adapter);


        user = FirebaseAuth.getInstance().getCurrentUser();
        final String fullEmail = user.getEmail();

        String[] splitEmail = fullEmail.split("@");
        final String email = splitEmail[0];

        String toUsername = "users/"+email;
        setDetails = FirebaseDatabase.getInstance().getReference(toUsername);
        setDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                emailUpdate.setText(user.getEmail());
                User current = dataSnapshot.getValue(User.class);
                getUserDetails.add(current);
                User me = getUserDetails.get(0);
                usernameUpdate.setText(me.getUsername());
                firstNameUpdate.setText(me.getFirstName());
                lastNameUpdate.setText(me.getLastName());
                phoneUpdate.setText(me.getPhone());
                setSpinText(countryUpdate,me.getCountry());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
                updateDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String emailID = emailUpdate.getText().toString();
                final String paswd = passwordForUpdate.getText().toString();
                final String firstNameID = firstNameUpdate.getText().toString();
                final String lastNameID = lastNameUpdate.getText().toString();
                final String phoneID = phoneUpdate.getText().toString();
                final String usernameID = usernameUpdate.getText().toString();
                final String countryID = countryUpdate.getSelectedItem().toString();

                boolean qwer = fullEmail.equals(emailID);
                System.out.println("bbbbbbbbbbbbee"+emailID.compareTo(fullEmail)+"eeeeeeeeeeeeeeeeeeeeeeeeee"+fullEmail+"a           b"+emailID+"b     a"+qwer);

                //updateEmail(emailID);
                database = FirebaseDatabase.getInstance();
                myref = database.getReference();
                myref.child("users").child(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                        if (firstNameID.isEmpty()) {
                            firstNameUpdate.setError("Provide your first name first!");
                            firstNameUpdate.requestFocus();
                        }
                        else if (lastNameID.isEmpty()) {
                            lastNameUpdate.setError("Provide your last name first!");
                            lastNameUpdate.requestFocus();
                        }
                        else if (phoneID.isEmpty()) {
                            phoneUpdate.setError("Provide your phone Number first!");
                            phoneUpdate.requestFocus();
                        }
                        else if (usernameID.isEmpty()) {
                            usernameUpdate.setError("Provide your username first!");
                            usernameUpdate.requestFocus();
                        }
                        else if (emailID.isEmpty()) {
                            emailUpdate.setError("Provide your Email first!");
                            emailUpdate.requestFocus();
                        }else if(paswd.isEmpty()){
                            passwordForUpdate.setError("Provide your Email first!");
                            passwordForUpdate.requestFocus();
                        }
                        else if (!(emailID.isEmpty() && paswd.isEmpty() && firstNameID.isEmpty() && lastNameID.isEmpty() && phoneID.isEmpty() && usernameID.isEmpty())) {
                            auth = FirebaseAuth.getInstance();
                            auth.signInWithEmailAndPassword(fullEmail, paswd)
                                .addOnCompleteListener(SettingsActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull final Task<AuthResult> task) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                                builder.setTitle("Confirm");
                                builder.setMessage("Are you sure you want to Update your details?");

                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        progressBar.setVisibility(View.VISIBLE);
                                        if (!task.isSuccessful()) {
                                            // there was an error
                                            if (paswd.length() < 6) {
                                                passwordForUpdate.setError(getString(R.string.minimum_password));
                                            } else {
                                                Toast.makeText(SettingsActivity.this, getString(R.string.login_failed), Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            progressBar.setVisibility(View.VISIBLE);
                                            dataSnapshot.getRef().child("country").setValue(countryID);
                                            dataSnapshot.getRef().child("username").setValue(usernameID);
                                            dataSnapshot.getRef().child("firstName").setValue(firstNameID);
                                            dataSnapshot.getRef().child("lastName").setValue(lastNameID);
                                            dataSnapshot.getRef().child("phone").setValue(phoneID);
                                            dataSnapshot.getRef().child("email").setValue(emailID);
                                            updateEmail(emailID);
                                            if(!fullEmail.equals(emailID)){
                                                myref.child("users").child(email).removeValue();
                                                MainActivity.writeNewUser(usernameID,emailID,firstNameID,lastNameID,phoneID,countryID);
                                                Toast.makeText(getApplicationContext(), "Login with Your new email", Toast.LENGTH_LONG).show();
                                                logout();
                                            }
                                            Intent intent = new Intent(SettingsActivity.this, HomePage.class);
                                            Toast.makeText(getApplicationContext(), "Data Updated Successfully", Toast.LENGTH_LONG).show();

                                            progressBar.setVisibility(View.GONE);
                                            startActivity(intent);
                                        }
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
                        });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("User", databaseError.getMessage());
                    }
                });

            }
        });

    }

    public void updateEmail(String newEmail) {
        // [START update_email]
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updateEmail(newEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User email address updated.");
                        }
                    }
                });
        // [END update_email]
    }
    public static void setSpinText(Spinner spin, String text)
    {
        for(int i= 0; i < spin.getAdapter().getCount(); i++)
        {
            if(spin.getAdapter().getItem(i).toString().contains(text))
            {
                spin.setSelection(i);
            }
        }

    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}