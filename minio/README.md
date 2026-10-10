# MinIO 图床服务

本目录是 RuoYi-Vue3 项目图片上传所用的 MinIO 服务配置与初始化脚本。

## 一、目录结构

```
E:\ruoyi-vue3\
├── docker-compose.yml       # Docker Compose 编排文件
└── minio\
    ├── data\                # MinIO 数据持久化目录（自动创建）
    ├── init-bucket.sh       # 初始化脚本：建桶 + 公开访问
    └── README.md             # 本文档
```

## 二、启动步骤

### 1. 启动 MinIO 容器

打开 PowerShell，进入项目根目录：

```powershell
cd E:\ruoyi-vue3
docker compose up -d
docker compose ps
```

等 `ruoyi-minio` 状态为 `Up` 且 `health` 为 `healthy`（约 10-30 秒）。

### 2. 初始化桶（只需执行一次）

```powershell
bash minio/init-bucket.sh
```

或在 Git Bash / WSL 下执行 `bash ./minio/init-bucket.sh`。

脚本会做三件事：
1. 等 MinIO 健康检查通过
2. 创建 `ruoyi-takeout` 桶
3. 设为 `public-read`（下载权限，前端可直接通过 URL 预览图片）

### 3. 访问控制台

打开浏览器：

- **Web 控制台**：[http://localhost:9001](http://localhost:9001)
  - 账号：`ruoyi`
  - 密码：`ruoyi123456`
- **S3 API**：`http://localhost:9000`

## 三、文件 URL 格式

上传到 `ruoyi-takeout` 桶的对象，公开访问 URL 为：

```
http://localhost:9000/ruoyi-takeout/takeout/dish/20261008/abc123.png
```

这条 URL **直接存到数据库**（`takeout_dish.image` 字段），前端 `<img :src="...">` 即可展示。

## 四、停止 / 重启

```powershell
# 停止（数据保留）
docker compose stop

# 启动
docker compose start

# 完全销毁（数据会丢）
docker compose down
```

## 五、关键配置项

| 位置 | 字段 | 当前值 |
|---|---|---|
| `docker-compose.yml` | `MINIO_ROOT_USER` | `ruoyi` |
| `docker-compose.yml` | `MINIO_ROOT_PASSWORD` | `ruoyi123456` |
| `docker-compose.yml` | 端口映射 `9000:9000` | S3 API |
| `docker-compose.yml` | 端口映射 `9001:9001` | Web 控制台 |
| `docker-compose.yml` | 数据卷 `./minio/data:/data` | 宿主机持久化 |
| `RuoYi-Vue\ruoyi-admin\src\main\resources\application.yml` | `minio.endpoint` | `http://127.0.0.1:9000` |
| `application.yml` | `minio.access-key` | `ruoyi` |
| `application.yml` | `minio.secret-key` | `ruoyi123456` |
| `application.yml` | `minio.bucket` | `ruoyi-takeout` |
| `application.yml` | `minio.url-prefix` | `http://127.0.0.1:9000` |
| `application.yml` | `minio.path-prefix` | `takeout` |

> 任何一边修改，另一边必须同步，否则上传/访问会失败。

## 六、常见问题

### 端口被占用

修改 `docker-compose.yml` 里的 ports 映射，例：

```yaml
ports:
  - "9090:9000"   # 改宿主机端口
  - "9091:9001"
```

**记得同步修改 `application.yml` 的 `minio.endpoint` 和 `minio.url-prefix`**。

### 上传后图片 403

桶未设为 public-read。再次执行 `bash minio/init-bucket.sh`，或到控制台手动把桶 Policy 设为 `download`。

### 容器一直重启

检查端口冲突，或执行 `docker logs ruoyi-minio` 看错误日志。

### 浏览器访问图片 URL 显示 SignatureDoesNotMatch

URL 中带了过期签名参数。本项目采用 public-read，**应直接返回无签名 URL**。如果出现这种情况，确认 `minio.url-prefix` 没多余路径，且 `MinioUtils.getObjectUrl` 实现未变。

## 七、生产环境注意

- `MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD` **必须改**为强密码
- 建议使用密钥管理（KMS / Vault）注入，不要写在仓库里
- `public-read` 适用于图片/视频等公开资源；私密文件请改用预签名 URL（`MinioUtils.getPresignedUrl` 预留接口，暂未启用）
