package com.fpt.service;

import com.fpt.dto.*;
import com.fpt.entity.*;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.OrderFormCreating;
import com.fpt.repository.LicenseRepository;
import com.fpt.repository.PaymentOrderRepository;
import com.fpt.repository.SubscriptionPackageRepository;
import com.fpt.repository.UserRepository;
import com.fpt.specification.PaymentOrderSpecificationBuilder;
import com.fpt.utils.LicenseKeyGenerate;
import com.fpt.websocket.PaymentSocketService;
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
    public Page<PaymentOrderDTO> getAllOrder(Pageable pageable, String search,Long subscriptionId, PaymentOrder.PaymentStatus status,SubscriptionPackage.TypePackage type) {
        PaymentOrderSpecificationBuilder specification = new PaymentOrderSpecificationBuilder(search,subscriptionId,status,type);
        return repository.findAll(specification.build(), pageable).map(this::toDto);

    }

    @Override
    public Page<PaymentOrderDTO> getUserOrder(Pageable pageable, String search, Long subscriptionId, PaymentOrder.PaymentStatus status, Long userId,SubscriptionPackage.TypePackage type) {
        PaymentOrderSpecificationBuilder specification = new PaymentOrderSpecificationBuilder(search,subscriptionId,status,userId,type);
        return repository.findAll(specification.build(), pageable).map(this::toDto);

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
                .orElseThrow(() -> new RuntimeException("Not found package plan"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Not found user"));
        if (repository.existsByOrderId(form.getOrderId())) {
            throw new RuntimeException("Code orderId is exist");
        }

        PaymentOrder order = new PaymentOrder();
        order.setUser(user);
        order.setSubscriptionPackage(subscription);
        order.setOrderId(form.getOrderId());
        order.setPaymentLink(form.getPaymentLink());
        order.setPrice(form.getPrice());
//        order.setBin(form.getBin());
//        order.setAccountName(form.getAccountName());
//        order.setAccountNumber(form.getAccountNumber());
//        order.setQrCode(form.getQrCode());
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
    public void updateOrderFromWebhook(int orderCode, String internalStatus,
                                       String bin, String accountName, String accountNumber,String dateTransfer, String ip) {
        PaymentOrder order = repository.findByOrderId(orderCode)
                .orElseThrow(() -> new RuntimeException("Not found order with orderCode: " + orderCode));

        // Update information from webhook
        if (bin != null) order.setBin(bin);
        if (accountName != null) order.setAccountName(accountName);
        if (accountNumber != null) order.setAccountNumber(accountNumber);
        if (dateTransfer != null) order.setDateTransfer(dateTransfer);
        order.setUpdatedAt(LocalDateTime.now());

        // Save information
        repository.save(order);

        // call service and send socket
        changeStatusOrderIdCreateLicense(orderCode, internalStatus,ip);
    }

    @Override
    public void syncBill(int orderCode, String bin, String accountName, String accountNumber,String dateTransfer) {
        PaymentOrder order = repository.findByOrderId(orderCode)
                .orElseThrow(() -> new RuntimeException("Not found order with orderCode: " + orderCode));

        // Update information from webhook
        if (bin != null) order.setBin(bin);
        if (accountName != null) order.setAccountName(accountName);
        if (accountNumber != null) order.setAccountNumber(accountNumber);
        if (dateTransfer != null) order.setDateTransfer(dateTransfer);
        order.setUpdatedAt(LocalDateTime.now());

        // Save information
        repository.save(order);
        paymentSocketService.notifySyncBill(orderCode, accountName);
    }

    @Override
    public void addReasonCancel(int orderCode, String cancelReason,String dateTransfer) {
        PaymentOrder order = repository.findByOrderId(orderCode)
                .orElseThrow(() -> new RuntimeException("Not found order with orderCode: " + orderCode));

        // Update information from webhook

        if (dateTransfer != null) order.setDateTransfer(dateTransfer);
        if (cancelReason != null) order.setCancelReason(cancelReason);
        order.setUpdatedAt(LocalDateTime.now());
        // Save information
        repository.save(order);
        paymentSocketService.notifySyncBill(orderCode, dateTransfer);
    }




    @Override
    public PaymentOrder changeStatusOrder(Long orderId, String newStatus) {
        PaymentOrder order = repository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Not found order"));

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
    public LicenseDTO createLicensePayOS(LicenseCreateForm form, String ip) {
        PaymentOrder order = repository.findByOrderIdForUpdate(form.getOrderId())
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
        License saved = licenseRepository.save(license);
        return toDtoWithSubscription(saved);
    }

    @Override
    public PaymentOrder changeStatusOrderIdCreateLicense(Integer orderId, String newStatus, String ip) {
        PaymentOrder order = repository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Not found order with orderId: " + orderId));

        try {
            PaymentOrder.PaymentStatus status = PaymentOrder.PaymentStatus.valueOf(newStatus);
            order.setPaymentStatus(status);
            order.setUpdatedAt(LocalDateTime.now());

            // create license
            if (status == PaymentOrder.PaymentStatus.SUCCESS && !Boolean.TRUE.equals(order.getLicenseCreated())) {
                LicenseCreateForm form = new LicenseCreateForm();
                form.setOrderId(orderId);
                createLicensePayOS(form, ip);

            }

            PaymentOrder savedOrder = repository.save(order);

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
                .cancelReason(entity.getCancelReason())
                .paymentStatus(entity.getPaymentStatus().name())
                .paymentMethod(entity.getPaymentMethod().name())
                .licenseCreated(entity.getLicenseCreated())
                .price(entity.getPrice())
                .canReport(canReport)
                .userId(user != null ? user.getId() : null)
                .buyer(buyerDto)
                .subscriptionId(subscription != null ? subscription.getId() : null)
                .dateTransfer(entity.getDateTransfer())
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
                .cancelReason(dto.getCancelReason())
                .user(userRepository.findById(dto.getUserId()).orElseThrow())
                .subscriptionPackage(subscriptionRepository.findById(dto.getSubscriptionId()).orElseThrow())
                .build();
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



    @Override
public Double getTotalRevenue() {
    return repository.findAll().stream()
            .filter(order -> order.getPaymentStatus() == PaymentOrder.PaymentStatus.SUCCESS)
            .mapToDouble(PaymentOrder::getPrice)
            .sum();
}
    @Override
    public Long countTotalOrders() {
        return repository.count();
    }
    @Override
    public Map<String, Long> countOrdersByStatus() {
        Map<String, Long> result = new HashMap<>();
        for (PaymentOrder.PaymentStatus status : PaymentOrder.PaymentStatus.values()) {
            long count = repository.countByPaymentStatus(status);
            result.put(status.name(), count);
        }
        return result;
    }
    @Override
    public Map<String, Long> countOrdersByPaymentMethod() {
        Map<String, Long> result = new HashMap<>();

        for (PaymentOrder.PaymentMethod method : PaymentOrder.PaymentMethod.values()) {
            long count = repository.countByPaymentMethod(method);
            result.put(method.name(), count);
        }

        return result;
    }
    @Override
    public Map<String, Double> revenueByPaymentMethod() {
        Map<String, Double> result = new HashMap<>();

        for (PaymentOrder.PaymentMethod method : PaymentOrder.PaymentMethod.values()) {
            double sum = repository.findAllByPaymentMethodAndPaymentStatus(method, PaymentOrder.PaymentStatus.SUCCESS)
                    .stream()
                    .mapToDouble(PaymentOrder::getPrice)
                    .sum();
            result.put(method.name(), sum);
        }

        return result;
    }

}
