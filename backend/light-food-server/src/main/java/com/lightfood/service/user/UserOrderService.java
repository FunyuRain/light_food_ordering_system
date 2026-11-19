package com.lightfood.service.user;

import com.lightfood.common.result.PageResult;
import com.lightfood.pojo.dto.CancelOrderDTO;
import com.lightfood.pojo.dto.OrderSubmitDTO;
import com.lightfood.pojo.vo.OrderVO;

public interface UserOrderService {
    OrderVO create(OrderSubmitDTO dto);

    PageResult<OrderVO> page(Integer status, Integer pageNum, Integer pageSize);

    OrderVO detail(Long id);

    OrderVO pay(Long id);

    OrderVO cancel(Long id, CancelOrderDTO dto);

    OrderVO complete(Long id);
}
