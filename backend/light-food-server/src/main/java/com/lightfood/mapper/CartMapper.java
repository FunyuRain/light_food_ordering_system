package com.lightfood.mapper;

import com.lightfood.pojo.entity.ShoppingCart;
import com.lightfood.pojo.vo.CartItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {

    List<CartItemVO> selectByUserId(Long userId);

    List<CartItemVO> selectByUserIdForUpdate(Long userId);

    ShoppingCart selectByUserAndDishForUpdate(@Param("userId") Long userId,
                                              @Param("dishId") Long dishId);

    ShoppingCart selectByIdAndUser(@Param("id") Long id,
                                   @Param("userId") Long userId);

    int insert(ShoppingCart cart);

    int updateQuantity(@Param("id") Long id,
                       @Param("userId") Long userId,
                       @Param("quantity") Integer quantity);

    int deleteByIdAndUser(@Param("id") Long id,
                          @Param("userId") Long userId);

    int deleteByUserId(Long userId);
}
