//package com.fpt.controller;
//
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
//
//import java.util.List;
//import java.util.Set;
//
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import com.fpt.dto.ProductDTO;
//import com.fpt.dto.filter.ProductFilter;
//import com.fpt.entity.Product;
//import com.fpt.form.ProductFormForCreating;
//import com.fpt.form.ProductFormForUpdating;
//import com.fpt.service.IProductService;
//
//@RestController
//@RequestMapping(value = "api/v1/products")
//public class ProductController {
//
//    @Autowired
//    private IProductService service;
//
//    @Autowired
//    private ModelMapper modelMapper;
//
//    @GetMapping()
//    public Page<ProductDTO> getAllProducts(Pageable pageable, ProductFilter filter, @RequestParam(required = false) String search) {
//        Page<Product> entityPages = service.getAllProducts(pageable, filter, search);
//
//        // convert entities --> dtos
//        List<ProductDTO> dtos = service.convertToDto(entityPages.getContent());
//        return new PageImpl<>(dtos, pageable, entityPages.getTotalElements());
//    }
//
//    @GetMapping(value = "/list")
//    public Page<ProductDTO> getListProduct(
//            @PageableDefault Pageable pageable,
//            @RequestParam(required = false) Integer categoryId) {
//
//        Page<Product> entityPages = service.getListProduct(pageable, categoryId);
//
//        // convert entities --> dtos
//        List<ProductDTO> dtos = service.convertToDto(entityPages.getContent());
//
//        Page<ProductDTO> dtoPages = new PageImpl<>(dtos, pageable, entityPages.getTotalElements());
//
//        return dtoPages;
//    }
//
//    @GetMapping(value = "/name/{name}")
//    public boolean existsProductByName(@PathVariable(name = "name") String name) {
//        return service.isProductExistsByName(name);
//    }
//
//    @PostMapping()
//    public void createProduct(@RequestBody ProductFormForCreating form) {
//        service.createProduct(form);
//    }
//
//    @GetMapping(value = "/{id}")
//    public ProductDTO getProductByID(@PathVariable(name = "id") int id) {
//        Product entity = service.getProductByID(id);
//
//        // convert entity to dto
//        ProductDTO dto = modelMapper.map(entity, ProductDTO.class);
//        if (entity.getCategory() != null) {
//            dto.setCategory_id(entity.getCategory().getId());
//            dto.setCategory_name(entity.getCategory().getName());
//        }
//        dto.add(linkTo(methodOn(ProductController.class).getProductByID(id)).withSelfRel());
//
//        return dto;
//    }
//
//    @PutMapping(value = "/update/{id}")
//    public void updateProduct(@PathVariable(name = "id") int id, @RequestBody ProductFormForUpdating form) {
//        form.setId(id);
//        service.updateProduct(form);
//    }
//
//    @DeleteMapping(value = "/{ids}")
//    public void deleteProducts(@PathVariable(name = "ids") Set<Integer> ids) {
//        service.deleteProducts(ids);
//    }
//}
