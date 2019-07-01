package com.dkarakaya.jobsafety;

import android.os.StrictMode;

import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_IP = "192.168.1.2";
    private static Client ourInstance = new Client();

    static Socket socket;
    public Socket getSocket(){return socket;}
    DataOutputStream output;
    BufferedReader input;
    int value = 0;
    String line;

    public static Client getInstance() {
        if(socket.isClosed()){
            ourInstance = new Client();
        }
        return ourInstance;
    }

    private Client() {
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
                    .detectDiskWrites().detectNetwork().penaltyLog().build());

            socket = new Socket(SERVER_IP, 12345);
            output = new DataOutputStream(socket.getOutputStream());
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void command(String msg) throws IOException {
        if (msg.toUpperCase().equals("DCN") || msg.toUpperCase().equals("STP")){
            closeUp(msg);
        }
        else
            write(output, msg.toUpperCase());

    }

    public String recv(String msg) throws IOException {
        System.out.println(line = input.readLine());
        return line;
    }

    private void write(DataOutputStream output, String message) {
        System.out.println("Sending: " + message);
        try {
            output.writeUTF(message.toUpperCase());
            System.out.println("Sent: " + message);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String closeUp(String msg) throws IOException {
        write(output, msg);
        System.out.println("Closing socket.");
        input.close();
        output.flush();
        output.close();
        socket.close();
        return "Stopped.";
    }
}