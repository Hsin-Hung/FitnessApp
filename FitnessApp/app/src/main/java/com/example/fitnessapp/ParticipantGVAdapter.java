package com.example.fitnessapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import fitnessapp_objects.ParticipantModel;

public class ParticipantGVAdapter extends ArrayAdapter<ParticipantModel> {


    public ParticipantGVAdapter(@NonNull Context context, ArrayList<ParticipantModel> courseModelArrayList) {
        super(context, 0, courseModelArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.participant_item, parent, false);
        }
        ParticipantModel participantModel = getItem(position);
        TextView participantTV = listitemView.findViewById(R.id.participant_name_tv);
        participantTV.setText(participantModel.getName());
        return listitemView;
    }
}
