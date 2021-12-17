package com.itheima.pinda.file.storage;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.impl.AbstractFileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * FastDFS配置
 * @author caoyifei
 */
@EnableConfigurationProperties(FileServerProperties.class)
@Configuration
@Slf4j
@ConditionalOnProperty(name = "pinda.file.type", havingValue = "FAST_DFS")
public class FastDfsAutoConfigure {


    /**
     * FastDFS文件策略处理类
     */
    @Service
    public class FastDfsServiceImpl extends AbstractFileStrategy {

        @Autowired
        private FastFileStorageClient storageClient; //操作FastDFS的客户端
        /**
         * 上传文件
         * @param file
         * @param multipartFile
         * @throws Exception
         */
        @Override
        public void uploadFile(File file, MultipartFile multipartFile)
            throws Exception {
            //调用FastDFS客户端将文件上传到FastDFS
            StorePath storePath = 
                storageClient.uploadFile(multipartFile.getInputStream(), 
                                         multipartFile.getSize(), 
                                         file.getExt(), 
                                         null);
            
            file.setUrl(fileServerProperties.getUriPrefix() +
                        storePath.getFullPath());
            file.setGroup(storePath.getGroup());
            file.setPath(storePath.getPath());
        }

        /**
         * 文件删除
         * @param file
         */
        @Override
        public void delete(FileDeleteDO file) {
            //调用FastDFS客户端删除文件
            storageClient.deleteFile(file.getGroup(), file.getPath());
        }
    }
}