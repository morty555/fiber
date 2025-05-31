import os
import re
import cv2
import numpy as np
import pandas as pd
from skimage.morphology import skeletonize
from PIL import Image, ImageDraw, ImageFont
import pytesseract
import json
from flask import Flask, request, jsonify
import io
import base64
import numpy as np
from typing import Dict

app = Flask(__name__)

# ---------- 配置区 ----------
input_folder  = "./input_images"
output_folder = "./output_results"
os.makedirs(output_folder, exist_ok=True)

# 过滤参数
MIN_AREA_PX      = 500
MIN_LENGTH_PX    = 100
MIN_ASPECT_RATIO = 5.0
font_path = "simhei.ttf"  # 中文字体文件

# ---------- OCR + 刻度提取（含去小竖线） ----------
def recognize_scale_and_unit(image):
    h, w = image.shape[:2]
    roi = image[int(h*0.85):h, 0:int(w*0.3)]
    gray = cv2.cvtColor(roi, cv2.COLOR_BGR2GRAY)
    if np.mean(gray) > 128:
        gray = cv2.bitwise_not(gray)
    _, bw = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)

    # 去小竖线
    cnts, _ = cv2.findContours(bw.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    Hroi, Wroi = bw.shape
    for cnt in cnts:
        x, y, cw, ch = cv2.boundingRect(cnt)
        if cw <= 5 and 0.1*Hroi <= ch <= 0.5*Hroi and (ch/(cw+1e-5)) > 3:
            cv2.drawContours(bw, [cnt], -1, 0, -1)

    text = pytesseract.image_to_string(bw, config="--psm 6", lang="eng").strip()
    m = re.search(r"([\d\.]+)", text)
    if not m:
        return None, None, None
    val = float(m.group(1))
    unit = text.lower().replace("\n","").replace("\r","")
    unit = "μm" if ("μm" in unit or "um" in unit) else unit

    contours, _ = cv2.findContours(bw, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    max_w = 0
    for cnt in contours:
        _, _, ww, hh = cv2.boundingRect(cnt)
        if hh > 0 and ww/hh > 5 and ww > max_w:
            max_w = ww
    if max_w == 0:
        return val, unit, None
    return val, unit, val / max_w

# ---------- 预处理 & 分割 ----------
def preprocess_image(image):
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    gray = cv2.medianBlur(gray, 3)
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8,8))
    return clahe.apply(gray)

def background_subtraction_gaussian(gray, sigma=60):
    background = cv2.GaussianBlur(gray, (0,0), sigmaX=sigma, sigmaY=sigma)
    norm = (gray.astype(np.float32) + 1) / (background.astype(np.float32) + 1)
    norm = cv2.normalize(norm, None, 0, 255, cv2.NORM_MINMAX)
    return norm.astype(np.uint8)

def binarize_image(gray):
    _, bw = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)
    if np.mean(bw==255) > 0.5:
        bw = cv2.bitwise_not(bw)
    h, w = bw.shape
    bw[int(h*0.88):h, 0:int(w*0.12)] = 0
    return bw

def remove_red_scale_bar(image, bin_img):
    hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
    mask1 = cv2.inRange(hsv, (0,100,100), (10,255,255))
    mask2 = cv2.inRange(hsv, (160,100,100), (179,255,255))
    red_mask = cv2.bitwise_or(mask1, mask2)
    bin_img[red_mask>0] = 0
    return bin_img

def split_crossing_fibers(bin_img):
    ske = skeletonize(bin_img//255).astype(np.uint8)
    h, w = bin_img.shape
    dt = cv2.distanceTransform(bin_img, cv2.DIST_L2, 5)
    neigh = [(-1,-1),(-1,0),(-1,1),(0,-1),(0,1),(1,-1),(1,0),(1,1)]
    branch_pts = []
    ys, xs = np.nonzero(ske)
    for y, x in zip(ys, xs):
        count = sum(1 for dy,dx in neigh if 0<=y+dy<h and 0<=x+dx<w and ske[y+dy,x+dx])
        if count >= 4:
            branch_pts.append((y, x))
    for y, x in branch_pts:
        r = int(dt[y, x]) + 2
        y0, y1 = max(0, y-r), min(h, y+r+1)
        x0, x1 = max(0, x-r), min(w, x+r+1)
        bin_img[y0:y1, x0:x1] = 0
    return bin_img

# ---------- 骨架长度计算函数 ----------
def compute_skeleton_length(mask):
    ske = skeletonize(mask).astype(np.uint8)
    pts = set(zip(*np.nonzero(ske)))
    length = 0.0
    for y, x in pts:
        if (y, x+1) in pts:
            length += 1
        if (y+1, x) in pts:
            length += 1
        if (y+1, x+1) in pts or (y+1, x-1) in pts:
            length += np.sqrt(2)
    return length

# ---------- 分析 & 输出 ----------
def analyze_fibers(bin_img, scale_val, scale_unit, upp):
    num_labels, labels = cv2.connectedComponents(bin_img)
    stats = []
    neigh = [(-1,-1),(-1,0),(-1,1),(0,-1),(0,1),(1,-1),(1,0),(1,1)]
    for lab in range(1, num_labels):
        mask = (labels==lab).astype(np.uint8)
        area = cv2.countNonZero(mask)
        if area < MIN_AREA_PX:
            continue
        L = compute_skeleton_length(mask)
        if L < MIN_LENGTH_PX:
            continue
        dist = cv2.distanceTransform(mask*255, cv2.DIST_L2, 5)
        max_w = 2 * dist.max()
        ske = skeletonize(mask).astype(np.uint8)
        pts_s = set(zip(*np.nonzero(ske)))
        endpts = [(y,x) for y,x in pts_s if sum(1 for dy,dx in neigh if (y+dy,x+dx) in pts_s)==1]
        core_vals = [dist[y,x] for y,x in pts_s if (y,x) not in endpts]
        min_w = 2*min(core_vals) if core_vals else 2*dist[mask>0].min()
        avg_w = area / L if L>0 else 0
        if avg_w>0 and (L/avg_w) < MIN_ASPECT_RATIO:
            continue
        coords = np.column_stack(np.nonzero(mask)).astype(np.float32)
        vx, vy, _, _ = cv2.fitLine(coords, cv2.DIST_L2,0,0.01,0.01).flatten()
        angle = np.degrees(np.arctan2(vy, vx))
        if angle < 0:
            angle += 180
        if angle > 90:
            angle = 180 - angle
        branch = any(sum(1 for dy,dx in neigh if (y+dy,x+dx) in pts_s)>2 for y,x in pts_s)
        L_u   = L*upp if upp else None
        max_u = max_w*upp if upp else None
        min_u = min_w*upp if upp else None
        avg_u = avg_w*upp if upp else None
        stats.append({
            "编号": lab,
            "长度(px)": round(L,1),
            f"长度({scale_unit})": round(L_u,2) if L_u else None,
            "最大宽(px)": round(max_w,1),
            f"最大宽({scale_unit})": round(max_u,2) if max_u else None,
            "最小宽(px)": round(min_w,1),
            f"最小宽({scale_unit})": round(min_u,2) if min_u else None,
            "平均宽(px)": round(avg_w,1),
            f"平均宽({scale_unit})": round(avg_u,2) if avg_u else None,
            "长宽比": round(L/avg_w,2) if avg_w>0 else None,
            "分支": "是" if branch else "否",
            "角度(°)": round(angle,1)
        })
    return stats, labels

def visualize_and_save(orig, stats, labels, scale_unit, upp, filename):
    base, _ = os.path.splitext(filename)
    # 1) 二值图
    gray = preprocess_image(orig)
    gray = background_subtraction_gaussian(gray)
    bw   = binarize_image(gray)
    cv2.imwrite(os.path.join(output_folder, f"{base}_binary.png"), bw)
    # 2) 骨架图
    ske = skeletonize(bw//255).astype(np.uint8)*255
    color = cv2.cvtColor(gray, cv2.COLOR_GRAY2BGR)
    color[ske==255] = (0,0,255)
    cv2.imwrite(os.path.join(output_folder, f"{base}_skeleton.png"), color)
    # 3) 编号图
    labeled = orig.copy()
    for row in stats:
        M = cv2.moments((labels==row["编号"]).astype(np.uint8))
        if M["m00"]>0:
            cx = int(M["m10"]/M["m00"])
            cy = int(M["m01"]/M["m00"])
            cv2.putText(labeled, str(row["编号"]), (cx,cy),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0,0,255), 2)
    cv2.imwrite(os.path.join(output_folder, f"{base}_labeled.png"), labeled)
    # 4) 注释图
    pil = Image.fromarray(cv2.cvtColor(orig, cv2.COLOR_BGR2RGB))
    draw = ImageDraw.Draw(pil)
    try:
        font = ImageFont.truetype(font_path, 20)
    except:
        font = ImageFont.load_default()
    for row in stats:
        txt = f"{row['编号']}: 长{row[f'长度({scale_unit})']}{scale_unit} 宽{row[f'平均宽({scale_unit})']}{scale_unit} 角{row['角度(°)']}°"
        M = cv2.moments((labels==row["编号"]).astype(np.uint8))
        if M["m00"]>0:
            cx = int(M["m10"]/M["m00"])
            cy = int(M["m01"]/M["m00"])
            draw.text((cx, cy), txt, font=font, fill=(255,0,0))
    annotated = cv2.cvtColor(np.array(pil), cv2.COLOR_RGB2BGR)
    cv2.imwrite(os.path.join(output_folder, f"{base}_annotated.png"), annotated)
    # 5) 对比图
    combo = np.hstack((orig, annotated))
    cv2.imwrite(os.path.join(output_folder, f"{base}_result.png"), combo)
    # 6) 导出 CSV
    df = pd.DataFrame(stats)
    csv_path = os.path.join(output_folder, f"{base}_data.csv")
    df.to_csv(csv_path, index=False, encoding="utf-8-sig")
    # 7) 导出 JSON 文件
    records = df.to_dict(orient='records')
    json_path = os.path.join(output_folder, f"{base}_data.json")
    # with open(json_path, 'w', encoding='utf-8') as f:
    #     json.dump(records, f, ensure_ascii=False, indent=2)
    print(f"  📄 已导出 CSV: {csv_path}")
    print(f"  📄 已导出 JSON: {json_path}")

def visualize_and_return(orig, stats, labels, scale_unit, upp):
    pil = Image.fromarray(cv2.cvtColor(orig, cv2.COLOR_BGR2RGB))
    draw = ImageDraw.Draw(pil)
    try:
        font = ImageFont.truetype(font_path, 20)
    except:
        font = ImageFont.load_default()

    for row in stats:
        txt = f"{row['编号']}: 长{row.get(f'长度({scale_unit})', '?')}{scale_unit} 宽{row.get(f'平均宽({scale_unit})', '?')}{scale_unit} 角{row['角度(°)']}°"
        M = cv2.moments((labels == row["编号"]).astype(np.uint8))
        if M["m00"] > 0:
            cx = int(M["m10"] / M["m00"])
            cy = int(M["m01"] / M["m00"])
            draw.text((cx, cy), txt, font=font, fill=(255, 0, 0))

    return cv2.cvtColor(np.array(pil), cv2.COLOR_RGB2BGR)


def analyze_image(image: Image.Image) -> Dict:
    # 转换 PIL.Image → OpenCV 图像
    img = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)

    val, unit, upp = recognize_scale_and_unit(img)
    if upp is None:
        return {"success": False, "message": "无法识别比例尺"}

    print(f"✔ 检测到比例尺：{val}{unit} → {upp:.4f}{unit}/px")

    gray = preprocess_image(img)
    gray = background_subtraction_gaussian(gray)
    bw = binarize_image(gray)
    bw = remove_red_scale_bar(img, bw)

    clean = cv2.morphologyEx(bw, cv2.MORPH_OPEN,
                             cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5)))

    num_labels, _ = cv2.connectedComponents(clean)
    if num_labels - 1 <= 1:
        bw2 = clean.copy()
    else:
        bw2 = split_crossing_fibers(clean.copy())

    stats, labels = analyze_fibers(bw2, val, unit, upp)

    # 可选：生成可视化图像
    vis_image = visualize_and_return(img, stats, labels, unit, upp)

    # 将图像转为 base64 字符串返回
    _, buffer = cv2.imencode(".png", vis_image)
    encoded_img = base64.b64encode(buffer).decode("utf-8")

    return {
        "success": True,
        "stats": stats,
        "unit": unit,
        "upp": upp,
        "image_base64": encoded_img
    }

@app.route('/analyze', methods=['POST'])
def analyze_endpoint():
    file = request.files['image']
    image = Image.open(file.stream).convert("RGB")  # 转为 PIL Image

    result = analyze_image(image)

    # 转换 numpy 类型 → Python 原生类型，避免 jsonify 报错
    def convert_numpy(obj):
        if isinstance(obj, dict):
            return {k: convert_numpy(v) for k, v in obj.items()}
        elif isinstance(obj, list):
            return [convert_numpy(i) for i in obj]
        elif isinstance(obj, (np.integer, np.int32, np.int64)):
            return int(obj)
        elif isinstance(obj, (np.floating, np.float32, np.float64)):
            return float(obj)
        else:
            return obj

    return jsonify(convert_numpy(result))

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)