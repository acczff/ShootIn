package org.sid.shootin.communication.net;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.sid.shootin.tools.Looger;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class Room {
    public static final int
            ROOM_FLAG_CREATE = 2,
            ROOM_FLAG_JOIN = 4,
            ROOM_FLAG_N = 0;
    private String roomName;
    private static Room instance;
    private ChildInfo me;
    private List<ChildInfo> outhers;
    private ServerSession serverSession;
    private ChildSession childSession;
    private boolean isClose = true;
    private final int flag;
    private OnAddChildLin onAddChildLin;
    private Thread accpetThread;

    private Room(int flag) {
        this.flag = flag;
        outhers = new ArrayList<>();
        onAddChildLin = null;
    }

    public static Room getInstance() {
        return instance;
    }

    public static Room createNewRoom(String roomName, String playerName, int port) {
        synchronized (Room.class) {
            if (instance != null && !instance.isClose())
                throw new RuntimeException("The room is not closed");
            instance = new Room(ROOM_FLAG_CREATE);
        }
        instance.roomName = roomName;
        instance.serverSession = new ServerSession(port);
        ChildInfo childInfo = new ChildInfo();
        childInfo.name = playerName;
        instance.setMe(childInfo);
        return instance;
    }

    public static Room joinNewRoom(String playerName, String ip, int port) {
        synchronized (Room.class) {
            if (instance != null && !instance.isClose())
                throw new RuntimeException("The room is not closed");
            instance = new Room(ROOM_FLAG_JOIN);
        }
        instance.childSession = new ChildSession(ip, port);
        ChildInfo childInfo = new ChildInfo();
        childInfo.name = playerName;
        instance.setMe(childInfo);
        return instance;
    }

    public Session getSession() {
        if (this.flag == ROOM_FLAG_CREATE)
            return serverSession;
        else if (this.flag == ROOM_FLAG_JOIN)
            return childSession;
        else return null;
    }

    /**
     * 开始等待
     */
    public void accept() {
        if (accpetThread != null) {
            close();
            throw new RuntimeException("上一个动作未结束！");
        }
        switch (flag) {
            case ROOM_FLAG_CREATE:
                acceptChild();
                break;
            case ROOM_FLAG_JOIN:
                acceptServer();
        }
        this.isClose = false;
    }

    /**
     * 等待服务器响应
     */
    private void acceptServer() {
        (accpetThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looger.e("start.accept.server");
                    Socket socket = childSession.linkServer().getChildSocket();
                    socket.setSoTimeout(2000);
                    byte[] rep = ("{\"server\":\"ok\",\"roomName\":\"" + roomName + "\",\"playerName\":\"" + getMe().name + "\"}").getBytes();
                    Message respoMessage = Message.createMessage(Message.TYPE_STRING, rep, rep.length);
                    Message.writeMessage(socket.getOutputStream(), respoMessage);

                    Message message = Message.readMessage(socket.getInputStream());
                    socket.setSoTimeout(0);
                    if (message == null || message.getType() != Message.TYPE_STRING) {
                        if (onAddChildLin != null)
                            onAddChildLin.onAdd(null);
                        return;
                    }
                    byte[] contents;
                    String content = new String((contents = message.getContent()) == null ? new byte[0] : contents);
                    try {
                        JSONObject jsonObject = new JSONObject(content);
                        roomName = jsonObject.getString("roomName");
                        ChildInfo childInfo = new ChildInfo();
                        childInfo.name = jsonObject.getString("playerName");
                        getOuthers().add(childInfo);

                        if (onAddChildLin != null)
                            onAddChildLin.onAdd(childInfo);

                    } catch (JSONException e) {
                        socket.close();
                        serverSession.close();
                    }
                } catch (SocketTimeoutException e) {
                    if (onAddChildLin != null)
                        onAddChildLin.onAdd(null);
                    Looger.e("time out :" + e.getMessage() + "");
                } catch (IOException e) {
                    if (onAddChildLin != null)
                        onAddChildLin.onAdd(null);
                } finally {
                    accpetThread = null;
                }
            }
        })).start();
    }

    private void acceptChild() {
        (accpetThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = serverSession.waitChild();
                    socket.setSoTimeout(2000);
                    Message message = Message.readMessage(socket.getInputStream());
                    socket.setSoTimeout(0);
                    if (message == null || message.getType() != Message.TYPE_STRING)
                        if (onAddChildLin != null)
                            onAddChildLin.onAdd(null);
                    byte[] contents;
                    String content = new String((contents = message.getContent()) == null ? new byte[0] : contents);
                    try {
                        JSONObject jsonObject = new JSONObject(content);
                        ChildInfo childInfo = new ChildInfo();
                        childInfo.name = jsonObject.getString("playerName");
                        getOuthers().add(childInfo);

                        byte[] rep = ("{\"server\":\"ok\",\"roomName\":\"" + roomName + "\",\"playerName\":\"" + getMe().name + "\"}").getBytes();
                        Message respoMessage = Message.createMessage(Message.TYPE_STRING, rep, rep.length);
                        Message.writeMessage(socket.getOutputStream(), respoMessage);
                        if (onAddChildLin != null)
                            onAddChildLin.onAdd(childInfo);
                    } catch (JSONException e) {
                        socket.close();
                        serverSession.close();
                    }
                } catch (IOException e) {
                    Looger.ec(getClass(), "socket is closed");
                } finally {
                    accpetThread = null;
                }
            }
        })).start();
    }

    public ChildInfo getMe() {
        return me;
    }

    public void setMe(ChildInfo me) {
        this.me = me;
    }

    public List<ChildInfo> getOuthers() {
        return outhers;
    }

    public int getFlag() {
        return flag;
    }

    public boolean isClose() {
        return isClose;
    }

    //是否等待完成
    public boolean isAccpetOk() {
        return accpetThread == null;
    }

    public void close() {
        if (isClose)
            return;
        Session session = getSession();
        try {

            if (session != null)
                session.close();
        } catch (Exception e) {
            Looger.e("session is close");
        }
        if (accpetThread != null)
            accpetThread.interrupt();
        accpetThread = null;
        this.isClose = true;
    }

    public static class ChildInfo implements Serializable {
        public String name;
        public String addrs;
    }

    public void setOnAddChildLin(OnAddChildLin onAddChildLin) {
        this.onAddChildLin = onAddChildLin;
    }

    public interface OnAddChildLin {
        public void onAdd(ChildInfo childInfo);
    }

}
