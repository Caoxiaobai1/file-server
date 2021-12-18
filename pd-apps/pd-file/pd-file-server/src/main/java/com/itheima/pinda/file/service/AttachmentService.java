package com.itheima.pinda.file.service;

import com.itheima.pinda.file.dto.AttachmentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author caoyifei
 * @create 2021/12/17 17:49
 */
public interface AttachmentService {
    AttachmentDTO upload(MultipartFile file, Long bizId, Long id, String bizType, Boolean isSingle);

    void remove(List<Long> ids);
}
