package com.lightfood.service.user.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lightfood.cache.MenuCache;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.mapper.CategoryMapper;
import com.lightfood.mapper.DishMapper;
import com.lightfood.pojo.vo.CategoryVO;
import com.lightfood.pojo.vo.DishVO;
import com.lightfood.service.user.UserMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMenuServiceImpl implements UserMenuService {

    private static final TypeReference<List<CategoryVO>> CATEGORY_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<DishVO>> DISH_LIST_TYPE = new TypeReference<>() {
    };

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final MenuCache menuCache;

    @Override
    public List<CategoryVO> categories() {
        List<CategoryVO> cached = menuCache.get(MenuCache.CATEGORIES_KEY, CATEGORY_LIST_TYPE);
        if (cached != null) {
            return cached;
        }
        List<CategoryVO> result = categoryMapper.selectWithOnSaleDish().stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(category.getId());
            vo.setName(category.getName());
            vo.setDescription(category.getDescription());
            vo.setSort(category.getSort());
            return vo;
        }).toList();
        menuCache.putCategories(result);
        return result;
    }

    @Override
    public List<DishVO> dishes(Long categoryId) {
        return dishMapper.selectOnSaleByCategory(categoryId);
    }

    @Override
    public List<DishVO> hotDishes() {
        List<DishVO> cached = menuCache.get(MenuCache.HOT_DISHES_KEY, DISH_LIST_TYPE);
        if (cached != null) {
            return cached;
        }
        List<DishVO> result = dishMapper.selectHotDishes(6);
        menuCache.putHotDishes(result);
        return result;
    }

    @Override
    public DishVO dish(Long id) {
        DishVO dish = dishMapper.selectOnSaleVOById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在或已下架");
        }
        return dish;
    }
}
