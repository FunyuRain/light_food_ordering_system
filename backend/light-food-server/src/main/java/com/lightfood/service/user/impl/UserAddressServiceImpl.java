package com.lightfood.service.user.impl;

import com.lightfood.common.context.BaseContext;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.mapper.AddressMapper;
import com.lightfood.pojo.dto.AddressDTO;
import com.lightfood.pojo.entity.UserAddress;
import com.lightfood.pojo.vo.AddressVO;
import com.lightfood.service.user.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<AddressVO> list() {
        return addressMapper.selectByUserId(BaseContext.getUserId())
                .stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public AddressVO create(AddressDTO dto) {
        Long userId = BaseContext.getUserId();
        boolean firstAddress = addressMapper.countByUserId(userId) == 0;
        boolean makeDefault = firstAddress || Boolean.TRUE.equals(dto.getIsDefault());

        if (makeDefault) {
            addressMapper.clearDefaults(userId);
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiverName(dto.getReceiverName().trim());
        address.setReceiverPhone(dto.getReceiverPhone().trim());
        address.setProvince(dto.getProvince().trim());
        address.setCity(dto.getCity().trim());
        address.setDistrict(dto.getDistrict().trim());
        address.setDetailAddress(dto.getDetailAddress().trim());
        address.setLabel(dto.getLabel() == null ? null : dto.getLabel().trim());
        address.setIsDefault(makeDefault ? 1 : 0);
        addressMapper.insert(address);
        return toVO(address);
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        Long userId = BaseContext.getUserId();
        if (addressMapper.selectByIdAndUser(id, userId) == null) {
            throw new BusinessException("地址不存在");
        }
        addressMapper.clearDefaults(userId);
        if (addressMapper.setDefault(id, userId) == 0) {
            throw new BusinessException("地址不存在");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = BaseContext.getUserId();
        UserAddress address = addressMapper.selectByIdAndUser(id, userId);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        boolean wasDefault = address.getIsDefault() != null && address.getIsDefault() == 1;
        addressMapper.deleteByIdAndUser(id, userId);
        if (wasDefault) {
            UserAddress next = addressMapper.selectFirstByUserId(userId);
            if (next != null) {
                addressMapper.setDefault(next.getId(), userId);
            }
        }
    }

    @Override
    public UserAddress getOwned(Long id) {
        UserAddress address = addressMapper.selectByIdAndUser(id, BaseContext.getUserId());
        if (address == null) {
            throw new BusinessException("收货地址不存在");
        }
        return address;
    }

    private AddressVO toVO(UserAddress address) {
        AddressVO vo = new AddressVO();
        vo.setId(address.getId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setLabel(address.getLabel());
        vo.setIsDefault(address.getIsDefault());
        vo.setFullAddress(address.getProvince() + address.getCity() + address.getDistrict()
                + address.getDetailAddress());
        return vo;
    }
}
