package com.fpt.service;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.License;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.LicenseVerifyRequestForm;
import com.fpt.payload.LicenseVerifyResponse;
import com.fpt.repository.LicenseRepository;
import com.fpt.repository.PaymentOrderRepository;
import com.fpt.repository.SubscriptionPackageRepository;
import com.fpt.repository.UserRepository;
import com.fpt.specification.LicenseSpecificationBuilder;
import com.fpt.specification.PaymentOrderSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
@Transactional
public class LicenseService implements ILicenseService {

    private static final Logger LOGGER = Logger.getLogger(LicenseService.class.getName());

    private final LicenseRepository licenseRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final SubscriptionPackageRepository subscriptionRepository;
    private final IPaymentOrderService paymentOrderService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Page<LicenseDTO> getAllLicense(Pageable pageable, String search) {
        LicenseSpecificationBuilder specification = new LicenseSpecificationBuilder(search);
        return licenseRepository.findAll(specification.build(), pageable)
                .map(this::toDtoWithSubscription);
    }

    @Override
    public Page<LicenseDTO> getUserLicense(Pageable pageable, String search, Long userId) {
        LicenseSpecificationBuilder specification = new LicenseSpecificationBuilder(search,userId);
        return licenseRepository.findAll(specification.build(), pageable)
                .map(this::toDtoWithSubscription);
    }

    @Override
    public List<LicenseDTO> convertToDto(List<License> licenses) {
        List<LicenseDTO> licenseDTOs = new ArrayList<>();
        for (License license : licenses) {
            LicenseDTO licenseDTO = modelMapper.map(license, LicenseDTO.class);
            licenseDTOs.add(licenseDTO);
        }
        return licenseDTOs;
    }

    @Override
    public List<LicenseDTO> getAll() {
        return licenseRepository.findAll().stream()
                .map(this::toDtoWithSubscription)
                .toList();
    }

    @Override
    public LicenseDTO getById(Long id) {
        return licenseRepository.findById(id)
                .map(this::toDtoWithSubscription)
                .orElseThrow(() -> new RuntimeException("License not found"));
    }

    @Override
    public LicenseDTO create(LicenseDTO dto) {
        return toDtoWithSubscription(licenseRepository.save(toEntity(dto)));
    }

    @Override
    public LicenseDTO createLicense(LicenseCreateForm form) {
        PaymentOrder order = paymentOrderRepository.findByOrderId(form.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        if (order.getPaymentStatus() != PaymentOrder.PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Đơn hàng chưa được thanh toán");
        }
        if (Boolean.TRUE.equals(order.getLicenseCreated())) {
            throw new IllegalArgumentException("License đã được tạo cho đơn hàng này");
        }

        SubscriptionPackage subscription = order.getSubscriptionPackage();
        int durationDays = switch (subscription.getBillingCycle()) {
            case MONTHLY -> 30;
            case HALF_YEARLY -> 182;
            case YEARLY -> 365;
            default -> throw new IllegalStateException("BillingCycle không hợp lệ");
        };

        Long userId = order.getUser().getId();
        boolean hasActiveLicense = licenseRepository.existsByUserIdAndCanUsedTrue(userId);


        License license = new License();
        license.setLicenseKey(generateLicenseKey());
        license.setDuration(durationDays);
        license.setIp(form.getIp());
        license.setHardwareId(form.getHardwareId());
        license.setUser(order.getUser());
        license.setSubscriptionPackage(subscription);
        license.setCanUsed(!hasActiveLicense);
        license.setOrderId(form.getOrderId());
        order.setLicenseCreated(true);
        License saved = licenseRepository.save(license);
        return toDtoWithSubscription(saved);
    }

    @Override
    public LicenseDTO activateNextLicense(Long userId, SubscriptionPackage.TypePackage type) {
        List<License> userLicenses = licenseRepository.findByUserId(userId)
                .stream()
                .filter(l -> l.getSubscriptionPackage().getTypePackage() == type)
                .toList();

        LocalDateTime now = LocalDateTime.now();

        // B1: Tìm key đang dùng hiện tại của loại này
        userLicenses.stream()
                .filter(l -> Boolean.TRUE.equals(l.getCanUsed()))
                .findFirst()
                .ifPresent(currentUsed -> {
                    if (currentUsed.getCreatedAt().plusDays(currentUsed.getDuration()).isAfter(now)) {
                        throw new IllegalArgumentException("Key " + type + " hiện tại vẫn còn hạn sử dụng");
                    }
                    currentUsed.setCanUsed(false);
                    licenseRepository.save(currentUsed);
                });

        // B2: Tìm key chưa dùng, còn hạn
        List<License> validUnusedLicenses = userLicenses.stream()
                .filter(l -> Boolean.FALSE.equals(l.getCanUsed()))
                .filter(l -> l.getCreatedAt().plusDays(l.getDuration()).isAfter(now))
                .sorted(Comparator.comparing(License::getCreatedAt))
                .toList();

        if (validUnusedLicenses.isEmpty()) {
            throw new IllegalArgumentException("Không còn key nào thuộc loại " + type + " còn hạn để sử dụng");
        }

        // B3: Kích hoạt
        License toActivate = validUnusedLicenses.get(0);
        toActivate.setCanUsed(true);
        licenseRepository.save(toActivate);

        return toDtoWithSubscription(toActivate);
    }


    public LicenseVerifyResponse verifyLicense(LicenseVerifyRequestForm request) {
        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(request.getLicenseKey());

        if (licenseOpt.isEmpty()) {
            return new LicenseVerifyResponse(false, 404, null, "License không tồn tại", null);
        }

        License license = licenseOpt.get();

        if (!Boolean.TRUE.equals(license.getCanUsed())) {
            return new LicenseVerifyResponse(false, 401, null, "License chưa được kích hoạt", null);
        }

        if (!license.getHardwareId().equals(request.getHardwareId())) {
            return new LicenseVerifyResponse(false, 403, null, "License không hợp lệ với thiết bị này", null);
        }

        LocalDateTime expiredAt = license.getCreatedAt().plusDays(license.getDuration());
        if (expiredAt.isBefore(LocalDateTime.now())) {
            return new LicenseVerifyResponse(false, 400, null, "License đã hết hạn", expiredAt);
        }

        SubscriptionPackage.TypePackage type = license.getSubscriptionPackage().getTypePackage();
        return new LicenseVerifyResponse(true, 200, type.toString(), "License hợp lệ", expiredAt);
    }

    public LicenseVerifyResponse verifyLicensePro(LicenseVerifyRequestForm request) {
        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(request.getLicenseKey());

        if (licenseOpt.isEmpty()) {
            return new LicenseVerifyResponse(false, 404, null, "License không tồn tại", null);
        }

        License license = licenseOpt.get();

        if (!Boolean.TRUE.equals(license.getCanUsed())) {
            return new LicenseVerifyResponse(false, 401, null, "License chưa được kích hoạt", null);
        }

        if (!license.getUser().getId().equals(request.getUserId())) {
            return new LicenseVerifyResponse(false, 403, null, "License không hợp lệ với người dùng này", null);
        }

        LocalDateTime expiredAt = license.getCreatedAt().plusDays(license.getDuration());
        if (expiredAt.isBefore(LocalDateTime.now())) {
            return new LicenseVerifyResponse(false, 400, null, "License đã hết hạn", expiredAt);
        }

        SubscriptionPackage.TypePackage type = license.getSubscriptionPackage().getTypePackage();

        return new LicenseVerifyResponse(true, 200, type.toString(), "License hợp lệ", expiredAt);
    }





    private String generateLicenseKey() {
        return "LIC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    @Override
    public LicenseDTO update(Long id, LicenseDTO dto) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("License not found"));

        license.setLicenseKey(dto.getLicenseKey());
        license.setDuration(dto.getDuration());
        license.setIp(dto.getIp());

        return toDtoWithSubscription(licenseRepository.save(license));
    }

    @Override
    public void delete(Long id) {
        licenseRepository.deleteById(id);
    }

    @Override
    public List<LicenseDTO> getByUserId(Long userId) {
        return licenseRepository.findByUserId(userId).stream()
                .filter(l -> l.getSubscriptionPackage() != null && Boolean.TRUE.equals(l.getSubscriptionPackage().getIsActive()))
                .map(this::toDtoWithSubscription)
                .toList();
    }

    // Dùng khi cần cả Subscription info
    private LicenseDTO toDtoWithSubscription(License l) {
        boolean isExpired;
        int daysLeft;

        if (Boolean.FALSE.equals(l.getCanUsed())) {
            isExpired = false;
            daysLeft = l.getDuration();
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiryDate = l.getCreatedAt().plusDays(l.getDuration());
            isExpired = now.isAfter(expiryDate);
            daysLeft = isExpired ? 0 : (int) Duration.between(now, expiryDate).toDays();
        }

        return LicenseDTO.builder()
                .id(l.getId())
                .licenseKey(l.getLicenseKey())
                .duration(l.getDuration())
                .ip(l.getIp())
                .hardwareId(l.getHardwareId())
                .userId(l.getUser().getId())
                .isExpired(isExpired)
                .daysLeft(daysLeft)
                .canUsed(l.getCanUsed())
                .subscriptionId(l.getSubscriptionPackage().getId())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .subscription(modelMapper.map(l.getSubscriptionPackage(), SubscriptionPackageDTO.class))
                .build();
    }



    private License toEntity(LicenseDTO dto) {
        return License.builder()
                .licenseKey(dto.getLicenseKey())
                .duration(dto.getDuration())
                .ip(dto.getIp())
                .user(userRepository.findById(dto.getUserId()).orElseThrow())
                .subscriptionPackage(subscriptionRepository.findById(dto.getSubscriptionId()).orElseThrow())
                .build();
    }
}
