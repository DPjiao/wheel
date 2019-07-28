package com.example.demo8.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 需要过程处理的转换
 * （必须加上这一注解，才能在运行时可见）
 * 这个注解的详细处理过程在JDBCDataSourceConfig类里
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ProcessConversion {
    String value();
    Class obj();
    String method();
}
