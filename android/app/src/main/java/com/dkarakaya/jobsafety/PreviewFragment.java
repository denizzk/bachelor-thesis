package com.dkarakaya.jobsafety;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;

public class PreviewFragment extends Fragment {

    Thread thread = null;
    ClientThread clientThread = null;
    Handler mHandler = new Handler(Looper.getMainLooper());

    ImageView imgViewCamera;

    int[] colors;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, container, false);

        thread = new Thread(clientThread = ClientThread.getInstance());
        thread.start();

        imgViewCamera = view.findViewById(R.id.imgViewCamera);

        ClientThreadSetListener(imgViewCamera, "cam");

        return view;
    }

    void ClientThreadSetListener(final ImageView imgView, final String message) {
        clientThread.setListener(new ClientThread.ChangeListener() {
            @Override
            public void onChange() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Stuff that updates the UI
                        String response = clientThread.getResponse();
                        if (response.contains("CAM") && !response.contains("CLR")) {
                            String[] r = response.split(":");
                            if (r.length == 3 && !r[2].equals(null)) {
                                updateFrame(r[2]);
                            }
                        }
                    }
                });
            }
        });
        clientThread.msg = message;
    }

    void updateFrame(String responseFrame) {
        Gson converter = new Gson();
        Bitmap bitmap;
        Integer[][][] pixels = converter.fromJson(responseFrame,
                Integer[][][].class);
        colors = new int[pixels.length * pixels[0].length];
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[i].length; j++) {
                colors[i * pixels.length + j] = Color.rgb(pixels[i][j][0],
                        pixels[i][j][1], pixels[i][j][2]);
            }
        }
        if (colors != null) {
            int size = (int) Math.sqrt(colors.length);
            bitmap = Bitmap.createBitmap(colors, size, size, Bitmap.Config.RGB_565);
            imgViewCamera.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        thread = new Thread(clientThread = ClientThread.getInstance());

        ClientThreadSetListener(null, "stp");
    }
}