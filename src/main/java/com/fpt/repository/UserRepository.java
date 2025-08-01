package com.fpt.repository;

import com.fpt.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpt.entity.User;
import com.fpt.entity.UserStatus;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	public boolean existsByUserName(String userName);

	public boolean existsByEmail(String email);
	public boolean existsByPhoneNumber(String phoneNumber);
	@Query("	SELECT 	status 		"
			+ "	FROM 	User 		"
			+ " WHERE 	email = :email")
	public UserStatus findStatusByEmail(@Param("email") String email);

	public User findByUserName(String name);

	@Query("SELECT u FROM User u WHERE u.userName = :username")
	Optional<User> findByUserName2(@Param("username") String username);


	public User findByEmail(String email);

	public Long countByRole(Role role);

}
