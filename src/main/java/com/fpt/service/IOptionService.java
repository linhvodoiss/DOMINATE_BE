package com.fpt.service;

import com.fpt.dto.DocDTO;
import com.fpt.dto.OptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOptionService {
    Page<OptionDTO> getAllOptions(Pageable pageable, String search, Boolean isActive);
    List<OptionDTO> getAll();
    OptionDTO getById(Long id);
    OptionDTO create(OptionDTO dto);
    OptionDTO update(Long id, OptionDTO dto);
    void delete(Long id);
    void deleteMany(List<Long> ids);
}
