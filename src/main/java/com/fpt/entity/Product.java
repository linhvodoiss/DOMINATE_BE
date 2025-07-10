//package com.fpt.entity;
//
//import java.io.Serializable;
//import java.util.Date;
//import java.util.List;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//import javax.persistence.OneToMany;
//import javax.persistence.Table;
//import javax.persistence.Temporal;
//import javax.persistence.TemporalType;
//
//import org.hibernate.annotations.CreationTimestamp;
//
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "`Product`")
//@Data
//@NoArgsConstructor
//public class Product implements Serializable {
//
//	private static final long serialVersionUID = 1L;
//
//	@Column(name = "id")
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private int id;
//
//	@Column(name = "name", length = 50, nullable = false, unique = true)
//	private String name;
//
//	@Column(name = "number_of_products", nullable = false)
//	private int number_of_products;
//
//	@Column(name = "price", nullable = false)
//	private float price;
//
//	@Column(name = "thumbnailUrl", length = 500)
//	private String thumbnailUrl;
//
//	@Column(name = "description", length = 500)
//	private String description;
//
//	@Column(name = "created_at", updatable = false)
//	@Temporal(TemporalType.TIMESTAMP)
//	@CreationTimestamp
//	private Date created_at;
//
//	@Column(name = "updated_at", updatable = false)
//	@Temporal(TemporalType.TIMESTAMP)
//	@CreationTimestamp
//	private Date updated_at;
//
//	@ManyToOne
//	@JoinColumn(name = "category_id")
//	private Category category;
//
//	@OneToMany(mappedBy = "product")
//	private List<CartItem> cartItems;
//
//}
