package com.gengzi.sftp.usermodel.service;

import com.gengzi.sftp.usermodel.dao.user.entity.User;
import com.gengzi.sftp.usermodel.dao.user.repository.UserRepository;
import com.gengzi.sftp.usermodel.dto.UserInfoRequest;
import com.gengzi.sftp.usermodel.dto.UserLoginRequest;
import com.gengzi.sftp.usermodel.response.JwtResponse;
import com.gengzi.sftp.usermodel.security.JwtTokenProvider;
import com.gengzi.sftp.usermodel.security.UserPrincipal;
import com.gengzi.sftp.usermodel.utils.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;


    @Autowired
    private UserRepository userRepository;

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal details = (UserPrincipal) authentication.getPrincipal();
        User user = new User();
        user.setUsername(userInfoRequest.getUsername());
        user.setPasswd(PasswordEncoderUtil.encodePassword(userInfoRequest.getPasswd()));
        user.setUserRootPath(userInfoRequest.getUserRootPath());
        user.setAccessStorageType(userInfoRequest.getAccessStorageType());
        user.setAccessStorageInfo(userInfoRequest.getAccessStorageInfo());
        user.setCreater(String.valueOf(details.getId()));
        user.setUpdater(String.valueOf(details.getId()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }
}
