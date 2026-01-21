package org.jeecg.modules.common.utils;

import org.jeecg.modules.common.constant.AnsiColor;

/**
 * @author Tong Ling
 * @date 2021/9/27
 */
public class Timer {
    private static long start = 0;
    private static long end = 0;
    private static String name = "Timer";

    public static void tic() {
        name = "Timer";
        start = System.currentTimeMillis();
    }

    public static void tic(final String timerName) {
        name = timerName;
        start = System.currentTimeMillis();
    }

    public static void toc() {
        end = System.currentTimeMillis();
        System.out.println(
            AnsiColor.ANSI_RED + "Elapsed time of [" + name + "]: " + (end - start) + " ms." + AnsiColor.ANSI_RESET);
    }
}
