package com.rongxiaotong.common;

/**
 * 业务异常：业务逻辑出错时手动抛出，例如"用户名已存在"、"库存不足"
 * 会被 GlobalExceptionHandler 捕获并转成友好的 JSON 返回给前端
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
