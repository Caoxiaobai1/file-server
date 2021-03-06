package com.itheima.pinda.file.storage;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.impl.AbstractFileStrategy;
import com.itheima.pinda.utils.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.itheima.pinda.utils.DateUtils.DEFAULT_MONTH_FORMAT_SLASH;

/**
 * 阿里云OSS配置
 *
 * @author caoyifei
 */
@EnableConfigurationProperties(FileServerProperties.class)
@Configuration
@Slf4j
@ConditionalOnProperty(name = "pinda.file.type", havingValue = "ALI")
public class AliOssAutoConfigure {
    /**
     * 阿里云OSS文件策略处理类
     */
    @Service
    public class AliServiceImpl extends AbstractFileStrategy {
        /**
         * 构建阿里云OSS客户端
         *
         * @return OSS
         */
        private OSS buildClient() {
            properties = fileServerProperties.getAli();
            return new OSSClientBuilder().
                    build(properties.getEndpoint(),
                            properties.getAccessKeyId(),
                            properties.getAccessKeySecret());
        }

        protected String getUriPrefix() {
            if (StringUtils.isNotEmpty(properties.getUriPrefix())) {
                return properties.getUriPrefix();
            } else {
                String prefix = properties.
                        getEndpoint().
                        contains("https://") ? "https://" : "http://";
                return prefix + properties.getBucketName() + "." +
                        properties.getEndpoint().replaceFirst(prefix, "");
            }
        }

        /**
         * 上传文件
         *
         * @param file file
         * @param multipartFile multipartFile
         * @throws Exception e
         */
        @Override
        public void uploadFile(File file, MultipartFile multipartFile)
                throws Exception {
            OSS client = buildClient();
            //获得OSS空间名称
            String bucketName = properties.getBucketName();
            if (!client.doesBucketExist(bucketName)) {
                //创建存储空间
                client.createBucket(bucketName);
            }

            //生成文件名
            String fileName = UUID.randomUUID().toString() +
                    StrPool.DOT +
                    file.getExt();
            //日期文件夹，例如：2020\04
            String relativePath =
                    Paths.get(LocalDate.now().
                                    format(DateTimeFormatter.
                                            ofPattern(DEFAULT_MONTH_FORMAT_SLASH))).
                            toString();
            // web服务器存放的相对路径
            String relativeFileName = relativePath + StrPool.SLASH + fileName;
            relativeFileName = StrUtil.replace(relativeFileName, "\\\\",
                    StrPool.SLASH);
            relativeFileName = StrUtil.replace(relativeFileName, "\\",
                    StrPool.SLASH);
            //对象元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentDisposition("attachment;fileName=" +
                    file.getSubmittedFileName());
            metadata.setContentType(file.getContextType());

            //上传请求对象
            PutObjectRequest request =
                    new PutObjectRequest(bucketName, relativeFileName,
                            multipartFile.getInputStream(),
                            metadata);
            //上传文件到阿里云OSS空间
            PutObjectResult result = client.putObject(request);

            log.info("result={}", JSONObject.toJSONString(result));

            String url = getUriPrefix() + StrPool.SLASH + relativeFileName;
            url = StrUtil.replace(url, "\\\\", StrPool.SLASH);
            url = StrUtil.replace(url, "\\", StrPool.SLASH);
            // 写入文件表
            file.setUrl(url);
            file.setFilename(fileName);
            file.setRelativePath(relativePath);

            file.setGroup(result.getETag());
            file.setPath(result.getRequestId());

            //关闭阿里云OSS客户端
            client.shutdown();
        }

        /**
         * 文件删除
         *
         * @param file file
         */
        @Override
        public void delete(FileDeleteDO file) {
            OSS client = buildClient();
            //获得OSS空间名称
            String bucketName = properties.getBucketName();
            // 删除文件
            client.deleteObject(bucketName, file.getRelativePath() +
                    StrPool.SLASH + file.getFileName());
            //关闭阿里云OSS客户端
            client.shutdown();
        }
    }
}