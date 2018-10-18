package org.sid.shootin.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GameInfo extends RealmObject {
    @PrimaryKey
    private int gid;
    private String your;         //你的名字
    private String hier;         //对手的名字
    private int yourscore;       //你的得分
    private int hierscore;      //对手的得分
    private String date;        //时间

    public GameInfo() {
        Date date = java.util.Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        this.date = simpleDateFormat.format(date);
    }

    public GameInfo(int gid, String your, String hier, int yourscore, int hierscore) {
        this();
        this.gid = gid;
        this.your = your;
        this.hier = hier;
        this.yourscore = yourscore;
        this.hierscore = hierscore;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getYour() {
        return your;
    }

    public void setYour(String your) {
        this.your = your;
    }

    public String getHier() {
        return hier;
    }

    public void setHier(String hier) {
        this.hier = hier;
    }

    public int getYourscore() {
        return yourscore;
    }

    public void setYourscore(int yourscore) {
        this.yourscore = yourscore;
    }

    public int getHierscore() {
        return hierscore;
    }

    public void setHierscore(int hierscore) {
        this.hierscore = hierscore;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
