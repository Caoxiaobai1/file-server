package com.itheima.pinda.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.pinda.base.id.IdGenerate;
import com.itheima.pinda.database.mybatis.conditions.Wraps;
import com.itheima.pinda.database.mybatis.conditions.query.LbqWrapper;
import com.itheima.pinda.dozer.DozerUtils;
import com.itheima.pinda.file.dao.AttachmentMapper;
import com.itheima.pinda.file.dto.AttachmentDTO;
import com.itheima.pinda.file.entity.Attachment;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.service.AttachmentService;
import com.itheima.pinda.file.strategy.FileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 附件业务实现类
 *
 * @author caoyifei
 * @create 2021/12/17 17:54
 */
@Service
@Slf4j
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements AttachmentService {

    @Resource
    private FileStrategy fileStrategy;
    @Resource
    private IdGenerate<Long> idGenerate;
    @Resource
    private DozerUtils dozerUtils;

    @Override
    public AttachmentDTO upload(MultipartFile file, Long bizId, Long id, String bizType, Boolean isSingle) {
        // 调用策略处理类文件上传
        File uploadFile = fileStrategy.upload(file);
        Attachment attachment = dozerUtils.map(uploadFile, Attachment.class);

        // 判断bizId是否为空,如果为空需要产生一个业务ID
        if (bizId == null) {
            Long bizIdLong = idGenerate.generate();
            attachment.setBizId(String.valueOf(bizIdLong));
        } else {
            attachment.setBizId(String.valueOf(bizId));
        }
        attachment.setBizType(bizType);

        // 判断当前业务下其他的文件信息从数据库删除
        if (isSingle != null && isSingle) {
            // 需要将当前业务下其他的文件信息从数据库中删除
            LbqWrapper<Attachment> eq = Wraps.<Attachment>lbQ().eq(Attachment::getBizId, attachment.getBizId()).eq(Attachment::getBizType, bizType);
            super.remove(eq);
        }
        if (id != null) {
            attachment.setId(id);
            super.updateById(attachment);
        } else {
            attachment.setId(idGenerate.generate());
            super.save(attachment);
        }

        return dozerUtils.map(attachment, AttachmentDTO.class);
    }
}
