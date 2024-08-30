package com.mycompany.user.repository;

import com.mycompany.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission,Long> {
    Permission findByUrl(String urlName);
    Boolean existsByUrl(String urlName);
}
