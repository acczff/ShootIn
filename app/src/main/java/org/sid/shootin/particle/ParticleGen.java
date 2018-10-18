package org.sid.shootin.particle;


import org.sid.shootin.Vec2;

import java.util.ArrayList;
import java.util.Random;

public class ParticleGen {

    public static ArrayList<Part> Gen(float r,float min_r,float max_r,int count)
    {
        ArrayList<Part> ps = new ArrayList<>();
        for(int i = 0;i < count;++i)
        {
            float x = new Random().nextFloat() * (r * 2.f) - r;
            float y = new Random().nextFloat() * (r * 2.f) - r;

            float p_r = (new Random().nextFloat() * (max_r - min_r)) + min_r;

            double d = Math.sqrt(x*x + y * y) ;
            if(Math.abs( d - (r - p_r) ) <= 0.001)
            {
                ps.add(new Part(new Vec2(x,y),new Vec2(x ,y ),p_r));
            }
            if(d < r - p_r)
            {
                ps.add(new Part(new Vec2(x,y),new Vec2(x ,y ),p_r));
            }
        }
        return ps;
    }
    public static ArrayList<Part> Gen(float w,float h,float min_r,float max_r,int count)
    {
        float half_w = w / 2.f;
        float half_h = h / 2.f;
        ArrayList<Part> ps = new ArrayList<>();
        for(int i = 0;i < count;++i) {
            float x = new Random().nextFloat() * (w) - half_w;
            float y = new Random().nextFloat() * (h) - half_h;

            float p_r = (new Random().nextFloat() * (max_r - min_r)) + min_r;

            ps.add(new Part(new Vec2(x,y),new Vec2(x,y),p_r));
        }
        return ps;
    }
}
