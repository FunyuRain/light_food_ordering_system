package com.lightfood.service.user;

import com.lightfood.pojo.dto.UserLoginDTO;
import com.lightfood.pojo.dto.UserRegisterDTO;
import com.lightfood.pojo.vo.LoginVO;

public interface UserAuthService {
    LoginVO login(UserLoginDTO dto);

    LoginVO register(UserRegisterDTO dto);
}
