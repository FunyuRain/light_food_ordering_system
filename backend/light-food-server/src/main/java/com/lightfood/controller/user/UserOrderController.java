package com.lightfood.controller.user;

import com.lightfood.common.result.PageResult;
import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.CancelOrderDTO;
import com.lightfood.pojo.dto.OrderSubmitDTO;
import com.lightfood.pojo.vo.OrderVO;
import com.lightfood.service.user.UserOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final UserOrderService orderService;

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderSubmitDTO dto) {
        return Result.success(orderService.create(dto));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> page(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.page(status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(id));
    }

    @PutMapping("/{id}/pay")
    public Result<OrderVO> pay(@PathVariable Long id) {
        return Result.success(orderService.pay(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id,
                                  @Valid @RequestBody(required = false) CancelOrderDTO dto) {
        return Result.success(orderService.cancel(id, dto));
    }

    @PutMapping("/{id}/complete")
    public Result<OrderVO> complete(@PathVariable Long id) {
        return Result.success(orderService.complete(id));
    }
}
