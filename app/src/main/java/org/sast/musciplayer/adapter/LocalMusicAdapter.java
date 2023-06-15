package org.sast.musciplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.sast.musciplayer.InfoActivity;
import org.sast.musciplayer.R;
import org.sast.musciplayer.dao.LocalMusicDao;
import org.sast.musciplayer.model.LocalMusicBean;

import java.util.List;

public class LocalMusicAdapter extends
        RecyclerView.Adapter<LocalMusicAdapter.LocalMusicViewHolder> {

    private final Context context;
    private final List<LocalMusicBean> mDatas;
    private final String type;

    public LocalMusicAdapter(Context context,
                             List<LocalMusicBean> mDatas,
                             String type) {
        this.context = context;
        this.mDatas = mDatas;
        this.type = type;
    }


    @NonNull
    @Override
    public LocalMusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_card, parent, false);
        return new LocalMusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalMusicViewHolder holder, int position) {
        LocalMusicDao dao = new LocalMusicDao(context);
        LocalMusicBean localMusicBean = mDatas.get(position);
        holder.numberTextView.setText(String.valueOf(position + 1));
        holder.songNameTextView.setText(localMusicBean.getSong());
        holder.songArtistTextView.setText(localMusicBean.getSinger());
        holder.songAlubmTextView.setText(localMusicBean.getAlbum());
        holder.songTimeTextView.setText(localMusicBean.getDuration());
        holder.songNameTextView.setOnClickListener(v -> {
            Intent intent = new Intent(context, InfoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("type", type);
            bundle.putSerializable("music", localMusicBean);
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
        holder.songLikeButton.setOnClickListener(v -> {
            if (localMusicBean.getStatus() == 0) {
                localMusicBean.setStatus(1);
                holder.songLikeButton.setImageResource(R.drawable.ic_yes_like);
            } else {
                localMusicBean.setStatus(0);
                holder.songLikeButton.setImageResource(R.drawable.ic_not_like);
            }
            dao.updateMusic(localMusicBean);
        });
        if (localMusicBean.getStatus() == 0) {
            holder.songLikeButton.setImageResource(R.drawable.ic_not_like);
        } else {
            holder.songLikeButton.setImageResource(R.drawable.ic_yes_like);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public static class LocalMusicViewHolder extends RecyclerView.ViewHolder {
        public TextView numberTextView;
        public TextView songNameTextView;
        public TextView songArtistTextView;
        public TextView songAlubmTextView;
        public TextView songTimeTextView;
        public ImageButton songLikeButton;

        public LocalMusicViewHolder(@NonNull View itemView) {
            super(itemView);
            numberTextView = itemView.findViewById(R.id.song_number);
            songNameTextView = itemView.findViewById(R.id.song_name);
            songArtistTextView = itemView.findViewById(R.id.song_artist);
            songAlubmTextView = itemView.findViewById(R.id.song_album);
            songTimeTextView = itemView.findViewById(R.id.song_time);
            songLikeButton = itemView.findViewById(R.id.song_like_button);
        }
    }
}
