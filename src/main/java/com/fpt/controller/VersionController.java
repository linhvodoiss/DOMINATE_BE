package com.fpt.controller;

import com.fpt.dto.CategoryDTO;
import com.fpt.dto.DocDTO;
import com.fpt.dto.VersionDTO;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.IVersionService;
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

@RestController
@RequestMapping("/api/v1/versions")
@RequiredArgsConstructor
@Validated
public class VersionController {

    private final IVersionService service;
    @GetMapping("/list")
    public ResponseEntity<SuccessResponse<List<VersionDTO>>> getAll() {
        List<VersionDTO> versions = service.getAll();
        return ResponseEntity.ok(
                new SuccessResponse<>(200, "Get all versions successfully!", versions)
        );
    }
    @GetMapping()
    public ResponseEntity<PaginatedResponse<VersionDTO>> getAllVersions(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive

    ) {
        Page<VersionDTO> dtoPage = service.getAllVersion(pageable, search,isActive);
        PaginatedResponse<VersionDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Take list version successfully.");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/customer")
    public ResponseEntity<PaginatedResponse<VersionDTO>> getAllVersionsCustomer(
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String search


    ) {
        Page<VersionDTO> dtoPage = service.getAllVersionCustomer(pageable, search);
        PaginatedResponse<VersionDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Take list version successfully.");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<VersionDTO>> getById(@PathVariable Long id) {
        VersionDTO dto = service.getByIdIfActive(id);
        if (dto == null) {
            return ResponseEntity.status(404).body(new SuccessResponse<>(404, "Version not found or inactive", null));
        }
        return ResponseEntity.ok(new SuccessResponse<>(200, "Get version successfully!", dto));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<VersionDTO>> create(@RequestBody VersionDTO dto) {
        try {
            VersionDTO saved = service.create(dto);
            return ResponseEntity.ok(new SuccessResponse<>(200, "Create successfully!", saved));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new SuccessResponse<>(500, "Create failed!", null));
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<VersionDTO>> update(@PathVariable Long id, @RequestBody VersionDTO dto) {
        try {
            VersionDTO saved = service.update(id, dto);
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
