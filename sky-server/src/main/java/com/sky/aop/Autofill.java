package com.sky.aop;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import javassist.bytecode.SignatureAttribute;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 切面类，用于执行公共字段的代码填充
 */
@Aspect
@Component
@Slf4j
public class Autofill {

    /**
     * 指定切入点表达式
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void pointCut(){};

    @Before("pointCut()")
    public void autoFillCommonFields(JoinPoint joinPoint) throws InvocationTargetException, IllegalAccessException {
        log.info("开始进行公共字段的填充...");
        //1.首先获取方法上的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();  //方法签名对象
        Method method = signature.getMethod();
        AutoFill annotation = method.getDeclaredAnnotation(AutoFill.class);

        //2.准备好需要插入公共字段的数据
        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime updateTime = LocalDateTime.now();
        Long createUser = BaseContext.getCurrentId();
        Long updateUser = BaseContext.getCurrentId();

        //3.使用反射获取需要操作属性的set方法
        Object object = joinPoint.getArgs();   //获取连接点方法的参数
        Method methodCreateTime = null;
        try {
            methodCreateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Method methodCreateUser = null;
        try {
            methodCreateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Method methodUpdateTime = null;
        try {
            methodUpdateTime = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Method methodUpdateUser = null;
        try {
            methodUpdateUser = object.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        //4.根据注解的不同类型，对方法参数做不同的操作
        if (annotation.value() == OperationType.INSERT)
        {
            methodCreateTime.invoke(object, createTime);
            methodCreateUser.invoke(object, createUser);
        }
        methodUpdateTime.invoke(object, updateTime);
        methodUpdateUser.invoke(object, updateUser);
    }
}
