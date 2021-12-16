package com.itheima.pinda.file.storage;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.impl.AbstractFileStrategy;
import com.itheima.pinda.utils.DateUtils;
import com.itheima.pinda.utils.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 本地上传策略配置类
 *
 * @author caoyifei
 * @create 2021/12/16 23:54
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(FileServerProperties.class)
@ConditionalOnProperty(name = "pinda.file.type", havingValue = "LOCAL")
public class LocalAutoConfigure {
    /**
     * 本地文件策略处理类
     */
    @Service
    public static class LocalServiceImpl extends AbstractFileStrategy {

        private void buildClient() {
            properties = fileServerProperties.getLocal();
        }

        /**
         * 文件上传
         *
         * @param file
         * @param multipartFile
         * @return
         */
        @Override
        public File uploadFile(File file, MultipartFile multipartFile) throws Exception {
            buildClient();
            String endpoint = properties.getEndpoint();
            String bucketName = properties.getBucketName();
            String uriPrefix = properties.getUriPrefix();

            // 使用UUID替换文件上传名
            String fileName = UUID.randomUUID() + StrPool.DOT + file.getExt();
            // 获取时间名路径;
            // paths.get()用以适应系统分隔符,并可自动拼接'\'
            String relativePath = Paths.get(LocalDate.now().format(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_MONTH_FORMAT_SLASH))).toString();
            // 上传文件存储的绝对目录
            String absolutePath = Paths.get(endpoint, bucketName, relativePath).toString();
            // 目标输出文件
            java.io.File outFile = new java.io.File(Paths.get(absolutePath, fileName).toString());
            // 向目标文件写入数据
            FileUtils.writeByteArrayToFile(outFile, multipartFile.getBytes());

            // 文件上传完成后设置file属性入库
            file.setUrl();
            file.setFilename()

            return null;
        }

        /**
         * 文件删除
         *
         * @param fileDeleteDO
         */
        @Override
        public void delete(FileDeleteDO fileDeleteDO) {

        }
    }
}
