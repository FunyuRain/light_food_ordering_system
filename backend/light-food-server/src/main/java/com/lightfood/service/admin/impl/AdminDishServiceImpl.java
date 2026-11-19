package com.lightfood.service.admin.impl;

import com.lightfood.cache.MenuCache;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.common.result.PageResult;
import com.lightfood.mapper.CategoryMapper;
import com.lightfood.mapper.DishMapper;
import com.lightfood.pojo.dto.DishDTO;
import com.lightfood.pojo.entity.Dish;
import com.lightfood.pojo.vo.DishVO;
import com.lightfood.service.admin.AdminDishService;
import com.lightfood.util.PageSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDishServiceImpl implements AdminDishService {

    private static final String DEFAULT_IMAGE = "/mock/dish/default.svg";

    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;
    private final MenuCache menuCache;

    @Override
    public PageResult<DishVO> page(Long categoryId, Integer status, String name,
                                   Integer pageNum, Integer pageSize) {
        int page = PageSupport.pageNum(pageNum);
        int size = PageSupport.pageSize(pageSize);
        String keyword = name == null ? null : name.trim();
        List<DishVO> records = dishMapper.selectPage(categoryId, status, keyword,
                PageSupport.offset(page, size), size);
        long total = dishMapper.countPage(categoryId, status, keyword);
        return new PageResult<>(total, records);
    }

    @Override
    public DishVO get(Long id) {
        DishVO dish = dishMapper.selectVOById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        return dish;
    }

    @Override
    @Transactional
    public DishVO create(DishDTO dto) {
        verifyCategory(dto.getCategoryId());
        Dish dish = new Dish();
        apply(dish, dto);
        dishMapper.insert(dish);
        menuCache.evictAll();
        return get(dish.getId());
    }

    @Override
    @Transactional
    public DishVO update(Long id, DishDTO dto) {
        Dish existing = dishMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("菜品不存在");
        }
        verifyCategory(dto.getCategoryId());
        apply(existing, dto);
        dishMapper.update(existing);
        menuCache.evictAll();
        return get(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (dishMapper.selectById(id) == null) {
            throw new BusinessException("菜品不存在");
        }
        dishMapper.deleteById(id);
        menuCache.evictAll();
    }

    @Override
    @Transactional
    public void changeStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态只能是0或1");
        }
        if (dishMapper.selectById(id) == null) {
            throw new BusinessException("菜品不存在");
        }
        dishMapper.updateStatus(id, status);
        menuCache.evictAll();
    }

    private void apply(Dish dish, DishDTO dto) {
        dish.setCategoryId(dto.getCategoryId());
        dish.setName(dto.getName().trim());
        dish.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());
        String imageUrl = dto.getImageUrl();
        if (imageUrl != null && !imageUrl.isBlank() && !imageUrl.trim().startsWith("/mock/")) {
            throw new BusinessException("菜品图片请使用 /mock/ 下的模拟路径");
        }
        dish.setImageUrl(imageUrl == null || imageUrl.isBlank() ? DEFAULT_IMAGE : imageUrl.trim());
        dish.setPrice(dto.getPrice());
        dish.setSort(dto.getSort() == null ? 0 : dto.getSort());
        dish.setStatus(dto.getStatus());
    }

    private void verifyCategory(Long categoryId) {
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BusinessException("分类不存在");
        }
    }
}
