package com.fpt.controller;

import com.fpt.annotation.CurrentUserId;
import com.fpt.dto.PayOSDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.form.OrderFormCreating;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.IEmailService;
import com.fpt.service.IPaymentOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
public class PaymentOrderController {

    private final IPaymentOrderService service;
    private final IEmailService emailService;
//    @GetMapping
//    public List<PaymentOrderDTO> getAll() {
//        return service.getAll();
//    }

    @GetMapping()
    public ResponseEntity<PaginatedResponse<PaymentOrderDTO>> getAllOrders(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long subscriptionId,
            @RequestParam(required = false) PaymentOrder.PaymentStatus status,
            @RequestParam(required = false) SubscriptionPackage.TypePackage type
    ) {
        Page<PaymentOrderDTO> dtoPage = service.getAllOrder(pageable, search,subscriptionId, status,type);
        PaginatedResponse<PaymentOrderDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Take data order successfully");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<?> createOrder(
            @CurrentUserId Long userId,
            @RequestBody @Valid OrderFormCreating form

    ) {;
        PaymentOrderDTO order = service.createOrder(form, userId);
        SuccessResponse<PaymentOrderDTO> response = new SuccessResponse<>(201, "Tạo đơn hàng thành công", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<SuccessResponse<String>> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam String newStatus) {
        try {
            PaymentOrder updatedOrder = service.changeStatusOrderByOrderId(orderId, newStatus);

            return ResponseEntity.ok(new SuccessResponse<>(
                    200,
                    "Update status successfully",
                    updatedOrder.getPaymentStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse<>(
                    400,
                    e.getMessage(),
                    null
            ));
        }
    }
    @PatchMapping("/admin/{orderId}")
    public ResponseEntity<SuccessResponse<String>> updateOrderStatusAdmin(
            @PathVariable Integer orderId,
            @RequestParam String newStatus) {
        try {
            PaymentOrder updatedOrder = service.changeStatusOrderByAdmin(orderId, newStatus);

            return ResponseEntity.ok(new SuccessResponse<>(
                    200,
                    "Update status successfully",
                    updatedOrder.getPaymentStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse<>(
                    400,
                    e.getMessage(),
                    null
            ));
        }
    }

    @PatchMapping("/silent/{orderId}")
    public ResponseEntity<SuccessResponse<String>> updateOrderStatusSilently(
            @PathVariable Integer orderId,
            @RequestParam String newStatus) {
        try {
            PaymentOrder updatedOrder = service.changeStatusOrderSilently(orderId, newStatus);
            return ResponseEntity.ok(new SuccessResponse<>(
                    200,
                    "Update status successfully (no socket)",
                    updatedOrder.getPaymentStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse<>(
                    400,
                    e.getMessage(),
                    null
            ));
        }
    }

    @PutMapping("/license/{orderId}")
    public ResponseEntity<SuccessResponse<String>> updateOrderLicense(
            @PathVariable Integer orderId,
            @RequestParam String newStatus, HttpServletRequest request) {
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
            PaymentOrder updatedOrder = service.changeStatusOrderIdCreateLicense(orderId, newStatus,ip);
            return ResponseEntity.ok(new SuccessResponse<>(
                    200,
                    "License create successfully",
                    updatedOrder.getPaymentStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse<>(
                    400,
                    e.getMessage(),
                    null
            ));
        }
    }


    @PostMapping("/email")
    public ResponseEntity<SuccessNoResponse> sendEmailCustomer(
            @RequestParam("packageId") Long packageId,
            @RequestParam("orderId") Integer orderId,
            @RequestParam("email") String email
    ) {
        emailService.sendEmailForCustomer(email, packageId, orderId);
        return ResponseEntity.ok(new SuccessNoResponse(
                200,
                "Your order have been received, waiting for confirmation"

        ));
    }

    @PostMapping("/emailAdmin")
    public ResponseEntity<SuccessNoResponse> sendEmailAdmin(
            @RequestParam("packageId") Long packageId,
            @RequestParam("orderId") Integer orderId,
            @RequestParam("email") String email
    ) {
        emailService.sendEmailForNotificationAdmin(email, packageId, orderId);
        return ResponseEntity.ok(new SuccessNoResponse(
                200,
                "Email have send."

        ));
    }

    @PostMapping("/emailReport")
    public ResponseEntity<SuccessNoResponse> sendEmailReport(
            @RequestParam("packageId") Long packageId,
            @RequestParam("orderId") Integer orderId,
            @RequestParam("content") String content
    ) {
        emailService.sendEmailReport(packageId, orderId,content);
        return ResponseEntity.ok(new SuccessNoResponse(
                200,
                "Your report have been send."
        ));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public ResponseEntity<PaginatedResponse<PaymentOrderDTO>> getByUserId( @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,@CurrentUserId Long userId,  @RequestParam(required = false) String search,
                                                             @RequestParam(required = false) Long subscriptionId,
                                                             @RequestParam(required = false) PaymentOrder.PaymentStatus status,
                                                                           @RequestParam(required = false) SubscriptionPackage.TypePackage type
    ) {
        Page<PaymentOrderDTO> dtoPage = service.getUserOrder(pageable, search,subscriptionId, status,userId,type);
        PaginatedResponse<PaymentOrderDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách đơn hàng của bạn thành công");
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{id}")
//    public PaymentOrderDTO getById(@PathVariable Long id) {
//        return service.getById(id);
//    }
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<PaymentOrderDTO>> getByOrderId(@PathVariable Integer id) {
        PaymentOrderDTO dto = service.getByOrderId(id);
        SuccessResponse<PaymentOrderDTO> response = new SuccessResponse<>(
                200,
                "Take data order success.",
                dto
        );
        return ResponseEntity.ok(response);
    }


//    @PutMapping("/{id}")
//    public PaymentOrderDTO update(@PathVariable Long id, @RequestBody PaymentOrderDTO dto) {
//        return service.update(id, dto);
//    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }


    @GetMapping("/exists/{orderId}")
    public ResponseEntity<Boolean> checkOrderIdExists(@PathVariable Integer orderId) {
        boolean exists = service.orderIdExists(orderId);
        return ResponseEntity.ok(exists);
    }
}
