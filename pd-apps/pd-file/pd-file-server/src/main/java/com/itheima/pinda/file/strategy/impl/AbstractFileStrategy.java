package com.itheima.pinda.file.strategy.impl;

import com.itheima.pinda.exception.BizException;
import com.itheima.pinda.exception.code.ExceptionCode;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.enumeration.IconType;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.FileStrategy;
import com.itheima.pinda.file.utils.FileDataTypeUtil;
import com.itheima.pinda.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽象文件策略处理类
 *
 * @author caoyifei
 * @create 2021/12/16 17:33
 */
@Slf4j
public abstract class AbstractFileStrategy implements FileStrategy {

    private static final String FILE_SPLIT = ".";

    @Autowired
    protected FileServerProperties fileServerProperties;
    /**
     * 子类中具体确定
     */
    protected FileServerProperties.Properties properties;

    @Override
    public File upload(MultipartFile multipartFile) {
        try {
            // 获取上传文件的原始名
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename != null && !originalFilename.contains(FILE_SPLIT)) {
                // 如果文件名不包括".",为非法文件名
                throw BizException.wrap(ExceptionCode.BASE_VALID_PARAM.build("上传文件格式错误"));
            }
            // 封装File对象,保存至数据库
            File file = File.builder()
                    // 文件是否被删除
                    .isDelete(false)
                    // 文件大小
                    .size(multipartFile.getSize())
                    // 文件类型
                    .contextType(multipartFile.getContentType())
                    // 数据类型,由contentType调用工具类获取
                    .dataType(FileDataTypeUtil.getDataType(multipartFile.getContentType()))
                    // 原始文件名称
                    .submittedFileName(originalFilename)
                    // 后缀
                    .ext(FilenameUtils.getExtension(originalFilename))
                    .build();
            // 设置文件图标
            file.setIcon(IconType.getIcon(file.getExt()).getIcon());
            // 设置文件创建时间
            LocalDateTime nowTime = LocalDateTime.now();
            file.setCreateMonth(DateUtils.formatAsYearMonthEn(nowTime));
            file.setCreateWeek(DateUtils.formatAsYearWeekEn(nowTime));
            file.setCreateDay(DateUtils.formatAsDateEn(nowTime));

            uploadFile(file, multipartFile);
            return file;
        } catch (Exception e) {
            log.error("e = {}", e.getMessage());
            throw BizException.wrap(ExceptionCode.BASE_VALID_PARAM.build("文件上传失败"));
        }
    }

    /**
     * 文件上传抽象方法,需要有当前类的子类实现
     *
     * @param file
     */
    public abstract void uploadFile(File file, MultipartFile multipartFile) throws Exception;

    @Override
    public Boolean delete(List<FileDeleteDO> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        // 删除操作是否成功标识
        boolean flag = false;
        for (FileDeleteDO fileDeleteDO : list) {
            try {
                delete(fileDeleteDO);
                flag = true;
            } catch (Exception e) {
                log.error("e = {}", e.getMessage());
            }
        }
        return flag;
    }

    /**
     * 文件删除抽象方法,需要当前类子类实现
     *
     * @param fileDeleteDO
     */
    public abstract void delete(FileDeleteDO fileDeleteDO);


    /**
     * 获取下载地址前缀
     */
    protected String getUriPredix() {
        if (StringUtils.isNotEmpty(properties.getUriPrefix())) {
            return properties.getUriPrefix();
        } else {
            return properties.getEndpoint();
        }
    }

}
