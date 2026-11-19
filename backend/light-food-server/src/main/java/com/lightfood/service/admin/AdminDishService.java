package com.lightfood.service.admin;

import com.lightfood.common.result.PageResult;
import com.lightfood.pojo.dto.DishDTO;
import com.lightfood.pojo.vo.DishVO;

public interface AdminDishService {
    PageResult<DishVO> page(Long categoryId, Integer status, String name, Integer pageNum, Integer pageSize);

    DishVO get(Long id);

    DishVO create(DishDTO dto);

    DishVO update(Long id, DishDTO dto);

    void delete(Long id);

    void changeStatus(Long id, Integer status);
}
