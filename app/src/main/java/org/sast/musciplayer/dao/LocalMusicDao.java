package org.sast.musciplayer.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.sast.musciplayer.model.LocalMusicBean;

import java.util.ArrayList;
import java.util.List;

public class LocalMusicDao {
    private final MusicDBHelper mHelper;

    public LocalMusicDao(Context context) {
        mHelper = new MusicDBHelper(context);
    }

    private static ContentValues getContentValues(LocalMusicBean bean) {
        ContentValues values = new ContentValues();
        values.put(MusicTable.Cols.COLUMN_SONG, bean.getSong());
        values.put(MusicTable.Cols.COLUMN_SINGER, bean.getSinger());
        values.put(MusicTable.Cols.COLUMN_ALBUM, bean.getAlbum());
        values.put(MusicTable.Cols.COLUMN_DURATION, bean.getDuration());
        values.put(MusicTable.Cols.COLUMN_PATH, bean.getPath());
        values.put(MusicTable.Cols.COLUMN_ALBUM_ART, bean.getAlbumArt());
        values.put(MusicTable.Cols.COLUMN_STATUS, bean.getStatus());
        values.put(MusicTable.Cols.COLUMN_FILENAME, bean.getFileName());
        return values;
    }

    public void addLocalMusic(LocalMusicBean bean) {
        try (SQLiteDatabase mDatabase = mHelper.getWritableDatabase()) {
            ContentValues values = getContentValues(bean);
            mDatabase.insert(MusicTable.TABLE_NAME, null, values);
        }
    }

    public List<LocalMusicBean> getMusicList() {
        List<LocalMusicBean> musicList = new ArrayList<>();
        try (SQLiteDatabase mDatabase = mHelper.getWritableDatabase()) {
            try (Cursor cursor = mDatabase.query(MusicTable.TABLE_NAME,
                    null, null, null,
                    null, null, null)) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    LocalMusicBean bean = new LocalMusicBean(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6),
                            cursor.getInt(7),
                            cursor.getString(8)
                    );
                    musicList.add(bean);
                    cursor.moveToNext();
                }
            }
        }
        return musicList;
    }

    public List<LocalMusicBean> getMusicByFilter(String selection,
                                                 List<String> selectionArgs) {
        List<LocalMusicBean> musicList = new ArrayList<>();
        try (SQLiteDatabase mDatabase = mHelper.getWritableDatabase()) {
            try (Cursor cursor = mDatabase.query(MusicTable.TABLE_NAME,
                    null, selection, selectionArgs.toArray(new String[0]),
                    null, null, null)) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    LocalMusicBean bean = new LocalMusicBean(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6),
                            cursor.getInt(7),
                            cursor.getString(8)
                    );
                    musicList.add(bean);
                    cursor.moveToNext();
                }
            }
        }
        return musicList;
    }

    public void updateMusic(LocalMusicBean bean) {
        ContentValues values = getContentValues(bean);
        try (SQLiteDatabase mDatabase = mHelper.getWritableDatabase()) {
            mDatabase.update(MusicTable.TABLE_NAME, values,
                    "id = ?",
                    new String[]{String.valueOf(bean.getId())});
        }
    }

    private static class MusicTable {
        static final String TABLE_NAME = "local_music";

        static class Cols {
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
