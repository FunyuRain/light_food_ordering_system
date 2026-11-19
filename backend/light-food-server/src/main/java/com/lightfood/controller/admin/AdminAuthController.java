package com.lightfood.controller.admin;

import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.AdminLoginDTO;
import com.lightfood.pojo.vo.LoginVO;
import com.lightfood.service.admin.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        return Result.success(adminAuthService.login(dto));
    }
}
