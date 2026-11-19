package com.lightfood.controller.user;

import com.lightfood.common.result.Result;
import com.lightfood.pojo.dto.AddressDTO;
import com.lightfood.pojo.vo.AddressVO;
import com.lightfood.service.user.UserAddressService;
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
@RequestMapping("/user/addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService addressService;

    @GetMapping
    public Result<List<AddressVO>> list() {
        return Result.success(addressService.list());
    }

    @PostMapping
    public Result<AddressVO> create(@Valid @RequestBody AddressDTO dto) {
        return Result.success(addressService.create(dto));
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success();
    }
}
