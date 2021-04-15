package com.example.fitnessapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.stripe.android.model.Card;

import java.util.Map;

public class PasswordDialogFragment extends DialogFragment {

    String roomID, password;
    Map<String, String> roomInfo;

    public interface PasswordDialogListener{

        public void onDialogPositiveClick(Map<String,String> roomInfo, boolean success);

    }

    PasswordDialogListener listener;

    public PasswordDialogFragment(String roomID, String password, Map<String,String> roomInfo) {

        this.roomID = roomID;
        this.password = password;
        this.roomInfo = roomInfo;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (PasswordDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(this.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.password_dialog, null));

        builder.setPositiveButton(R.string.join, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                EditText passwordET = (EditText) getDialog().findViewById(R.id.pass_card_et);

                listener.onDialogPositiveClick(roomInfo, passwordET.getText().toString().equals(password));

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });


        return builder.create();
    }
}
