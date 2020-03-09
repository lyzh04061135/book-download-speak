package com.demo.utils;

public class CommonUtil {
    public static boolean isBlank(String s) {
        boolean flag=false;
        if (s==null || s.equals("")) {
            return true;
        }
        s=s.trim();
        if (s.equals("")) {
            return true;
        }
        return flag;
    }
}
