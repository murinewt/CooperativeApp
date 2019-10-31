package com.example.dairyfarmer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class About extends AppCompatActivity {

    TextView aboutMe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        aboutMe = findViewById(R.id.aboutMe);
        aboutMe.append("\nMade by\nNewton Murithi Mwirigi");
    }
}
