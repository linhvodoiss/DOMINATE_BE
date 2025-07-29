package com.fpt.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.fpt.annotation.CurrentUserId;
import com.fpt.dto.*;
import com.fpt.dto.filter.ProductFilter;
import com.fpt.entity.UserStatus;
import com.fpt.form.ChangePasswordForm;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.IPaymentOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.entity.User;
import com.fpt.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping(value = "/api/v1/account")
//@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IPaymentOrderService paymentOrderService;
    @GetMapping
    public ResponseEntity<PaginatedResponse<UserListDTO>> getAllUsers(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean isActive
    ) {
        Page<User> entityPages = userService.getAllUser(pageable, search, status,isActive);
        List<UserListDTO> dtos = userService.convertToDto(entityPages.getContent());
        Page<UserListDTO> dtoPage = new PageImpl<>(dtos, pageable, entityPages.getTotalElements());

        PaginatedResponse<UserListDTO> response = new PaginatedResponse<>(
                dtoPage,
                HttpServletResponse.SC_OK,
                "Get list user successfully"
        );

        return ResponseEntity.ok(response);
    }
    @PostMapping
    public ResponseEntity<SuccessResponse<UserDTO>> createUser(@RequestBody @Valid UserDTO userDTO) {
        UserDTO createdUser = userService.addUserByAdmin(userDTO);
        return ResponseEntity.ok(new SuccessResponse<>(200, "User created successfully", createdUser));
    }


    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserDTO userDTO
    ) {
        UserDTO updatedUser = userService.updateUserByAdmin(id, userDTO);
        return ResponseEntity.ok(new SuccessResponse<>(200, "User updated successfully", updatedUser));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessNoResponse> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(new SuccessNoResponse(200, "Delete successfully!"));
    }

    @DeleteMapping
    public ResponseEntity<SuccessNoResponse> deleteMany(@RequestBody List<Long> ids) {
        userService.deleteMany(ids);
        return ResponseEntity.ok(new SuccessNoResponse(200, "Delete successfully!"));
    }


    @PatchMapping("/ban/{id}")
    public ResponseEntity<SuccessNoResponse> toggleBanUser(@PathVariable Long id) {
        try {
            boolean isActive = userService.updateActiveStatus(id);
            String message = isActive ? "Unbanned account" : "Banned account";

            return ResponseEntity.ok(new SuccessNoResponse(200, message));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessNoResponse(500, "Lỗi hệ thống"));
        }
    }

    @PatchMapping("/changePassword/{userId}")
    public ResponseEntity<?> changePasswordAdmin(
            @PathVariable Long userId,
            @RequestBody ChangePasswordForm request
    ) {
        try {
            userService.changePasswordAdmin(userId, request);
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.OK.value());
            response.put("message", "Change password successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Change password failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }
    @GetMapping("/dashboard")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> getAdminDashboard() {
        long totalCustomers = userService.countCustomerAccounts();
        long totalOrders = paymentOrderService.countTotalOrders();
        double totalRevenue = paymentOrderService.getTotalRevenue();
        Map<String, Long> ordersByStatus = paymentOrderService.countOrdersByStatus();

        Map<String, Long> ordersByPaymentMethod = paymentOrderService.countOrdersByPaymentMethod();
        Map<String, Double> revenueByPaymentMethod = paymentOrderService.revenueByPaymentMethod();

        Map<String, Object> data = new HashMap<>();
        data.put("totalCustomers", totalCustomers);
        data.put("totalOrders", totalOrders);
        data.put("totalRevenue", totalRevenue);
        data.put("ordersByStatus", ordersByStatus);
        data.put("ordersByPaymentMethod", ordersByPaymentMethod);
        data.put("revenueByPaymentMethod", revenueByPaymentMethod);

        SuccessResponse<Map<String, Object>> response = new SuccessResponse<>(
                HttpServletResponse.SC_OK,
                "Get dashboard data successfully",
                data
        );

        return ResponseEntity.ok(response);
    }




}
