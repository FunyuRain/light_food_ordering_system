package com.lightfood.service.admin.impl;

import com.lightfood.cache.MenuCache;
import com.lightfood.common.exception.BusinessException;
import com.lightfood.mapper.CategoryMapper;
import com.lightfood.pojo.dto.CategoryDTO;
import com.lightfood.pojo.entity.Category;
import com.lightfood.pojo.vo.CategoryVO;
import com.lightfood.service.admin.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryMapper categoryMapper;
    private final MenuCache menuCache;

    @Override
    public List<CategoryVO> list() {
        return categoryMapper.selectAll().stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public CategoryVO create(CategoryDTO dto) {
        String name = normalizeName(dto.getName());
        verifyNameAvailable(name, null);

        Category category = new Category();
        category.setName(name);
        category.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        categoryMapper.insert(category);
        menuCache.evictAll();
        return toVO(categoryMapper.selectById(category.getId()));
    }

    @Override
    @Transactional
    public CategoryVO update(Long id, CategoryDTO dto) {
        Category existing = categoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        String name = normalizeName(dto.getName());
        verifyNameAvailable(name, id);

        existing.setName(name);
        existing.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());
        existing.setSort(dto.getSort() == null ? 0 : dto.getSort());
        categoryMapper.update(existing);
        menuCache.evictAll();
        return toVO(categoryMapper.selectById(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (categoryMapper.selectById(id) == null) {
            throw new BusinessException("分类不存在");
        }
        if (categoryMapper.countDishes(id) > 0) {
            throw new BusinessException("分类下还有菜品，不能删除");
        }
        categoryMapper.deleteById(id);
        menuCache.evictAll();
    }

    private String normalizeName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException("分类名称不能为空");
        }
        return name.trim();
    }

    private void verifyNameAvailable(String name, Long excludeId) {
        if (categoryMapper.countByName(name, excludeId) > 0) {
            throw new BusinessException("分类名称已存在");
        }
    }

    private CategoryVO toVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setDescription(category.getDescription());
        vo.setSort(category.getSort());
        return vo;
    }
}
