package org.sast.musciplayer.utils;

import org.sast.musciplayer.model.LrcBean;

import java.util.ArrayList;
import java.util.List;

public class LrcUtil {

    /**
     * 解析歌词，将字符串歌词封装成LrcBean的集合
     *
     * @param lrcStr 字符串的歌词，歌词有固定的格式，一般为
     *               [ti:喜欢你]
     *               [ar:.]
     *               [al:]
     *               [by:]
     *               [offset:0]
     *               [00:00.10]喜欢你 - G.E.M. 邓紫棋 (Gem Tang)
     *               [00:00.20]词：黄家驹
     *               [00:00.30]曲：黄家驹
     *               [00:00.40]编曲：Lupo Groinig
     * @return 歌词集合
     */
    public static List<LrcBean> parseStr2List(String lrcStr) {
        List<LrcBean> res = new ArrayList<>();
        //根据转行字符对字符串进行分割
        String[] subLrc = lrcStr.split("\n");
        //跳过前四行，从第五行开始，因为前四行的歌词我们并不需要
        for (int i = 5; i < subLrc.length; i++) {
            String lineLrc = subLrc[i];
            //[00:00.10]喜欢你 - G.E.M. 邓紫棋 (Gem Tang)
            String min = lineLrc.substring(lineLrc.indexOf("[") + 1, lineLrc.indexOf("[") + 3);
            String sec = lineLrc.substring(lineLrc.indexOf(":") + 1, lineLrc.indexOf(":") + 3);
            String mills = lineLrc.substring(lineLrc.indexOf(".") + 1, lineLrc.indexOf(".") + 3);
            //进制转化，转化成毫秒形式的时间
            long startTime = getTime(min, sec, mills);
            //歌词
            String lrcText = lineLrc.substring(lineLrc.indexOf("]") + 1);
            //有可能是某个时间段是没有歌词，则跳过下面
            if (lrcText.equals("")) continue;
            //在第一句歌词中有可能是很长的，我们只截取一部分，即歌曲加演唱者
            //比如 光年之外 (《太空旅客（Passengers）》电影中国区主题曲) - G.E.M. 邓紫棋 (Gem Tang)
            if (i == 5) {
                int lineIndex = lrcText.indexOf("-");
                int first = lrcText.indexOf("(");
                if (first < lineIndex && first != -1) {
                    lrcText = lrcText.substring(0, first) + lrcText.substring(lineIndex);
                }
                LrcBean lrcBean = new LrcBean();
                lrcBean.setStart(startTime);
                lrcBean.setLrc(lrcText);
                res.add(lrcBean);
                continue;
            }
            //添加到歌词集合中
            LrcBean lrcBean = new LrcBean();
            lrcBean.setStart(startTime);
            lrcBean.setLrc(lrcText);
            res.add(lrcBean);
            //如果是最后一句歌词，其结束时间是不知道的，我们将人为的设置为开始时间加上 100s
            if (i == subLrc.length - 1) {
                res.get(res.size() - 1).setEnd(startTime + 100000);
            } else if (res.size() > 1) {
                //当集合数目大于 1 时，这句的歌词的开始时间就是上一句歌词的结束时间
                res.get(res.size() - 2).setEnd(startTime);
            }

        }
        return res;
    }

    /**
     * 根据时分秒获得总时间
     *
     * @param min   分钟
     * @param sec   秒
     * @param mills 毫秒
     * @return 总时间
     */
    private static long getTime(String min, String sec, String mills) {
        return Long.parseLong(min) * 60 * 1000 + Long.parseLong(sec) * 1000 + Long.parseLong(mills);
    }
}
