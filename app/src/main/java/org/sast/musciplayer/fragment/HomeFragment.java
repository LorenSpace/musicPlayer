package org.sast.musciplayer.fragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.sast.musciplayer.R;
import org.sast.musciplayer.adapter.LocalMusicAdapter;
import org.sast.musciplayer.dao.LocalMusicDao;
import org.sast.musciplayer.model.LocalMusicBean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private List<LocalMusicBean> mDatas;
    private View view;

    // 申请权限的 ActivityResultLauncher
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        boolean allGranted = true;
                        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                            if (!entry.getValue()) {
                                allGranted = false;
                                break;
                            }
                        }
                        if (allGranted) {
                            // 权限申请成功
                            loadLocalMusicData(view.getContext()); // 加载本地音乐数据
                        } else {
                            Toast.makeText(view.getContext(), "权限被拒绝！", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    public HomeFragment() {
        // Required empty public constructor
        mDatas = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        // 初始化数据库
        LocalMusicDao localMusicDao = new LocalMusicDao(view.getContext());

        // 动态权限申请
        if (hasPermissions()) {
            // 已经授权，加载本地音乐数据
            loadLocalMusicData(view.getContext());
        } else {
            // 未授权，申请权限
            requestPermissions();
        }

        // 如果非重名 Bean 就添加进数据库
        String selection = "song = ? and singer = ? and album = ?";
        for (LocalMusicBean bean : mDatas) {
            List<String> args = new ArrayList<>();
            args.add(bean.getSong());
            args.add(bean.getSinger());
            args.add(bean.getAlbum());
            if (localMusicDao.getMusicByFilter(selection, args).size() == 0) {
                localMusicDao.addLocalMusic(bean);
            }
        }

        mDatas = localMusicDao.getMusicList();

        // 初始化本地音乐列表
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        LocalMusicAdapter adapter = new LocalMusicAdapter(view.getContext(), mDatas, "normal");
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private boolean hasPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        requestPermissionLauncher.launch(PERMISSIONS);
    }

    private void loadLocalMusicData(Context context) {
        // 更新媒体库 mp3 文件
        String file_path = Environment.getExternalStorageDirectory().getPath() + "/Music";
        MediaScannerConnection.scanFile(context,
                new String[]{file_path},
                new String[]{"mp3"}, // 指定扫描 mp3 文件，缩短扫描时间
                (path, uri) -> Log.i("ExternalStorage", "Scanned" + path + ":" + uri));

        // 加载本地存储中的音乐 mp3 文件到集合中
        ContentResolver resolver = view.getContext().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        try (Cursor cursor = resolver.query(uri, null,
                null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String song = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    );
                    String singer = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    );
                    String album = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    );
                    String path = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                    );
                    File file = new File(path);
                    String filename = file.getName();
                    long duration = cursor.getLong(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    );
                    String album_id = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                    );
                    String albumArt = getAlbumArt(album_id);
                    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.US);
                    String time = sdf.format(new Date(duration));
                    LocalMusicBean bean = new LocalMusicBean(
                            song, singer, album, time, path, albumArt, 0, filename
                    );
                    mDatas.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(view.getContext(), "加载本地音乐出错！", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAlbumArt(String album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = view.getContext().getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + album_id),
                projection, null, null, null);
        String album_art = null;
        if (cur != null) {
            if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
                cur.moveToNext();
                album_art = cur.getString(0);
            }
            cur.close();
        }
        return album_art;
    }
}
