package com.lightfood.mapper;

import com.lightfood.pojo.entity.Dish;
import com.lightfood.pojo.vo.DishVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DishMapper {

    List<DishVO> selectPage(@Param("categoryId") Long categoryId,
                            @Param("status") Integer status,
                            @Param("name") String name,
                            @Param("offset") int offset,
                            @Param("pageSize") int pageSize);

    long countPage(@Param("categoryId") Long categoryId,
                   @Param("status") Integer status,
                   @Param("name") String name);

    DishVO selectVOById(Long id);

    DishVO selectOnSaleVOById(Long id);

    Dish selectById(Long id);

    Dish selectOnSaleById(Long id);

    int insert(Dish dish);

    int update(Dish dish);

    int deleteById(Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    List<DishVO> selectOnSaleByCategory(@Param("categoryId") Long categoryId);

    List<DishVO> selectHotDishes(@Param("limit") int limit);
}
