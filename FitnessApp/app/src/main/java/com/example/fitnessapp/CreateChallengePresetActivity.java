package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.example.fitnessapp.DatePickerFragment.OnDatePickListener;

import org.w3c.dom.Text;

import java.sql.Timestamp;
import java.util.Calendar;

public class CreateChallengePresetActivity extends AppCompatActivity implements OnItemSelectedListener, OnDatePickListener {

    EditText roomNameET, challDescrET, betAmountET, passwordET;
    Button pickDateBTN;
    Spinner challTypeSpinner;
    Switch betSwitch;
    DialogFragment dialogFragment;
    TextView errorTV;

    Timestamp endDateTimestamp;
    int challTypePicked = 0;
    boolean isBet = false;
    final int PASSWORD_MIN_LEN = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge_preset);

        roomNameET = (EditText) findViewById(R.id.room_name_et);
        challDescrET = (EditText) findViewById(R.id.chall_descr_et);
        betAmountET = (EditText) findViewById(R.id.bet_amount_et);
        passwordET = (EditText) findViewById(R.id.room_pass_et);

        challTypeSpinner = (Spinner) findViewById(R.id.chall_type_spinner);
        betSwitch = (Switch) findViewById(R.id.bet_switch);
        pickDateBTN = (Button) findViewById(R.id.pick_date_btn);
        errorTV = (TextView) findViewById(R.id.create_chall_error_tv);


        challTypePicked = 0;
        betAmountET.setEnabled(false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.challenge_types, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        challTypeSpinner.setAdapter(adapter);
        challTypeSpinner.setOnItemSelectedListener(this);
        setInitDate();


        betSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isBet = isChecked;
                if(isChecked){
                    betAmountET.setEnabled(true);
                }else{
                    betAmountET.setEnabled(false);
                }
            }
        });

    }

    private void setInitDate(){
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        pickDateBTN.setText(year +"/"+ (month+1) +"/"+ day);
        endDateTimestamp = new Timestamp(c.getTimeInMillis());
        dialogFragment = new DatePickerFragment(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        challTypePicked = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        challTypePicked = 0;
    }

    public void showDatePickerDialog(View v) {
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void setDate(int year, int month, int day) {
        String dateString = year +"/"+ (month+1) +"/"+ day;
        pickDateBTN.setText(dateString);
        Calendar c = Calendar.getInstance();
        c.set(year, month, day,0,0);
        endDateTimestamp.setTime(c.getTimeInMillis());
        System.out.println(endDateTimestamp.getTime());
    }


    public boolean checkAllFieldValid(){

        if(roomNameET.getText().toString().isEmpty()){
            errorTV.setText("Room name cannot be empty !");
        }else if(!(challTypePicked>=0 && challTypePicked<=2)){
            errorTV.setText("Need to pick a challenge type !");
        }else if(endDateTimestamp==null){
            errorTV.setText("Need to pick a challenge end date !");
        }else if(betSwitch.isChecked() && betAmountET.getText().toString().isEmpty()){
            errorTV.setText("Need to enter a bet amount !");
        }else if(passwordET.getText().toString().length()<PASSWORD_MIN_LEN){
            errorTV.setText("Password too short, need to be at least 6 characters !");
        }else{
            return true;
        }


        return false;

    }

    public void createChallenge(View view){

        if(checkAllFieldValid()){


        }

    }
}