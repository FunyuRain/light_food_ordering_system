package com.lightfood.controller;

import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.AdminRegisterDTO;
import com.lightfood.pojo.dto.UserRegisterDTO;
import com.lightfood.pojo.vo.LoginVO;
import com.lightfood.service.admin.AdminAuthService;
import com.lightfood.service.user.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public registration endpoints. They are outside the /user and /admin
 * interceptor prefixes and therefore do not require a JWT.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserAuthService userAuthService;
    private final AdminAuthService adminAuthService;

    @PostMapping("/user/register")
    public Result<LoginVO> registerUser(@Valid @RequestBody UserRegisterDTO dto) {
        return Result.success(userAuthService.register(dto));
    }

    @PostMapping("/admin/register")
    public Result<LoginVO> registerAdmin(@Valid @RequestBody AdminRegisterDTO dto) {
        return Result.success(adminAuthService.register(dto));
    }
}
