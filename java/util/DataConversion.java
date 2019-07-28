package com.example.demo8.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 讲指定的sql查询结果指定列和对象属性对应起来
 * （必须加上这一注解，才能在运行时可见）
 * 这个注解的详细处理过程在JDBCDataSourceConfig类里
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DataConversion {
    String value();
}
