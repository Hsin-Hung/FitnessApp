package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

public class CreateChallengePresetActivity extends AppCompatActivity {

    EditText roomNameET, challDescrET, endDateET, betAmountET, passwordET;
    Spinner challTypeSpinner;
    RadioButton betRBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge_preset);
    }


}