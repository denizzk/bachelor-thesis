package com.dkarakaya.jobsafety;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;

public class DeleteTrainingDialog extends AppCompatDialogFragment {

    private DeleteTrainingDialogListener listener;
    boolean isAccepted = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delete_training, null);

        builder.setView(view)
                .setTitle("Do you want to delete all training data?")
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isAccepted = false;
                        listener.deleteTrainingData(isAccepted);
                    }
                })
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isAccepted = true;
                        listener.deleteTrainingData(isAccepted);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DeleteTrainingDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement " +
                    "DeleteTrainingDialogListener");
        }
    }

    public interface DeleteTrainingDialogListener {
        void deleteTrainingData(boolean isAccepted);
    }
}
