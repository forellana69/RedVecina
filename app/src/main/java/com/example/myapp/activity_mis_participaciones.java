package com.example.myapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class activity_mis_participaciones extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mis_participaciones);

        NavigationHelper.configurarNavegacion(this);
    }
}