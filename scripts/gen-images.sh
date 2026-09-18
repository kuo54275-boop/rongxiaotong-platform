#!/bin/bash
# 生成种子数据用到的 SVG 占位图（绿色农业主题，不依赖外网图片）
# 运行方式：在项目根目录执行 bash scripts/gen-images.sh
set -e
DIR="D:/Idea/workspace/rongxiaotong-platform/backend/src/main/resources/static/images"
mkdir -p "$DIR"

# 轮播图/新闻图 800x450
banner() { # $1=文件名 $2=渐变色1 $3=渐变色2 $4=标题
cat > "$DIR/$1.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="800" height="450" viewBox="0 0 800 450">
<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="$2"/><stop offset="100%" stop-color="$3"/></linearGradient></defs>
<rect width="800" height="450" fill="url(#g)"/>
<text x="400" y="220" font-size="42" fill="#ffffff" text-anchor="middle" font-family="Microsoft YaHei, sans-serif" font-weight="bold">$4</text>
<text x="400" y="272" font-size="20" fill="#ffffff" opacity="0.85" text-anchor="middle" font-family="Microsoft YaHei, sans-serif">融小通 · 助力乡村振兴</text>
</svg>
EOF
}

# 商品图/文章封面 400x300
card() { # $1=文件名 $2=渐变色1 $3=渐变色2 $4=标题
cat > "$DIR/$1.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300" viewBox="0 0 400 300">
<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="$2"/><stop offset="100%" stop-color="$3"/></linearGradient></defs>
<rect width="400" height="300" fill="url(#g)"/>
<text x="200" y="158" font-size="34" fill="#ffffff" text-anchor="middle" font-family="Microsoft YaHei, sans-serif" font-weight="bold">$4</text>
</svg>
EOF
}

# 头像 200x200
avatar() { # $1=文件名 $2=背景色 $3=文字
cat > "$DIR/$1.svg" <<EOF
<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200">
<circle cx="100" cy="100" r="100" fill="$2"/>
<text x="100" y="128" font-size="76" fill="#ffffff" text-anchor="middle" font-family="Microsoft YaHei, sans-serif" font-weight="bold">$3</text>
</svg>
EOF
}

banner news1 "#67C23A" "#409E11" "融小通平台正式上线"
banner news2 "#F39C12" "#E67E22" "秋季小麦播种指南"
banner news3 "#409EFF" "#1E6FFF" "普惠金融进乡村"

card article1 "#2E7D32" "#66BB6A" "小麦越冬期管理"
card article2 "#8BC34A" "#558B2F" "玉米病虫害防控"
card article3 "#26A69A" "#00897B" "大棚温湿度管理"
card article4 "#7E57C2" "#5E35B1" "蛋鸡养殖对策"

card p-wheat "#C8A75A" "#A08030" "优质冬小麦"
card p-flour "#E8C08A" "#C89B5A" "石磨面粉"
card p-corn "#E6C34C" "#C79A28" "黄玉米"
card p-sweetcorn "#F0D060" "#D0A830" "甜糯玉米"
card p-peanut "#C9A67B" "#9A7848" "鲜花生"
card p-egg "#F5E0C0" "#D9B98A" "散养土鸡蛋"
card p-garlic "#C9B8D8" "#9A84B8" "紫皮大蒜"
card p-honey "#E8A93C" "#C07E18" "农家土蜂蜜"

avatar avatar-farmer "#4CAF50" "农"
avatar avatar-buyer "#FF9800" "买"
avatar avatar-expert "#2196F3" "专"
avatar avatar-bank "#F44336" "银"
avatar avatar-admin "#607D8B" "管"

echo "已生成 $(ls "$DIR" | wc -l) 张 SVG 图片到 $DIR"
