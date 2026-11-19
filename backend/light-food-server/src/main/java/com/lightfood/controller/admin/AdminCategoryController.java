package com.lightfood.controller.admin;

import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.CategoryDTO;
import com.lightfood.pojo.vo.CategoryVO;
import com.lightfood.service.admin.AdminCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping
    public Result<CategoryVO> create(@Valid @RequestBody CategoryDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<CategoryVO> update(@PathVariable Long id,
                                     @Valid @RequestBody CategoryDTO dto) {
        return Result.success(categoryService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
