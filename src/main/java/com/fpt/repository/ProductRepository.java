//package com.fpt.repository;
//
//import java.util.List;
//import java.util.Set;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//
//import com.fpt.entity.Product;
//
//public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
//
//    Product findByName(String name);
//
//    boolean existsByName(String name);
//
//    void deleteByIdIn(Set<Integer> ids);
//
//    List<Product> findByCategory_Id(Integer id);
//
//    Page<Product> findAllByCategoryId(Integer categoryId, Pageable pageable);
//
//	Page<Product> getListProductByCategoryId(Pageable pageable, Integer id);
//}
