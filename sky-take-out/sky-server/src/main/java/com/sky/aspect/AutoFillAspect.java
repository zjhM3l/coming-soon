package com.sky.aspect;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;

import lombok.extern.slf4j.Slf4j;

/*
 * 自定义切面类，实现公共字段自动填充处理
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点（对哪些类的哪些方法进行增强处理）
     */
    // 切入点：com.sky.mapper包下的所有类的所有方法中被@AutoFill注解标记的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
        
    }

    /**
     * 前置通知：增强处理（在通知中进行公共字段的赋值）
     */
    // 通知有很多种：前置、后置、环绕、异常、最终...这里用前置
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        // 1.获取当前被拦截的方法上的数据库操作类型（INSERT/UPDATE）
        // 方法签名对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); 
        // 获取方法上的@AutoFill注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class); 
        // 获取操作类型
        OperationType operationType = autoFill.value();

        // 2.获取方法的参数（实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        // 获取第一个参数，约定实体对象在第一个参数位置
        // 注意这里用的是Object类型，不是具体的实体类类型，增加了代码的通用性
        Object entity = args[0];

        // 3.为实体对象的公共字段赋值
        // 3.a准备公共字段的值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId(); // 还是用ThreadLocal获取当前登录用户id

        // 3.b根据操作类型，给实体对应的属性通过反射赋值
        if (operationType == OperationType.INSERT) {
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod("setCreateUser", Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod("setUpdateUser", Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
