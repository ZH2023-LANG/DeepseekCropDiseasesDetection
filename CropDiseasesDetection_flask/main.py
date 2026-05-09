import json
import shutil
import cv2
from flask import Flask, Response, request
from flask_socketio import SocketIO, emit
from ultralytics import YOLO
from utils.Fun import Fun
from utils import predictImg, chatApi, predictBatch


# Flask 应用设置
class VideoProcessingApp:
    def __init__(self, host='0.0.0.0', port=5000):
        """初始化 Flask 应用并设置路由"""
        self.app = Flask(__name__)
        self.socketio = SocketIO(self.app, cors_allowed_origins="*")  # 初始化 SocketIO
        self.host = host
        self.port = port
        self.fun = Fun()
        self.setup_routes()
        self.DeepSeek = 'sk-ae11edc8f7d442b6843003a7f54a7453'
        self.Qwen = 'sk-jutikhhwhnuuqhqegyrrzfvacqurjhuukwgacdqyjnfljudi'
        self.data = {}  # 存储接收参数
        self.paths = {
            'download': './runs/video/download.mp4',
            'output': './runs/video/output.mp4',
            'camera_output': "./runs/video/camera_output.avi",
            'video_output': "./runs/video/camera_output.avi"
        }
        self.recording = False  # 标志位，判断是否正在录制视频

    def setup_routes(self):
        """设置所有路由"""
        self.app.add_url_rule('/file_names', 'file_names', self.file_names, methods=['GET'])
        self.app.add_url_rule('/predictImgBatch', 'predictImgBatch', self.predictImgBatch, methods=['POST'])
        self.app.add_url_rule('/predictImg', 'predictImg', self.predictImg, methods=['POST'])
        self.app.add_url_rule('/predictVideo', 'predictVideo', self.predictVideo)
        self.app.add_url_rule('/predictCamera', 'predictCamera', self.predictCamera)
        self.app.add_url_rule('/stopCamera', 'stopCamera', self.stopCamera, methods=['GET'])

        # 添加 WebSocket 事件
        @self.socketio.on('connect')
        def handle_connect():
            print("WebSocket connected!")
            emit('message', {'data': 'Connected to WebSocket server!'})

        @self.socketio.on('disconnect')
        def handle_disconnect():
            print("WebSocket disconnected!")

    def run(self):
        """启动 Flask 应用"""
        self.socketio.run(self.app, host=self.host, port=self.port, allow_unsafe_werkzeug=True)

    def predictImgBatch(self):
        """图片预测接口"""
        data = request.get_json()
        self.data.clear()
        self.data.update({
            "imgFolderUrl": data['imgFolderUrl'], "username": data['username'],
            "weight": data['weight'], "conf": data['conf']
        })
        self.fun.download_folder(self.data["imgFolderUrl"], './runs/imgBatch')

        predictor = predictBatch.ImagePredictor(
            weights_path="./weights/" + self.data["weight"],
            input_folder="./runs/imgBatch",
            output_folder="./runs/resultBatch",
            conf=float(self.data["conf"]),
            data=self.data
        )
        batch_result = predictor.predict_batch()
        print(batch_result)
        shutil.rmtree("./runs/imgBatch")
        shutil.rmtree("./runs/resultBatch")
        data = {"code": 0, "message": "预测成功", "data": batch_result}

        return data

    def file_names(self):
        """模型列表接口"""
        weight_items = [{'value': name, 'label': name} for name in self.fun.get_file_names("./weights")]
        return json.dumps({'weight_items': weight_items})

    def predictImg(self):
        """图片预测接口"""
        data = request.get_json()
        self.data.clear()
        self.data.update({
            "username": data['username'], "weight": data['weight'],
            "conf": data['conf'], "startTime": data['startTime'],
            "inputImg": data['inputImg'], "ai": data['ai']
        })
        predict = predictImg.ImagePredictor(weights_path=f'./weights/{self.data["weight"]}',
                                            img_path=self.data["inputImg"], save_path='./runs/result.jpg',
                                            conf=float(self.data["conf"]))
        # 执行预测
        results = predict.predict()
        uploadedUrl = self.fun.upload('./runs/result.jpg')
        if results['labels'] != '预测失败':
            self.data["status"] = 200
            self.data["message"] = "预测成功"
            self.data["outImg"] = uploadedUrl
            self.data["allTime"] = results['allTime']
            self.data["confidence"] = json.dumps(results['confidences'])
            self.data["label"] = json.dumps(results['labels'])
        else:
            self.data["status"] = 400
            self.data["message"] = "该图片无法识别，请重新上传！"
        if self.data["ai"] == 'DeepSeek' and self.data["status"] == 200:
            self.socketio.emit('message', {'data': '已检测完成，正在生成DeepSeekAI建议！'})
            chat = chatApi.ChatAPI(
                deepseek_api_key=self.DeepSeek,
                qwen_api_key=self.Qwen
            )
            list_input = self.fun.process_list(results['labels'])
            text = ("我使用yolo对农作物病虫害进行检测。接下来我会告诉你检测到了哪些目标。"
                    "请你帮我生成一些实质性的建议，包括为什么会产生这样的疾病、对该作物种植的建议、对该疾病的治疗措施等。只需回答我要的结果。这是我检测到的结果：")
            for i in list_input:
                text += i
                text += "，"
            messages = [
                {"role": "user",
                 "content": text}
            ]
            self.data["suggestion"] = chat.deepseek_request([{"role": "system", "content": "You are a helpful assistant"}] + messages)
        elif self.data["ai"] == 'Qwen' and self.data["status"] == 200:
            self.socketio.emit('message', {'data': '已检测完成，正在生成QwenAI建议！'})
            chat = chatApi.ChatAPI(
                deepseek_api_key=self.DeepSeek,
                qwen_api_key=self.Qwen
            )
            list_input = self.fun.process_list(results['labels'])
            text = ("我使用yolo对农作物病虫害进行检测。接下来我会告诉你检测到了哪些目标。"
                    "请你帮我生成一些实质性的建议，包括为什么会产生这样的疾病、对该作物种植的建议、对该疾病的治疗措施等。只需回答我要的结果。这是我检测到的结果：")
            for i in list_input:
                text += i
                text += "，"
            messages = [
                {"role": "user",
                 "content": text}
            ]
            self.data["suggestion"] = chat.qwen_request(messages)
        else:
            self.data["suggestion"] = '未选择AI，无AI建议！'
        self.fun.cleanup_files(['./' + self.data["inputImg"].split('/')[-1]])
        return json.dumps(self.data, ensure_ascii=False)

    def predictVideo(self):
        """视频流处理接口"""
        self.data.clear()
        self.data.update({
            "username": request.args.get('username'), "weight": request.args.get('weight'),
            "conf": request.args.get('conf'), "startTime": request.args.get('startTime'),
            "inputVideo": request.args.get('inputVideo')
        })
        self.fun.download(self.data["inputVideo"], self.paths['download'])
        cap = cv2.VideoCapture(self.paths['download'])
        if not cap.isOpened():
            raise ValueError("无法打开视频文件")
        fps = int(cap.get(cv2.CAP_PROP_FPS))
        print(fps)

        # 视频写入器
        video_writer = cv2.VideoWriter(
            self.paths['video_output'],
            cv2.VideoWriter_fourcc(*'XVID'),
            fps,
            (640, 480)
        )
        model = YOLO(f'./weights/{self.data["weight"]}')

        def generate():
            try:
                while cap.isOpened():
                    ret, frame = cap.read()
                    if not ret:
                        break
                    frame = cv2.resize(frame, (640, 480))
                    results = model.predict(source=frame, conf=float(self.data['conf']), show=False)
                    processed_frame = results[0].plot()
                    video_writer.write(processed_frame)
                    _, jpeg = cv2.imencode('.jpg', processed_frame)
                    yield b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + jpeg.tobytes() + b'\r\n'
            finally:
                self.fun.cleanup_resources(cap, video_writer)
                self.socketio.emit('message', {'data': '处理完成，正在保存！'})
                for progress in self.fun.convert_avi_to_mp4(self.paths['video_output']):
                    self.socketio.emit('progress', {'data': progress})
                uploadedUrl = self.fun.upload(self.paths['output'])
                self.data["outVideo"] = uploadedUrl
                self.fun.save_data(json.dumps(self.data), 'http://localhost:9999/videoRecords')
                self.fun.cleanup_files([self.paths['download'], self.paths['output'], self.paths['video_output']])

        return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=frame')

    def predictCamera(self):
        """摄像头视频流处理接口"""
        self.data.clear()
        self.data.update({
            "username": request.args.get('username'), "weight": request.args.get('weight'),
            "conf": request.args.get('conf'), "startTime": request.args.get('startTime')
        })
        self.socketio.emit('message', {'data': '正在加载，请稍等！'})
        model = YOLO(f'./weights/{self.data["weight"]}')
        cap = cv2.VideoCapture(0)
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
        video_writer = cv2.VideoWriter(self.paths['camera_output'], cv2.VideoWriter_fourcc(*'XVID'), 20, (640, 480))
        self.recording = True

        def generate():
            try:
                while self.recording:
                    ret, frame = cap.read()
                    if not ret:
                        break
                    results = model.predict(source=frame, imgsz=640, conf=float(self.data['conf']), show=False)
                    processed_frame = results[0].plot()
                    if self.recording and video_writer:
                        video_writer.write(processed_frame)
                    _, jpeg = cv2.imencode('.jpg', processed_frame)
                    yield b'--frame\r\n' b'Content-Type: image/jpeg\r\n\r\n' + jpeg.tobytes() + b'\r\n'
            finally:
                self.fun.cleanup_resources(cap, video_writer)
                self.socketio.emit('message', {'data': '处理完成，正在保存！'})
                for progress in self.fun.convert_avi_to_mp4(self.paths['camera_output']):
                    self.socketio.emit('progress', {'data': progress})
                uploadedUrl = self.fun.upload(self.paths['output'])
                self.data["outVideo"] = uploadedUrl
                self.fun.save_data(json.dumps(self.data), 'http://localhost:9999/cameraRecords')
                self.fun.cleanup_files([self.paths['download'], self.paths['output'], self.paths['camera_output']])

        return Response(generate(), mimetype='multipart/x-mixed-replace; boundary=frame')

    def stopCamera(self):
        """停止摄像头预测"""
        self.recording = False
        return json.dumps({"status": 200, "message": "预测成功", "code": 0})


# 启动应用
if __name__ == '__main__':
    video_app = VideoProcessingApp()
    video_app.run()
