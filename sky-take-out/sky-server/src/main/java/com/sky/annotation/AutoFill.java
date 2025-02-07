package com.sky.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sky.enumeration.OperationType;

/**
 * 自定义注解，用于标识某个方法需要进行公共字段自动填充处理
 */
@Target({ElementType.METHOD}) // 指定注解可以应用于方法
@Retention(RetentionPolicy.RUNTIME) // 指定注解在运行时仍然可用
public @interface AutoFill {
    // 指定当前数据库的操作的类型(common的enumeration中的OperationType枚举类)
    // 数据库操作类型，UPDATE INSERT
    OperationType value();
}
