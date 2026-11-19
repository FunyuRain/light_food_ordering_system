package com.lightfood.service.user;

import com.lightfood.pojo.vo.CategoryVO;
import com.lightfood.pojo.vo.DishVO;

import java.util.List;

public interface UserMenuService {
    List<CategoryVO> categories();

    List<DishVO> dishes(Long categoryId);

    List<DishVO> hotDishes();

    DishVO dish(Long id);
}
