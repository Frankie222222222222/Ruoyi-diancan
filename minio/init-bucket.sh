#!/usr/bin/env bash
# MinIO 初始化脚本：创建 ruoyi-takeout 桶，并设为 public-read（下载权限）
# 说明：必须在 docker compose up -d 之后，且 MinIO 健康检查通过再执行

set -e

CONTAINER_NAME=ruoyi-minio
ALIAS=local
BUCKET=ruoyi-takeout
ROOT_USER=ruoyi
ROOT_PASSWORD=ruoyi123456

# 等待 MinIO 就绪
echo "[1/4] 等待 MinIO 容器就绪 ..."
for i in $(seq 1 30); do
  if docker exec ${CONTAINER_NAME} curl -sf http://localhost:9000/minio/health/live >/dev/null 2>&1; then
    echo "  OK"
    break
  fi
  sleep 1
  if [ $i -eq 30 ]; then
    echo "  ERROR: MinIO 容器未在 30s 内就绪" >&2
    exit 1
  fi
done

# 配置 mc 别名（mc 已包含在 minio/minio 镜像内）
echo "[2/4] 配置 mc 别名 ..."
docker exec ${CONTAINER_NAME} mc alias set ${ALIAS} http://localhost:9000 ${ROOT_USER} ${ROOT_PASSWORD}

# 创建桶
echo "[3/4] 创建桶 ${BUCKET} ..."
docker exec ${CONTAINER_NAME} mc mb --ignore-existing ${ALIAS}/${BUCKET}

# 设为 public-read（下载权限，前端可通过 URL 直接预览）
echo "[4/4] 设置桶 ${BUCKET} 为 public-read ..."
docker exec ${CONTAINER_NAME} mc anonymous set download ${ALIAS}/${BUCKET}

echo "完成！桶 ${BUCKET} 已就绪并可公开下载。"
echo "控制台： http://localhost:9001  (账号 ruoyi / 密码 ruoyi123456)"
echo "访问图片： http://localhost:9000/${BUCKET}/<object-name>"
