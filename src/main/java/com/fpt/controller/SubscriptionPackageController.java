package com.fpt.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.dto.UserListDTO;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.entity.User;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.ISubscriptionPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Validated
public class SubscriptionPackageController {

    private final ISubscriptionPackageService service;

    @GetMapping
    public List<SubscriptionPackageDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/list")
    public ResponseEntity<PaginatedResponse<SubscriptionPackageDTO>> getAllPackages(
             Pageable pageable,
            @RequestParam(required = false) String search,
             @RequestParam(required = false) Boolean isActive,
                 @RequestParam(required = false) Double minPrice,
             @RequestParam(required = false) Double maxPrice,
             @RequestParam(required = false) SubscriptionPackage.TypePackage type,
             @RequestParam(required = false) SubscriptionPackage.BillingCycle cycle
    ) {
        Page<SubscriptionPackageDTO> dtoPage = service.getAllPackage(pageable, search,isActive,minPrice,maxPrice,type,cycle);
        PaginatedResponse<SubscriptionPackageDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách gói đăng ký thành công");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/list")
    public ResponseEntity<PaginatedResponse<SubscriptionPackageDTO>> getAllPackagesCustomer(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
              @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) SubscriptionPackage.TypePackage type,
            @RequestParam(required = false) SubscriptionPackage.BillingCycle cycle

    ) {
        Page<SubscriptionPackageDTO> dtoPage = service.getAllPackageCustomer(pageable, search,minPrice,maxPrice,type,cycle);
        PaginatedResponse<SubscriptionPackageDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách gói đăng ký thành công");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<SubscriptionPackageDTO>> getById(@PathVariable Long id) {
        SubscriptionPackageDTO dto = service.getById(id);
        SuccessResponse<SubscriptionPackageDTO> response = new SuccessResponse<>(
                HttpServletResponse.SC_OK,
                "Lấy gói đăng ký thành công",
                dto
        );
        return ResponseEntity.ok(response);
    }

//    @PostMapping
//    public SubscriptionPackageDTO create(@RequestBody SubscriptionPackageDTO dto) {
//        return service.create(dto);
//    }

    @PutMapping("/{id}")
    public SubscriptionPackageDTO update(@PathVariable Long id, @RequestBody SubscriptionPackageDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @DeleteMapping()
    public ResponseEntity<?> deleteMore(@RequestBody List<Long> ids) {
        try {
            service.deleteMore(ids);
            return ResponseEntity.ok(Map.of("code", 200, "message", "Delete packages successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code", 500, "message", "Delete packages failed!"));
        }
    }

}
