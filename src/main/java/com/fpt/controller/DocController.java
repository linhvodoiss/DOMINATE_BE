package com.fpt.controller;

import com.fpt.dto.DocDTO;
import com.fpt.dto.PaymentOrderDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.IDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/docs")
@RequiredArgsConstructor
@Validated
public class DocController {

    private final IDocService service;

//    @GetMapping
//    public List<DocDTO> getAll() {
//        return service.getAll();
//    }
@GetMapping()
public ResponseEntity<PaginatedResponse<DocDTO>> getAllDocs(
        @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean isActive,
          @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long versionId

) {
    Page<DocDTO> dtoPage = service.getAllDoc(pageable, search,isActive,categoryId,versionId);
    PaginatedResponse<DocDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Take list doc successfully.");
    return ResponseEntity.ok(response);
}
//    @GetMapping("/customer")
//    public ResponseEntity<PaginatedResponse<DocDTO>> getAllDocsCustomer(
//            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false) Long categoryId,
//            @RequestParam(required = false) Long versionId
//
//    ) {
//        Page<DocDTO> dtoPage = service.getAllDocCustomer(pageable, search,categoryId,versionId);
//        PaginatedResponse<DocDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Take list doc successfully.");
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/customer")
    public ResponseEntity<?> getDocsForCustomer() {
        List<Map<String, Object>> docs = service.getDocsByAllVersions();
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "Get docs successfully!",
                "data", docs
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<DocDTO>> getById(@PathVariable Long id) {
        DocDTO dto = service.getByIdIfActive(id);
        if (dto == null) {
            return ResponseEntity.status(404).body(new SuccessResponse<>(404, "Doc not found or inactive", null));
        }
        return ResponseEntity.ok(new SuccessResponse<>(200, "Get doc successfully!", dto));
    }


    @PostMapping
    public ResponseEntity<SuccessResponse<DocDTO>> create(@RequestBody DocDTO dto) {
        try {
            DocDTO saved = service.create(dto);
            return ResponseEntity.ok(new SuccessResponse<>(200, "Create successfully!", saved));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessResponse<>(500, "Create failed!", null));
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<DocDTO>> update(@PathVariable Long id, @RequestBody DocDTO dto) {
        try {
            DocDTO saved = service.update(id, dto);
            return ResponseEntity.ok(new SuccessResponse<>(200, "Update successfully!", saved));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessResponse<>(500, "Update failed!", null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessNoResponse> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(new SuccessNoResponse(200, "Delete successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessNoResponse(500, "Delete failed!"));
        }
    }

    @DeleteMapping
    public ResponseEntity<SuccessNoResponse> deleteMore(@RequestBody List<Long> ids) {
        try {
            service.deleteMore(ids);
            return ResponseEntity.ok(new SuccessNoResponse(200, "Delete successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessNoResponse(500, "Delete failed!"));
        }
    }
}
