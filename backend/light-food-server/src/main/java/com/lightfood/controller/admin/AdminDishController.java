package com.lightfood.controller.admin;

import com.lightfood.common.result.PageResult;
import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.DishDTO;
import com.lightfood.pojo.dto.DishStatusDTO;
import com.lightfood.pojo.vo.DishVO;
import com.lightfood.service.admin.AdminDishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dishes")
@RequiredArgsConstructor
public class AdminDishController {

    private final AdminDishService dishService;

    @GetMapping
    public Result<PageResult<DishVO>> page(@RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String name,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(dishService.page(categoryId, status, name, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<DishVO> detail(@PathVariable Long id) {
        return Result.success(dishService.get(id));
    }

    @PostMapping
    public Result<DishVO> create(@Valid @RequestBody DishDTO dto) {
        return Result.success(dishService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<DishVO> update(@PathVariable Long id,
                                 @Valid @RequestBody DishDTO dto) {
        return Result.success(dishService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dishService.delete(id);
        return Result.success();
    }

    @PatchMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                     @Valid @RequestBody DishStatusDTO dto) {
        dishService.changeStatus(id, dto.getStatus());
        return Result.success();
    }
}
