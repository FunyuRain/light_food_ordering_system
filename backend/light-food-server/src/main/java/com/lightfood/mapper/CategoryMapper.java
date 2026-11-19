package com.lightfood.mapper;

import com.lightfood.pojo.entity.Category;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CategoryMapper {

    List<Category> selectAll();

    List<Category> selectWithOnSaleDish();

    Category selectById(Long id);

    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    int insert(Category category);

    int update(Category category);

    int deleteById(Long id);

    int countDishes(Long categoryId);
}
