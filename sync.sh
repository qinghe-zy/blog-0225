#!/bin/bash
echo "开始提交代码..."
git add .
read -p "请输入提交说明 (直接回车默认 'update'): " msg
if [ -z "$msg" ]; then
    msg="update"
fi
git commit -m "$msg"
git push
echo "搞定！已推送到 GitHub"