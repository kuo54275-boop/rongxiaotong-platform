package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 注册请求参数：前端注册表单提交的数据
 */
@Data
public class RegisterDTO {

    private String username;   // 登录账号
    private String password;   // 密码（后端用 BCrypt 加密后存库）
    private String realName;   // 真实姓名
    private String phone;      // 手机号
    private String role;       // 选择身份：FARMER / BANK / BUYER / EXPERT
}
