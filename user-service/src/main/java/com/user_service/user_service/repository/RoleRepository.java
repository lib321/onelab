package com.user_service.user_service.repository;

import com.user_service.user_service.enumu.RoleName;
import com.user_service.user_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);
}
