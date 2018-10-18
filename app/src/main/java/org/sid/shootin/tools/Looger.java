package org.sid.shootin.tools;

import android.util.Log;

public class Looger {
    public static String TAG = "<-" + Looger.class.getName() + "->";
    public static final int
            FLAG_I = 1,
            FLAG_E = 2,
            FLAG_V = 4,
            FLAG_D = 8;
    public static boolean DEBUG = true;

    public static void ec(Class cs, Object str) {
        print(FLAG_E, cs.getName(), String.valueOf(str));
    }

    public static void vc(Class cs, Object str) {
        print(FLAG_V, cs.getName(), String.valueOf(str));
    }

    public static void ic(Class cs, Object str) {
        print(FLAG_I, cs.getName(), String.valueOf(str));
    }

    public static void dc(Class cs, Object str) {
        print(FLAG_D, cs.getName(), String.valueOf(str));
    }

    public static void e(Object str) {
        print(FLAG_E, TAG, String.valueOf(str));
    }

    public static void i(Object str) {
        print(FLAG_I, TAG, String.valueOf(str));
    }

    public static void v(Object str) {
        print(FLAG_V, TAG, String.valueOf(str));
    }

    public static void d(Object str) {
        print(FLAG_D, TAG, String.valueOf(str));
    }

    public synchronized static void print(int flag, String tag, String str) {
        if (!DEBUG)
            return;
        if (exisFlag(flag, FLAG_D)) {
            Log.d(tag, str);
        }
        if (exisFlag(flag, FLAG_E)) {
            Log.e(tag, str);
        }
        if (exisFlag(flag, FLAG_V)) {
            Log.v(tag, str);
        }
        if (exisFlag(flag, FLAG_I)) {
            Log.i(tag, str);
        }
    }

    public static boolean exisFlag(int flag, int f) {
        return (flag & f) == f;
    }

    public static int addFlags(int... flag) {
        int f = 0;
        for (int fl : flag) {
            f |= fl;
        }
        return f;
    }
}

