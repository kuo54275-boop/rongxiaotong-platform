package com.rongxiaotong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rongxiaotong.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据访问接口：继承 BaseMapper 后自动拥有 select/insert/update/delete 能力
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
