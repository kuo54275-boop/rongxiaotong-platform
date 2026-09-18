package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 登录成功返回的数据：用户信息 + 令牌
 */
@Data
public class LoginVO {

    private Long id;
    private String username;
    private String realName;
    private String avatar;
    private String role;
    private String token;   // JWT 令牌，前端保存后每次请求携带
}
