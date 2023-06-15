package org.sast.musciplayer.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sast.musciplayer.R;
import org.sast.musciplayer.adapter.LocalMusicAdapter;
import org.sast.musciplayer.dao.LocalMusicDao;
import org.sast.musciplayer.model.LocalMusicBean;

import java.util.ArrayList;
import java.util.List;

public class LoveFragment extends Fragment {
    private List<LocalMusicBean> mDatas;

    public LoveFragment() {
        // Required empty public constructor
        mDatas = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_love, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化数据库
        LocalMusicDao localMusicDao = new LocalMusicDao(view.getContext());
        String selection = "status = ?";
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add("1");
        mDatas = localMusicDao.getMusicByFilter(selection, selectionArgs);

        // 初始化本地音乐列表
        RecyclerView recyclerView = view.findViewById(R.id.like_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        LocalMusicAdapter adapter = new LocalMusicAdapter(view.getContext(), mDatas, "like");
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}