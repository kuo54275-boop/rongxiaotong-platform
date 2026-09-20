package com.rongxiaotong.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.PageResult;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.PasswordDTO;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.UserMapper;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务：基础信息、密码修改、管理员用户管理
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    /** 修改基础信息（姓名、电话、头像） */
    public void updateProfile(User form) {
        User me = userMapper.selectById(UserContext.getUserId());
        if (me == null) {
            throw new BizException("用户不存在");
        }
        if (StrUtil.isBlank(form.getRealName())) {
            throw new BizException("姓名不能为空");
        }
        me.setRealName(form.getRealName());
        me.setPhone(form.getPhone());
        if (StrUtil.isNotBlank(form.getAvatar())) {
            me.setAvatar(form.getAvatar());
        }
        userMapper.updateById(me);
    }

    /** 修改密码：校验原密码，新密码加密存储 */
    public void changePassword(PasswordDTO dto) {
        User me = userMapper.selectById(UserContext.getUserId());
        if (me == null) {
            throw new BizException("用户不存在");
        }
        if (StrUtil.isBlank(dto.getOldPassword()) || StrUtil.isBlank(dto.getNewPassword())) {
            throw new BizException("请填写原密码和新密码");
        }
        if (!BCrypt.checkpw(dto.getOldPassword(), me.getPassword())) {
            throw new BizException("原密码不正确");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new BizException("新密码长度不能少于 6 位");
        }
        me.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        userMapper.updateById(me);
    }

    // ==================== 管理员：用户管理 ====================

    /** 用户列表（分页 + 账号/姓名搜索，密码不外传） */
    public PageResult<User> adminUserPage(long page, long size, String keyword) {
        requireAdmin();
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .and(StrUtil.isNotBlank(keyword), w -> w
                        .like(User::getUsername, keyword)
                        .or().like(User::getRealName, keyword))
                .orderByDesc(User::getCreateTime);
        Page<User> p = userMapper.selectPage(new Page<>(page, size), qw);
        p.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(p);
    }

    /** 启用 / 禁用用户（不能操作自己） */
    public void adminToggleStatus(Long id, Integer status) {
        requireAdmin();
        if (id.equals(UserContext.getUserId())) {
            throw new BizException("不能操作自己的账号");
        }
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BizException("用户不存在");
        }
        u.setStatus(status);
        userMapper.updateById(u);
    }

    /** 管理员：查看各角色用户数（简单统计，用户管理页顶部展示） */
    public List<Long> adminRoleCounts() {
        requireAdmin();
        return List.of(
                userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, Roles.FARMER)),
                userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, Roles.BUYER)),
                userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, Roles.EXPERT)),
                userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, Roles.BANK))
        );
    }

    private void requireAdmin() {
        if (!UserContext.isRole(Roles.ADMIN)) {
            throw new BizException("只有管理员可以操作");
        }
    }
}
