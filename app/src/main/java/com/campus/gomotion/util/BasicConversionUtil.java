package com.campus.gomotion.util;

/**
 * Author: zhong.zhou
 * Date: 16/4/24
 * Email: muxin_zg@163.com
 */
public class BasicConversionUtil {
    /**
     * tow bytes combine to short
     *
     * @param a byte
     * @param b byte
     * @return short
     */
    public static short combine(byte a, byte b) {
        return (short) (((short) ((short) a << 8)) | (short) b);
    }

    /**
     * transfer fixed short to float
     *
     * @param fixedValue fixed short
     * @return float
     */
    public static float fixedToFloat(short fixedValue) {
        return ((float) (fixedValue) / (float) (1 << (15)));
    }
}
