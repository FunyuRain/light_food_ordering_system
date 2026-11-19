package com.lightfood.controller.user;

import com.lightfood.common.result.Result;
import com.lightfood.pojo.vo.CategoryVO;
import com.lightfood.pojo.vo.DishVO;
import com.lightfood.service.user.UserMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserMenuController {

    private final UserMenuService menuService;

    @GetMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.success(menuService.categories());
    }

    @GetMapping("/dishes")
    public Result<List<DishVO>> dishes(@RequestParam(required = false) Long categoryId) {
        return Result.success(menuService.dishes(categoryId));
    }

    @GetMapping("/dishes/hot")
    public Result<List<DishVO>> hotDishes() {
        return Result.success(menuService.hotDishes());
    }

    @GetMapping("/dishes/{id}")
    public Result<DishVO> dish(@PathVariable Long id) {
        return Result.success(menuService.dish(id));
    }
}
