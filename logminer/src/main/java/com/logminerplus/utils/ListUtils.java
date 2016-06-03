package com.logminerplus.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils<T> {

    public static String listToString(List<String> list) {
        String str = "";
        if (list != null && list.size() > 0) {
            for (String string : list) {
                str += string + ",";
            }
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static String arrayToString(String[] array) {
        String str = "";
        if (array != null && array.length > 0) {
            for (String string : array) {
                str += string + ",";
            }
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * 将传入的集合以30000为单位拆分为该类型集合的数组 sublist：(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选)
     * 创建人：dingkang 修改人：dingkang
     * 
     * @param list
     * @return List<T>[]
     * @exception
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public List<T>[] sublist(List<T> list) {
        int size = 0;
        if (list != null && list.size() > 0) {
            size = list.size();
        } else {
            size = 1;
        }
        int sheets = size / 30000 + 1;
        List<T>[] result = new List[sheets];
        if (size / 30000 > 0) {
            for (int i = 1; i < sheets; i++) {
                List<T> sheet = new ArrayList<T>();
                sheet = list.subList(30000 * (i - 1), 30000 * i);
                result[i - 1] = sheet;
            }
            List<T> last = new ArrayList<T>();
            last = list.subList(30000 * (sheets - 1), size);
            result[sheets - 1] = last;
        } else {
            result[0] = list;
        }
        return result;
    }

    /**
     * 拆分集合 sublist：(这里用一句话描述这个方法的作用) (这里描述这个方法适用条件 – 可选) 创建人：dingkang
     * 修改人：dingkang
     * 
     * @param list
     * @param length
     * @return List<T>[]拆分后的集合数组
     * @exception
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public List<T>[] sublist(List<T> list, int length) {
        int size = 0;
        if (list != null && list.size() > 0) {
            size = list.size();
        } else {
            size = 1;
        }
        int sheets = size / length + 1;
        List<T>[] result = new List[sheets];
        if (size / length > 0) {
            for (int i = 1; i < sheets; i++) {
                List<T> sheet = new ArrayList<T>();
                sheet = list.subList(length * (i - 1), length * i);
                result[i - 1] = sheet;
            }
            List<T> last = new ArrayList<T>();
            last = list.subList(length * (sheets - 1), size);
            result[sheets - 1] = last;
        } else {
            result[0] = list;
        }
        return result;
    }
}
