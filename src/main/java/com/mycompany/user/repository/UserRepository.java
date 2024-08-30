package com.mycompany.user.repository;

import com.mycompany.user.dto.request.UserPageRequest;
import com.mycompany.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long>, JpaRepository<User, Long> {
    Long countById(int id);

    @Query("SELECT u FROM User u WHERE " +
            "(:#{#request.id} IS NULL OR u.id = :#{#request.id}) " +
            "AND (:#{#request.firstName} IS NULL OR lower(u.firstName) LIKE concat('%', lower(:#{#request.firstName}), '%')) " +
            "AND (:#{#request.lastName} IS NULL OR lower(u.lastName) LIKE concat('%', lower(:#{#request.lastName}), '%')) " +
            "AND (:#{#request.email} IS NULL OR lower(u.email) LIKE concat('%', lower(:#{#request.email}), '%'))")
    Page<User> findByCriteria(UserPageRequest request, Pageable pageable);

    List<User> findAll();
    boolean existsByEmail(String email);

    Optional <User> findByEmail(String email);

    @Query("SELECT r.name FROM User u JOIN u.roles r WHERE u.id = :userId")
    Set<String> findRoleNamesByUserId(@Param("userId") Long userId);

    Optional <User> findById(Long Id);

}
