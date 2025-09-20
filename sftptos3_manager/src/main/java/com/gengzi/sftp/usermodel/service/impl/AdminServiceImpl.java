package com.gengzi.sftp.usermodel.service.impl;

import com.gengzi.sftp.usermodel.config.BusinessException;
import com.gengzi.sftp.usermodel.dao.user.entity.Admin;
import com.gengzi.sftp.usermodel.dao.user.repository.AdminRepository;
import com.gengzi.sftp.usermodel.dto.AdminInfoRequest;
import com.gengzi.sftp.usermodel.dto.AdminInfoUpdateRequest;
import com.gengzi.sftp.usermodel.response.ResultCode;
import com.gengzi.sftp.usermodel.security.UserPrincipal;
import com.gengzi.sftp.usermodel.service.AdminService;
import com.gengzi.sftp.usermodel.utils.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class AdminServiceImpl implements AdminService {



    @Autowired
    private AdminRepository adminRepository;


    @Override
    public void createUser(AdminInfoRequest adminInfoRequest) {
        if(adminRepository.existsUserByUsername(adminInfoRequest.getUsername())){
                throw new BusinessException(ResultCode.USER_EXIST);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal details = (UserPrincipal) authentication.getPrincipal();
        Admin user = new Admin();
        user.setUsername(adminInfoRequest.getUsername());
        user.setPasswd(PasswordEncoderUtil.encodePassword(adminInfoRequest.getPasswd()));
        user.setCreater(details.getId());
        user.setUpdater(details.getId());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        adminRepository.save(user);
    }

    @Override
    public Page<Admin> list(String username, Pageable pageable) {
        Specification<Admin> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 条件1：名称模糊匹配（如果name不为空）
            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("username"),  username + "%"));
            }
            // 组合所有条件
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return adminRepository.findAll(spec,pageable);
    }

    @Override
    public void updateUser(AdminInfoUpdateRequest adminInfoUpdateRequest) {
        Optional<Admin> user = adminRepository.findById(adminInfoUpdateRequest.getId());
        if (user.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal details = (UserPrincipal) authentication.getPrincipal();
            Admin updateUser = new Admin();
            updateUser.setPasswd(PasswordEncoderUtil.encodePassword(adminInfoUpdateRequest.getPasswd()));
            updateUser.setUpdater(details.getId());
            updateUser.setUpdateTime(LocalDateTime.now());
            adminRepository.save(updateUser);
        }else{
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

    }

    @Override
    public Boolean removeUser(Long id) {
        Optional<Admin> user = adminRepository.findById(id);
        if (user.isPresent()) {
            if("admin".equals(user.get().getUsername())){
                throw new BusinessException(ResultCode.ADMIN_USER_PROHIBIT_DEL);
            }
            adminRepository.deleteById(id);
            return true;
        } else {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
    }
}
