package com.lightfood.mapper;

import com.lightfood.pojo.entity.OrderDetail;
import com.lightfood.pojo.vo.OrderItemVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderDetailMapper {

    int batchInsert(@Param("list") List<OrderDetail> list);

    List<OrderItemVO> selectByOrderId(Long orderId);
}
