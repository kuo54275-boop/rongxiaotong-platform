package com.rongxiaotong.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：任何接口抛出异常都会到这里，统一转成 JSON 返回，
 * 避免前端收到乱七八糟的错误页面
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：把异常信息直接告诉用户（如"用户名已存在"） */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.error(e.getMessage());
    }

    /** 其他所有异常：打印日志，返回友好提示 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        // 开发阶段把具体报错也返回，方便排查；正式上线可改为固定文案
        return Result.error("系统异常：" + e.getMessage());
    }
}
