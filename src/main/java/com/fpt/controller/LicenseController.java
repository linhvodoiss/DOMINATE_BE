package com.fpt.controller;

import com.fpt.annotation.CurrentUserId;
import com.fpt.dto.LicenseDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.UserLicenseViewDTO;
import com.fpt.entity.License;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.LicenseVerifyRequestForm;
import com.fpt.payload.*;
import com.fpt.repository.LicenseRepository;
import com.fpt.service.ILicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/licenses")
@RequiredArgsConstructor
@Validated
public class LicenseController {
    @Autowired
    private LicenseRepository licenseRepository;
    private final ILicenseService service;

//    @GetMapping
//    public List<LicenseDTO> getAll() {
//        return service.getAll();
//    }
@GetMapping()
public ResponseEntity<PaginatedResponse<LicenseDTO>> getAllOrders(
        @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(required = false) String search

) {
    Page<LicenseDTO> dtoPage = service.getAllLicense(pageable, search);
    PaginatedResponse<LicenseDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách các license thành công");
    return ResponseEntity.ok(response);
}
    @PreAuthorize("isAuthenticated()")
    @GetMapping("user")
    public ResponseEntity<PaginatedResponse<LicenseDTO>> getByUserId(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable, @CurrentUserId Long userId, @RequestParam(required = false) String search, @RequestParam(required = false) SubscriptionPackage.TypePackage type) {
        Page<LicenseDTO> dtoPage = service.getUserLicense(pageable, search,userId,type);
        PaginatedResponse<LicenseDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách các license thành công");
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("canUsed")
    public ResponseEntity<SuccessResponse<List<LicenseDTO>>> getCanUsedLicenses(@CurrentUserId Long userId) {
        List<LicenseDTO> licenseDTOs = service.getLicenseIsActiveOfUser(userId);
        return ResponseEntity.ok(new SuccessResponse<>(200, "Lấy danh sách license đang sử dụng thành công", licenseDTOs));
    }




    @GetMapping("/{id}")
    public LicenseDTO getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLicense(@RequestBody LicenseCreateForm form, HttpServletRequest request) {
        try {

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }

            LicenseDTO license = service.createLicense(form, ip);
            return ResponseEntity.ok(new SuccessResponse<>(200, "Tạo license thành công", license));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorNoResponse(400, ex.getMessage()));
        }
    }

    @PostMapping("/bind-hardware")
    public ResponseEntity<?> bindHardware(@RequestBody LicenseCreateForm form) {


        if (form.getLicenseKey() == null || form.getHardwareId() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "Lost licenseKey or hardwareId"
            ));
        }

        try {
            LicenseDTO dto = service.bindHardwareIdToLicense(form);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "Register device successfully",
                    "data", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "code", 400,
                    "message", e.getMessage()
            ));
        }
    }
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/activate-next")
    public ResponseEntity<?> activateNextLicense(
            @CurrentUserId Long userId,
            @RequestParam SubscriptionPackage.TypePackage type
    ) {
        try {
            LicenseDTO dto = service.activateNextLicense(userId, type);
            return ResponseEntity.ok(new SuccessResponse<>(200, "Get new license "+type+ " key to activate successfully", dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Có lỗi xảy ra"));
        }
    }

    @PatchMapping("/unbind-hardware")
    public ResponseEntity<SuccessNoResponse> unbindHardware(@RequestParam String licenseKey) {

        try {
            service.unbindHardwareIdFromLicense(licenseKey);
            return ResponseEntity.ok(new SuccessNoResponse(200, "Unbind hardware successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessNoResponse(500, "Unbind hardware failed!"));
        }


    }



    @PostMapping("/verify")
    public ResponseEntity<LicenseVerifyResponse> verifyLicense(@RequestBody LicenseVerifyRequestForm request) {
        return ResponseEntity.ok(service.verifyLicense(request));
    }
    @PostMapping("/verifyPro")
    public ResponseEntity<LicenseVerifyResponse> verifyLicensePro(@RequestBody LicenseVerifyRequestForm request) {
        return ResponseEntity.ok(service.verifyLicensePro(request));
    }

    @PostMapping
    public LicenseDTO create(@RequestBody LicenseDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public LicenseDTO update(@PathVariable Long id, @RequestBody LicenseDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<LicenseDTO>> getByUserId(@PathVariable Long userId) {
//        return ResponseEntity.ok(service.getByUserId(userId));
//    }
}
