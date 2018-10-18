package org.sid.shootin.particle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;

import org.sid.shootin.Vec2;

import java.util.ArrayList;

public class ParticleSys implements Particleable{
    private double sin_n;
    private double MAX_SIN_N = Math.PI;
    private double MIN_SIN_N = Math.PI / 2.0;
    private int color;
    private Paint paint;
    private float x,y;
    private double zl = 0.01;
    private ArrayList<Part> myPs;

    public ParticleSys(@NonNull ArrayList<Part> ps, int c, float x, float y)
    {
        myPs = new ArrayList<>();
        for(Part p : ps){
            myPs.add(new Part(new Vec2(p.pos.x,p.pos.y),new Vec2(p.v.x,p.v.y),p.r));
        }

        this.x = x;
        this.y = y;
        color = c;
        sin_n = MIN_SIN_N;
        paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
    }
    @Override
    public boolean isOver() {
        return sin_n >= MAX_SIN_N;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(x,y);
        for(Part p : myPs)
        {
            paint.setAlpha(p.a);
            canvas.drawCircle(p.pos.x,p.pos.y,p.r,paint);
        }
        canvas.restore();
    }

    @Override
    public void update(float delatiem) {
        for(Part p : myPs)
        {
            p.pos.x += p.v.x * delatiem * 0.01f * Math.sin(sin_n);
            p.pos.y += p.v.y * delatiem * 0.01f * Math.sin(sin_n);

            p.r -= delatiem * 0.005f * Math.sin(sin_n);
        }
        sin_n += zl;
    }
}
