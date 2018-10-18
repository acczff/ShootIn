package org.sid.shootin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.sid.shootin.communication.net.Message;
import org.sid.shootin.communication.net.Room;
import org.sid.shootin.communication.net.Session;
import org.sid.shootin.communication.net.Util;
import org.sid.shootin.tools.Looger;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, Animation.AnimationListener {

    private TextView bt_creat;
    private TextView bt_join;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private TextView my_name;
    private TextView your_name;
    private EditText et_name;
    private View gotoplayButton;
    Handler handler;
    private Room room;
    private int scrnWidth;
    private int scrnHeight;
    private float btn_width = (4f / 5f);
    private Bitmap anmBitmap;
    private ImageView anmView;
    Animation animation;
    private ProgressBar createLoadingView;
    private ProgressBar joinLoadingView;
    private View main_setting;
    private View main_history;
    private ImageView main_titleImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Converter.init(this);

        setContentView(R.layout.activity_main);
        Bitmap bitmap = null;
        Bitmap titleImage = null;
        try {
            InputStream inputStream = getAssets().open("background_img_1.jpg");
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            inputStream = getAssets().open("shootin.png");
            titleImage = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap == null)
            this.getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        else
            this.getWindow().getDecorView().setBackground(new BitmapDrawable(getResources(), bitmap));
        bt_creat = findViewById(R.id.bt_creat);
        bt_join = findViewById(R.id.bt_join);
        main_setting = findViewById(R.id.main_setting);
        main_setting.setOnClickListener(oc);
        main_history = findViewById(R.id.main_hisotry);
        main_titleImage = findViewById(R.id.main_titleImage);
        main_titleImage.setImageBitmap(titleImage);
        bt_join.setOnClickListener(oc);
        bt_creat.setOnClickListener(oc);
        main_history.setOnClickListener(oc);
        anmView = findViewById(R.id.anm_show);
        loadTypeFace();

        handler = new Handler();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        scrnWidth = displayMetrics.widthPixels;
        scrnHeight = displayMetrics.heightPixels;
        ;

        bt_join.setOnTouchListener(this);
        bt_creat.setOnTouchListener(this);
        main_setting.setOnTouchListener(this);
        main_history.setOnTouchListener(this);
        animation = AnimationUtils.loadAnimation(this, R.anim.scal_zoom_big);
        animation.setAnimationListener(this);
    }

    private void loadTypeFace() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/ttf2.ttf");
        bt_creat.setTypeface(typeface, bt_creat.getTypeface().getStyle());
        bt_join.setTypeface(typeface, bt_join.getTypeface().getStyle());
        ((TextView) main_setting).setTypeface(typeface);
        ((TextView) main_history).setTypeface(typeface);
    }

    View.OnClickListener oc = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.bt_creat:
                    if (Util.openWifiAp(MainActivity.this, "ShootIn")) {
                        String playerName = getSharedPreferences("gameinfo", MODE_PRIVATE).getString("player_name", "default");
                        alertDialog = createdialog(MainActivity.this, playerName);
                        final Room room = Room.createNewRoom("new", playerName, 8889);
                        room.setOnAddChildLin(new Room.OnAddChildLin() {
                            @Override
                            public void onAdd(final Room.ChildInfo childInfo) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        your_name.setVisibility(View.VISIBLE);
                                        createLoadingView.setVisibility(View.GONE);
                                        your_name.setText(childInfo.name);
                                        if (gotoplayButton != null)
                                            gotoplayButton.setVisibility(View.VISIBLE);
                                    }
                                });

                            }
                        });
                        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                room.close();
                            }
                        });
                        room.accept();
                        alertDialog.show();
                    } else {
                        Toast.makeText(MainActivity.this, "热点开启失败，请手动开始", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.bt_join:
                    if (!Util.openWifi(MainActivity.this)) {
                        Toast.makeText(MainActivity.this, "WIFI开启失败，请手动开始", Toast.LENGTH_SHORT).show();
                    }
                    alertDialog = joindialog(MainActivity.this);
                    alertDialog.show();
                    break;
                case R.id.main_setting:
                    createInputNameDialog(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SharedPreferences sharedPreferences = getSharedPreferences("gameinfo", MODE_PRIVATE);
                            String name = et_name.getText().toString();
                            sharedPreferences.edit().putString("player_name", name.isEmpty() ? "default" : name).apply();
                        }
                    }).show();
                    break;
                case R.id.main_hisotry:
                    Intent intent = new Intent(MainActivity.this, M2Activity.class);
                    startActivity(intent);
            }
        }
    };

    private AlertDialog createdialog(Context context, String myname) {
        builder = new AlertDialog.Builder(context);
        View v = View.inflate(this, R.layout.dialog_room_create, null);
        my_name = v.findViewById(R.id.my_name);
        your_name = v.findViewById(R.id.your_name);
        my_name.setText(myname);
        builder.setView(v);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        (gotoplayButton = v.findViewById(R.id.gotoPlay))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Room.getInstance().getSession().sendMessage(Message.createMessage(Message.TYPE_STRING, "#START#".getBytes(), 0));
                        GameActivity.gotoPlay(MainActivity.this);
                        alertDialog.dismiss();
                        finish();
                    }
                });
        createLoadingView = v.findViewById(R.id.loading);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Room.getInstance().close();
            }
        });
        return alertDialog;

    }


    private AlertDialog joindialog(Context context) {
        builder = new AlertDialog.Builder(context);
        View v = View.inflate(context, R.layout.dialog_room_join, null);
        v.findViewById(R.id.et_name).setVisibility(View.GONE);
        v.findViewById(R.id.pb).setVisibility(View.VISIBLE);
        v.findViewById(R.id.dialog_join_ok).setVisibility(View.GONE);
        builder.setView(v)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (room != null) {
                            room.close();
                            room = null;
                        }
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo info = wifiManager.getDhcpInfo();
        String ip = Util.intToIp(info.serverAddress);
        final String playerName = getSharedPreferences("gameinfo", MODE_PRIVATE).getString("player_name", "default");
        if (Util.isWifiOpen(MainActivity.this) || Util.linkWifi(MainActivity.this, "ShootIn", "")) {
            room = Room.joinNewRoom(playerName, ip, 8889);
            room.setOnAddChildLin(new Room.OnAddChildLin() {
                @Override
                public void onAdd(final Room.ChildInfo childInfo) {
                    alertDialog.dismiss();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (childInfo == null) {
                                Toast.makeText(MainActivity.this, "无法连接", Toast.LENGTH_SHORT).show();
                                if (Room.getInstance() != null)
                                    Room.getInstance().close();
                            } else {
                                AlertDialog room = createdialog(MainActivity.this, childInfo.name);
                                your_name.setText(playerName);
                                your_name.setVisibility(View.VISIBLE);
                                createLoadingView.setVisibility(View.GONE);
                                room.show();
                                Room.getInstance().getSession().setOnRevc(new Session.OnReceiveLin() {
                                    @Override
                                    public void onRevc(Message message) {
                                        Looger.e(new String(message.getContent()));
                                        if ("#START#".equals(new String(message.getContent()))) {
                                            GameActivity.gotoPlay(MainActivity.this);
                                        }
                                        Room.getInstance().getSession().setOnRevc(null);

                                    }
                                });
                                Room.getInstance().getSession().startRecv();
                            }
                            alertDialog.dismiss();
                        }
                    });
                }
            });
            room.accept();
        } else {
            Toast.makeText(MainActivity.this, "WIFI连接失败，请手动连接", Toast.LENGTH_SHORT).show();
        }
        return alertDialog;
    }

    private AlertDialog createInputNameDialog(final View.OnClickListener okListener) {
        View contentView;
        builder = new AlertDialog.Builder(this)
                .setView((contentView = getLayoutInflater().inflate(R.layout.dialog_room_join, null)));
        et_name = contentView.findViewById(R.id.et_name);
        joinLoadingView = contentView.findViewById(R.id.pb);
        joinLoadingView.setVisibility(View.GONE);
        final AlertDialog alertDialog = builder.create();
        contentView.findViewById(R.id.dialog_join_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okListener.onClick(v);
                alertDialog.dismiss();
            }
        });
        return alertDialog;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!animation.hasEnded())
                    animation.cancel();
                if (anmBitmap != null)
                    anmBitmap.recycle();
                anmBitmap = getCacheBitmapFromView(v);
                AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) anmView.getLayoutParams();
                layoutParams.width = v.getWidth();
                layoutParams.height = v.getHeight();
                int[] locPos = new int[2];
                v.getLocationOnScreen(locPos);
                layoutParams.x = locPos[0];
                layoutParams.y = locPos[1];
                anmView.setLayoutParams(layoutParams);
                anmView.setImageBitmap(anmBitmap);
                anmView.setVisibility(View.VISIBLE);
                anmView.startAnimation(animation);
                break;
        }

        return false;
    }

    private Bitmap getCacheBitmapFromView(View view) {
        final boolean drawingCacheEnabled = true;
        view.setDrawingCacheEnabled(drawingCacheEnabled);
        view.buildDrawingCache(drawingCacheEnabled);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        anmView.setVisibility(View.GONE);

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
