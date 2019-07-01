package com.dkarakaya.jobsafety;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;

    Thread thread = null;
    ClientThread clientThread = null;
    TextView textViewConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        textViewConnection = findViewById(R.id.textViewConnection);

        thread = new Thread(clientThread = ClientThread.getInstance());
        thread.start();
        clientThread.setListener(new ClientThread.ChangeListener() {
            @Override
            public void onChange() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Stuff that updates the UI
                        String connStr = clientThread.getResponse();
                        try {
                            Thread.sleep(1000);
                            if (connStr.contains("CON-ACK"))
                                textViewConnection.setText("Succesfully Connected");
                            toMainAcitivty(thread);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        clientThread.msg = "con";


    }


    protected void toMainAcitivty(final Thread thread) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
