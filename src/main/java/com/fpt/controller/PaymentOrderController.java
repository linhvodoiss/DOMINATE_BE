package com.fpt.controller;

import com.fpt.annotation.CurrentUserId;
import com.fpt.dto.PayOSDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.PaymentOrder;
import com.fpt.form.OrderFormCreating;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.IPaymentOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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

//    @GetMapping
//    public List<PaymentOrderDTO> getAll() {
//        return service.getAll();
//    }

    @GetMapping()
    public ResponseEntity<PaginatedResponse<PaymentOrderDTO>> getAllOrders(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long subscriptionId,
            @RequestParam(required = false) PaymentOrder.PaymentStatus status
    ) {
        Page<PaymentOrderDTO> dtoPage = service.getAllPackage(pageable, search,subscriptionId, status);
        PaginatedResponse<PaymentOrderDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách các đơn hàng trên hệ thống thành công");
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<?> createOrder(
            @CurrentUserId Long userId,
            @RequestBody @Valid OrderFormCreating form

    ) {;
        PaymentOrder order = service.createOrder(form, userId);
        SuccessResponse<PaymentOrder> response = new SuccessResponse<>(201, "Tạo đơn hàng thành công", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<SuccessResponse<String>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String newStatus) {
        try {
            PaymentOrder updatedOrder = service.changeStatusOrder(orderId, newStatus);
            return ResponseEntity.ok(new SuccessResponse<>(
                    200,
                    "Cập nhật trạng thái thành công",
                    updatedOrder.getPaymentStatus().name()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse<>(
                    400,
                    e.getMessage(),
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessResponse<>(
                    500,
                    "Lỗi hệ thống",
                    null
            ));
        }
    }




    @GetMapping("/user/{userId}")
    public ResponseEntity<PaginatedResponse<PaymentOrderDTO>> getByUserId(Pageable pageable,@PathVariable Long userId,            @RequestParam(required = false) String search,
                                                             @RequestParam(required = false) Long subscriptionId,
                                                             @RequestParam(required = false) PaymentOrder.PaymentStatus status) {
        Page<PaymentOrderDTO> dtoPage = service.getUserPackage(pageable, search,subscriptionId, status,userId);
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
                "Lấy thông tin đơn hàng thành công",
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
