package com.dkarakaya.jobsafety;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ConnectDialog extends AppCompatDialogFragment {

    private EditText editTextIp;
    private ConnectDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view= inflater.inflate(R.layout.dialog_connect, null);

        builder.setView(view)
                .setTitle(" ")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    String ip=editTextIp.getText().toString();
                    listener.applyText(ip);
                    }
                });
        editTextIp=view.findViewById(R.id.editIp);

    return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener= (ConnectDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"must implement ConnectDialogListener");
        }
    }

    public interface ConnectDialogListener{
        void applyText(String ip);
    }
}
