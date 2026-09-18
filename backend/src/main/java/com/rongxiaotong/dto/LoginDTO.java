package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 登录请求参数：前端表单提交 { username, password }
 */
@Data
public class LoginDTO {

    private String username;
    private String password;
}
