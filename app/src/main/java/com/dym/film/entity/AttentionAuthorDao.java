package com.dym.film.entity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.QueryBuilder;

import com.dym.film.entity.AttentionAuthor;
import com.dym.film.manager.DatabaseManager;

import java.util.List;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ATTENTION_AUTHOR".
*/
public class AttentionAuthorDao extends AbstractDao<AttentionAuthor, Long> {

    public static final String TABLENAME = "ATTENTION_AUTHOR";

    /**
     * Properties of entity AttentionAuthor.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property AttentionAuthorId = new Property(1, String.class, "AttentionAuthorId", false, "ATTENTION_AUTHOR_ID");
        public final static Property AttentionAuthorDes = new Property(2, String.class, "AttentionAuthorDes", false, "ATTENTION_AUTHOR_DES");
    };


    public AttentionAuthorDao(DaoConfig config) {
        super(config);
    }
    
    public AttentionAuthorDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ATTENTION_AUTHOR\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"ATTENTION_AUTHOR_ID\" TEXT NOT NULL ," + // 1: AttentionAuthorId
                "\"ATTENTION_AUTHOR_DES\" TEXT);"); // 2: AttentionAuthorDes
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ATTENTION_AUTHOR\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, AttentionAuthor entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getAttentionAuthorId());
 
        String AttentionAuthorDes = entity.getAttentionAuthorDes();
        if (AttentionAuthorDes != null) {
            stmt.bindString(3, AttentionAuthorDes);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public AttentionAuthor readEntity(Cursor cursor, int offset) {
        AttentionAuthor entity = new AttentionAuthor( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // AttentionAuthorId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // AttentionAuthorDes
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, AttentionAuthor entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAttentionAuthorId(cursor.getString(offset + 1));
        entity.setAttentionAuthorDes(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(AttentionAuthor entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(AttentionAuthor entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }






    /**
     * 保存消息到数据库ng
     * 如果本地保存的消息超过20条，删除最老的条
     * @param message
     */
    public void saveUserAttentionAuthor(AttentionAuthor message)
    {
        DaoSession session = DatabaseManager.getInstance().getDaoSession();
        if (session != null) {
            AttentionAuthorDao dao = session.getAttentionAuthorDao();
            try {
                if(isSaved(dao,message.getAttentionAuthorId())){
                    isDeleted(dao,message.getAttentionAuthorId());
                    dao.insert(message);
                }
                else{
                    dao.insert(message);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 查询某个表是否包含某个id:
     * @param ID
     * @return
     */
    public boolean isSaved(AttentionAuthorDao dao ,String ID)
    {
        QueryBuilder<AttentionAuthor> qb = dao.queryBuilder();
        qb.where(Properties.AttentionAuthorId.eq(ID));
        qb.buildCount().count();
        return qb.buildCount().count() > 0 ? true : false;
    }

    /**
     * 查询某个表是否包含某个id:
     * @param ID
     * @return
     */
    public void isDeleted(AttentionAuthorDao dao ,String ID)
    {
        QueryBuilder<AttentionAuthor> qb = dao.queryBuilder();
        qb.where(Properties.AttentionAuthorId.eq(ID));
        qb.buildCount().count();
        if(qb.buildCount().count() > 0 ? true : false)
            dao.deleteByKey(qb.list().get(0).getId());
    }

    /**
     * 查询所有
     * @param dao
     * @return
     */
    public List<AttentionAuthor> getAttentionAuthorList(AttentionAuthorDao dao,int i)
    {
        if(i==-1){
            QueryBuilder<AttentionAuthor> qb = dao.queryBuilder();
            qb.orderDesc(Properties.Id);
            return qb.list();
        }else {
            QueryBuilder<AttentionAuthor> qb = dao.queryBuilder();
            qb.orderDesc(Properties.Id).limit(i);
            return qb.list();
        }
    }
    /**
     * 删除前5条
     * @param dao
     * @return
     */
    public void delectAttentionAuthorList(AttentionAuthorDao dao)
    {
        QueryBuilder<AttentionAuthor> qb = dao.queryBuilder();
        qb.limit(5);
        for (AttentionAuthor AttentionAuthor:qb.list()){
            dao.delete(AttentionAuthor);
        }
    }

}
