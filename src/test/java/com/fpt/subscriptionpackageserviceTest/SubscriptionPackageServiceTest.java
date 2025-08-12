package com.fpt.subscriptionpackageserviceTest;

import com.fpt.dto.SubscriptionPackageDTO;
import com.fpt.entity.Option;
import com.fpt.entity.PaymentOrder;
import com.fpt.entity.SubscriptionPackage;
import com.fpt.repository.OptionRepository;
import com.fpt.repository.PaymentOrderRepository;
import com.fpt.repository.SubscriptionPackageRepository;
import com.fpt.service.SubscriptionPackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionPackageServiceTest {

    @Mock
    private SubscriptionPackageRepository repository;
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    @Mock
    private OptionRepository optionRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SubscriptionPackageService service;

    private SubscriptionPackage sp1;
    private SubscriptionPackage sp2;

    @BeforeEach
    void initData() {
        sp1 = new SubscriptionPackage();
        sp1.setId(1L);
        sp1.setName("Package 1");
        sp1.setIsActive(true);
        sp1.setPrice(100f);
        sp1.setDiscount(0f);
        sp1.setBillingCycle(SubscriptionPackage.BillingCycle.MONTHLY);
        sp1.setSimulatedCount(0L);
        sp1.setDescription("Desc 1");
        sp1.setOptions(List.of());

        sp2 = new SubscriptionPackage();
        sp2.setId(2L);
        sp2.setName("Package 2");
        sp2.setIsActive(true);
        sp2.setPrice(200f);
        sp2.setDiscount(10f);
        sp2.setBillingCycle(SubscriptionPackage.BillingCycle.YEARLY);
        sp2.setSimulatedCount(0L);
        sp2.setDescription("Desc 2");
        sp2.setOptions(List.of());
    }

    // ===== getAllPackageCustomer =====

    @Test
    void getAllPackageCustomer_normal() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<SubscriptionPackage> page = new PageImpl<>(List.of(sp1, sp2), pageable, 2);
        when(repository.findAll(any(Specification.class), eq(pageable)));
        when(paymentOrderRepository.countBySubscriptionPackageIdAndPaymentStatus(anyLong(), any())).thenReturn(1L);

        Page<SubscriptionPackageDTO> result = service.getAllPackageCustomer(pageable, null, null, null, null, null);
        assertThat(result).hasSize(2);
    }

    @Test
    void getAllPackageCustomer_boundary_pageSize1() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<SubscriptionPackage> page = new PageImpl<>(List.of(sp1), pageable, 2);
        when(repository.findAll(any(Specification.class), eq(pageable)));
        when(paymentOrderRepository.countBySubscriptionPackageIdAndPaymentStatus(anyLong(), any())).thenReturn(1L);

        Page<SubscriptionPackageDTO> result = service.getAllPackageCustomer(pageable, null, null, null, null, null);
        assertThat(result.getSize()).isEqualTo(1);
    }

    @Test
    void getAllPackageCustomer_abnormal_noResult() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(any(Specification.class), eq(pageable)));
        Page<SubscriptionPackageDTO> result = service.getAllPackageCustomer(pageable, "nothing", null, null, null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void getAllPackageCustomer_externalError() {
        Pageable pageable = PageRequest.of(0, 10);
        when(repository.findAll(any(Specification.class), eq(pageable)));
        assertThatThrownBy(() -> service.getAllPackageCustomer(pageable, null, null, null, null, null))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("DB error");
    }

    // ===== getById =====

    @Test
    void getById_normal() {
        when(repository.findById(1L)).thenReturn(Optional.of(sp1));
        when(paymentOrderRepository.countBySubscriptionPackageIdAndPaymentStatus(anyLong(), any())).thenReturn(5L);
        SubscriptionPackageDTO dto = service.getById(1L);
        assertThat(dto).isNotNull();
    }

    @Test
    void getById_boundary_id0() {
        when(repository.findById(0L)).thenReturn(Optional.of(sp1));
        SubscriptionPackageDTO dto = service.getById(0L);
        assertThat(dto.getId()).isEqualTo(1L);
    }

    @Test
    void getById_abnormal_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subscription not found");
    }

    @Test
    void getById_externalError() {
        when(repository.findById(anyLong())).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.getById(1L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // ===== create =====

    @Test
    void create_normal() {
        SubscriptionPackageDTO dto = SubscriptionPackageDTO.builder()
                .name("New Pkg").price(150f).discount(5f)
                .billingCycle("MONTHLY").typePackage("BASIC")
                .optionsId(List.of(1L)).build();
        Option option = new Option();
        option.setId(1L);
        when(optionRepository.findAllById(any())).thenReturn(List.of(option));
        when(repository.save(any())).thenReturn(sp1);

        SubscriptionPackageDTO result = service.create(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void create_boundary_minFields() {
        SubscriptionPackageDTO dto = SubscriptionPackageDTO.builder()
                .name("NewPkg").price(100f)
                .billingCycle("MONTHLY").typePackage("BASIC").build();
        when(repository.save(any())).thenReturn(sp1);
        SubscriptionPackageDTO result = service.create(dto);
        assertThat(result).isNotNull();
    }

    @Test
    void create_abnormal_optionNotFound() {
        SubscriptionPackageDTO dto = SubscriptionPackageDTO.builder()
                .name("NewPkg").price(100f)
                .billingCycle("MONTHLY").typePackage("BASIC")
                .optionsId(List.of(999L)).build();
        when(optionRepository.findAllById(any())).thenReturn(Collections.emptyList());
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("One or more options not found");
    }

    @Test
    void create_externalError() {
        SubscriptionPackageDTO dto = SubscriptionPackageDTO.builder()
                .name("NewPkg").price(100f)
                .billingCycle("MONTHLY").typePackage("BASIC").build();
        when(repository.save(any())).thenThrow(new RuntimeException("DB error"));
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // ===== update =====

    @Test
    void update_normal() {
        SubscriptionPackageDTO dto = SubscriptionPackageDTO.builder()
                .name("UpdatedPkg").price(200f).discount(10f)
                .billingCycle("YEARLY").isActive(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(sp1));
        when(repository.save(any())).thenReturn(sp1);
        SubscriptionPackageDTO result = service.update(1L, dto);
        assertThat(result).isNotNull();
    }

    @Test
    void update_boundary_minFields() {
        SubscriptionPackageDTO dto = SubscriptionPackageDTO.builder()
                .name("ShortUpdate").price(100f).billingCycle("MONTHLY").build();
        when(repository.findById(1L)).thenReturn(Optional.of(sp1));
        when(repository.save(any())).thenReturn(sp1);
        SubscriptionPackageDTO result = service.update(1L, dto);
        assertThat(result).isNotNull();
    }

    @Test
    void update_abnormal_idNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        SubscriptionPackageDTO dto = SubscriptionPackageDTO.builder().name("X").build();
        assertThatThrownBy(() -> service.update(99L, dto)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subscription not found");
    }

    @Test
    void update_abnormal_optionNotFound() {
        SubscriptionPackageDTO dto = SubscriptionPackageDTO.builder()
                .optionsId(List.of(1L, 999L)).billingCycle("MONTHLY").build();
        when(repository.findById(1L)).thenReturn(Optional.of(sp1));
        //when(optionRepository.findAllById(any())).thenReturn(List.of(new Option(1L, "Opt", true, null, null)));
        assertThatThrownBy(() -> service.update(1L, dto)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Some Option IDs not found");
    }

    // ===== delete =====

    @Test
    void delete_normal() {
        when(repository.findById(1L)).thenReturn(Optional.of(sp1));
        service.delete(1L);
        verify(repository).delete(sp1);
    }

    @Test
    void delete_boundary_id0() {
        when(repository.findById(0L)).thenReturn(Optional.of(sp1));
        service.delete(0L);
        verify(repository).delete(sp1);
    }

    @Test
    void delete_abnormal_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Subscription not found");
    }

    @Test
    void delete_externalError() {
        when(repository.findById(1L)).thenReturn(Optional.of(sp1));
        doThrow(new RuntimeException("DB error")).when(repository).delete(sp1);
        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // ===== getTop3MostUsedPackages =====

    @Test
    void getTop3MostUsedPackages_normal() {
        when(repository.findAll()).thenReturn(List.of(sp1, sp2));
        when(paymentOrderRepository.countBySubscriptionPackageIdAndPaymentStatus(anyLong(), any()))
                .thenReturn(10L);
        List<SubscriptionPackageDTO> result = service.getTop3MostUsedPackages();
        assertThat(result).isNotEmpty();
    }

    @Test
    void getTop3MostUsedPackages_boundary_lessThan3() {
        when(repository.findAll()).thenReturn(List.of(sp1));
        when(paymentOrderRepository.countBySubscriptionPackageIdAndPaymentStatus(anyLong(), any()))
                .thenReturn(0L);
        List<SubscriptionPackageDTO> result = service.getTop3MostUsedPackages();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    void getTop3MostUsedPackages_abnormal_zeroUsage() {
        when(repository.findAll()).thenReturn(List.of(sp1, sp2));
        when(paymentOrderRepository.countBySubscriptionPackageIdAndPaymentStatus(anyLong(), any()))
                .thenReturn(0L);
        List<SubscriptionPackageDTO> result = service.getTop3MostUsedPackages();
        assertThat(result.stream().allMatch(dto -> dto.getTotalCount() == 0L)).isTrue();
    }

    @Test
    void getTop3MostUsedPackages_externalError() {
        when(repository.findAll()).thenReturn(List.of(sp1));
        when(paymentOrderRepository.countBySubscriptionPackageIdAndPaymentStatus(anyLong(), any()))
                .thenThrow(new RuntimeException("Count error"));
        assertThatThrownBy(() -> service.getTop3MostUsedPackages())
                .isInstanceOf(RuntimeException.class).hasMessageContaining("Count error");
    }
}
