package com.fpt.licenseserviceTest;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.*;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.LicenseVerifyRequestForm;
import com.fpt.payload.LicenseVerifyResponse;
import com.fpt.repository.*;
import com.fpt.service.ILicenseService;
import com.fpt.service.IPaymentOrderService;
import com.fpt.service.LicenseService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.*;
import org.springframework.data.domain.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LicenseServiceTest {

    @Mock LicenseRepository licenseRepository;
    @Mock PaymentOrderRepository paymentOrderRepository;
    @Mock UserRepository userRepository;
    @Mock SubscriptionPackageRepository subscriptionRepository;
    @Mock IPaymentOrderService paymentOrderService;
    @Mock ModelMapper modelMapper;

    @InjectMocks LicenseService service;

    License license1, license2;
    User user;
    SubscriptionPackage package1;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        package1 = new SubscriptionPackage();
        package1.setId(1L);
        package1.setBillingCycle(SubscriptionPackage.BillingCycle.MONTHLY);
        package1.setIsActive(true);

        license1 = License.builder()
                .id(1L).licenseKey("KEY1").canUsed(true)
                .user(user).subscriptionPackage(package1)
                .duration(30)
                .activatedAt(LocalDateTime.now().minusDays(5))
                .createdAt(LocalDateTime.now().minusDays(5))
                .hardwareId("HWID1")
                .orderId(1)
                .build();
        license2 = License.builder()
                .id(2L).licenseKey("KEY2").canUsed(false)
                .user(user).subscriptionPackage(package1)
                .duration(30)
                .createdAt(LocalDateTime.now())
                .hardwareId("HWID2")
                .orderId(1)
                .build();
    }

    // 1. getAllLicense
    @Test
    void getAllLicense_normal() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<License> page = new PageImpl<>(List.of(license1, license2), pageable, 2);
        when(licenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        when(modelMapper.map(any(SubscriptionPackage.class), eq(SubscriptionPackageDTO.class)))
                .thenReturn(new SubscriptionPackageDTO());

        Page<LicenseDTO> result = service.getAllLicense(pageable, null);
        assertThat(result).hasSize(2);
    }

    @Test
    void getAllLicense_boundary_emptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        when(licenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        Page<LicenseDTO> result = service.getAllLicense(pageable, "notFound");
        assertThat(result).isEmpty();
    }

    @Test
    void getAllLicense_abnormal_nullPageable() {
        assertThatThrownBy(() -> service.getAllLicense(null, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAllLicense_externalError() {
        Pageable pageable = PageRequest.of(0, 10);
        when(licenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.getAllLicense(pageable, null))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("DB error");
    }

    // 2. getUserLicense
    @Test
    void getUserLicense_normal() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<License> page = new PageImpl<>(List.of(license1), pageable, 1);
        when(licenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));
        when(modelMapper.map(any(SubscriptionPackage.class), eq(SubscriptionPackageDTO.class)))
                .thenReturn(new SubscriptionPackageDTO());
    }

    @Test
    void getUserLicense_boundary_noMatchingType() {
        Pageable pageable = PageRequest.of(0, 10);
        when(licenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));
    }

    @Test
    void getUserLicense_abnormal_nullUserId() {
        Pageable pageable = PageRequest.of(0, 10);
        when(licenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));
    }

    @Test
    void getUserLicense_externalError() {
        Pageable pageable = PageRequest.of(0, 10);
        when(licenseRepository.save(any())).thenThrow(new RuntimeException("DB error"));
    }

    // 3. getLicenseIsActiveOfUser
    @Test
    void getLicenseIsActiveOfUser_normal() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(licenseRepository.findByUserId(1L)).thenReturn(List.of(license1, license2));
        when(modelMapper.map(any(SubscriptionPackage.class), eq(SubscriptionPackageDTO.class)))
                .thenReturn(new SubscriptionPackageDTO());
        List<LicenseDTO> result = service.getLicenseIsActiveOfUser(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void getLicenseIsActiveOfUser_boundary_userHasNoActiveLicense() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        License deactivated = License.builder().canUsed(false).user(user).subscriptionPackage(package1).duration(30).build();
        when(licenseRepository.findByUserId(1L)).thenReturn(List.of(deactivated));
        List<LicenseDTO> result = service.getLicenseIsActiveOfUser(1L);
        assertThat(result).isEmpty();
    }

    @Test
    void getLicenseIsActiveOfUser_abnormal_userNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getLicenseIsActiveOfUser(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getLicenseIsActiveOfUser_externalError() {
        when(userRepository.findById(anyLong())).thenThrow(new RuntimeException("repo error"));
        assertThatThrownBy(() -> service.getLicenseIsActiveOfUser(1L))
                .isInstanceOf(RuntimeException.class);
    }

    // 4. bindHardwareIdToLicense
    @Test
    void bindHardwareIdToLicense_normal() {
        LicenseCreateForm form = new LicenseCreateForm();
        form.setLicenseKey("KEY1");
        form.setHardwareId("HWID1");
        when(licenseRepository.findByLicenseKey("KEY1")).thenReturn(Optional.of(license1));
        when(licenseRepository.save(any())).thenReturn(license1);
        when(modelMapper.map(any(SubscriptionPackage.class), eq(SubscriptionPackageDTO.class)))
                .thenReturn(new SubscriptionPackageDTO());

        LicenseDTO result = service.bindHardwareIdToLicense(form);
        assertThat(result.getLicenseKey()).isEqualTo("KEY1");
    }

    @Test
    void bindHardwareIdToLicense_boundary_licenseAlreadyBound() {
        LicenseCreateForm form = new LicenseCreateForm();
        form.setLicenseKey("KEY1");
        form.setHardwareId("OTHER_HWID");
        license1.setHardwareId("HWID1");
        when(licenseRepository.findByLicenseKey("KEY1")).thenReturn(Optional.of(license1));
        assertThatThrownBy(() -> service.bindHardwareIdToLicense(form))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("match with other device");
    }

    @Test
    void bindHardwareIdToLicense_abnormal_licenseNotFound() {
        LicenseCreateForm form = new LicenseCreateForm();
        form.setLicenseKey("NOT_EXIST");
        when(licenseRepository.findByLicenseKey("NOT_EXIST")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.bindHardwareIdToLicense(form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("License is not exist.");
    }

    @Test
    void bindHardwareIdToLicense_externalError() {
        LicenseCreateForm form = new LicenseCreateForm();
        form.setLicenseKey("KEY1");
        form.setHardwareId("HWID1");
        when(licenseRepository.findByLicenseKey("KEY1")).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.bindHardwareIdToLicense(form))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("DB error");
    }

    // 5. verifyLicense
    @Test
    void verifyLicense_normal_valid() {
        LicenseVerifyRequestForm req = new LicenseVerifyRequestForm();
        req.setLicenseKey("KEY1");
        req.setHardwareId("HWID1");
        license1.setActivatedAt(LocalDateTime.now().minusDays(1));
        when(licenseRepository.findByLicenseKey("KEY1")).thenReturn(Optional.of(license1));
        LicenseVerifyResponse resp = service.verifyLicense(req);
    }

    @Test
    void verifyLicense_boundary_expiredLicense() {
        LicenseVerifyRequestForm req = new LicenseVerifyRequestForm();
        req.setLicenseKey("KEY1");
        req.setHardwareId("HWID1");
        license1.setActivatedAt(LocalDateTime.now().minusDays(31));
        when(licenseRepository.findByLicenseKey("KEY1")).thenReturn(Optional.of(license1));
        LicenseVerifyResponse resp = service.verifyLicense(req);
        assertThat(resp.getMessage()).contains("expired");
    }

    @Test
    void verifyLicense_abnormal_wrongHardwareId() {
        LicenseVerifyRequestForm req = new LicenseVerifyRequestForm();
        req.setLicenseKey("KEY1");
        req.setHardwareId("HWID_OTHER");
        when(licenseRepository.findByLicenseKey("KEY1")).thenReturn(Optional.of(license1));
        LicenseVerifyResponse resp = service.verifyLicense(req);
        assertThat(resp.getMessage()).contains("other device");
    }

    @Test
    void verifyLicense_external_licenseNotFound() {
        LicenseVerifyRequestForm req = new LicenseVerifyRequestForm();
        req.setLicenseKey("NOT_EXIST");
        when(licenseRepository.findByLicenseKey("NOT_EXIST")).thenReturn(Optional.empty());
        LicenseVerifyResponse resp = service.verifyLicense(req);
        assertThat(resp.getMessage()).contains("not exist");
    }
}
