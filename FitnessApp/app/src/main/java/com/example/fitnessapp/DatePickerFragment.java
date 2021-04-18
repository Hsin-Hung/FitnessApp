package com.example.fitnessapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.google.android.gms.common.util.DataUtils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

    OnDatePickListener onDatePickListener;
    final Calendar c;
    int year, month, day;

    public DatePickerFragment(OnDatePickListener onDatePickListener){

        this.onDatePickListener = onDatePickListener;
        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
    }

    public interface OnDatePickListener{

        public void setDate(int year, int month, int day);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        dialog.getDatePicker().setMinDate(c.getTimeInMillis());
        c.add(Calendar.YEAR,1);
        dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        onDatePickListener.setDate(year,month,day);
        this.year = year;
        this.month = month;
        this.day = day;
    }


}