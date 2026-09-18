package com.rongxiaotong.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rongxiaotong.common.BizException;
import com.rongxiaotong.common.Roles;
import com.rongxiaotong.dto.LoginDTO;
import com.rongxiaotong.dto.LoginVO;
import com.rongxiaotong.dto.RegisterDTO;
import com.rongxiaotong.entity.ExpertProfile;
import com.rongxiaotong.entity.FarmerProfile;
import com.rongxiaotong.entity.User;
import com.rongxiaotong.mapper.ExpertProfileMapper;
import com.rongxiaotong.mapper.FarmerProfileMapper;
import com.rongxiaotong.mapper.UserMapper;
import com.rongxiaotong.util.JwtUtil;
import com.rongxiaotong.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 账号服务：注册、登录、获取当前用户信息
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final FarmerProfileMapper farmerProfileMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final JwtUtil jwtUtil;

    /** 注册：创建账号，农户/专家自动初始化对应的档案 */
    public LoginVO register(RegisterDTO dto) {
        // 1. 参数校验
        if (StrUtil.isBlank(dto.getUsername()) || StrUtil.isBlank(dto.getPassword())) {
            throw new BizException("账号和密码不能为空");
        }
        if (dto.getPassword().length() < 6) {
            throw new BizException("密码长度不能少于 6 位");
        }
        if (!StrUtil.equalsAny(dto.getRole(), Roles.FARMER, Roles.BANK, Roles.BUYER, Roles.EXPERT)) {
            throw new BizException("请选择正确的注册身份");
        }
        // 2. 检查账号是否已被注册
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BizException("该账号已被注册");
        }
        // 3. 创建用户（密码用 BCrypt 加密，数据库不存明文）
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setStatus(1);
        user.setAvatar("/api/images/avatar-" + dto.getRole().toLowerCase() + ".svg");
        user.setCreateTime(DateUtil.now());
        userMapper.insert(user);
        // 4. 按角色初始化档案
        if (Roles.FARMER.equals(dto.getRole())) {
            FarmerProfile fp = new FarmerProfile();
            fp.setUserId(user.getId());
            fp.setCreditScore(80);   // 新农户默认信用分 80
            farmerProfileMapper.insert(fp);
        }
        if (Roles.EXPERT.equals(dto.getRole())) {
            ExpertProfile ep = new ExpertProfile();
            ep.setUserId(user.getId());
            ep.setRating(5.0);
            ep.setStatus(1);
            expertProfileMapper.insert(ep);
        }
        return buildLoginVO(user);
    }

    /** 登录：校验账号密码，成功则签发令牌 */
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        // 用户不存在或密码错误，统一提示（不暴露具体是哪个错，更安全）
        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("账号已被禁用，请联系管理员");
        }
        return buildLoginVO(user);
    }

    /** 获取当前登录用户信息（密码不外传） */
    public User info() {
        User user = userMapper.selectById(UserContext.getUserId());
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /** 组装登录返回：用户信息 + JWT 令牌 */
    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setToken(jwtUtil.createToken(user.getId(), user.getRole(), user.getUsername()));
        return vo;
    }
}
