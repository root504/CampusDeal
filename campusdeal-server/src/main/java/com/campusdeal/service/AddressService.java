package com.campusdeal.service;

import com.campusdeal.entity.Address;
import com.campusdeal.dto.request.AddressRequest;

import java.util.List;

public interface AddressService {
    List<Address> getUserAddresses(Long userId);

    Address getAddressById(Long userId, Long addressId);

    Address createAddress(Long userId, AddressRequest request);

    Address updateAddress(Long userId, Long addressId, AddressRequest request);

    void deleteAddress(Long userId, Long addressId);

    Address setDefaultAddress(Long userId, Long addressId);
}
