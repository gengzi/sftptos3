package com.gengzi.sftp.usermodel.service.impl;

import com.gengzi.sftp.usermodel.config.BusinessException;
import com.gengzi.sftp.usermodel.dao.s3.entity.S3Storage;
import com.gengzi.sftp.usermodel.dao.s3.repository.S3StorageRepository;
import com.gengzi.sftp.usermodel.dao.user.entity.User;
import com.gengzi.sftp.usermodel.dao.user.repository.UserRepository;
import com.gengzi.sftp.usermodel.dto.UserInfoRequest;
import com.gengzi.sftp.usermodel.dto.UserInfoUpdateRequest;
import com.gengzi.sftp.usermodel.dto.UserLoginRequest;
import com.gengzi.sftp.usermodel.response.JwtResponse;
import com.gengzi.sftp.usermodel.response.ResultCode;
import com.gengzi.sftp.usermodel.security.JwtTokenProvider;
import com.gengzi.sftp.usermodel.security.UserPrincipal;
import com.gengzi.sftp.usermodel.service.UserService;
import com.gengzi.sftp.usermodel.utils.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3StorageRepository s3StorageRepository;

    @Override
    public JwtResponse longing(UserLoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPasswd()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return new JwtResponse(jwt,
                userPrincipal.getId(),
                userPrincipal.getUsername());
    }

    @Override
    public void createUser(UserInfoRequest userInfoRequest) {
        if (userRepository.existsUserByUsername(userInfoRequest.getUsername())) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal details = (UserPrincipal) authentication.getPrincipal();
        User user = new User();
        user.setUsername(userInfoRequest.getUsername());
        user.setPasswd(PasswordEncoderUtil.encodePassword(userInfoRequest.getPasswd()));
        user.setUserRootPath(userInfoRequest.getUserRootPath());
        user.setAccessStorageType(userInfoRequest.getAccessStorageType());
        user.setAccessStorageInfo(userInfoRequest.getAccessStorageInfo());
        user.setSecretKey(userInfoRequest.getSecretKey());
        user.setCreater(String.valueOf(details.getId()));
        user.setUpdater(String.valueOf(details.getId()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public Page<User> list(String username, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 条件1：名称模糊匹配（如果name不为空）
            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("username"), username + "%"));
            }
            // 组合所有条件
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> userPage = userRepository.findAll(spec, pageable);

        // 2. 提取所有用户的deptId（去重）
        List<String> accessStorageInfo = userPage.getContent().stream()
                .map(User::getAccessStorageInfo)
                .filter(Objects::nonNull)
                .filter(id -> !id.isEmpty())// 过滤null
                .distinct()
                .collect(Collectors.toList());
        List<Long> deptIds = accessStorageInfo.stream().map(id -> Long.parseLong(id)).toList();

        // 3. 批量查询部门ID对应的名称（避免N+1查询）
        Map<Long, String> deptNameMap = s3StorageRepository.findAllById(deptIds).stream()
                .collect(Collectors.toMap(S3Storage::getId, S3Storage::getS3Name));

        // 4. 给每个用户设置部门名称
        userPage.getContent().forEach(user -> {
            user.setAccessStorageInfo(deptNameMap.getOrDefault(
                    user.getAccessStorageInfo() == null || user.getAccessStorageInfo().isEmpty() ? -1L :
                            Long.parseLong(user.getAccessStorageInfo()), "-"));
        });
        return userPage;
    }

    @Override
    public void updateUser(UserInfoUpdateRequest userInfoRequest) {
        Optional<User> user = userRepository.findById(userInfoRequest.getId());
        if (user.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal details = (UserPrincipal) authentication.getPrincipal();
            User updatedUser = user.get();
            updatedUser.setUsername(userInfoRequest.getUsername());
            String passwd = userInfoRequest.getPasswd();
            if(passwd != null && !passwd.isEmpty() && !passwd.equals(updatedUser.getPasswd())){
                updatedUser.setPasswd(PasswordEncoderUtil.encodePassword(passwd));
            }
            updatedUser.setUserRootPath(userInfoRequest.getUserRootPath());
            updatedUser.setAccessStorageType(userInfoRequest.getAccessStorageType());
            updatedUser.setAccessStorageInfo(userInfoRequest.getAccessStorageInfo());
            updatedUser.setSecretKey(userInfoRequest.getSecretKey());
            updatedUser.setUpdater(String.valueOf(details.getId()));
            updatedUser.setUpdateTime(LocalDateTime.now());
            userRepository.save(updatedUser);
        } else {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

    }

    @Override
    public Boolean removeUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
            return true;
        } else {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
    }

    @Override
    public User details(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
    }
}
