package com.rongxiaotong.dto;

import lombok.Data;

/**
 * 修改密码请求参数
 */
@Data
public class PasswordDTO {

    private String oldPassword;   // 原密码
    private String newPassword;   // 新密码
}
