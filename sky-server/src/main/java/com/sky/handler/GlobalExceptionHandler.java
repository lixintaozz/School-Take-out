package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice           //@ResponseBody + @ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler(BaseException.class)
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获员工重复注册异常
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex)
    {
        String message = ex.getMessage();
        if (message.contains("Duplicate entry"))
        {
            String[] strs = message.split(" ");
            String error_message = strs[2] + MessageConstant.ALREADY_EXISTS;
            log.info("异常信息：{}", error_message);
            return Result.error(error_message);
        }else
            return Result.error(MessageConstant.UNKNOWN_ERROR);
    }


}


/*
 *  Spring的事务管理
 *  通过在service层的方法上添加@Transactional来实现，默认只能处理Runtime Exception及其子类
 *  但是可以通过指定@Transactional的rollbackFor属性来处理其他类型的异常
 * */
