package com.fpt.controller;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.UserLicenseViewDTO;
import com.fpt.entity.License;
import com.fpt.entity.PaymentOrder;
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
    public ResponseEntity<?> createLicense(@RequestBody LicenseCreateForm form) {
        try {
            LicenseDTO license = service.createLicense(form);
            return ResponseEntity.ok(new SuccessResponse<>(200, "Tạo license thành công", license));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorNoResponse(400, ex.getMessage()));
        }
    }

    @PostMapping("/activate-next/{userId}")
    public ResponseEntity<?> activateNextLicense(@PathVariable Long userId) {
        try {
            LicenseDTO dto = service.activateNextLicense(userId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Có lỗi xảy ra"));
        }
    }



    @PostMapping("/verify")
    public ResponseEntity<LicenseVerifyResponse> verifyLicense(@RequestBody LicenseVerifyRequestForm request) {
        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(request.getLicenseKey());

        if (licenseOpt.isEmpty()) {
            return ResponseEntity.ok(new LicenseVerifyResponse(
                    false, 404, "License không tồn tại", null
            ));
        }

        License license = licenseOpt.get();

        //Haven't active
        if (!Boolean.TRUE.equals(license.getCanUsed())) {
            return ResponseEntity.ok(new LicenseVerifyResponse(
                    false, 401, "License chưa được kích hoạt", null
            ));
        }

        //Wrong device
        if (!license.getHardwareId().equals(request.getHardwareId())) {
            return ResponseEntity.ok(new LicenseVerifyResponse(
                    false, 403, "License không hợp lệ với thiết bị này", null
            ));
        }

        //expire
        LocalDateTime expiredAt = license.getCreatedAt().plusDays(license.getDuration());
        if (expiredAt.isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(new LicenseVerifyResponse(
                    false, 400, "License đã hết hạn", expiredAt
            ));
        }

        //check
        return ResponseEntity.ok(new LicenseVerifyResponse(
                true, 200, "License hợp lệ", expiredAt
        ));
    }
    @PostMapping("/verifyPro")
    public ResponseEntity<LicenseVerifyResponse> verifyLicensePro(@RequestBody LicenseVerifyRequestForm request) {
        Optional<License> licenseOpt = licenseRepository.findByLicenseKey(request.getLicenseKey());

        if (licenseOpt.isEmpty()) {
            return ResponseEntity.ok(new LicenseVerifyResponse(
                    false, 404, "License không tồn tại", null
            ));
        }

        License license = licenseOpt.get();

        //Haven't active
        if (!Boolean.TRUE.equals(license.getCanUsed())) {
            return ResponseEntity.ok(new LicenseVerifyResponse(
                    false, 401, "License chưa được kích hoạt", null
            ));
        }

        //Wrong device
        if (!license.getUser().getId().equals(request.getUserId())) {
            return ResponseEntity.ok(new LicenseVerifyResponse(
                    false, 403, "License không hợp lệ", null
            ));
        }

        //expire
        LocalDateTime expiredAt = license.getCreatedAt().plusDays(license.getDuration());
        if (expiredAt.isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(new LicenseVerifyResponse(
                    false, 400, "License đã hết hạn", expiredAt
            ));
        }

        //check
        return ResponseEntity.ok(new LicenseVerifyResponse(
                true, 200, "License hợp lệ", expiredAt
        ));
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
