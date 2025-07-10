package com.fpt.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.fpt.annotation.CurrentUserId;
import com.fpt.dto.*;
import com.fpt.dto.filter.ProductFilter;
import com.fpt.form.ChangePasswordForm;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
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
@RequestMapping(value = "/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminController {

    @Autowired
    private IUserService userService;
    @GetMapping("/list")
    public ResponseEntity<PaginatedResponse<UserListDTO>> getAllUsers(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer status
    ) {
        Page<User> entityPages = userService.getAllUser(pageable, search, status);
        List<UserListDTO> dtos = userService.convertToDto(entityPages.getContent());
        Page<UserListDTO> dtoPage = new PageImpl<>(dtos, pageable, entityPages.getTotalElements());

        PaginatedResponse<UserListDTO> response = new PaginatedResponse<>(
                dtoPage,
                HttpServletResponse.SC_OK,
                "Lấy danh sách người dùng thành công"
        );

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/ban/{id}")
    public ResponseEntity<SuccessNoResponse> toggleBanUser(@PathVariable Long id) {
        try {
            boolean isActive = userService.updateActiveStatus(id);
            String message = isActive ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản";

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
            response.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", HttpStatus.BAD_REQUEST.value());
            response.put("message", "Đổi mật khẩu thất bại: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }


}
