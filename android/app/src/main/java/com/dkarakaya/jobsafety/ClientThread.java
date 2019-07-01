package com.dkarakaya.jobsafety;

public class ClientThread implements Runnable {
    private static final ClientThread ourInstance = new ClientThread(true);
    private ChangeListener listener;

    Client client = null;
    public String msg;
    private String response = "";

    public void setResponse(String response) {
        this.response = response;

        if (this.listener != null) this.listener.onChange();
    }

    public String getResponse() {
        return response;
    }

    String finalMessage = "-ACK";
    String ping = "clr";

    private volatile boolean awake;

    public static ClientThread getInstance() {
        return ourInstance;
    }

    private ClientThread(boolean awake) {
        this.awake = awake;
    }

    @Override
    public void run() {

        System.out.println("Thread has started");
        String oldMsg = "";

        try {
            if (client == null || client.getSocket().isClosed())
                client = Client.getInstance();
            while (awake) {
                if (!msg.trim().equals(oldMsg)) {
                    System.out.println(msg);
                    client.command(msg);
                    setResponse(client.recv(msg));
                    oldMsg = msg;

                } else if (!msg.equals("clr") && !getResponse().contains(finalMessage)) {
                    System.out.println(msg);
                    Thread.sleep(500);
                    client.command(ping);
                    setResponse(client.recv(msg));
                }
            }
            response = client.closeUp(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Thread has ended");
    }

    public void setListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChange();
    }
}
