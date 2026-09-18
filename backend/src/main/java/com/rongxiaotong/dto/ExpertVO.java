package com.rongxiaotong.dto;

import com.rongxiaotong.entity.ExpertProfile;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专家展示对象：专家信息 + 姓名头像
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExpertVO extends ExpertProfile {

    private String realName;   // 专家姓名
    private String avatar;     // 头像
}
