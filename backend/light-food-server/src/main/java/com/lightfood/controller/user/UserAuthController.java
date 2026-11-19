package com.lightfood.controller.user;

import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.UserLoginDTO;
import com.lightfood.pojo.vo.LoginVO;
import com.lightfood.service.user.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userAuthService.login(dto));
    }
}
