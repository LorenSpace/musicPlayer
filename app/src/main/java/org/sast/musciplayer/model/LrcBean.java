package org.sast.musciplayer.model;

public class LrcBean {
    private String lrc;//歌词
    private long start;//开始时间
    private long end;//结束时间

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
