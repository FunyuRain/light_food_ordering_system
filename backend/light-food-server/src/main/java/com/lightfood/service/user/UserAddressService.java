package com.lightfood.service.user;

import com.lightfood.pojo.dto.AddressDTO;
import com.lightfood.pojo.entity.UserAddress;
import com.lightfood.pojo.vo.AddressVO;

import java.util.List;

public interface UserAddressService {
    List<AddressVO> list();

    AddressVO create(AddressDTO dto);

    void setDefault(Long id);

    void delete(Long id);

    UserAddress getOwned(Long id);
}
