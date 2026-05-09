import json
import time
from utils.Fun import Fun
from ultralytics import YOLO
from pathlib import Path
from datetime import datetime

class ImagePredictor:
    def __init__(self, weights_path, input_folder, output_folder="./runs", conf=0.5, data=None):
        """
        初始化ImagePredictor类
        :param weights_path: 权重文件路径
        :param input_folder: 输入图像文件夹路径
        :param output_folder: 结果保存文件夹路径
        :param conf: 置信度阈值
        """
        self.model = YOLO(weights_path)
        self.conf = conf
        self.data = data
        self.fun = Fun()
        self.input_folder = Path(input_folder)
        self.output_folder = Path(output_folder)
        self.output_folder.mkdir(parents=True, exist_ok=True)
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
        # 支持的图片格式
        self.supported_extensions = ('.jpg', '.jpeg', '.png', '.bmp')

    def get_image_files(self):
        """
        获取输入文件夹中所有支持的图片文件
        """
        return [f for f in self.input_folder.glob('*')
                if f.suffix.lower() in self.supported_extensions]

    def predict_batch(self):
        """
        批量预测文件夹中的所有图片并保存结果
        """
        batch_results = []  # 存储所有图片的预测结果

        # 获取所有图片文件
        image_files = self.get_image_files()

        if not image_files:
            print("未找到任何支持的图片文件！")
            return []

        for img_path in image_files:
            start_time = time.time()  # 开始计时
            # 为每张图片生成保存路径
            save_path = self.output_folder / f"result_{img_path.name}"

            # 执行预测，使用 predict 方法并指定 CPU
            results = self.model.predict(
                source=str(img_path),
                conf=self.conf,
                save_conf=True,
                device='cpu',
                verbose=False
            )

            # 初始化单张图片的结果
            img_result = {
                'image_name': img_path.name,
                'label': [],
                'confidence': [],
                'status': 'success',
                'allTime': '0.000秒'
            }

            try:
                # 检查是否有检测结果
                if len(results) == 0:
                    img_result.update({
                        'label': ['预测失败'],
                        'confidence': ['0.00%'],
                        'status': 'failed'
                    })
                else:
                    for result in results:
                        # 提取置信度和标签
                        confidence = result.boxes.conf if hasattr(result.boxes, 'conf') else []
                        label = result.boxes.cls if hasattr(result.boxes, 'cls') else []

                        # 检查 confidence 和 label 是否为空
                        if confidence.numel() == 0 or label.numel() == 0:
                            img_result.update({
                                'label': ['预测失败'],
                                'confidence': ['0.00%'],
                                'status': 'failed'
                            })
                            break

                        # 获取标签名称和对应置信度
                        label_names = [self.labels[int(cls)] for cls in label]
                        predictions = list(zip(label_names, confidence))

                        # 保存预测结果
                        for label, conf in predictions:
                            img_result['label'].append(label)
                            img_result['confidence'].append(f"{conf * 100:.2f}%")

                        result.save(filename=str(save_path))  # 保存结果图片

            except Exception as e:
                img_result.update({
                    'label': ['预测失败'],
                    'confidence': ['0.00%'],
                    'status': 'error',
                    'error_message': str(e)
                })

            end_time = time.time()  # 结束计时
            elapsed_time = end_time - start_time  # 计算单张图片用时
            out_img = self.fun.upload(str(save_path))
            img_result['allTime'] = f"{elapsed_time:.3f}秒"
            img_result['inputImg'] = self.data["imgFolderUrl"] + "/" + img_path.name
            img_result['conf'] = self.conf
            img_result['outImg'] = out_img
            img_result['weight'] = self.data['weight']
            img_result['username'] = self.data['username']
            img_result['ai'] = '不使用Al'
            img_result['suggestion'] = '未选择AI，无AI建议！'
            img_result['startTime'] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            batch_results.append(img_result)

            if img_result['status'] == 'success':
                data = img_result
                data['label'] = json.dumps(data['label'], ensure_ascii=False)
                data['confidence'] = json.dumps(data['confidence'], ensure_ascii=False)
                data = json.dumps(data, ensure_ascii=False)
                self.fun.save_data(data, "http://localhost:9999/imgRecords")

        return batch_results


if __name__ == '__main__':
    # 初始化预测器
    predictor = ImagePredictor(
        weights_path="../weights/best.pt",
        input_folder="../runs/imgBatch",
        output_folder="../runs/resultBatch",
        conf=0.1
    )

    # 执行批量预测
    batch_result = predictor.predict_batch()

    # 打印结果
    # print(json.dumps(batch_result, ensure_ascii=False, indent=2))
    # print(f"处理图片数量: {len(batch_result)}")