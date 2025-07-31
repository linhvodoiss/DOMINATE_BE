package com.fpt.service;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.UserLicenseViewDTO;
import com.fpt.entity.License;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.LicenseVerifyRequestForm;
import com.fpt.payload.LicenseVerifyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ILicenseService {
    Page<LicenseDTO> getAllLicense(Pageable pageable, String search);
    Page<LicenseDTO> getUserLicense(Pageable pageable, String search,Long userId,SubscriptionPackage.TypePackage type);
    List<LicenseDTO> convertToDto(List<License> licenses);
    List<LicenseDTO> getAll();
    LicenseDTO getById(Long id);
    List<LicenseDTO> getLicenseIsActiveOfUser(Long userId);
    LicenseDTO create(LicenseDTO dto);
    LicenseDTO createLicense(LicenseCreateForm form,String ip);
    LicenseDTO bindHardwareIdToLicense(LicenseCreateForm form);
    LicenseDTO activateNextLicense(Long userId, SubscriptionPackage.TypePackage type);
    LicenseVerifyResponse verifyLicense(LicenseVerifyRequestForm request);
    LicenseVerifyResponse verifyLicensePro(LicenseVerifyRequestForm request);
    LicenseDTO update(Long id, LicenseDTO dto);
    void unbindHardwareIdFromLicense(String licenseKey);
    void delete(Long id);
    List<LicenseDTO> getByUserId(Long userId);
}
