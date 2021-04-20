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
import java.util.List;

import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.ParticipantModel;

public class LeaderBoardParticipantLVAdapter extends ArrayAdapter<ParticipantModel> {


    public LeaderBoardParticipantLVAdapter(@NonNull Context context, ArrayList<ParticipantModel> participantModelArrayList) {
        super(context, 0, participantModelArrayList);
    }

    @NonNull
    @Override

    //this shows the components/lists that would go inside of leaderboard for ranking
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.participant_lb_item, parent, false);
        }

        ParticipantModel participantModel = getItem(position);
        TextView name = listitemView.findViewById(R.id.par_name_lb_tv),
                stats = listitemView.findViewById(R.id.par_stats_tv);

        name.setText(participantModel.getName());
        if(participantModel.getType()== ChallengeType.DISTANCE){
            stats.setText(String.valueOf(participantModel.getDistance()));
        }else if(participantModel.getType()==ChallengeType.WEIGHTLOSS){
            stats.setText(String.valueOf(participantModel.getWeight()));
        }

        return listitemView;


    }
}
