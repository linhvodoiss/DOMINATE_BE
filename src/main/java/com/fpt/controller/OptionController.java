package com.fpt.controller;

import com.fpt.dto.LicenseDTO;
import com.fpt.dto.OptionDTO;
import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.payload.PaginatedResponse;
import com.fpt.payload.SuccessNoResponse;
import com.fpt.payload.SuccessResponse;
import com.fpt.service.IOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
@Validated
public class OptionController {

    private final IOptionService optionService;
    @GetMapping
    public ResponseEntity<PaginatedResponse<OptionDTO>> getAll(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive
    ) {
        Page<OptionDTO> dtoPage = optionService.getAllOptions(pageable, search,isActive);
        PaginatedResponse<OptionDTO> response = new PaginatedResponse<>(dtoPage, HttpServletResponse.SC_OK, "Lấy danh sách gói đăng ký thành công");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/list")
    public ResponseEntity<SuccessResponse<List<OptionDTO>>> getListAllActiveTrue() {
        List<OptionDTO> list = optionService.getAll();
        return ResponseEntity.ok(new SuccessResponse<>(200, "Get all successfully!", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<OptionDTO>> getById(@PathVariable Long id) {
        OptionDTO dto = optionService.getById(id);
        return ResponseEntity.ok(new SuccessResponse<>(200, "Get by ID successfully!", dto));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<OptionDTO>> create(@RequestBody OptionDTO dto) {
        OptionDTO created = optionService.create(dto);
        return ResponseEntity.ok(new SuccessResponse<>(200, "Create successfully!", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<OptionDTO>> update(@PathVariable Long id, @RequestBody OptionDTO dto) {
        OptionDTO updated = optionService.update(id, dto);
        return ResponseEntity.ok(new SuccessResponse<>(200, "Update successfully!", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessNoResponse> delete(@PathVariable Long id) {
        optionService.delete(id);
        return ResponseEntity.ok(new SuccessNoResponse(200, "Delete successfully!"));
    }

    @DeleteMapping
    public ResponseEntity<SuccessNoResponse> deleteMany(@RequestBody List<Long> ids) {
        optionService.deleteMany(ids);
        return ResponseEntity.ok(new SuccessNoResponse(200, "Delete successfully!"));
    }
}
