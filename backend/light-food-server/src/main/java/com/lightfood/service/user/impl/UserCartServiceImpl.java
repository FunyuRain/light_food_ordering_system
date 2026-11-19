package com.lightfood.service.user.impl;

import com.lightfood.common.context.BaseContext;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.mapper.CartMapper;
import com.lightfood.mapper.DishMapper;
import com.lightfood.pojo.dto.CartAddDTO;
import com.lightfood.pojo.entity.Dish;
import com.lightfood.pojo.entity.ShoppingCart;
import com.lightfood.pojo.vo.CartItemVO;
import com.lightfood.pojo.vo.CartVO;
import com.lightfood.service.user.UserCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCartServiceImpl implements UserCartService {

    private static final int MAX_QUANTITY = 99;

    private final CartMapper cartMapper;
    private final DishMapper dishMapper;

    @Override
    public CartVO getCart() {
        return buildCart(BaseContext.getUserId());
    }

    @Override
    @Transactional
    public CartVO add(CartAddDTO dto) {
        Long userId = BaseContext.getUserId();
        Dish dish = dishMapper.selectOnSaleById(dto.getDishId());
        if (dish == null) {
            throw new BusinessException("菜品不存在或已下架");
        }
        ShoppingCart existing = cartMapper.selectByUserAndDishForUpdate(userId, dto.getDishId());
        if (existing == null) {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setDishId(dto.getDishId());
            cart.setQuantity(dto.getQuantity());
            cartMapper.insert(cart);
        } else {
            int newQuantity = existing.getQuantity() + dto.getQuantity();
            if (newQuantity > MAX_QUANTITY) {
                throw new BusinessException("单个菜品最多购买99份");
            }
            cartMapper.updateQuantity(existing.getId(), userId, newQuantity);
        }
        return buildCart(userId);
    }

    @Override
    @Transactional
    public CartVO updateQuantity(Long id, Integer quantity) {
        Long userId = BaseContext.getUserId();
        if (cartMapper.selectByIdAndUser(id, userId) == null) {
            throw new BusinessException("购物车记录不存在");
        }
        int rows = cartMapper.updateQuantity(id, userId, quantity);
        if (rows == 0) {
            throw new BusinessException("购物车记录不存在");
        }
        return buildCart(userId);
    }

    @Override
    @Transactional
    public CartVO remove(Long id) {
        Long userId = BaseContext.getUserId();
        int rows = cartMapper.deleteByIdAndUser(id, userId);
        if (rows == 0) {
            throw new BusinessException("购物车记录不存在");
        }
        return buildCart(userId);
    }

    @Override
    @Transactional
    public CartVO clear() {
        Long userId = BaseContext.getUserId();
        cartMapper.deleteByUserId(userId);
        return buildCart(userId);
    }

    private CartVO buildCart(Long userId) {
        List<CartItemVO> items = cartMapper.selectByUserId(userId);
        if (items == null) {
            items = Collections.emptyList();
        }
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemVO item : items) {
            totalQuantity += item.getQuantity();
            totalAmount = totalAmount.add(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
        }
        CartVO cart = new CartVO();
        cart.setItems(items);
        cart.setTotalQuantity(totalQuantity);
        cart.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        return cart;
    }
}
