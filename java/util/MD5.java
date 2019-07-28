package com.example.demo8.util;

import org.springframework.util.DigestUtils;

public class MD5 {
    /**
     * 传入处理的字符串，得到MD5字符串
     * @param parameter
     * @return
     */
    public static String toMD5(String parameter){
        return DigestUtils.md5DigestAsHex(parameter.getBytes());
    }
}
