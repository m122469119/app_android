package com.dym.film.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbDaoBase
{

    public Context mContext;
    public static SQLiteDatabase db;

    public DbDaoBase(Context pContext)
    {
        mContext = pContext;
        if (db == null) {
            db = new DbHelper(pContext).getWritableDatabase();
        }
    }

    /**
     * 添加数据到数据库
     *
     * @param student 底层的Student类对象
     */
    public long insert(String tableName, Map<String, String> dataMap)
    {

        // ContentValues实质是HashMap(这个ContentValues是针对数据库从新定义的HashMap)
        ContentValues values = new ContentValues();
        // Key就是创建表的字段名,Value就是插入该字段名的值
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            values.put(entry.getKey(), entry.getValue());
        }
        long id = db.insert(tableName, // 表名
                null, // 插入是默认没有填写对应字段的值的默认值
                values);

        return id;
    }

    /**
     * 添加数据到数据库
     */
    public long insert(String tableName, ContentValues values)
    {

        long id = db.insert(tableName, // 表名
                null, // 插入是默认没有填写对应字段的值的默认值
                values);
        return id;
    }

    /**
     * 删除数据库数据
     *
     * @param 根据主键id删除
     */
    public int deleteById(String tableName, String id, String value)
    {
        // 返回的int值表示删除数据的数量,失败返回-1
        int count = db.delete(tableName, // 表名
                id + "=?", // 粗略的条件
                new String[]{value} // 条件的具体内容,把确定的内容放入数组中
        );
        return count;
    }

    /**
     * 删除数据库数据
     *
     * @param 根据主键id删除
     */
    public int delete(String tableName, String strWhere, String[] whereArgs)
    {
        // 返回的int值表示删除数据的数量,失败返回-1
        int count = db.delete(tableName, // 表名
                strWhere, // 粗略的条件
                whereArgs // 条件的具体内容,把确定的内容放入数组中
        );
        return count;
    }

    /**
     * 修改数据库数据 根据id查出数据
     */
    public int updateById(String tableName, String id, String idValue, Map<String, String> dataMap)
    {

        // ContentValues实质是HashMap(这个ContentValues是针对数据库从新定义的HashMap)
        ContentValues values = new ContentValues();
        // Key就是创建表的字段名,Value就是插入该字段名的值
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            values.put(entry.getKey(), entry.getValue());
        }

        // 返回的int值表示修改数据的数量,失败返回-1
        int count = db.update(tableName, // 表名
                values, // 需要修改的值
                id + "= ?", // 查数据的条件
                new String[]{idValue} // 条件的具体内容
        );

        return count;
    }

    /**
     * 修改数据库数据 根据id查出数据
     */
    public int updateById(String tableName, String id, String idValue, ContentValues values)
    {

        // 返回的int值表示修改数据的数量,失败返回-1
        int count = db.update(tableName, // 表名
                values, // 需要修改的值
                id + "= ?", // 查数据的条件
                new String[]{idValue} // 条件的具体内容
        );

        return count;
    }

    /**
     * 修改数据库数据 根据条件
     */
    public int update(String tableName, String strWhere, String[] whereArgs, Map<String, String> dataMap)
    {
        // ContentValues实质是HashMap(这个ContentValues是针对数据库从新定义的HashMap)
        ContentValues values = new ContentValues();
        // Key就是创建表的字段名,Value就是插入该字段名的值
        for (Map.Entry<String, String> entry : dataMap.entrySet()) {
            values.put(entry.getKey(), entry.getValue());
        }
        // 返回的int值表示修改数据的数量,失败返回-1
        int count = db.update(tableName, // 表名
                values, // 需要修改的值
                strWhere, // 查数据的条件
                whereArgs // 条件的具体内容
        );

        return count;
    }

    /**
     * 修改数据库数据 根据条件
     */
    public int update(String tableName, String strWhere, String[] whereArgs, ContentValues values)
    {

        // 返回的int值表示修改数据的数量,失败返回-1
        int count = db.update(tableName, // 表名
                values, // 需要修改的值
                strWhere, // 查数据的条件
                whereArgs // 条件的具体内容
        );

        return count;
    }

    /**
     * 列表查询用
     *
     * @param tableName 表名
     * @param strCols   列名
     * @param strWhere  条件
     * @param whereArgs 条件具体的内容
     * @param groupBy   分组
     * @param having    限制
     * @param orderBy   排序规则
     * @param limit数量限制
     * @return 数据指针
     */
    public List<Map<String, String>> select(String tableName, String[] strCols, String strWhere, String[] whereArgs, String groupBy, String having, String orderBy, String limit)
    {
        Cursor cursor = db.query(tableName, // 表名
                strCols, // 返回需要查看的列
                strWhere, // 根据什么条件查(where)
                whereArgs, // 条件具体的内容
                groupBy, // sql语句中的(group by) //分组
                having, // sql语句中的(having) //限制
                orderBy, // sql语句中的(orderBy) 排序规则
                limit // sql语句中的(limit) 数量限制
        );

        List<Map<String, String>> listMap = new ArrayList<Map<String, String>>();
        while (cursor.moveToNext()) {// 如果返回false没有数据了
            // cursor.getInt数据是int类型的
            // 并且是id字段的,是根据ColumnIndex获取的
            Map<String, String> map = new HashMap<String, String>();
            int count = cursor.getColumnCount();
            for (int i = 0; i < count; i++) {
                map.put(cursor.getColumnName(i), cursor.getString(i));

            }

            listMap.add(map);

        }
        // 如果返回Cursor要关闭,一定要小心
        cursor.close();

        return listMap;
    }

    // 查询所有的数据,返回list对象
    public List<Map<String, String>> selectAll(String tableName, String[] strCols)
    {

        return this.select(tableName, strCols, null, null, null, null, null, null);
    }

    // 根据主键，查询数据,返回list对象
    public List<Map<String, String>> selectById(String tableName, String[] strCols, String pkName, String... pkValue)
    {

        return this.select(tableName, strCols, pkName + "=?", pkValue, null, null, null, null);
    }

    // 根据主键，查询数据,返回map对象
    public Map<String, String> findById(String tableName, String[] strCols, String pkName, String pkValue)
    {
        List<Map<String, String>> listMap = this.select(tableName, strCols, pkName + "=?", new String[]{pkValue}, null, null, null, " 1");
        return listMap.get(0);
    }

    // 根据条件，查询数据,返回map对象
    public Map<String, String> find(String tableName, String[] strCols, String strWhere, String[] whereArgs)
    {
        List<Map<String, String>> listMap = this.select(tableName, strCols, strWhere, whereArgs, null, null, null, " 1");
        return listMap.get(0);
    }


    public List<Map<String, String>> query(String sql, String[] whereArgs)
    {

        Cursor cursor = db.rawQuery(sql, whereArgs);
        List<Map<String, String>> listMap = new ArrayList<Map<String, String>>();
        while (cursor.moveToNext()) {// 如果返回false没有数据了
            // cursor.getInt数据是int类型的
            // 并且是id字段的,是根据ColumnIndex获取的
            Map<String, String> map = new HashMap<String, String>();
            int count = cursor.getColumnCount();
            for (int i = 0; i < count; i++) {
                map.put(cursor.getColumnName(i), cursor.getString(i));

            }

            listMap.add(map);
        }
        // 如果返回Cursor要关闭,一定要小心
        cursor.close();

        return listMap;
    }

    /**
     * 执行sql语句，包括创建表、删除、插入 ,更新
     *
     * @param sql
     */
    public void executeSql(String sql, Object[] bindArgs)
    {
        if (bindArgs == null) {
            db.execSQL(sql);
        }
        else {
            db.execSQL(sql, bindArgs);
        }

    }

    /**
     * 关闭数据库
     */
    public void close()
    {
        if (null != db) {
            db.close();
        }

    }

    /**
     * 开始事务
     */
    protected void beginTransaction()
    {
        db.beginTransaction();
    }

    /**
     * 提交事务及结束事务
     */
    protected void commit()
    {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * 回滚事务
     */
    protected void rollback()
    {
        db.endTransaction();
    }


}
