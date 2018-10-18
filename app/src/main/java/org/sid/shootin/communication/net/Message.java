package org.sid.shootin.communication.net;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class Message implements Serializable {
    private int len;
    private byte type;
    private byte[] content;
    public static final byte
            TYPE_STREAM = 2,
            TYPE_STRING = 1,
            TYPE_NOTHING = -1;

    public Message() {
    }

    public static Message createMessage(byte type, byte[] content, int len) {
        Message message = new Message();
        message.type = type;
        switch (message.type) {
            case TYPE_STREAM:
                message.len = len;
                break;
            case TYPE_STRING:
                if (content == null)
                    content = new byte[0];
                message.len = content.length;
                message.content = content;
                break;
            case TYPE_NOTHING:
                message.content = null;
                message.len = -1;
                break;
        }
        return message;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public static Message readMessage(InputStream inputStream) throws IOException {
        byte[] bs = new byte[4];
        if (inputStream.read(bs) < 4) {
            return Message.createMessage(Message.TYPE_NOTHING, new byte[0], 0);
        }
        int len = Util.ByteArrT2Int(bs);
        byte[] content = new byte[len];
        byte[] typebuf = new byte[1];

        byte type = Message.TYPE_NOTHING;
        if (inputStream.read(typebuf) > 0)
            type = typebuf[0];
        int count = 0;
        while (count < len) {
            count += inputStream.read(content, count, len - count);
        }
        Log.e("red=================>",new String(content));
        return Message.createMessage(type, content, content.length);
    }

    public static void writeMessage(OutputStream outputStream, Message message) throws IOException {
        int count = message.len;
        byte[] length = Util.IntToByteArr(count);
        outputStream.write(length);
        outputStream.write(message.getType());
        outputStream.write(message.getContent());
        Log.e("write=================>",new String(message.getContent()));
    }
}
