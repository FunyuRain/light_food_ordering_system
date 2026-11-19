package com.lightfood.mapper;

import com.lightfood.pojo.entity.Orders;
import com.lightfood.pojo.vo.OrderVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderMapper {

    int insert(Orders order);

    List<OrderVO> selectPageAdmin(@Param("status") Integer status,
                                  @Param("orderNo") String orderNo,
                                  @Param("offset") int offset,
                                  @Param("pageSize") int pageSize);

    long countAdmin(@Param("status") Integer status,
                    @Param("orderNo") String orderNo);

    List<OrderVO> selectPageUser(@Param("userId") Long userId,
                                 @Param("status") Integer status,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    long countUser(@Param("userId") Long userId,
                   @Param("status") Integer status);

    OrderVO selectVOById(Long id);

    Orders selectById(Long id);

    Orders selectByIdAndUser(@Param("id") Long id,
                             @Param("userId") Long userId);

    int pay(@Param("id") Long id,
            @Param("userId") Long userId,
            @Param("time") LocalDateTime time);

    int cancelByUser(@Param("id") Long id,
                     @Param("userId") Long userId,
                     @Param("time") LocalDateTime time,
                     @Param("reason") String reason);

    int complete(@Param("id") Long id,
                 @Param("userId") Long userId,
                 @Param("time") LocalDateTime time);

    int accept(@Param("id") Long id,
               @Param("time") LocalDateTime time);

    int cancelByAdmin(@Param("id") Long id,
                      @Param("time") LocalDateTime time,
                      @Param("reason") String reason);
}
