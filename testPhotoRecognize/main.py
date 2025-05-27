# fiber_analysis_service.py
from flask import Flask, request, jsonify
from PIL import Image
import io
import base64
import cv2
import numpy as np

app = Flask(__name__)

def analyze_image(image: Image.Image) -> dict:
    # 将 PIL 图片转为 OpenCV 格式
    image_cv = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
    
    # 转为灰度图并检测边缘（模拟识别过程）
    gray = cv2.cvtColor(image_cv, cv2.COLOR_BGR2GRAY)
    edges = cv2.Canny(gray, threshold1=50, threshold2=150)

    # 将边缘图转换成Base64字符串
    # edges是单通道灰度图，需要转换成三通道才方便编码为png/jpg
    edges_color = cv2.cvtColor(edges, cv2.COLOR_GRAY2BGR)
    success, encoded_image = cv2.imencode('.png', edges_color)
    if not success:
        return {"error": "图像编码失败"}
    base64_encoded_edges = base64.b64encode(encoded_image.tobytes()).decode('utf-8')

    # 统计有多少个边缘像素
    edge_count = np.sum(edges > 0)
    result = {
        "edge_pixel_count": int(edge_count),
        "analysis": f"图像边缘像素点数量：{edge_count}",
        "analyzed_image": base64_encoded_edges
    }
    return result


@app.route("/analyze", methods=["POST"])
def analyze():
    data = request.get_json()
    base64_str = data.get("image")
    image_data = base64.b64decode(base64_str)
    image = Image.open(io.BytesIO(image_data)).convert("RGB")

    result = analyze_image(image)
    return jsonify(result)

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
