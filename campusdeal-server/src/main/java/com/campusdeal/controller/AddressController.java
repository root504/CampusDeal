package com.campusdeal.controller;

import com.campusdeal.common.Result;
import com.campusdeal.dto.request.AddressRequest;
import com.campusdeal.entity.Address;
import com.campusdeal.service.AddressService;
import com.campusdeal.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {
    private final AddressService addressService;
    private final JwtUtil jwtUtil;

    public AddressController(AddressService addressService, JwtUtil jwtUtil) {
        this.addressService = addressService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public Result<List<Address>> list(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(addressService.getUserAddresses(userId));
    }

    @GetMapping("/{id}")
    public Result<Address> get(@RequestHeader("Authorization") String authHeader,
                                @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(addressService.getAddressById(userId, id));
    }

    @PostMapping
    public Result<Address> create(@RequestHeader("Authorization") String authHeader,
                                   @Valid @RequestBody AddressRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(addressService.createAddress(userId, request));
    }

    @PutMapping("/{id}")
    public Result<Address> update(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable Long id,
                                   @Valid @RequestBody AddressRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(addressService.updateAddress(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@RequestHeader("Authorization") String authHeader,
                             @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        addressService.deleteAddress(userId, id);
        return Result.success(null);
    }

    @PutMapping("/{id}/default")
    public Result<Address> setDefault(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(addressService.setDefaultAddress(userId, id));
    }
}
