package org.sid.shootin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends AppCompatActivity {
    GameView gv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gv = findViewById(R.id.gv);
        //gv.init();
    }

    public static void gotoPlay(Context context)
    {
        context.startActivity(new Intent(context,GameActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gv.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gv.pause();
    }

}
