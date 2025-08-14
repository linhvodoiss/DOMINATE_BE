package com.fpt.service;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.*;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.LicenseVerifyRequestForm;
import com.fpt.payload.LicenseVerifyResponse;
import com.fpt.repository.LicenseRepository;
import com.fpt.repository.PaymentOrderRepository;
import com.fpt.repository.SubscriptionPackageRepository;
import com.fpt.repository.UserRepository;
import com.fpt.specification.LicenseSpecificationBuilder;
import com.fpt.utils.LicenseKeyGenerate;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    public Page<LicenseDTO> getUserLicense(Pageable pageable, String search, Long userId,SubscriptionPackage.TypePackage type) {
        LicenseSpecificationBuilder specification = new LicenseSpecificationBuilder(search, userId,type);
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
    public List<LicenseDTO> getLicenseIsActiveOfUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<License> licenses = licenseRepository.findByUserId(userId)
                .stream()
                .filter(license -> Boolean.TRUE.equals(license.getCanUsed()))
                .toList();

        return licenses.stream()
                .map(this::toDtoWithSubscription)
                .collect(Collectors.toList());
    }


    @Override
    public LicenseDTO create(LicenseDTO dto) {
        return toDtoWithSubscription(licenseRepository.save(toEntity(dto)));
    }

    @Override
    public LicenseDTO createLicense(LicenseCreateForm form, String ip) {
        PaymentOrder order = paymentOrderRepository.findByOrderIdForUpdate(form.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Not found order."));

        if (order.getPaymentStatus() != PaymentOrder.PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Unpaid order.");
        }
        if (Boolean.TRUE.equals(order.getLicenseCreated())) {
            throw new IllegalArgumentException("License have created with this order.");
        }

        SubscriptionPackage subscription = order.getSubscriptionPackage();
        int durationDays = switch (subscription.getBillingCycle()) {
            case MONTHLY -> 30;
            case HALF_YEARLY -> 182;
            case YEARLY -> 365;
            default -> throw new IllegalStateException("BillingCycle is not valid.");
        };

        Long userId = order.getUser().getId();
        boolean hasActiveLicense = licenseRepository.existsByUserIdAndCanUsedTrue(userId);

        License license = new License();
        license.setLicenseKey(LicenseKeyGenerate.generateLicenseKey());
        license.setDuration(durationDays);
        license.setIp(ip);
        license.setUser(order.getUser());
        license.setSubscriptionPackage(subscription);
        license.setOrderId(form.getOrderId());
        license.setCanUsed(!hasActiveLicense);

        if (!hasActiveLicense) {
            license.setActivatedAt(LocalDateTime.now());
        } else {
            license.setActivatedAt(null);
        }

        order.setLicenseCreated(true);
        paymentOrderService.changeStatusOrderByOrderId(form.getOrderId(), "SUCCESS");
        License saved = licenseRepository.save(license);
        return toDtoWithSubscription(saved);
    }

    public LicenseDTO bindHardwareIdToLicense(LicenseCreateForm form) {
        License license = licenseRepository.findByLicenseKey(form.getLicenseKey())
                .orElseThrow(() -> new IllegalArgumentException("License is not exist."));
        User licenseOwner = license.getUser();

        if (Boolean.FALSE.equals(licenseOwner.getIsActive())) {
            throw new IllegalStateException("Owner is banned. License cannot be used.");
        }
        if (licenseOwner.getStatus()== UserStatus.NOT_ACTIVE) {
            throw new IllegalStateException("This account is not active. License cannot be used.");
        }
        if (!license.getSubscriptionPackage().getTypePackage().equals(form.getType())) {
            throw new IllegalStateException("License type mismatch.");
        }
        if (license.getHardwareId() != null && !license.getHardwareId().equals(form.getHardwareId())) {
            throw new IllegalStateException("License don't have match with other device.");
        }

        if (!Boolean.TRUE.equals(license.getCanUsed())) {
            throw new IllegalStateException("License haven't active.");
        }

        LocalDateTime expiredAt = license.getActivatedAt().plusDays(license.getDuration());
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("License have expired.");
        }

        license.setHardwareId(form.getHardwareId());
        licenseRepository.save(license);

        return toDtoWithSubscription(license);
    }

    @Override
    public LicenseDTO activateNextLicense(Long userId, SubscriptionPackage.TypePackage type) {
        List<License> userLicenses = licenseRepository.findByUserId(userId)
                .stream()
                .filter(l -> l.getSubscriptionPackage().getTypePackage() == type)
                .toList();

        LocalDateTime now = LocalDateTime.now();

        userLicenses.stream()
                .filter(l -> Boolean.TRUE.equals(l.getCanUsed()))
                .findFirst()
                .ifPresent(currentUsed -> {
                    if (currentUsed.getActivatedAt() != null &&
                            currentUsed.getActivatedAt().plusDays(currentUsed.getDuration()).isAfter(now)) {
                        throw new IllegalArgumentException("Key " + type + " still valid.");
                    }
                    currentUsed.setCanUsed(false);
                    licenseRepository.save(currentUsed);
                });

        List<License> validUnusedLicenses = userLicenses.stream()
                .filter(l -> Boolean.FALSE.equals(l.getCanUsed()))
                .filter(l -> l.getCreatedAt().plusDays(l.getDuration()).isAfter(now))
                .sorted(Comparator.comparing(License::getCreatedAt))
                .toList();

        if (validUnusedLicenses.isEmpty()) {
            throw new IllegalArgumentException("No key belong " + type + " have expired date.");
        }

        License toActivate = validUnusedLicenses.get(0);
        toActivate.setCanUsed(true);
        toActivate.setActivatedAt(now);
        licenseRepository.save(toActivate);

        return toDtoWithSubscription(toActivate);
    }

    public LicenseVerifyResponse verifyLicense(LicenseVerifyRequestForm request) {
        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(request.getLicenseKey());

        if (licenseOpt.isEmpty()) {
            return new LicenseVerifyResponse(false, 404, null, "License is not exist.", null);
        }

        License license = licenseOpt.get();

        if (!Boolean.TRUE.equals(license.getCanUsed())) {
            return new LicenseVerifyResponse(false, 401, null, "License haven't active.", null);
        }

        if (!license.getHardwareId().equals(request.getHardwareId())) {
            return new LicenseVerifyResponse(false, 403, null, "License don't have match with other device.", null);
        }

        LocalDateTime expiredAt = license.getActivatedAt().plusDays(license.getDuration());
        if (expiredAt.isBefore(LocalDateTime.now())) {
            return new LicenseVerifyResponse(false, 400, null, "License is expired.", expiredAt);
        }

        SubscriptionPackage.TypePackage type = license.getSubscriptionPackage().getTypePackage();
        return new LicenseVerifyResponse(true, 200, type.toString(), "License is valid.", expiredAt);
    }

    public LicenseVerifyResponse verifyLicensePro(LicenseVerifyRequestForm request) {
        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(request.getLicenseKey());

        if (licenseOpt.isEmpty()) {
            return new LicenseVerifyResponse(false, 404, null, "License is not exist.", null);
        }

        License license = licenseOpt.get();

        if (!Boolean.TRUE.equals(license.getCanUsed())) {
            return new LicenseVerifyResponse(false, 401, null, "License haven't active.", null);
        }

        if (!license.getUser().getId().equals(request.getUserId())) {
            return new LicenseVerifyResponse(false, 403, null, "License don't have match with these user.", null);
        }

        LocalDateTime expiredAt = license.getActivatedAt().plusDays(license.getDuration());
        if (expiredAt.isBefore(LocalDateTime.now())) {
            return new LicenseVerifyResponse(false, 400, null, "License is expired.", expiredAt);
        }

        SubscriptionPackage.TypePackage type = license.getSubscriptionPackage().getTypePackage();
        return new LicenseVerifyResponse(true, 200, type.toString(), "License is valid.", expiredAt);
    }

    @Override
    public LicenseDTO update(Long id, LicenseDTO dto) {
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("License not found."));

        license.setLicenseKey(dto.getLicenseKey());
        license.setDuration(dto.getDuration());
        license.setIp(dto.getIp());

        return toDtoWithSubscription(licenseRepository.save(license));
    }

    @Override
    public void unbindHardwareIdFromLicense(String licenseKey) {
        License license = licenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new IllegalArgumentException("License not found."));

        license.setHardwareId(null);
        licenseRepository.save(license);
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

    private LicenseDTO toDtoWithSubscription(License l) {
        boolean isExpired;
        int daysLeft;

        if (Boolean.FALSE.equals(l.getCanUsed())) {
            isExpired = false;
            daysLeft = l.getDuration();
        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiryDate = l.getActivatedAt().plusDays(l.getDuration());
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
                .orderId(l.getOrderId())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .activatedAt(l.getActivatedAt())
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
