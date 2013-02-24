package se.qxx.jukebox.domain.dao;

import se.qxx.dao.db.Cursor;
import se.qxx.dao.db.SQLiteDatabase;
import se.qxx.dao.db.SQLiteStatement;

import se.qxx.dao.AbstractDao;
import se.qxx.dao.Property;
import se.qxx.dao.internal.DaoConfig;

import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle.Builder;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table SUBTITLE.
*/
public class SubtitleDao extends AbstractDao<Subtitle, Void> {

    public static final String TABLENAME = "SUBTITLE";

    /**
     * Properties of entity Subtitle.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property MediaIndex = new Property(0, Integer.class, "MediaIndex", false, "MEDIA_INDEX");
    };


    public SubtitleDao(DaoConfig config) {
        super(config);
    }
    
    public SubtitleDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'SUBTITLE' (" + //
                "'MEDIA_INDEX' INTEGER);"); // 0: MediaIndex
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'SUBTITLE'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Subtitle entity) {
        stmt.clearBindings();
        if(entity.hasMediaIndex()) {
            stmt.bindLong(1, entity.getMediaIndex());
        }
    }

    /** @inheritdoc */
    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }    

    /** @inheritdoc */
    @Override
    public Subtitle readEntity(Cursor cursor, int offset) {
        Builder builder = Subtitle.newBuilder();
        if (!cursor.isNull(offset + 0)) {
            builder.setMediaIndex(cursor.getInt(offset + 0));
        }
        return builder.build();
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Subtitle entity, int offset) {
        throw new UnsupportedOperationException("Protobuf objects cannot be modified");
     }
    
    /** @inheritdoc */
    @Override
    protected Void updateKeyAfterInsert(Subtitle entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }
    
    /** @inheritdoc */
    @Override
    public Void getKey(Subtitle entity) {
        return null;
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return false;
    }
    
}
