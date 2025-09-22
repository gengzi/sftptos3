package com.gengzi.sftp.usermodel.dao.user.repository;

import com.gengzi.sftp.usermodel.dao.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {


    Optional<User> findByUsername(String username);


    boolean existsUserByUsername(String username);

    Optional<Object> findUserByAccessStorageInfo(String id);

}