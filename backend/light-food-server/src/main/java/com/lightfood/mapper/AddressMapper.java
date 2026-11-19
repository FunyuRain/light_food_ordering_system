package com.lightfood.mapper;

import com.lightfood.pojo.entity.UserAddress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressMapper {

    List<UserAddress> selectByUserId(Long userId);

    UserAddress selectByIdAndUser(@Param("id") Long id,
                                  @Param("userId") Long userId);

    UserAddress selectFirstByUserId(Long userId);

    int countByUserId(Long userId);

    int insert(UserAddress address);

    int clearDefaults(Long userId);

    int setDefault(@Param("id") Long id,
                   @Param("userId") Long userId);

    int deleteByIdAndUser(@Param("id") Long id,
                          @Param("userId") Long userId);
}
