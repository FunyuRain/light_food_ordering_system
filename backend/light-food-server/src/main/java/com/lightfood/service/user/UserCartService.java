package com.lightfood.service.user;

import com.lightfood.pojo.dto.CartAddDTO;
import com.lightfood.pojo.vo.CartVO;

public interface UserCartService {
    CartVO getCart();

    CartVO add(CartAddDTO dto);

    CartVO updateQuantity(Long id, Integer quantity);

    CartVO remove(Long id);

    CartVO clear();
}
