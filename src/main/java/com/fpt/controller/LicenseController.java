package com.fpt.controller;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.UserLicenseViewDTO;
import com.fpt.entity.License;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.form.LicenseCreateForm;
import com.fpt.form.LicenseVerifyRequestForm;
import com.fpt.payload.ErrorNoResponse;
import com.fpt.payload.LicenseVerifyResponse;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.repository.LicenseRepository;
import com.fpt.service.ILicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
        Pageable pageable,
        @RequestParam(required = false) String search

) {
    Page<LicenseDTO> dtoPage = service.getAllLicense(pageable, search);
    PaginatedResponse<LicenseDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách các license thành công");
    return ResponseEntity.ok(response);
}

    @GetMapping("user/{userId}")
    public ResponseEntity<PaginatedResponse<LicenseDTO>> getByUserId(Pageable pageable,@PathVariable Long userId, @RequestParam(required = false) String search) {
        Page<LicenseDTO> dtoPage = service.getUserLicense(pageable, search,userId);
        PaginatedResponse<LicenseDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách các license thành công");
        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> bindHardware(@RequestBody Map<String, String> request) {
        String licenseKey = request.get("licenseKey");
        String hardwareId = request.get("hardwareId");

        if (licenseKey == null || hardwareId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "Lost licenseKey or hardwareId"
            ));
        }

        try {
            LicenseDTO dto = service.bindHardwareIdToLicense(licenseKey, hardwareId);
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

    @PostMapping("/activate-next/{userId}")
    public ResponseEntity<?> activateNextLicense(
            @PathVariable Long userId,
            @RequestParam SubscriptionPackage.TypePackage type
    ) {
        try {
            LicenseDTO dto = service.activateNextLicense(userId, type);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Có lỗi xảy ra"));
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
