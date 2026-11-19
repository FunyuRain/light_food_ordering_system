package com.lightfood.service.admin;

import com.lightfood.pojo.dto.CategoryDTO;
import com.lightfood.pojo.vo.CategoryVO;

import java.util.List;

public interface AdminCategoryService {
    List<CategoryVO> list();

    CategoryVO create(CategoryDTO dto);

    CategoryVO update(Long id, CategoryDTO dto);

    void delete(Long id);
}
