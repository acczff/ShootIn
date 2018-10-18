package org.sid.shootin.particle;

import org.sid.shootin.Vec2;

public class Part {
    Vec2 pos;
    Vec2 v;
    float r;
    int a;

    public Part(Vec2 pos, Vec2 v,float r) {
        this.pos = pos;
        this.v = v;
        this.a = 255;
        this.r = r;
    }

    public Part() {
        pos = new Vec2();
        v = new Vec2();
        this.a = 255;
    }
}
