package com.gengzi.sftp.usermodel.dao.user.repository;

import com.gengzi.sftp.usermodel.dao.user.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> , JpaSpecificationExecutor<Admin> {

    Optional<Admin> findByUsername(String username);

    boolean existsUserByUsername(@NotBlank String username);

}