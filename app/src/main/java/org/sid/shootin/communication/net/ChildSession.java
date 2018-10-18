package org.sid.shootin.communication.net;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChildSession extends Session implements Runnable {
    private String targetIp;
    private int targetPort;
    private Socket childSocket;
    private Thread theThred;
    private OutputStream theOutput;
    private final Object lock = new Object();
    private ExecutorService executorService;

    public ChildSession(String targetIp, int targetPort) {
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        theThred = new Thread(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    public ChildSession linkServer() throws IOException {
        if (this.childSocket != null)
            throw new RuntimeException("the session is not closed");
        this.childSocket = new Socket(targetIp, targetPort);
        return this;
    }

    @Override
    public void sendMessage(final Message message) {
        try {
            theOutput = childSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        Message.writeMessage(ChildSession.this.theOutput, message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    boolean isStartRecv = false;

    @Override
    public void startRecv() {
        if (isStartRecv)
            return;
        isStartRecv = true;
        if (this.theThred != null) {
            theThred.interrupt();
            theThred = null;
        }
        theThred = new Thread(this);
        theThred.start();
    }

    @Override
    public void close() throws IOException {
        if (this.childSocket != null)
            if (!this.childSocket.isClosed()) {
                this.childSocket.close();
            }
        isStartRecv = false;
    }

    public Socket getChildSocket() {
        return childSocket;
    }

    public void setSocket(Socket socket) {
        try {
            this.close();
            this.childSocket = socket;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            InputStream theInput = childSocket.getInputStream();
            while (!childSocket.isClosed() && isStartRecv) {
                Message message = Message.readMessage(theInput);
                if (getReceiveLin() != null) {
                    getReceiveLin().onRevc(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
