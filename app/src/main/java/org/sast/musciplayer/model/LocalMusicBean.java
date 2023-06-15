package org.sast.musciplayer.model;

import java.io.Serializable;
import java.util.Objects;

public class LocalMusicBean implements Serializable {
    private int id; // 歌曲 id
    private String song; // 歌曲名称
    private String singer; // 歌手名称
    private String album; // 专辑名称
    private String duration; // 歌曲时长
    private String path; // 歌曲路径
    private String albumArt;  // 专辑地址（无法获取）
    private int status; // 是否喜欢
    private String fileName; // 文件名

    public LocalMusicBean(String song, String singer, String album,
                          String duration, String path, String albumArt,
                          int status, String fileName) {
        this.song = song;
        this.singer = singer;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.albumArt = albumArt;
        this.status = status;
        this.fileName = fileName;
    }

    public LocalMusicBean(int id, String song, String singer,
                          String album, String duration,
                          String path, String albumArt,
                          int status, String fileName) {
        this.id = id;
        this.song = song;
        this.singer = singer;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.albumArt = albumArt;
        this.status = status;
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalMusicBean bean = (LocalMusicBean) o;
        return Objects.equals(song, bean.song) &&
                Objects.equals(singer, bean.singer) &&
                Objects.equals(album, bean.album);
    }

    @Override
    public int hashCode() {
        return Objects.hash(song, singer, album);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    @Override
    public String toString() {
        return "LocalMusicBean{" +
                "id=" + id +
                ", song='" + song + '\'' +
                ", singer='" + singer + '\'' +
                ", album='" + album + '\'' +
                ", duration='" + duration + '\'' +
                ", path='" + path + '\'' +
                ", albumArt='" + albumArt + '\'' +
                ", status=" + status +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    public String getFileName() {
        return fileName;
    }
}
