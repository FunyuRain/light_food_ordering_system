package com.lightfood.service.user.impl;

import com.lightfood.common.constant.OrderStatus;
import com.lightfood.common.context.BaseContext;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.common.result.PageResult;
import com.lightfood.mapper.CartMapper;
import com.lightfood.mapper.OrderDetailMapper;
import com.lightfood.mapper.OrderMapper;
import com.lightfood.pojo.dto.CancelOrderDTO;
import com.lightfood.pojo.dto.OrderSubmitDTO;
import com.lightfood.pojo.entity.OrderDetail;
import com.lightfood.pojo.entity.Orders;
import com.lightfood.pojo.entity.UserAddress;
import com.lightfood.pojo.vo.CartItemVO;
import com.lightfood.pojo.vo.OrderVO;
import com.lightfood.service.user.UserAddressService;
import com.lightfood.service.user.UserOrderService;
import com.lightfood.util.OrderNoGenerator;
import com.lightfood.util.PageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserOrderServiceImpl implements UserOrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final CartMapper cartMapper;
    private final UserAddressService userAddressService;

    @Override
    @Transactional
    public OrderVO create(OrderSubmitDTO dto) {
        Long userId = BaseContext.getUserId();
        List<CartItemVO> cartItems = cartMapper.selectByUserIdForUpdate(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException("购物车为空，无法下单");
        }
        for (CartItemVO item : cartItems) {
            if (item.getStatus() == null || item.getStatus() != 1) {
                throw new BusinessException("菜品「" + item.getDishName() + "」已下架，请调整购物车");
            }
        }

        UserAddress address = userAddressService.getOwned(dto.getAddressId());
        BigDecimal total = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();
        for (CartItemVO item : cartItems) {
            BigDecimal amount = item.getAmount() == null
                    ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                    : item.getAmount();
            amount = amount.setScale(2, RoundingMode.HALF_UP);
            total = total.add(amount);

            OrderDetail detail = new OrderDetail();
            detail.setDishId(item.getDishId());
            detail.setDishName(item.getDishName());
            detail.setDishImage(item.getDishImage());
            detail.setUnitPrice(item.getPrice());
            detail.setQuantity(item.getQuantity());
            detail.setAmount(amount);
            details.add(detail);
        }

        Orders order = new Orders();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setUserId(userId);
        order.setAddressId(address.getId());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setDeliveryAddress(fullAddress(address));
        order.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        orderMapper.insert(order);

        for (OrderDetail detail : details) {
            detail.setOrderId(order.getId());
        }
        orderDetailMapper.batchInsert(details);
        cartMapper.deleteByUserId(userId);
        return getOwnedVO(order.getId(), userId);
    }

    @Override
    public PageResult<OrderVO> page(Integer status, Integer pageNum, Integer pageSize) {
        verifyStatus(status);
        Long userId = BaseContext.getUserId();
        int page = PageSupport.pageNum(pageNum);
        int size = PageSupport.pageSize(pageSize);
        List<OrderVO> records = orderMapper.selectPageUser(userId, status,
                PageSupport.offset(page, size), size);
        records.forEach(this::fillStatus);
        long total = orderMapper.countUser(userId, status);
        return new PageResult<>(total, records);
    }

    @Override
    public OrderVO detail(Long id) {
        return getOwnedVO(id, BaseContext.getUserId());
    }

    @Override
    @Transactional
    public OrderVO pay(Long id) {
        Long userId = BaseContext.getUserId();
        if (orderMapper.selectByIdAndUser(id, userId) == null) {
            throw new BusinessException("订单不存在");
        }
        int rows = orderMapper.pay(id, userId, LocalDateTime.now());
        if (rows == 0) {
            throw new BusinessException("订单当前状态不可支付");
        }
        return getOwnedVO(id, userId);
    }

    @Override
    @Transactional
    public OrderVO cancel(Long id, CancelOrderDTO dto) {
        Long userId = BaseContext.getUserId();
        if (orderMapper.selectByIdAndUser(id, userId) == null) {
            throw new BusinessException("订单不存在");
        }
        String reason = dto == null || !StringUtils.hasText(dto.getReason())
                ? "用户取消" : dto.getReason().trim();
        if (reason.length() > 200) {
            throw new BusinessException("取消原因不能超过200个字符");
        }
        int rows = orderMapper.cancelByUser(id, userId, LocalDateTime.now(), reason);
        if (rows == 0) {
            throw new BusinessException("只能取消待付款订单");
        }
        return getOwnedVO(id, userId);
    }

    @Override
    @Transactional
    public OrderVO complete(Long id) {
        Long userId = BaseContext.getUserId();
        if (orderMapper.selectByIdAndUser(id, userId) == null) {
            throw new BusinessException("订单不存在");
        }
        int rows = orderMapper.complete(id, userId, LocalDateTime.now());
        if (rows == 0) {
            throw new BusinessException("只有已接单订单可以确认完成");
        }
        return getOwnedVO(id, userId);
    }

    private OrderVO getOwnedVO(Long id, Long userId) {
        OrderVO order = orderMapper.selectVOById(id);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        order.setItems(orderDetailMapper.selectByOrderId(id));
        fillStatus(order);
        return order;
    }

    private void fillStatus(OrderVO order) {
        order.setStatusText(OrderStatus.text(order.getStatus()));
    }

    private void verifyStatus(Integer status) {
        if (status != null && !OrderStatus.isValid(status)) {
            throw new BusinessException("订单状态不正确");
        }
    }

    private String fullAddress(UserAddress address) {
        return address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress();
    }
}
