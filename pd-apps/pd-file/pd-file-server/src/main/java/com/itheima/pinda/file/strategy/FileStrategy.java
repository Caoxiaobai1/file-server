package com.itheima.pinda.file.strategy;

import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 最高层策略处理接口
 *
 * @author caoyifei
 * @create 2021/12/16 17:22
 */
public interface FileStrategy {

    /**
     * 文件上传
     *
     * @param file file
     * @return
     */
    File upload(MultipartFile file);

    /**
     * 文件删除
     *
     * @param list
     * @return
     */
    Boolean delete(List<FileDeleteDO> list);
}
