package com.lightfood.controller.admin;

import com.lightfood.common.result.PageResult;
import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.CancelOrderDTO;
import com.lightfood.pojo.vo.OrderVO;
import com.lightfood.service.admin.AdminOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService orderService;

    @GetMapping
    public Result<PageResult<OrderVO>> page(@RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) String orderNo,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.page(status, orderNo, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(id));
    }

    @PutMapping("/{id}/accept")
    public Result<OrderVO> accept(@PathVariable Long id) {
        return Result.success(orderService.accept(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id,
                                  @Valid @RequestBody CancelOrderDTO dto) {
        return Result.success(orderService.cancel(id, dto));
    }
}
