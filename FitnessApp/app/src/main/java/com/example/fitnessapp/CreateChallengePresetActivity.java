package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import java.util.Calendar;

public class CreateChallengePresetActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DatePickerFragment.OnDatePickListener {

    EditText roomNameET, challDescrET, betAmountET, passwordET;
    Button pickDateBTN;
    Spinner challTypeSpinner;
    RadioButton betRBTN;
    DialogFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge_preset);

        roomNameET = (EditText) findViewById(R.id.room_name_et);
        challDescrET = (EditText) findViewById(R.id.chall_descr_et);
        betAmountET = (EditText) findViewById(R.id.bet_amount_et);
        passwordET = (EditText) findViewById(R.id.room_pass_et);

        challTypeSpinner = (Spinner) findViewById(R.id.chall_type_spinner);
        betRBTN = (RadioButton) findViewById(R.id.bet_rbtn);
        pickDateBTN = (Button) findViewById(R.id.pick_date_btn);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.challenge_types, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        challTypeSpinner.setAdapter(adapter);
        challTypeSpinner.setOnItemSelectedListener(this);
        setInitDate();


    }

    private void setInitDate(){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        pickDateBTN.setText(year +"/"+ (month+1) +"/"+ (day+1));
        dialogFragment = new DatePickerFragment(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void showDatePickerDialog(View v) {
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void setDate(int year, int month, int day) {
        String dateString = year +"/"+ month +"/"+ day;
        pickDateBTN.setText(dateString);
    }
}