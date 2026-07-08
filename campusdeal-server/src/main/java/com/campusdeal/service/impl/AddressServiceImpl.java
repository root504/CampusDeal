package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.dto.request.AddressRequest;
import com.campusdeal.entity.Address;
import com.campusdeal.repository.AddressRepository;
import com.campusdeal.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
    }

    @Override
    public Address getAddressById(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException("地址不存在"));
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该地址");
        }
        return address;
    }

    @Override
    @Transactional
    public Address createAddress(Long userId, AddressRequest request) {
        long count = addressRepository.countByUserId(userId);
        boolean isDefault = (request.getIsDefault() != null && request.getIsDefault() == 1) || count == 0;

        if (isDefault) {
            addressRepository.clearDefaultByUserId(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .receiverName(request.getReceiverName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .detail(request.getDetail())
                .isDefault(isDefault ? 1 : 0)
                .build();

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException("地址不存在"));
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该地址");
        }

        if (request.getReceiverName() != null) address.setReceiverName(request.getReceiverName());
        if (request.getPhone() != null) address.setPhone(request.getPhone());
        if (request.getProvince() != null) address.setProvince(request.getProvince());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict());
        if (request.getDetail() != null) address.setDetail(request.getDetail());
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            addressRepository.clearDefaultByUserId(userId);
            address.setIsDefault(1);
        }

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException("地址不存在"));
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该地址");
        }
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public Address setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException("地址不存在"));
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该地址");
        }

        addressRepository.clearDefaultByUserId(userId);
        address.setIsDefault(1);
        return addressRepository.save(address);
    }
}
