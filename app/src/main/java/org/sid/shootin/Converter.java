package org.sid.shootin;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class Converter {
    private static Converter self;
    static {
        self = null;
    }
    private int W,H;
    private Converter(int w,int h){
        this.W = w;
        this.H = h;
    }
    public static void init(@NonNull Context context)
    {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        self = new Converter(dm.widthPixels,dm.heightPixels);
    }
    public static Converter getInstance() {
        return self;
    }

    public Vec2 convert(Vec2 v)
    {
        Vec2 res = new Vec2();
        res.x = v.x / (float)W;
        res.y = v.y / (float)H;
        return res;
    }

    public Vec2 deConvert(Vec2 v)
    {
        Vec2 res = new Vec2();
        res.x = v.x * (float)W;
        res.y = v.y * (float)H;
        return res;
    }

    public float convertW(float v)
    {
        return v / (float)W;
    }

    public float deConvertW(float v)
    {
        return v * (float)W;
    }
    public int getW()
    {
        return W;
    }
    public int getH()
    {
        return H;
    }
}
