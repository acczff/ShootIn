package org.sid.shootin;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.sid.shootin.communication.net.Room;
import org.sid.shootin.communication.net.Session;
import org.sid.shootin.database.GameInfoDaoImpl;
import org.sid.shootin.entity.GameInfo;
import org.sid.shootin.particle.Part;
import org.sid.shootin.particle.ParticleGen;
import org.sid.shootin.particle.ParticleSys;
import org.sid.shootin.particle.Particleable;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedList;

public class GameView extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    private static String LT = "GV";
    private static final float BALL_X = 0.1f;

    private static final int TIME_IN_FRAME = 16;

    private static final int A_WHAT = 2;
    private static final int B_WHAT = 3;
    private static final int C_WHAT = 4;
    private static final int D_WHAT = 5;
    private static final int E_WHAT = 6;

    private boolean isRuning;
    private Canvas mCanvas;
    private SurfaceHolder surfaceHolder;

    private int Width;
    private int Height;
    private float bfy30; // 百分之30 高
    private float bfy90; // 百分之90 高
    private float bfx5;  // 百分之5 W
    private float bfx66;  // 百分之66 W
    private float bfy22;  // 百分之22 H
    private float bfx30;
    private float bfy80;
    private float mid_x;
    private float mid_y;
    private float bfy5;
    private float bfy40;
    private Paint paint_line;
    private Paint paint_ball;
    private Vec2 ball_pos;
    private Vec2 bvpos;
    private Vec2 begin_pos;
    private int delatime;
    private long lastTimeTick;
    private MyHandler handler;

    private boolean inThere;
    private boolean isFZ;
    private RectF beginRect;
    private RectF beginRect_pressed;
    private State state = State.Pause;
    private Paint paint_begin;
    private boolean beginIsPressed = false;
    private boolean isHand = false;
    private RectF handRect;
    private Vec2 handPos;
    private Vec2 hvpos;
    private Vec2 lastPos;
    private float ballW; //球的宽度
    private float handW; //handler 的宽度
    private float hw_bwc; // handler 宽度 和 球的 宽度的差
    private float ballWbf60; // 球宽度的百分之60
    private float handMinY;
    private float handMaxY;
    private int score_me = 0;
    private int score_his = 0;
    private Vec2 s_me_pos;
    private Vec2 s_his_pos;
    private float begin_text_size;
    private float score_text_size;
    private Room room;
    private Session recver;

    private RectF againRect;
    private RectF againRect_pressed;
    private boolean isAgainPressed = false;
    private Vec2 againPos;
    private float again_text_size;

    private LinkedList<Particleable> parcelables;
    private ArrayList<Particleable> rm_list;

    private ArrayList<Part> circle_prat;
    private ArrayList<Part> again_part;
    private ArrayList<Part> play_part;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRuning = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRuning = false;
    }

    @Override
    public void run() {
        while (isRuning) {

            long startTime = System.currentTimeMillis();
            try {
                mCanvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    draw();
                }
            }catch (Exception e)
            {}finally {
                if(mCanvas != null)
                    surfaceHolder.unlockCanvasAndPost(mCanvas);
            }
            long endTime = System.currentTimeMillis();

            int diffTime  = (int)(endTime - startTime);
            diffTime = TIME_IN_FRAME - diffTime;
            if (diffTime > 0){
                try { Thread.sleep(diffTime);}catch (Exception e){}
            }
        }
    }

    public enum State{
        Pause,
        Playing,
        Finish,
        Exception
    }

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init()
    {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);

        Width = Converter.getInstance().getW();
        Height = Converter.getInstance().getH();

        room = Room.getInstance();
        if( room.getFlag() == Room.ROOM_FLAG_JOIN)
            isFZ = false;
        else
            isFZ = true;

        inThere = isFZ;

        recver = room.getSession();
        recver.setOnRevc(new Session.OnReceiveLin() {
            @Override
            public void onRevc(org.sid.shootin.communication.net.Message message) {
                if(message.getType() == org.sid.shootin.communication.net.Message.TYPE_NOTHING) {
                    handler.sendEmptyMessage(-1); //
                }else{
                    String s = new String(message.getContent());
                    log("onRecv " + s);
                    switch (s.charAt(0))
                    {
                        case 'A':
                        {
                            String[] ss = s.split("#");
                            Vec2 v = Converter.getInstance().deConvert(new Vec2(Float.parseFloat(ss[1]),Float.parseFloat(ss[2])));
                            float x = Converter.getInstance().deConvertW(Float.parseFloat(ss[3]));
                            Message m = new Message();
                            m.what = A_WHAT;
                            m.obj = new AInfo(v,x);
                            handler.sendMessage(m);
                        }
                            break;
                        case 'B':
                            handler.sendEmptyMessage(B_WHAT);
                            break;
                        case 'C':
                            handler.sendEmptyMessage(C_WHAT);
                            break;
                        case 'D': {
                            String[] ss = s.split("#");
                            DInfo info = new DInfo(Integer.parseInt(ss[1]),
                                    Integer.parseInt(ss[3]),
                                    Integer.parseInt(ss[2]));
                            Message m = new Message();
                            m.what = D_WHAT;
                            m.obj = info;
                            handler.sendMessage(m);
                        }
                            break;
                        case 'E':
                            handler.sendEmptyMessage(E_WHAT);
                            break;
                    }
                }
            }
        });
        recver.startRecv();

        bfy30 = (float)Height * 0.3f;
        bfy90 = (float)Height * 0.9f;
        bfx5 = (float)Width * 0.05f;
        bfx66 = (float)Width * 0.66f;
        bfy22 = (float)Height * 0.22f;
        bfx30 = (float)Width * 0.3f;
        bfy5 = (float)Height * 0.05f;
        bfy80 = (float)Height * 0.8f;
        bfy40 = (float)Height * 0.4f;
        float bx = (Width - bfx66) / 2.f;
        float by = (Height - bfy22) / 2.f;
        mid_x = Width / 2.0f;
        mid_y = Height / 2.0f;
        beginRect = new RectF( bx, by, bx + bfx66 , by + bfy22);
        beginRect_pressed = new RectF(beginRect.left + 5.f , beginRect.top + 5.f ,
                beginRect.right - 5.f , beginRect.bottom - 5.f);


        ball_pos = new Vec2(mid_x,mid_y);

        hvpos = new Vec2();
        lastPos = new Vec2();


        paint_line = new Paint();
        paint_line.setColor(Color.RED);
        paint_line.setStyle(Paint.Style.STROKE);

        paint_ball = new Paint();
        paint_ball.setAntiAlias(true);
        paint_ball.setDither(true);
        paint_ball.setColor(0xFF00aaaa);

        paint_begin = new Paint();
        paint_begin.setTypeface(Typeface.createFromAsset(getContext().getAssets(),"font/ttf2.ttf"));
        paint_begin.setAntiAlias(true);
        paint_begin.setDither(true);
        paint_begin.setColor(0xFF00aaaa);
        paint_begin.setStyle(Paint.Style.FILL);
        paint_begin.setTextAlign(Paint.Align.CENTER);
        begin_text_size = (bfx66 * 0.7f) / 4.0f;
        paint_begin.setTextSize( begin_text_size );
        Paint.FontMetrics fm = paint_begin.getFontMetrics();
        //Log.e(LT, " " + begin_text_size );
        //Log.e(LT,""+ fm.descent + "  " + fm.ascent + "   " + fm.bottom + "  " + fm.top + " " + fm.leading);
        begin_pos = new Vec2(mid_x,mid_y + fm.descent);
        bvpos = new Vec2(1.2f,2.0f);

        handRect = new RectF(-bfx30 / 2.f,-bfy5 / 2.f,bfx30 / 2.f,bfy5 / 2.f );
        handPos = new Vec2(mid_x,bfy80);

        ballW = bfx5 * 2.f;
        handW = bfx30;

        hw_bwc = handW - ballW;

        ballWbf60 = ballW * 0.6f;
        handMinY = bfy30 + handRect.bottom;
        handMaxY = bfy90 - handRect.bottom;

        score_text_size = bfx30;

        paint_begin.setTextSize(score_text_size);
        fm = paint_begin.getFontMetrics();
        Log.e(LT,""+ fm.descent + "  " + fm.ascent + "   " + fm.bottom + "  " + fm.top + " " + fm.leading);
        s_me_pos = new Vec2(Width * 0.25f, bfy40 + fm.descent);
        s_his_pos = new Vec2(Width * 0.75f , bfy40 + fm.descent);

        bx = (Width - bfx30) / 2.f;
        by = (Height - bfy5) / 2.f;
        againRect = new RectF(bx,by,bx + bfx30,by + bfy5);
        againRect_pressed = new RectF(againRect.left + 5,againRect.top + 5, againRect.right - 5 ,againRect.bottom - 5 );


        again_text_size = (bfx30 * 0.7f) / 4.f;
        paint_begin.setTextSize(again_text_size);
        fm = paint_begin.getFontMetrics();
        againPos = new Vec2(mid_x,mid_y + fm.descent);

        circle_prat = ParticleGen.Gen(bfx5,1.0f,6.0f,160);
        again_part = ParticleGen.Gen(bfx30,bfy5,1.f,5.f,100);
        play_part = ParticleGen.Gen(bfx66,bfy22,1.f,5.f,300);

        parcelables = new LinkedList<>();
        rm_list = new ArrayList<>();


        lastTimeTick = System.currentTimeMillis();
        handler = new MyHandler(new SoftReference<GameView>(this));
    }
    private void draw() {
        long now = System.currentTimeMillis();
        delatime = (int)(now - lastTimeTick);

        mCanvas.drawARGB(255,0,0,0);
        switch (state)
        {
            case Playing:
                mCanvas.drawLine(0.f,bfy30,(float)Width,bfy30,paint_line);
                mCanvas.drawLine(0.f,bfy90,(float)Width,bfy90,paint_line);
                if(inThere)
                    drawBall(mCanvas);
                drawHand(mCanvas);
                if(inThere)
                    step(mCanvas);
                break;
            case Pause:
                drawPause(mCanvas);
                break;
            case Finish:
                drawFinish(mCanvas);
                break;
            case Exception:
                drawException(mCanvas);
                break;
        }
        rm_list.clear();
        for(Particleable pa : parcelables)
        {
            if(pa.isOver())
            {
                rm_list.add(pa);
                continue;
            }
            pa.draw(mCanvas);
            pa.update(16);
        }
        for(Particleable pa : rm_list)
        {
            parcelables.remove(pa);
        }
        lastTimeTick = System.currentTimeMillis();
    }

    void drawException(Canvas canvas)
    {
        paint_begin.setTextSize(bfx5);
        canvas.drawText("对方异常退出",mid_x,mid_y,paint_begin);
    }

    void drawFinish(Canvas canvas)
    {
        paint_begin.setTextSize(score_text_size);
        canvas.drawText(""+score_me,s_me_pos.x,s_me_pos.y,paint_begin);
        canvas.drawText(""+score_his,s_his_pos.x,s_his_pos.y,paint_begin);


        paint_begin.setStyle(Paint.Style.STROKE);
        if(isAgainPressed)
            canvas.drawRoundRect(againRect_pressed,20,20,paint_begin);
        else
            canvas.drawRoundRect(againRect,20,20,paint_begin);
        paint_begin.setStyle(Paint.Style.FILL);
        paint_begin.setTextSize(again_text_size);
        canvas.drawText("再来一次",againPos.x,againPos.y ,paint_begin);
    }

    void drawBall(Canvas canvas)
    {
        paint_ball.setStyle(Paint.Style.FILL);
        canvas.drawCircle(ball_pos.x,ball_pos.y,bfx5,paint_ball);
    }

    void drawHand(Canvas canvas)
    {
        int save = canvas.save();

        canvas.translate(handPos.x,handPos.y);
        canvas.drawRoundRect(handRect,10,10,paint_ball);

        canvas.restoreToCount(save);
    }

    void drawPause(Canvas canvas)
    {
        paint_begin.setStyle(Paint.Style.STROKE);
        if(!beginIsPressed)
            canvas.drawRoundRect(beginRect,20,20,paint_begin);
        else
            canvas.drawRoundRect(beginRect_pressed,20,20,paint_begin);
        paint_begin.setStyle(Paint.Style.FILL);
        paint_begin.setTextSize( begin_text_size );
        canvas.drawText("开始游戏",begin_pos.x,begin_pos.y,paint_begin);
    }

    void step(Canvas canvas)
    {
        ball_pos.x += bvpos.x * BALL_X * delatime;
        ball_pos.y += bvpos.y * BALL_X * delatime;



        if(bvpos.x >= 0 && ball_pos.x + bfx5 > Width)
            bvpos.x = -bvpos.x;
        if(bvpos.x <= 0 && ball_pos.x < bfx5)
            bvpos.x = -bvpos.x;
        if(ball_pos.y + bfx5 > Height){
            parcelables.add(new ParticleSys(circle_prat,0xFF00aaaa,ball_pos.x,ball_pos.y));
            score_his++;
            inThere = false;
            sendScoreChange();
            if(score_his == 3)
            {
                GameInfoDaoImpl.getInstace().addNewScore(room.getMe().name,room.getOuthers().get(0).name,score_me,score_his);
                state = State.Finish;
            }
            resetBall();
        }

        if(ball_pos.y < -bfx5 ) {
            sendBallOut();
            resetBall();
            inThere = false;
        }

        RectF temp_ball = new RectF(ball_pos.x - bfx5,ball_pos.y - bfx5 ,
                ball_pos.x + bfx5 , ball_pos.y + bfx5);

        RectF temp_hand = new RectF(handPos.x + handRect.left,handPos.y + handRect.top ,
                handPos.x + handRect.right , handPos.y + handRect.bottom);



        if(temp_ball.bottom > temp_hand.top && temp_ball.top < temp_hand.bottom
                && temp_ball.right > temp_hand.left && temp_ball.left < temp_hand.right)
        {
//            paint_line.setColor(Color.YELLOW);
//            canvas.drawRect(temp_ball,paint_line);
//            canvas.drawRect(temp_hand,paint_line);

            float gdc = temp_hand.top - temp_ball.top;
            float kdc = temp_hand.left - temp_ball.left;

            float chgd = 0.f; // 重合高度
            float chkd = 0.f; // ^

            if(gdc > 0)
                chgd = temp_ball.bottom - temp_hand.top;
            else
                chgd = temp_hand.bottom - temp_ball.top;

            if(kdc > 0)
                chkd = temp_ball.right - temp_hand.left;
            else
                chkd = temp_hand.right - temp_ball.left;



            if(chgd > chkd) {

                if (kdc > 0) {
                    float t_kdc = temp_ball.right - temp_hand.left;
                    float vx = Math.abs(-bvpos.x) > Math.abs(hvpos.x) ? -bvpos.x : hvpos.x;
                    bvpos.x = vx * 0.76f;
                    ball_pos.x -= t_kdc;
                } else if (kdc < -hw_bwc) {
                    float t_kdc = temp_hand.right - temp_ball.left;
                    float vx = Math.abs(-bvpos.x) > Math.abs(hvpos.x) ? -bvpos.x : hvpos.x;
                    bvpos.x = vx * 0.76f;
                    ball_pos.x += t_kdc;
                }
            }else {
                if (gdc > 0) {
                    float vx = Math.abs(bvpos.x) > Math.abs(hvpos.x) ? bvpos.x : hvpos.x ;//* 0.8f;
                    float vy = Math.abs(-bvpos.y) > Math.abs(hvpos.y) ? -bvpos.y : hvpos.y; //* 0.8f;
                    bvpos.y = vy * 0.76f;
                    bvpos.x = vx * 0.76f;
                    float t_gdc = temp_ball.bottom - temp_hand.top;
                    ball_pos.y -= t_gdc;
                } else if (gdc < 0) {
                    float vx = Math.abs(bvpos.x) > Math.abs(hvpos.x) ? bvpos.x : hvpos.x;
                    float vy = Math.abs(-bvpos.y) > Math.abs(hvpos.y) ? -bvpos.y : hvpos.y;
                    bvpos.y = vy * 0.76f;
                    bvpos.x = vx * 0.76f;
                    float t_gdc = temp_hand.bottom - temp_ball.top;
                    ball_pos.y += t_gdc;
                }
            }
        }

        //paint_line.setColor(Color.RED);
    }
    static class MyHandler extends Handler{
        SoftReference<GameView> gv;
        public MyHandler(SoftReference<GameView> gv){
            this.gv = gv;
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                {
                    GameView v = gv.get();
                    if(v != null)
                    {
                        v.invalidate();
                    }
                }
                break;
                case -1:
                {
                    GameView v = gv.get();
                    if(v != null)
                    {
                        synchronized (v.surfaceHolder) {
                            v.state = State.Exception;
                        }
                    }
                }
                break;
                case A_WHAT: {
                    GameView v = gv.get();
                    if (v != null) {
                        synchronized (v.surfaceHolder) {
                            AInfo info = (AInfo) msg.obj;
                            v.inThere = true;
                            v.ball_pos.x = info.x;
                            v.ball_pos.y = -v.bfx5 + 1;
                            v.bvpos = info.v;
                        }
                    }
                }
                    break;
                case B_WHAT:
                {
                    GameView v = gv.get();
                    if (v != null) {
                        synchronized (v.surfaceHolder) {
                            v.state = State.Pause;
                        }
                    }
                }
                    break;
                case C_WHAT:
                {
                    GameView v = gv.get();
                    if (v != null) {
                        synchronized (v.surfaceHolder) {
                            v.state = State.Playing;
                        }
                    }
                }
                break;
                case D_WHAT:
                {
                    GameView v = gv.get();
                    if (v != null) {
                        synchronized (v.surfaceHolder) {
                            DInfo info = (DInfo) msg.obj;
                            if (info.inThere == 1)
                                v.inThere = true;
                            v.score_me = info.me_score;
                            v.score_his = info.his_score;
                            if (v.score_me == 3) {
                                GameInfoDaoImpl.getInstace().addNewScore(v.room.getMe().name,v.room.getOuthers().get(0).name,v.score_me,v.score_his);
                                v.state = State.Finish;
                            }
                        }
                    }
                }
                break;
                case  E_WHAT:
                    GameView v = gv.get();
                    if (v != null) {
                        synchronized (v.surfaceHolder) {
                            v.resetBall();
                            v.resetHand();
                            v.score_me = 0;
                            v.score_his = 0;
                            v.inThere = !v.inThere;
                            v.state = State.Playing;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (surfaceHolder) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (state == State.Pause) {
                        if (beginRect.contains(event.getX(), event.getY())) {
                            beginIsPressed = true;
                        }
                    } else if (state == State.Playing) {
                        RectF temp_hand = new RectF(handPos.x + handRect.left, handPos.y + handRect.top,
                                handPos.x + handRect.right, handPos.y + handRect.bottom);
                        if (temp_hand.contains(event.getX(), event.getY())) {
                            isHand = true;
                            lastPos.x = event.getX();
                            lastPos.y = event.getY();
                        }
                    } else if (state == State.Finish) {
                        if (againRect.contains(event.getX(), event.getY())) {
                            isAgainPressed = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (state == State.Pause) {
                        if (beginRect.contains(event.getX(), event.getY())) {
                            beginIsPressed = false;
                            Log.e(LT, "Playing");
                            state = State.Playing;
                            sendPlay();
                            parcelables.add(new ParticleSys(play_part,0xFF00aaaa,begin_pos.x,begin_pos.y));
                        }
                    } else if (state == State.Playing) {
                        hvpos.x = 0.f;
                        hvpos.y = 0.f;
                        isHand = false;
                    } else if (state == State.Finish) {
                        if (againRect.contains(event.getX(), event.getY())) {
                            isAgainPressed = false;
                            resetBall();
                            resetHand();
                            score_me = 0;
                            score_his = 0;
                            inThere = !inThere;
                            state = State.Playing;
                            sendPlayAgain();

                            parcelables.add(new ParticleSys(again_part,0xFF00aaaa,againPos.x,againPos.y));
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isHand) {
                        hvpos.x = lastPos.x - handPos.x;
                        hvpos.y = lastPos.y - handPos.y;
//                    Log.e(LT," " + handPos.x + "  " + handPos.y);
//                    Log.e(LT," " + lastPos.x + "  " + lastPos.y);
//                    Log.e(LT," " + hvpos.x + "  " + hvpos.y);
                        handPos.x += hvpos.x;
                        handPos.y += hvpos.y;
                        if (handPos.y < handMinY) {
                            handPos.y -= hvpos.y;
                            hvpos.y = 0.f;
                        }
                        if(handPos.y > handMaxY){
                            handPos.y -= hvpos.y;
                            hvpos.y = 0.f;
                        }

                        lastPos.x = event.getX();
                        lastPos.y = event.getY();
                    }
                    break;
            }
        }
        return true;
    }
    private void sendBallOut()
    {
        Vec2 v = Converter.getInstance().convert(new Vec2(-bvpos.x,-bvpos.y));
        float x = Converter.getInstance().convertW(ball_pos.x);
        Session session = room.getSession();
        String s = "A#"+v.x + "#" + v.y + "#" + x;

        session.sendMessage(
                org.sid.shootin.communication.net.Message.createMessage(
                        org.sid.shootin.communication.net.Message.TYPE_STRING,
                        s.getBytes(),0));

        log("sendBallOut() " +s);
    }
    private void sendPause(){
        String s = "B";
        Session session = room.getSession();
        session.sendMessage(
                org.sid.shootin.communication.net.Message.createMessage(
                        org.sid.shootin.communication.net.Message.TYPE_STRING,
                        s.getBytes(),0
                ));
        log("sendPause() " +s);
    }
    private void sendPlay(){
        String s = "C";
        Session session = room.getSession();
        session.sendMessage(
                org.sid.shootin.communication.net.Message.createMessage(
                        org.sid.shootin.communication.net.Message.TYPE_STRING,
                        s.getBytes(),0
                ));

        log("sendPlay() " + s);
    }
    private void sendScoreChange(){
        StringBuffer sb = new StringBuffer();
        sb.append("D#");
        if(inThere)
            sb.append("0");
        else
            sb.append("1");
        sb.append("#");
        sb.append(score_me+"#"+score_his);
        String s = sb.toString();
        Session session = room.getSession();
        session.sendMessage(
                org.sid.shootin.communication.net.Message.createMessage(
                        org.sid.shootin.communication.net.Message.TYPE_STRING,
                        s.getBytes(),0
                ));

        log("sendScoreChange() " +s);
    }
    private void sendPlayAgain()
    {
        String s = "E";
        Session session = room.getSession();
        session.sendMessage(
                org.sid.shootin.communication.net.Message.createMessage(
                        org.sid.shootin.communication.net.Message.TYPE_STRING,
                        s.getBytes(),0
                ));

        log("sendPlay() " + s);
    }
    private void resetBall()
    {
        ball_pos.x = mid_x;
        ball_pos.y = mid_y;
        bvpos.x = 0.f;
        bvpos.y = 0.f;
    }
    private void resetHand()
    {
        handPos.x = mid_x;
        handPos.y = bfy80;
        hvpos.x = 0.f;
        hvpos.y = 0.f;
    }

    public void stop()
    {
        state = State.Finish;
    }
    public void pause()
    {
        state = State.Pause;
        sendPause();
    }
    public void destroy()
    {
        //timer.cancel();
        room.close();
        getContext().startActivity(new Intent(getContext(),MainActivity.class));
    }

    static void log(String s){
        Log.e(LT,s);
    }
}

class AInfo{
    public Vec2 v;
    public float x;

    public AInfo(Vec2 v, float x) {
        this.v = v;
        this.x = x;
    }
}

class DInfo{
    public int inThere,me_score,his_score;

    public DInfo(int inThere, int me_score, int his_score) {
        this.inThere = inThere;
        this.me_score = me_score;
        this.his_score = his_score;
    }
}
