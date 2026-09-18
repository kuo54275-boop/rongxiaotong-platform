package com.rongxiaotong.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户表：四类角色（农户/银行/买家/技术专家）+ 管理员共用一张表，用 role 字段区分
 */
@Data
@TableName("users")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;      // 登录账号
    private String password;      // BCrypt 加密后的密码（数据库里不存明文）
    private String realName;      // 真实姓名
    private String phone;         // 手机号
    private String avatar;        // 头像图片路径
    private String role;          // FARMER 农户 / BANK 银行 / BUYER 买家 / EXPERT 专家 / ADMIN 管理员
    private Integer status;       // 1 正常 / 0 禁用
    private String createTime;    // 注册时间
}
