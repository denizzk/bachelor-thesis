package com.dkarakaya.jobsafety;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    Thread thread = null;
    ClientThread clientThread = null;
    Handler mHandler = new Handler(Looper.getMainLooper());

    Button buttonRun, buttonDelete;
    TextView textViewConn, textViewResult;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        thread = new Thread(clientThread = ClientThread.getInstance());

        textViewConn = view.findViewById(R.id.txtConnection);
        textViewResult = view.findViewById(R.id.txtResult);

        buttonRun = view.findViewById(R.id.buttonRun);
        buttonRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!thread.isAlive()){
                    thread = new Thread(clientThread = ClientThread.getInstance());
                    thread.start();
                }
                ClientThreadSetListener("run");
            }
        });

        buttonDelete = view.findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!thread.isAlive()){
                    thread = new Thread(clientThread = ClientThread.getInstance());
                    thread.start();
                }
                openDeleteTrainingDialog();
            }
        });
        return view;
    }

    void ClientThreadSetListener(final String message) {
        clientThread.setListener(new ClientThread.ChangeListener() {
            @Override
            public void onChange() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Stuff that updates the UI
                        String[] r = clientThread.getResponse().split(":");
                        if (r[0].contains("RUN")) {

                            if (r[1].contains("Class 0")) {
                                textViewResult.setText("Running...");
                            } else if (r[1].contains("Class 1")) {
                                textViewResult.setText("Running...");
                            } else textViewResult.setText(r[1]);
                            if (r[0].contains("STP-ACK")) {
                                textViewResult.setText("Ready");
                            }
                        }
                    }
                });
            }
        });
        clientThread.msg = message;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        thread = new Thread(clientThread = ClientThread.getInstance());

        ClientThreadSetListener("stp");
    }

    public void openDeleteTrainingDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_delete_training, null);

        builder.setView(view)
                .setTitle("Do you want to delete all training data?")
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClientThreadSetListener("rmv");
                        SharedPreferences.Editor editor = getActivity().getPreferences(Context
                                .MODE_PRIVATE).edit();
                        editor.remove("req");
                        editor.remove("rne");
                        editor.commit();
                    }
                });
        builder.show();
    }
}