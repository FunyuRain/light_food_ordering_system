package com.lightfood.service.user.impl;

import com.lightfood.common.constant.Role;
import com.lightfood.common.context.LoginUser;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.common.util.JwtUtil;
import com.lightfood.mapper.UserMapper;
import com.lightfood.pojo.dto.UserLoginDTO;
import com.lightfood.pojo.dto.UserRegisterDTO;
import com.lightfood.pojo.entity.User;
import com.lightfood.pojo.vo.LoginVO;
import com.lightfood.service.user.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(UserLoginDTO dto) {
        String login = dto.getUsername().trim();
        User user = userMapper.selectByLogin(login);
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("手机号/账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("用户账号已被禁用");
        }
        userMapper.updateLastLogin(user.getId(), LocalDateTime.now());

        String name = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
        LoginUser principal = new LoginUser(user.getId(), Role.USER, name);
        return new LoginVO(user.getId(), user.getUsername(), name, Role.USER, jwtUtil.generate(principal));
    }

    @Override
    @Transactional
    public LoginVO register(UserRegisterDTO dto) {
        String username = dto.getUsername().trim();
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        // selectByLogin also checks the phone column, preventing ambiguous login identifiers.
        if (userMapper.selectByLogin(username) != null) {
            throw new BusinessException("用户名已被使用");
        }

        User user = new User();
        user.setUsername(username);
        user.setPhone(null);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(username);
        user.setAvatarUrl(null);
        user.setStatus(1);
        userMapper.insert(user);
        userMapper.updateLastLogin(user.getId(), LocalDateTime.now());

        LoginUser principal = new LoginUser(user.getId(), Role.USER, username);
        return new LoginVO(user.getId(), username, username, Role.USER, jwtUtil.generate(principal));
    }
}
