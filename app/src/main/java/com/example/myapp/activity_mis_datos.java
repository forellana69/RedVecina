package com.example.myapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class activity_mis_datos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mis_datos);

        NavigationHelper.configurarNavegacion(this);
    }
}