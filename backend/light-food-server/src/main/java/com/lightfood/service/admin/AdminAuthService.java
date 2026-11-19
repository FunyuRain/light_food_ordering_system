package com.lightfood.service.admin;

import com.lightfood.pojo.dto.AdminLoginDTO;
import com.lightfood.pojo.dto.AdminRegisterDTO;
import com.lightfood.pojo.vo.LoginVO;

public interface AdminAuthService {
    LoginVO login(AdminLoginDTO dto);

    LoginVO register(AdminRegisterDTO dto);
}
