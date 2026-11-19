package com.lightfood.service.admin.impl;

import com.lightfood.common.constant.Role;
import com.lightfood.common.context.LoginUser;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.common.util.JwtUtil;
import com.lightfood.mapper.AdminMapper;
import com.lightfood.pojo.dto.AdminLoginDTO;
import com.lightfood.pojo.dto.AdminRegisterDTO;
import com.lightfood.pojo.entity.Admin;
import com.lightfood.pojo.vo.LoginVO;
import com.lightfood.service.admin.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(AdminLoginDTO dto) {
        String username = dto.getUsername().trim();
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null || !passwordEncoder.matches(dto.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException("管理员账号已被禁用");
        }
        adminMapper.updateLastLogin(admin.getId(), LocalDateTime.now());

        String name = StringUtils.hasText(admin.getRealName()) ? admin.getRealName() : admin.getUsername();
        LoginUser principal = new LoginUser(admin.getId(), Role.ADMIN, name);
        return new LoginVO(admin.getId(), admin.getUsername(), name, Role.ADMIN, jwtUtil.generate(principal));
    }

    @Override
    @Transactional
    public LoginVO register(AdminRegisterDTO dto) {
        String username = dto.getUsername().trim();
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        if (adminMapper.selectByUsername(username) != null) {
            throw new BusinessException("管理员用户名已被使用");
        }

        String realName = StringUtils.hasText(dto.getRealName()) ? dto.getRealName().trim() : username;
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        admin.setRealName(realName);
        admin.setPhone(null);
        admin.setStatus(1);
        adminMapper.insert(admin);
        adminMapper.updateLastLogin(admin.getId(), LocalDateTime.now());

        LoginUser principal = new LoginUser(admin.getId(), Role.ADMIN, realName);
        return new LoginVO(admin.getId(), username, realName, Role.ADMIN, jwtUtil.generate(principal));
    }
}
