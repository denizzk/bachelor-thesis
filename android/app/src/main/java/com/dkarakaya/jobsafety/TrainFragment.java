package com.dkarakaya.jobsafety;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TrainFragment extends Fragment {

    Thread thread = null;
    ClientThread clientThread = null;
    Handler mHandler = new Handler(Looper.getMainLooper());

    Button buttonRecordEquipment, buttonRecordNoEquipment, buttonTrain;
    TextView textViewResponse, textViewReq, textViewRne;

    String frameCount="";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_train, container, false);

        thread = new Thread(clientThread = ClientThread.getInstance());
        thread.start();

        textViewReq = view.findViewById(R.id.txtReq);
        textViewRne = view.findViewById(R.id.txtRne);
        textViewResponse = view.findViewById(R.id.txtResponse);

        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        textViewReq.setText(pref.getString("req", "0 frames"));
        textViewRne.setText(pref.getString("rne", "0 frames"));

        buttonRecordEquipment = view.findViewById(R.id.buttonRecEq);
        buttonRecordEquipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!thread.isAlive()){
                    thread = new Thread(clientThread = ClientThread.getInstance());
                    thread.start();
                }
                ClientThreadSetListener(textViewReq, "req");

                textViewReq.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.gradient_dark_left));
                buttonRecordEquipment.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.left_rounded_bg_gray));

                textViewRne.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.gradient));
                buttonRecordNoEquipment.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.right_rounded_bg));
            }
        });

        buttonRecordNoEquipment = view.findViewById(R.id.buttonRecNoEq);
        buttonRecordNoEquipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientThreadSetListener(textViewRne, "rne");
                if(!thread.isAlive()){
                    thread = new Thread(clientThread = ClientThread.getInstance());
                    thread.start();
                }

                textViewRne.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.gradient_dark));
                buttonRecordNoEquipment.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.right_rounded_bg_gray));

                textViewReq.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.gradient_left));
                buttonRecordEquipment.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.left_rounded_bg));
            }
        });
        buttonTrain = view.findViewById(R.id.buttonTrain);
        buttonTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!thread.isAlive()){
                    thread = new Thread(clientThread = ClientThread.getInstance());
                    thread.start();
                }
                ClientThreadSetListener(null, "trn");
            }
        });

        return view;
    }

    void ClientThreadSetListener(final TextView textView, final String message) {
        clientThread.setListener(new ClientThread.ChangeListener() {
            @Override
            public void onChange() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Stuff that updates the UI
                        String[] r = clientThread.getResponse().split(":");
                        if (r.length == 2) {
                            String response = r[1];
                            textViewResponse.setText(response);
                            if (r[0].contains("-ACK")) {
                                textViewResponse.setText("Ready");
                                if (textView != null){
                                    frameCount=response;
                                    // To save data to SP
                                    SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                                    editor.putString(message, response);
                                    editor.commit();
                                    textView.setText(response);
                                }
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

        ClientThreadSetListener(null, "stp");
    }
}