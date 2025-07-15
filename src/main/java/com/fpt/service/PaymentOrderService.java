package com.fpt.service;

import com.fpt.dto.OptionDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.Option;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.entity.User;
import com.fpt.form.OrderFormCreating;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentOrderService implements IPaymentOrderService {

    private final PaymentOrderRepository repository;
    private final UserRepository userRepository;
    private final SubscriptionPackageRepository subscriptionRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PaymentSocketService paymentSocketService;
    @Override
    public Page<PaymentOrderDTO> getAllPackage(Pageable pageable, String search,Long subscriptionId, PaymentOrder.PaymentStatus status) {
        PaymentOrderSpecificationBuilder specification = new PaymentOrderSpecificationBuilder(search,subscriptionId,status);
        return repository.findAll(specification.build(), pageable)
                .map(subscription -> modelMapper.map(subscription, PaymentOrderDTO.class));
    }

    @Override
    public Page<PaymentOrderDTO> getUserPackage(Pageable pageable, String search, Long subscriptionId, PaymentOrder.PaymentStatus status, Long userId) {
        PaymentOrderSpecificationBuilder specification = new PaymentOrderSpecificationBuilder(search,subscriptionId,status,userId);
        return repository.findAll(specification.build(), pageable)
                .map(subscription -> modelMapper.map(subscription, PaymentOrderDTO.class));
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
        order.setBin(form.getBin());
        order.setAccountName(form.getAccountName());
        order.setAccountNumber(form.getAccountNumber());
        order.setQrCode(form.getQrCode());
        order.setPaymentMethod(form.getPaymentMethod());
        order.setPaymentStatus(PaymentOrder.PaymentStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        PaymentOrder savedOrder = repository.save(order);
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
            throw new RuntimeException("Trạng thái không hợp lệ");
        }
    }
    @Override
    public PaymentOrder changeStatusOrderByOrderId(Integer orderId, String newStatus) {
        PaymentOrder order = repository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với orderId: " + orderId));

        try {
            PaymentOrder.PaymentStatus status = PaymentOrder.PaymentStatus.valueOf(newStatus);
            order.setPaymentStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            PaymentOrder savedOrder = repository.save(order);

            // Send Socket
            paymentSocketService.notifyOrderStatus(orderId, newStatus);

            return savedOrder;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ");
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
                    .map(option -> OptionDTO.builder()
                            .id(option.getId())
                            .name(option.getName())
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
                    .options(optionDTOs) // ✅ optionDTOs không chứa subscriptionPackages
                    .simulatedCount(subscription.getSimulatedCount())
                    .createdAt(subscription.getCreatedAt())
                    .updatedAt(subscription.getUpdatedAt())
                    .build();
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
                .userId(entity.getUser().getId())
                .subscriptionId(subscription != null ? subscription.getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .subscription(subscriptionDto)
                .build();
    }




    private PaymentOrder toEntity(PaymentOrderDTO dto) {
        return PaymentOrder.builder()
                .orderId(dto.getOrderId())
                .paymentLink(dto.getPaymentLink())
                .paymentStatus(PaymentOrder.PaymentStatus.valueOf(dto.getPaymentStatus()))
                .paymentMethod(PaymentOrder.PaymentMethod.valueOf(dto.getPaymentMethod()))
                .licenseCreated(dto.getLicenseCreated())
                .bin(dto.getBin())
                .accountName(dto.getAccountName())
                .accountNumber(dto.getAccountNumber())
                .qrCode(dto.getQrCode())
                .user(userRepository.findById(dto.getUserId()).orElseThrow())
                .subscriptionPackage(subscriptionRepository.findById(dto.getSubscriptionId()).orElseThrow())
                .build();
    }

}
