package com.lightfood.controller.user;

import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.CartAddDTO;
import com.lightfood.pojo.dto.CartQuantityDTO;
import com.lightfood.pojo.vo.CartVO;
import com.lightfood.service.user.UserCartService;
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

@RestController
@RequestMapping("/user/cart")
@RequiredArgsConstructor
public class UserCartController {

    private final UserCartService cartService;

    @GetMapping
    public Result<CartVO> get() {
        return Result.success(cartService.getCart());
    }

    @PostMapping
    public Result<CartVO> add(@Valid @RequestBody CartAddDTO dto) {
        return Result.success(cartService.add(dto));
    }

    @PutMapping("/{id}")
    public Result<CartVO> update(@PathVariable Long id,
                                 @Valid @RequestBody CartQuantityDTO dto) {
        return Result.success(cartService.updateQuantity(id, dto.getQuantity()));
    }

    @DeleteMapping("/{id}")
    public Result<CartVO> remove(@PathVariable Long id) {
        return Result.success(cartService.remove(id));
    }

    @DeleteMapping
    public Result<CartVO> clear() {
        return Result.success(cartService.clear());
    }
}
