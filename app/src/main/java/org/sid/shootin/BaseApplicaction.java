package org.sid.shootin;

import android.app.Application;

import org.sid.shootin.database.GameInfoDaoImpl;

import io.realm.Realm;

/**
 * Created by lenovo on 2018/9/2.
 */

public class BaseApplicaction extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        int a = 0;
        Realm.init(this);
        GameInfoDaoImpl.getInstace();
    }
}
