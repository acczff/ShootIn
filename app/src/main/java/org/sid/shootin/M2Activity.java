package org.sid.shootin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.sid.shootin.adapter.RecordAdapter;
import org.sid.shootin.database.GameInfoDaoImpl;
import org.sid.shootin.entity.GameInfo;

import java.util.HashMap;
import java.util.List;

public class M2Activity extends AppCompatActivity implements View.OnClickListener {
    private ListView listview;
    private ImageView iv;
    private TextView te;
    private List<GameInfo> list;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m2);
        listview = findViewById(R.id.listview);
        iv = findViewById(R.id.iv);
        te = findViewById(R.id.te);
        //查询目标信息
        sp = getSharedPreferences("gameinfo", MODE_PRIVATE);
        String playname = sp.getString("player_name", "default");
        list = selectRecord(playname);

        RecordAdapter adapter = new RecordAdapter(list);
        listview.setAdapter(adapter);
        listview.setDivider(null);
        listview.setDividerHeight(0);
        if(list.size()==0){
            te.setText("您一共参加了" + list.size() + "场比赛,胜率为:" + victory() + "%\n快去和玩家对战,证明自己的实力吧");
        }else if (list.size()!=0&&victory() < 30) {
            te.setText("您一共参加了" + list.size() + "场比赛,胜率为:" + victory() + "%\n你太菜了,多去练习吧");
        } else if (victory() >= 30 && victory() < 60) {
            te.setText("您一共参加了" + list.size() + "场比赛,胜率为:" + victory() + "%\n继续努力吧,你的上升空间还很大");
        } else if (victory() >= 60 && victory() < 90) {
            te.setText("您一共参加了" + list.size() + "场比赛,胜率为:" + victory() + "%\n你是个优秀的玩家,去追逐更完美的技术吧");
        } else if (victory() >= 90 && victory() < 100) {
            te.setText("您一共参加了" + list.size() + "场比赛,胜率为:" + victory() + "%\n你的实力登峰造极,去击败更多的挑战者吧");
        } else if (victory() == 100) {
            te.setText("您一共参加了" + list.size() + "场比赛,胜率为:" + victory() + "%\n独孤求败");
        }
        iv.setOnClickListener(this);
    }

    public List<GameInfo> selectRecord(String playname) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("your", playname);
        List<GameInfo> list = GameInfoDaoImpl.getInstace().queryAll(hashMap).success();
        return list;
    }

    public float victory() {
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getYourscore() > list.get(i).getHierscore()) {
                count++;
            }
        }
        float result = list.size() == 0 ? 0 : Float.valueOf(count) / list.size() * 100;
        count = 0;
        return result;
    }

    @Override
    public void onClick(View v) {
        if (v == iv) {
            finish();
        }
    }
}
