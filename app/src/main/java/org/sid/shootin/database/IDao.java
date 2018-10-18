package org.sid.shootin.database;

import java.util.List;
import java.util.Map;

public interface IDao<T> {
    /**
     * 添加一个数据
     *
     * @param entity 实体
     * @return 结果
     */
    DataResult<T> insertInto(T entity);

    /**
     * 根据某一个字段等于某一个值查询一个数据，如果多项符合条件，则返回第一个
     *
     * @param filedName 字段名
     * @param value     值
     * @return 结果，携带查询到的数据
     */
    DataResult<T> queryWhere(String filedName, Object value);

    /**
     * 查询所有，根据条件集合(参见:{@link #queryWhere(String, Object)})查询所有符合条件的数据
     *
     * @param condition 条件集合
     * @return 结果，携带查询到的数据集合
     */
    DataResult<? extends List<T>> queryAll(Map<String, Object> condition);

    /**
     * 套评价所有
     *
     * @param data 要添加的实体集合
     * @return 结果
     */
    DataResult<? extends List<T>> insertAll(List<T> data);
}
