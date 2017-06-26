package android.study.leer.coolweather.util;

import java.util.Random;

/**
 * Created by Leer on 2017/6/26.
 */

public class DayWords {
    //注意种子数和生成随机数的区间无关
    private static Random mRandom = new Random();
    private static String[] mDayWords = new String[]{
            "聪明人是快乐的，自以为聪明的才烦恼。帮助别人减轻三分烦恼，自己就会享受七分快乐",
            "每个人都有潜在的能量，只是很容易被习惯所掩盖，被时间所迷离，被惰性所消磨",
            "有理想在的地方，地狱就是天堂。有希望在的地方，痛苦也成欢乐",
            "不要对挫折叹气，姑且把这一切看成是在你成大事之前，必须经受的准备工作",
            "河流之所以能够到达目的地，是因为它懂得怎样避开障碍",
            "一个成功的竞争者应该经得起风雨，应该具有抗挫折的能力。在竞争中流泪是弱者，只有在困境中奋起，才能成为强者",
            "成功沒有捷径，历史上有成就的人，总是敢于行动，也会经常失败。不要让对失败的恐惧，绊住你尝试新事物的脚步",
            "学会理解，因为只有理解别人，才会被别人理解",
            "学会忘记，因为只有忘记已经失去的才能立足当前，展望未来",
            "学会快乐，因为只有开心度过每一天，活得才精彩"
    };

    public static String getDayWord() {
        int index = mRandom.nextInt(10);
        if (index < 0) {
            index = 0;
        } else if (index > mDayWords.length - 1) {
            index = mDayWords.length-1;
        }

        return mDayWords[index];
    }
}
