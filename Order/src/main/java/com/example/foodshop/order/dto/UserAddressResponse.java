package com.example.foodshop.order.dto;

import com.example.foodshop.order.entity.UserAddress;
import java.time.LocalDateTime;

public class UserAddressResponse {
    private Long id;
    private String fullAddress;
    private Integer provinceCode;
    private Integer districtCode;
    private Integer wardCode;
    private String street;
    private String phoneNumber;
    private String label;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    public UserAddressResponse() {}

    public UserAddressResponse(UserAddress address) {
        this.id = address.getId();
        this.fullAddress = address.getFullAddress();
        this.provinceCode = address.getProvinceCode();
        this.districtCode = address.getDistrictCode();
        this.wardCode = address.getWardCode();
        this.street = address.getStreet();
        this.phoneNumber = address.getPhoneNumber();
        this.label = address.getLabel();
        this.isDefault = address.getIsDefault();
        this.createdAt = address.getCreatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public Integer getProvinceCode() { return provinceCode; }
    public void setProvinceCode(Integer provinceCode) { this.provinceCode = provinceCode; }

    public Integer getDistrictCode() { return districtCode; }
    public void setDistrictCode(Integer districtCode) { this.districtCode = districtCode; }

    public Integer getWardCode() { return wardCode; }
    public void setWardCode(Integer wardCode) { this.wardCode = wardCode; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
