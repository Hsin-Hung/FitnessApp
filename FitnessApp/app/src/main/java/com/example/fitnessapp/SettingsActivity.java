package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    SwitchCompat color_switch;
    Button save_btn;
    EditText height_edt, weight_edt;
    boolean colorState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        color_switch = (SwitchCompat) findViewById(R.id.color_switch);
        save_btn = (Button)findViewById(R.id.save_btn);
        height_edt = (EditText) findViewById(R.id.height_edt);
        weight_edt = (EditText) findViewById(R.id.weight_edt);

        SharedPreferences preferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        colorState = preferences.getBoolean("color", false);
        color_switch.setChecked(colorState);

        height_edt.setText(preferences.getString("height",null));
        weight_edt.setText(preferences.getString("weight",null));

        color_switch.setOnClickListener(v -> {
            colorState = !colorState;
            color_switch.setChecked(colorState);
            editor.putBoolean("color", colorState);
            editor.apply();
        });

        save_btn.setOnClickListener(v -> {
            editor.putString("height", height_edt.getText().toString());
            editor.putString("weight", weight_edt.getText().toString());
            editor.apply();
            onBackPressed();
        });
    }


}