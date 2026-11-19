package com.lightfood.service.admin.impl;

import com.lightfood.common.constant.OrderStatus;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.common.result.PageResult;
import com.lightfood.mapper.OrderDetailMapper;
import com.lightfood.mapper.OrderMapper;
import com.lightfood.pojo.dto.CancelOrderDTO;
import com.lightfood.pojo.entity.Orders;
import com.lightfood.pojo.vo.OrderVO;
import com.lightfood.service.admin.AdminOrderService;
import com.lightfood.util.PageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;

    @Override
    public PageResult<OrderVO> page(Integer status, String orderNo, Integer pageNum, Integer pageSize) {
        verifyStatus(status);
        int page = PageSupport.pageNum(pageNum);
        int size = PageSupport.pageSize(pageSize);
        String number = orderNo == null ? null : orderNo.trim();
        List<OrderVO> records = orderMapper.selectPageAdmin(status, number,
                PageSupport.offset(page, size), size);
        records.forEach(this::fillStatus);
        long total = orderMapper.countAdmin(status, number);
        return new PageResult<>(total, records);
    }

    @Override
    public OrderVO detail(Long id) {
        OrderVO order = orderMapper.selectVOById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setItems(orderDetailMapper.selectByOrderId(id));
        fillStatus(order);
        return order;
    }

    @Override
    @Transactional
    public OrderVO accept(Long id) {
        requireOrder(id);
        int rows = orderMapper.accept(id, LocalDateTime.now());
        if (rows == 0) {
            throw new BusinessException("订单当前状态不可接单");
        }
        return detail(id);
    }

    @Override
    @Transactional
    public OrderVO cancel(Long id, CancelOrderDTO dto) {
        requireOrder(id);
        if (dto == null || !StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("请填写取消原因");
        }
        String reason = dto.getReason().trim();
        if (reason.length() > 200) {
            throw new BusinessException("取消原因不能超过200个字符");
        }
        int rows = orderMapper.cancelByAdmin(id, LocalDateTime.now(), reason);
        if (rows == 0) {
            throw new BusinessException("订单当前状态不可取消");
        }
        return detail(id);
    }

    private Orders requireOrder(Long id) {
        Orders order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void verifyStatus(Integer status) {
        if (status != null && !OrderStatus.isValid(status)) {
            throw new BusinessException("订单状态不正确");
        }
    }

    private void fillStatus(OrderVO order) {
        order.setStatusText(OrderStatus.text(order.getStatus()));
    }
}
