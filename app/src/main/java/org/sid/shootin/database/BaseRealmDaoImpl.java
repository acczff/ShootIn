package org.sid.shootin.database;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

public class BaseRealmDaoImpl<T extends RealmObject> implements IDao<T> {
    private Realm realm;
    private final Class<T> type;

    public BaseRealmDaoImpl(Class<T> type) {
        this.type = type;
    }

    @Override
    public DataResult<T> insertInto(@NonNull T entity) {
        realm.beginTransaction();
        T result = realm.copyToRealmOrUpdate(entity);
        realm.commitTransaction();
        return DataResult.ok(result);
    }

    @Override
    public DataResult<T> queryWhere(String filedName, Object value) {
        RealmQuery<T> query = this.realm.where(type);
        T result = equalTo(query, filedName, value).findFirst();
        if (result == null)
            return DataResult.err();
        return DataResult.ok(result);
    }

    private <I> RealmQuery<T> equalTo(RealmQuery<T> query, String filedName, I value) {
        if (value instanceof String)
            return query.equalTo(filedName, (String) value);
        else if (value instanceof Integer)
            return query.equalTo(filedName, (Integer) value);
        else if (value instanceof Byte)
            return query.equalTo(filedName, (Byte) value);
        else if (value instanceof Short)
            return query.equalTo(filedName, (Short) value);
        else if (value instanceof Long)
            return query.equalTo(filedName, (Long) value);
        else if (value instanceof Float)
            return query.equalTo(filedName, (Float) value);
        else if (value instanceof Double)
            return query.equalTo(filedName, (Double) value);
        else if (value instanceof Boolean)
            return query.equalTo(filedName, (Boolean) value);
        else if (value instanceof byte[])
            return query.equalTo(filedName, (byte[]) value);
        else if (value instanceof Date)
            return query.equalTo(filedName, (Date) value);
        else throw new TypeNotPresentException(value.getClass().getName(), null);
    }

    @Override
    public DataResult<List<T>> queryAll(Map<String, Object> condition) {
        RealmQuery<T> realmQuery = this.realm.where(type);
        if (condition == null || condition.size() == 0) {
            List<T> contacts = realmQuery.findAll();
            return DataResult.ok(contacts);
        }
        Set<Map.Entry<String, Object>> entrys = condition.entrySet();
        Iterator<Map.Entry<String, Object>> iterable = entrys.iterator();
        Map.Entry<String, Object> next;
        equalTo(realmQuery, (next = iterable.next()).getKey(), next.getValue());
        while (iterable.hasNext()) {
            equalTo(realmQuery.or(), (next = iterable.next()).getKey(), next.getValue());
        }
        List<T> contactsList = realmQuery.findAll();
        return DataResult.ok(contactsList);
    }

    /**
     * 查询所有数据
     *
     * @return 结果
     */
    public DataResult<List<T>> queryAll() {
        return queryAll(null);
    }


    @Override
    public DataResult<List<T>> insertAll(List<T> dataList) {
        this.realm.beginTransaction();
        List<T> result = realm.copyToRealmOrUpdate(dataList);
        this.realm.commitTransaction();
        return DataResult.ok(result);
    }

    public void close() {
        if (this.realm != null)
            realm.close();
    }

    public void setRealm(Realm realm) {
        this.realm = realm;
    }
}
