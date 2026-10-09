package com.ruoyi.common.utils.file;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.ErrorResponseException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.MinioConfig;
import com.ruoyi.common.exception.file.FileNameLengthLimitExceededException;
import com.ruoyi.common.exception.file.FileSizeLimitExceededException;
import com.ruoyi.common.exception.file.FileUploadException;

/**
 * MinIO 对象存储工具类
 *
 * <p>替换 {@link FileUploadUtils} 的本地磁盘写入，将文件推送到 MinIO 桶。
 * 返回的对象结构与原 {@code CommonController} 约定完全一致，
 * 便于前端 {@code /common/upload} 无感知切换。
 *
 * @author ruoyi
 */
@Component
public class MinioUtils {

    /** 默认大小 50M */
    public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024L;

    /** 默认文件名最大长度 100 */
    public static final int DEFAULT_FILE_NAME_LENGTH = 100;

    /** 允许的扩展名（与 FileUploadUtils 一致，覆盖图片/办公/视频/PDF） */
    private static final String[] DEFAULT_ALLOWED_EXTENSION = MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 上传文件到 MinIO，自动建桶，按日期分目录。
     *
     * @param file 上传文件
     * @return 文件名（桶内 key，例 {@code takeout/dish/20261008/UUID.png}）
     * @throws Exception 任何 IO/校验错误
     */
    public String upload(MultipartFile file) throws Exception {
        // 1. 文件名校验
        String original = file.getOriginalFilename();
        if (original == null || original.length() == 0) {
            throw new FileUploadException("文件名校验失败");
        }
        if (original.length() > DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(DEFAULT_FILE_NAME_LENGTH);
        }
        // 2. 扩展名校验
        assertAllowed(file, DEFAULT_ALLOWED_EXTENSION);
        // 3. 生成桶内 key
        String fileName = extractUploadFilename(file);
        // 4. 确保桶存在
        ensureBucket();
        // 5. 推送对象
        try (InputStream in = file.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(fileName)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();
            minioClient.putObject(args);
        }
        return fileName;
    }

    /**
     * 获取可公开访问的 URL。
     *
     * @param objectName 桶内 key（即 {@link #upload} 返回值）
     * @return 完整可访问 URL
     */
    public String getObjectUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return null;
        }
        String prefix = minioConfig.getUrlPrefix();
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String bucket = minioConfig.getBucket();
        if (!objectName.startsWith("/")) {
            objectName = "/" + objectName;
        }
        return prefix + "/" + bucket + objectName;
    }

    /**
     * 删除对象。
     *
     * @param objectName 桶内 key
     */
    public void delete(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            // 静默删除失败，避免影响主流程
        }
    }

    // ----------------------------------------------------------------
    // 内部工具
    // ----------------------------------------------------------------

    /**
     * 确保桶存在，桶不存在则创建。
     * 注意：若桶已设为 public-read，本方法只保证存在，不再重复授权。
     */
    private void ensureBucket() throws Exception {
        String bucket = minioConfig.getBucket();
        boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        } catch (ErrorResponseException e) {
            found = false;
        }
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    /**
     * 拼接桶内 key：
     * {@code <pathPrefix>/<yyyyMMdd>/<UUID>.<ext>}
     */
    private String extractUploadFilename(MultipartFile file) {
        String original = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(original);
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = minioConfig.getPathPrefix() == null ? "" : minioConfig.getPathPrefix();
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        if (!prefix.isEmpty()) {
            prefix = prefix + "/";
        }
        return prefix + dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }

    /**
     * 校验文件类型是否在白名单内（白名单复制自 {@code FileUploadUtils.assertAllowed}）。
     */
    private void assertAllowed(MultipartFile file, String[] allowedExtension) throws FileUploadException {
        String original = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(original == null ? "" : original);
        if (ext == null || ext.isEmpty()) {
            throw new FileUploadException("文件扩展名不能为空");
        }
        for (String allow : allowedExtension) {
            if (allow.equalsIgnoreCase(ext)) {
                return;
            }
        }
        throw new FileUploadException("[" + original + "] 文件类型不被允许，允许类型：" + String.join(",", allowedExtension));
    }
}
