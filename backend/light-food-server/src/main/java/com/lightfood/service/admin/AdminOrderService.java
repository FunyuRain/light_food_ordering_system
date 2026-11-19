package com.lightfood.service.admin;

import com.lightfood.common.result.PageResult;
import com.lightfood.pojo.dto.CancelOrderDTO;
import com.lightfood.pojo.vo.OrderVO;

public interface AdminOrderService {
    PageResult<OrderVO> page(Integer status, String orderNo, Integer pageNum, Integer pageSize);

    OrderVO detail(Long id);

    OrderVO accept(Long id);

    OrderVO cancel(Long id, CancelOrderDTO dto);
}
