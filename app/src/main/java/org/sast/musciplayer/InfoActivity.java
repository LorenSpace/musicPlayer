package org.sast.musciplayer;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.sast.musciplayer.dao.LocalMusicDao;
import org.sast.musciplayer.model.LocalMusicBean;
import org.sast.musciplayer.model.LrcBean;
import org.sast.musciplayer.utils.LrcUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class InfoActivity extends AppCompatActivity implements View.OnClickListener {
    private final static int[] modeIcon = {
            R.drawable.ic_mode_list_loop,
            R.drawable.ic_mode_single_loop,
            R.drawable.ic_song_random_mode
    };
    private static int modeIndex = 0;
    private MediaPlayer mediaPlayer;
    // 图像控件：
    private ImageView coverImage;
    private SeekBar seekBar;
    private TextView titleText, songText, singerText, startText, durationText;
    // 按钮：播放、下一首、上一首
    private TextView lrcText;
    private ImageButton playIb;
    private ImageButton modeIb;
    private ImageButton songLikeIb;
    // 数据源 自定义类数组 存放各歌曲信息
    private List<LocalMusicBean> mDatas;
    // 记录当前正在播放的音乐的位置
    private int currentPlayPosition = -1;
    // 记录暂停音乐时进度条的位置
    private int currentPausePositionInSong = 0;
    private List<LrcBean> lrcList;
    private int currentIndex = 0;
    private LocalMusicBean bean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        initView();
        LocalMusicDao dao = new LocalMusicDao(this);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(false);

        Bundle bundle = getIntent().getExtras();
        String type = bundle.getString("type");
        if (type != null) {
            Log.d("type", type);
        }
        bean = (LocalMusicBean) bundle.getSerializable("music");
        if (bean != null) {
            Log.d("bean", bean.toString());
        }
        if (type.equals("normal")) {
            mDatas = dao.getMusicList();
            titleText.setText("本地音乐");
        } else if (type.equals("like")) {
            String selection = "status = ?";
            List<String> args = new ArrayList<>();
            args.add("1");
            mDatas = dao.getMusicByFilter(selection, args);
            titleText.setText("我的收藏");
        }

        for (modeIndex = 0; modeIndex < 3; modeIndex++) {
            // 获取 modeIb 中 src id
            Drawable drawable = ContextCompat.getDrawable(this, modeIcon[modeIndex]);
            if (drawable != null && modeIb.getDrawable() != null &&
                    drawable.getConstantState() != null &&
                    drawable.getConstantState().equals(modeIb.getDrawable().getConstantState())) {
                break;
            }
        }
        Log.d("modeIndex", String.valueOf(modeIndex));

        modeIb.setImageResource(modeIcon[modeIndex]);
        modeIb.setOnClickListener(this);

        for (currentPlayPosition = 0; currentPlayPosition < mDatas.size(); currentPlayPosition++) {
            if (mDatas.get(currentPlayPosition).equals(bean)) {
                break;
            }
        }

        // 这个功能有问题
//        mediaPlayer.setOnCompletionListener(mp -> {
//            // 播放完成后自动切换到下一首
//            currentPlayPosition = (currentPlayPosition + 1) % mDatas.size();
//            LocalMusicBean nextBean = mDatas.get(currentPlayPosition);
//            stopMusic();
//            playMusicInMusicBean(nextBean);
//        });


        if (bean != null) {
            playMusicInMusicBean(bean);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 保存当前的播放进度
        SharedPreferences.Editor editor =
                getSharedPreferences("music", MODE_PRIVATE).edit();
        editor.putInt(bean.getSong(), mediaPlayer.getCurrentPosition());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 读取保存的播放进度并恢复播放
        SharedPreferences preferences = getSharedPreferences("music", MODE_PRIVATE);
        int progress = preferences.getInt(bean.getSong(), 0);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(progress);
        }
    }

    private void initView() {
        /* 初始化控件 */
        titleText = findViewById(R.id.info_title_text);
        songText = findViewById(R.id.song_text);
        singerText = findViewById(R.id.singer_text);
        coverImage = findViewById(R.id.cover_image);
        seekBar = findViewById(R.id.music_seek_bar);
        startText = findViewById(R.id.song_current_time_text);
        durationText = findViewById(R.id.song_duration_text);
        modeIb = findViewById(R.id.mode_button);
        ImageButton prevIb = findViewById(R.id.prev_button);
        playIb = findViewById(R.id.play_button);
        ImageButton nextIb = findViewById(R.id.next_button);
        songLikeIb = findViewById(R.id.info_song_like);
        lrcText = findViewById(R.id.lrc_text);

        prevIb.setOnClickListener(this);
        playIb.setOnClickListener(this);
        nextIb.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void updateLrc(int time) {
        if (lrcList == null || lrcList.isEmpty()) {
            return;
        }
        if (currentIndex < lrcList.size() - 1 && time >= lrcList.get(currentIndex + 1).getStart()) {
            currentIndex++;
        }
        if (currentIndex > 0 && time < lrcList.get(currentIndex).getStart()) {
            currentIndex--;
        }
        lrcText.setText(lrcList.get(currentIndex).getLrc());
    }

    private void stopMusic() {
        /* 停止音乐的函数 */
        if (mediaPlayer != null) {
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            // 播放器暂停
            mediaPlayer.seekTo(0);
            // 调至 0
            mediaPlayer.stop();
            // 停止
            playIb.setImageResource(R.drawable.ic_music_play);
            // 显示图标
        }

    }

    private void SeekBarSet() {
        /* 新增 seekbar 进度条 */
        int duration = mediaPlayer.getDuration();
        // 获取视频总时间
        seekBar.setMax(duration);
        // 将音乐总时间设置为 seekbar 的最大值

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        }, 0, 50);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int time_end = mediaPlayer.getDuration() / 1000;
                // 获取歌曲时长
                durationText.setText(calculateTime(time_end));
                int time_now = mediaPlayer.getCurrentPosition() / 1000;
                startText.setText(calculateTime(time_now));
                updateLrc(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    public String calculateTime(int time) {
        int minute, second;
        if (time > 60) {
            minute = time / 60; //分钟取整
            second = time % 60; //秒钟取余
            // 分钟再 0~9
            if (minute < 10) { // 0x:0x
                // 判断秒
                if (second < 10) {
                    return "0" + minute + ":" + "0" + second;
                } else {
                    return "0" + minute + ":" + second;
                }
            } else {
                // 分钟大于 10 再判断秒
                if (second < 10) {
                    return minute + ":" + "0" + second;
                } else {
                    return minute + ":" + second;
                }
            }
        } else if (time < 60) {
            second = time;
            if (second >= 0 && second < 10) {
                return "00:" + "0" + second;
            } else {
                return "00:" + second;
            }
        }
        return null;
    }

    public void playMusicInMusicBean(LocalMusicBean musicBean) { //传入一首歌 信息
        /*根据传入对象播放音乐*/
        // 设置底部显示的歌手名称和歌曲名
        singerText.setText(musicBean.getSinger());    //获取当前信息并设置
        songText.setText(musicBean.getSong());
        stopMusic();

        // 清除之前保存的进度
        SharedPreferences.Editor editor =
                getSharedPreferences("music", MODE_PRIVATE).edit();
        editor.remove(musicBean.getPath());
        editor.apply();

        if (musicBean.getStatus() == 1) {
            songLikeIb.setImageResource(R.drawable.ic_yes_like);
        } else {
            songLikeIb.setImageResource(R.drawable.ic_not_like);
        }
        Log.d("musicBean", musicBean.toString());
        String tempPath = musicBean.getFileName();
        Log.d("tempPath", tempPath);
        String[] tempArray = tempPath.split("\\.");
        Log.d("tempArray", tempArray[0]);

        if (lrcList != null) {
            lrcList.clear();
        }
        // 读取歌词文件并解析歌词
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Documents/" + tempArray[0] + "1.lrc");
            if (file.exists()) {
                InputStream inputStream = Files.newInputStream(file.toPath());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                lrcList = LrcUtil.parseStr2List(stringBuilder.toString());
            } else {
                lrcText.setText("暂无歌词");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 重置多媒体播放器
        mediaPlayer.reset();
        // 设置新的播放路径
        try {
            mediaPlayer.setDataSource(musicBean.getPath());
            String albumArt = musicBean.getAlbumArt();
            Log.i("lsh123", "playMusicInMusicBean: album path==" + albumArt);
            Bitmap bm = BitmapFactory.decodeFile(albumArt);
            Log.i("lsh123", "playMusicInMusicBean: bm==" + bm);
            loadCover(musicBean.getPath());
            playMusic();
            SeekBarSet();   //设置进度条及其时间显示

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseMusic() {
        /* 暂停音乐的函数*/
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentPausePositionInSong = mediaPlayer.getCurrentPosition(); //获取进度条
            mediaPlayer.pause();
            playIb.setImageResource(R.drawable.ic_music_play);
        }
    }

    private void playMusic() {
        /* 播放音乐的函数 */
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (currentPausePositionInSong == 0) {
                // 如果已经停止播放
                try {
                    mediaPlayer.prepare();
                    // 重新准备播放
                    mediaPlayer.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // 从暂停到播放
                mediaPlayer.seekTo(currentPausePositionInSong);
                mediaPlayer.start();
            }
            playIb.setImageResource(R.drawable.ic_music_pause);
        }
    }

    private void loadCover(String path) throws IOException {
        try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
            mediaMetadataRetriever.setDataSource(path);
            byte[] cover = mediaMetadataRetriever.getEmbeddedPicture();
            Bitmap bitmap = null;
            if (cover != null) {
                bitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
            }
            coverImage.setImageBitmap(bitmap);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.prev_button) {
            switch (modeIndex) {
                case 0:
                    if (currentPlayPosition == 0) {
                        currentPlayPosition = mDatas.size() - 1;
                    } else {
                        Log.d("lsh123", "onClick: currentPlayPosition==" + currentPlayPosition);
                        currentPlayPosition = currentPlayPosition - 1;
                        Log.d("lsh123", "onClick: currentPlayPosition==" + currentPlayPosition);
                    }
                    LocalMusicBean lastBean = mDatas.get(currentPlayPosition);
                    playMusicInMusicBean(lastBean);
                    break;
                case 1:
                    LocalMusicBean newBean = mDatas.get(currentPlayPosition);
                    playMusicInMusicBean(newBean);
                    break;
                case 2:
                    Random random = new Random();
                    int newId = 0;
                    do {
                        newId = random.nextInt(mDatas.size());
                    } while (newId == currentPlayPosition);
                    currentPlayPosition = newId;
                    LocalMusicBean randomBean = mDatas.get(currentPlayPosition);
                    playMusicInMusicBean(randomBean);
                    break;
            }
        } else if (id == R.id.next_button) {
            switch (modeIndex) {
                case 0:
                    currentPlayPosition = (currentPlayPosition + 1) % mDatas.size();
                    LocalMusicBean nextBean = mDatas.get(currentPlayPosition);
                    playMusicInMusicBean(nextBean);
                    break;
                case 1:
                    LocalMusicBean lastBean = mDatas.get(currentPlayPosition);
                    playMusicInMusicBean(lastBean);
                    break;
                case 2:
                    Random random = new Random();
                    int newId = 0;
                    do {
                        newId = random.nextInt(mDatas.size());
                    } while (newId == currentPlayPosition);
                    currentPlayPosition = newId;
                    LocalMusicBean randomBean = mDatas.get(currentPlayPosition);
                    playMusicInMusicBean(randomBean);
                    break;
            }

        } else if (id == R.id.play_button) {
            if (currentPlayPosition == -1) {
                Toast.makeText(this, "请选择想要播放的音乐", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mediaPlayer.isPlaying()) {
                pauseMusic();
            } else {
                playMusic();
            }
        } else if (id == R.id.mode_button) {
            modeIndex = (modeIndex + 1) % 3;
            modeIb.setImageResource(modeIcon[modeIndex]);
        }
    }
}