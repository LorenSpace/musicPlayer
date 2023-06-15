package org.sast.musciplayer.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MusicDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "music_db";
    private static final int DB_VERSION = 1;

    public MusicDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建本地音乐表
        db.execSQL(MusicTable.CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 升级数据库
        db.execSQL("DROP TABLE IF EXISTS " + MusicTable.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.enableWriteAheadLogging();
        Log.d("MusicDBHelper",
                "SQLite database is configured with " +
                        "write-ahead logging and other settings");
    }

    // 表名
    private static class MusicTable {
        static final String TABLE_NAME = "local_music";

        // 创建表的SQL语句
        static final String CREATE_TABLE_SQL =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + Cols.COLUMN_ID + " INTEGER not null PRIMARY KEY AUTOINCREMENT, "
                        + Cols.COLUMN_SONG + " varchar not null, "
                        + Cols.COLUMN_SINGER + " varchar not null, "
                        + Cols.COLUMN_ALBUM + " varchar not null, "
                        + Cols.COLUMN_DURATION + " varchar not null, "
                        + Cols.COLUMN_PATH + " varchar not null, "
                        + Cols.COLUMN_ALBUM_ART + " varchar,"
                        + Cols.COLUMN_STATUS + " INTEGER not null, "
                        + Cols.COLUMN_FILENAME + " varchar not null)";

        static class Cols {
            private static final String COLUMN_ID = "id";
            private static final String COLUMN_SONG = "song";
            private static final String COLUMN_SINGER = "singer";
            private static final String COLUMN_ALBUM = "album";
            private static final String COLUMN_DURATION = "duration";
            private static final String COLUMN_PATH = "path";
            private static final String COLUMN_ALBUM_ART = "album_art";
            private static final String COLUMN_STATUS = "status";
            private static final String COLUMN_FILENAME = "filename";
        }
    }
}