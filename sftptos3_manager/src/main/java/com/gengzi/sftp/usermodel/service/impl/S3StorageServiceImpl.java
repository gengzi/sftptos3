package com.gengzi.sftp.usermodel.service.impl;

import com.gengzi.sftp.usermodel.config.BusinessException;
import com.gengzi.sftp.usermodel.dao.s3.entity.S3Storage;
import com.gengzi.sftp.usermodel.dao.s3.repository.S3StorageRepository;
import com.gengzi.sftp.usermodel.dao.user.repository.UserRepository;
import com.gengzi.sftp.usermodel.dto.S3StorageRequest;
import com.gengzi.sftp.usermodel.dto.S3StorageUpdateRequest;
import com.gengzi.sftp.usermodel.response.ResultCode;
import com.gengzi.sftp.usermodel.response.S3NamesResponse;
import com.gengzi.sftp.usermodel.security.UserPrincipal;
import com.gengzi.sftp.usermodel.service.S3StorageService;
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
public class S3StorageServiceImpl implements S3StorageService {


    @Autowired
    private S3StorageRepository s3StorageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void createS3Storage(S3StorageRequest s3StorageRequest) {
        // 判断名称标识是否已经存在
        if (s3StorageRepository.existsByS3Name(s3StorageRequest.getS3Name())) {
            throw new BusinessException(ResultCode.CONFIG_EXIST.getCode(), "名称标识已经存在");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal details = (UserPrincipal) authentication.getPrincipal();
        S3Storage s3Storage = new S3Storage();
        s3Storage.setBucket(s3StorageRequest.getBucket());
        s3Storage.setEndpoint(s3StorageRequest.getEndpoint());
        s3Storage.setAccessKey(s3StorageRequest.getAccessKey());
        s3Storage.setAccessSecret(s3StorageRequest.getAccessSecret());
        s3Storage.setS3Name(s3StorageRequest.getS3Name());
        s3Storage.setRegion(s3StorageRequest.getRegion());
        s3Storage.setStatus(true);
        s3Storage.setCreator(details.getId());
        s3Storage.setUpdater(details.getId());
        s3Storage.setCreateTime(LocalDateTime.now());
        s3Storage.setUpdateTime(LocalDateTime.now());
        s3StorageRepository.save(s3Storage);
    }

    @Override
    public List<S3NamesResponse> s3names() {
        return s3StorageRepository.findAllS3Name();
    }

    @Override
    public Page<S3Storage> list(String s3Name, Pageable pageable) {
        // 构建动态查询条件
        Specification<S3Storage> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 条件1：名称模糊匹配（如果name不为空）
            if (s3Name != null && !s3Name.isEmpty()) {
                predicates.add(cb.like(root.get("s3Name"), s3Name + "%"));
            }
            // 组合所有条件
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return s3StorageRepository.findAll(spec, pageable);
    }

    @Override
    public void update(S3StorageUpdateRequest s3StorageRequest) {
        Optional<S3Storage> s3StorageById = s3StorageRepository.findById(s3StorageRequest.getId());
        if (s3StorageById.isPresent()) {
            S3Storage s3Storage = s3StorageById.get();
            s3Storage.setId(s3StorageRequest.getId());
            s3Storage.setBucket(s3StorageRequest.getBucket());
            s3Storage.setEndpoint(s3StorageRequest.getEndpoint());
            s3Storage.setAccessKey(s3StorageRequest.getAccessKey());
            s3Storage.setAccessSecret(s3StorageRequest.getAccessSecret());
            s3Storage.setRegion(s3StorageRequest.getRegion());
            s3StorageRepository.save(s3Storage);
        } else {
            throw new BusinessException(ResultCode.CONFIG_NOT_EXIST);
        }

    }

    @Override
    public void remove(Long id) {
        // 判断当前配置是否有用户在使用，在使用不允许删除
        userRepository.findUserByAccessStorageInfo(String.valueOf(id)).ifPresent(user -> {
            throw new BusinessException(ResultCode.CONFIG_IN_USE);
        });
        s3StorageRepository.deleteById(id);
    }
}
