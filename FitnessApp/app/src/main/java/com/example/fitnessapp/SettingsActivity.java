package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    SwitchCompat color;
    EditText height;
    EditText weight;
    boolean colorstate;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        color = (SwitchCompat) findViewById(R.id.switchColor);
        height = (EditText) findViewById(R.id.edtHeight);
        weight = (EditText) findViewById(R.id.edtWeight);

        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        colorstate = preferences.getBoolean("color", false);

//        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
//        height = (AppCompatEditText) editor.putString("height", height.getText().toString());
//        editor.commit();
//
//        SharedPreferences.Editor editor1 = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
//        weight = (AppCompatEditText) editor1.putString("height", weight.getText().toString());
//        editor1.commit();



        color.setChecked(colorstate);

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorstate = !colorstate;
                color.setChecked(colorstate);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("color", colorstate);
                editor.apply();
            }
        });

    }
}