import json
import time
from ultralytics import YOLO


class ImagePredictor:
    def __init__(self, weights_path, img_path, save_path="./runs/resultBatch.jpg", conf=0.5):
        """
        初始化ImagePredictor类
        :param weights_path: 权重文件路径
        :param img_path: 输入图像路径
        :param save_path: 结果保存路径
        :param conf: 置信度阈值
        """
        # 直接加载模型，不进行设备转换
        self.model = YOLO(weights_path)
        self.conf = conf
        self.img_path = img_path
        self.save_path = save_path
        self.labels =[
            "苹果-黑腐病", "苹果-健康", "苹果-结痂", "甜椒-细菌性斑疹", "甜椒-健康",
            "木薯-褐条病", "木薯-细菌性枯萎病", "木薯-绿斑病", "樱桃-健康", "樱桃-白粉病",
            "玉米-叶斑病", "玉米-普通锈病", "玉米-健康", "葡萄-黑腐病", "葡萄-健康",
            "葡萄-叶枯病", "木薯-健康", "木薯-花叶病毒", "玉米-大斑病", "柑桔-黄龙病",
            "桃子-桃细菌性穿孔病", "桃子-健康", "土豆-早疫病", "土豆-健康", "土豆-晚疫病",
            "水稻-褐斑病", "水稻-健康", "水稻-稻瘟病", "草莓-白粉病", "草莓-健康",
            "草莓-角斑病", "番茄-细菌性斑疹病", "番茄-早疫病", "番茄-晚疫病", "番茄-健康",
            "番茄-叶霉病", "番茄-斑枯病"
        ]
    def predict(self):
        """
        预测图像并保存结果
        """
        start_time = time.time()
        # 使用 ONNX 模型进行推理
        results = self.model.predict(
            source=self.img_path,
            conf=self.conf,
            save_conf=True,
            device='cpu',  # 指定使用 CPU
            verbose=False  # 关闭详细输出
        )
        elapsed_time = time.time() - start_time

        all_results = {
            'labels': [],
            'confidences': [],
            'allTime': f"{elapsed_time:.3f}秒"
        }

        try:
            if len(results) == 0:
                return {
                    'labels': '预测失败',
                    'confidences': "0.00%",
                    'allTime': f"{elapsed_time:.3f}秒"
                }

            for result in results:
                confidences = result.boxes.conf if hasattr(result.boxes, 'conf') else []
                labels = result.boxes.cls if hasattr(result.boxes, 'cls') else []

                if confidences.numel() == 0 or labels.numel() == 0:
                    return {
                        'labels': '预测失败',
                        'confidences': "0.00%",
                        'allTime': f"{elapsed_time:.3f}秒"
                    }

                label_names = [self.labels[int(cls)] for cls in labels]
                predictions = list(zip(label_names, confidences))

                for label, conf in predictions:
                    all_results['labels'].append(label)
                    all_results['confidences'].append(f"{conf * 100:.2f}%")

                result.save(filename=self.save_path)

            return all_results
        except Exception as e:
            print(f"预测过程中发生异常: {e}")
            return {
                'labels': '预测失败',
                'confidences': "0.00%",
                'allTime': f"{elapsed_time:.3f}秒"
            }


if __name__ == '__main__':
    predictor = ImagePredictor(
        weights_path="../weights/helmet_best.pt",
        img_path="../test.jpg",
        save_path="../runs/result.jpg",
        conf=0.1
    )
    result = predictor.predict()
    print(f"标签: {result['labels']}")
    print(f"置信度: {result['confidences']}")
    print(f"用时: {result['allTime']}")
