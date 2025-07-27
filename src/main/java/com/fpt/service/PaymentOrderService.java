package com.fpt.service;

import com.fpt.dto.*;
import com.fpt.entity.*;
import com.fpt.form.OrderFormCreating;
import com.fpt.repository.LicenseRepository;
import com.fpt.repository.PaymentOrderRepository;
import com.fpt.repository.SubscriptionPackageRepository;
import com.fpt.repository.UserRepository;
import com.fpt.specification.PaymentOrderSpecificationBuilder;
import com.fpt.websocket.PaymentSocketService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentOrderService implements IPaymentOrderService {

    private final PaymentOrderRepository repository;
    private final UserRepository userRepository;
    private final LicenseRepository licenseRepository;

    private final SubscriptionPackageRepository subscriptionRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PaymentSocketService paymentSocketService;

    @Override
    public Page<PaymentOrderDTO> getAllPackage(Pageable pageable, String search,Long subscriptionId, PaymentOrder.PaymentStatus status,SubscriptionPackage.TypePackage type) {
        PaymentOrderSpecificationBuilder specification = new PaymentOrderSpecificationBuilder(search,subscriptionId,status,type);
        return repository.findAll(specification.build(), pageable).map(this::toDto);
//                .map(subscription -> modelMapper.map(subscription, PaymentOrderDTO.class));
    }

    @Override
    public Page<PaymentOrderDTO> getUserPackage(Pageable pageable, String search, Long subscriptionId, PaymentOrder.PaymentStatus status, Long userId,SubscriptionPackage.TypePackage type) {
        PaymentOrderSpecificationBuilder specification = new PaymentOrderSpecificationBuilder(search,subscriptionId,status,userId,type);
        return repository.findAll(specification.build(), pageable).map(this::toDto);
//                .map(subscription -> modelMapper.map(subscription, PaymentOrderDTO.class));
    }

    @Override
    public List<PaymentOrderDTO> convertToDto(List<PaymentOrder> paymentOrders) {
        List<PaymentOrderDTO> paymentOrderDTOs = new ArrayList<>();
        for (PaymentOrder paymentOrder : paymentOrders) {
            PaymentOrderDTO paymentOrderDTO = modelMapper.map(paymentOrder, PaymentOrderDTO.class);
            paymentOrderDTOs.add(paymentOrderDTO);
        }
        return paymentOrderDTOs;
    }

    @Override
    public PaymentOrderDTO createOrder(OrderFormCreating form, Long userId) {
        SubscriptionPackage subscription = subscriptionRepository.findById(form.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói đăng ký"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (repository.existsByOrderId(form.getOrderId())) {
            throw new RuntimeException("Mã orderId đã tồn tại");
        }

        PaymentOrder order = new PaymentOrder();
        order.setUser(user);
        order.setSubscriptionPackage(subscription);
        order.setOrderId(form.getOrderId());
        order.setPaymentLink(form.getPaymentLink());
        order.setPrice(form.getPrice());
        order.setBin(form.getBin());
        order.setAccountName(form.getAccountName());
        order.setAccountNumber(form.getAccountNumber());
        order.setQrCode(form.getQrCode());
        order.setPaymentMethod(form.getPaymentMethod());
        order.setPaymentStatus(PaymentOrder.PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        PaymentOrder savedOrder = repository.save(order);
        paymentSocketService.notifyNewOrder(
                savedOrder.getOrderId(),
                savedOrder.getUser().getUserName(),
                savedOrder.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                savedOrder.getSubscriptionPackage().getName(),
                savedOrder.getPrice(),
                savedOrder.getPaymentMethod().name() );
        return toDto(savedOrder);
    }


    @Override
    public PaymentOrder changeStatusOrder(Long orderId, String newStatus) {
        PaymentOrder order = repository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        try {
            PaymentOrder.PaymentStatus status = PaymentOrder.PaymentStatus.valueOf(newStatus);
            order.setPaymentStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            return repository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status is invalid");
        }
    }
    @Override
    public PaymentOrder changeStatusOrderByOrderId(Integer orderId, String newStatus) {
        PaymentOrder order = repository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Not found order with orderId: " + orderId));

        try {
            PaymentOrder.PaymentStatus status = PaymentOrder.PaymentStatus.valueOf(newStatus);
            order.setPaymentStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            PaymentOrder savedOrder = repository.save(order);

            // Send Socket
            paymentSocketService.notifyOrderStatus(orderId, newStatus);

            return savedOrder;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status is invalid");
        }
    }

    @Override
    public PaymentOrder changeStatusOrderByAdmin(Integer orderId, String newStatus) {
        PaymentOrder order = repository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Not found order with orderId: " + orderId));

        try {
            PaymentOrder.PaymentStatus status = PaymentOrder.PaymentStatus.valueOf(newStatus);
            order.setPaymentStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            PaymentOrder savedOrder = repository.save(order);

            // Send Socket
            paymentSocketService.notifyAdminStatus(orderId, newStatus);

            return savedOrder;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status is invalid");
        }
    }
    @Override
    public PaymentOrder changeStatusOrderSilently(Integer orderId, String newStatus) {
        PaymentOrder order = repository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Not found order with orderId: " + orderId));

        try {
            PaymentOrder.PaymentStatus status = PaymentOrder.PaymentStatus.valueOf(newStatus);
            order.setPaymentStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            return repository.save(order);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status is invalid");
        }
    }





    @Override
    public List<PaymentOrderDTO> getAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }
//    @Override
//    public List<PaymentOrderDTO> getByUserId(Long userId) {
//        return repository.findByUserId(userId)
//                .stream()
//                .map(this::toDto)
//                .collect(Collectors.toList());
//    }



    @Override
    public PaymentOrderDTO getById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Payment order not found"));
    }
    @Override
    public PaymentOrderDTO getByOrderId(Integer orderId) {
        return repository.findByOrderId(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Payment order not found"));
    }

    @Override
    public boolean orderExists(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean orderIdExists(Integer orderId) {
        return repository.existsByOrderId(orderId);
    }

    @Override
    public PaymentOrderDTO create(PaymentOrderDTO dto) {
        return toDto(repository.save(toEntity(dto)));
    }

    @Override
    public PaymentOrderDTO update(Long id, PaymentOrderDTO dto) {
        PaymentOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment order not found"));

        order.setOrderId(dto.getOrderId());
        order.setPaymentLink(dto.getPaymentLink());
        order.setPaymentStatus(PaymentOrder.PaymentStatus.valueOf(dto.getPaymentStatus()));

        return toDto(repository.save(order));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private PaymentOrderDTO toDto(PaymentOrder entity) {
        SubscriptionPackage subscription = entity.getSubscriptionPackage();
        SubscriptionPackageDTO subscriptionDto = null;

        if (subscription != null) {
            List<OptionDTO> optionDTOs = subscription.getOptions().stream()
                    .filter(option -> Boolean.TRUE.equals(option.getIsActive()))
                    .map(option -> OptionDTO.builder()
                            .id(option.getId())
                            .name(option.getName())
                            .isActive(option.getIsActive())
                            .createdAt(option.getCreatedAt())
                            .updatedAt(option.getUpdatedAt())
                            .build())
                    .toList();

            subscriptionDto = SubscriptionPackageDTO.builder()
                    .id(subscription.getId())
                    .name(subscription.getName())
                    .price(subscription.getPrice())
                    .discount(subscription.getDiscount())
                    .billingCycle(subscription.getBillingCycle().name())
                    .typePackage(subscription.getTypePackage().name())
                    .isActive(subscription.getIsActive())
                    .options(optionDTOs)
                    .simulatedCount(subscription.getSimulatedCount())
                    .createdAt(subscription.getCreatedAt())
                    .updatedAt(subscription.getUpdatedAt())
                    .build();
        }
        User user = entity.getUser();
        UserDTO buyerDto = null;
        if (user != null) {
            buyerDto = UserDTO.builder()
                    .id(user.getId())
                    .userName(user.getUserName())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phoneNumber(user.getPhoneNumber())
                    .build();
        }
        LicenseDTO licenseDto = null;
        Optional<License> licenseOpt = licenseRepository.findByOrderId(entity.getOrderId());

        if (licenseOpt.isPresent()) {
            License license = licenseOpt.get();
            boolean isExpired;
            int daysLeft;

            if (Boolean.FALSE.equals(license.getCanUsed())) {
                isExpired = false;
                daysLeft = license.getDuration();
            } else {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expiryDate = license.getActivatedAt().plusDays(license.getDuration());
                isExpired = now.isAfter(expiryDate);
                daysLeft = isExpired ? 0 : (int) Duration.between(now, expiryDate).toDays();
            }
            licenseDto = LicenseDTO.builder()
                    .id(license.getId())
                    .orderId(license.getOrderId())
                    .licenseKey(license.getLicenseKey())
                    .duration(license.getDuration())
                    .ip(license.getIp())
                    .hardwareId(license.getHardwareId())
                    .isExpired(isExpired)
                    .daysLeft(daysLeft)
                    .canUsed(license.getCanUsed())
                    .userId(license.getUser().getId())
                    .subscriptionId(license.getSubscriptionPackage().getId())
                    .createdAt(license.getCreatedAt())
                    .updatedAt(license.getUpdatedAt())
                    .activatedAt(license.getActivatedAt())
                    .build();
        }
        boolean canReport = false;
        if (entity.getCreatedAt() != null) {
            canReport = LocalDateTime.now().isAfter(entity.getCreatedAt().plusMinutes(1));
        }
        return PaymentOrderDTO.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .paymentLink(entity.getPaymentLink())
                .bin(entity.getBin())
                .accountName(entity.getAccountName())
                .accountNumber(entity.getAccountNumber())
                .qrCode(entity.getQrCode())
                .paymentStatus(entity.getPaymentStatus().name())
                .paymentMethod(entity.getPaymentMethod().name())
                .licenseCreated(entity.getLicenseCreated())
                .price(entity.getPrice())
                .canReport(canReport)
                .userId(user != null ? user.getId() : null)
                .buyer(buyerDto)
                .subscriptionId(subscription != null ? subscription.getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .subscription(subscriptionDto)
                .license(licenseDto)
                .build();
    }




    private PaymentOrder toEntity(PaymentOrderDTO dto) {
        return PaymentOrder.builder()
                .orderId(dto.getOrderId())
                .paymentLink(dto.getPaymentLink())
                .paymentStatus(PaymentOrder.PaymentStatus.valueOf(dto.getPaymentStatus()))
                .paymentMethod(PaymentOrder.PaymentMethod.valueOf(dto.getPaymentMethod()))
                .licenseCreated(dto.getLicenseCreated())
                .price(dto.getPrice())
                .bin(dto.getBin())
                .accountName(dto.getAccountName())
                .accountNumber(dto.getAccountNumber())
                .qrCode(dto.getQrCode())
                .user(userRepository.findById(dto.getUserId()).orElseThrow())
                .subscriptionPackage(subscriptionRepository.findById(dto.getSubscriptionId()).orElseThrow())
                .build();
    }

}
