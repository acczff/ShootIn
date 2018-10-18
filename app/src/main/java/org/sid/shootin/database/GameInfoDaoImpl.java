package org.sid.shootin.database;

import android.content.Intent;
import android.os.Handler;

import org.sid.shootin.entity.GameInfo;

import java.util.UUID;

import io.realm.Realm;

public class GameInfoDaoImpl extends BaseRealmDaoImpl<GameInfo> {

    private static GameInfoDaoImpl instace;
    private Handler handler;

    private GameInfoDaoImpl() {
        super(GameInfo.class);
        this.setRealm(
                Realm.getDefaultInstance()
        );
        this.handler = new Handler();
    }

    public static GameInfoDaoImpl getInstace() {
        if (instace == null)
            instace = new GameInfoDaoImpl();
        return instace;
    }

    public void addNewScore(final String your, final String hier, final int yourscore, final int hierscore) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                GameInfo gameInfo = new GameInfo(UUID.randomUUID().hashCode(), your, hier, yourscore, hierscore);
                GameInfoDaoImpl.this.insertInto(gameInfo).success();
            }
        });

    }

    @Override
    public void close() {
        super.close();
        instace = null;
    }
}
