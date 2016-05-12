package com.campus.gomotion.util;

import java.sql.Time;

/**
 * Author: zhong.zhou
 * Date: 16/5/12
 * Email: muxin_zg@163.com
 */
public class TypeConvertUtil {
    /**
     * turn time to double
     *
     * @param t Time
     * @return double
     */
    public static double timeToDouble(Time t) {
        double result, hours, minutes, seconds;
        String string = t.toString();
        String[] strings = string.split(":");
        hours = Double.parseDouble(strings[0]);
        minutes = Double.parseDouble(strings[1]) / (double) (60);
        seconds = Double.parseDouble(strings[2]) / (double) (60 * 60);
        return hours + minutes + seconds;
    }
}
