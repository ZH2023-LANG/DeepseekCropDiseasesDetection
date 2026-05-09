import os
import shutil
import subprocess
import zipfile

import cv2
import requests
from typing import List, Generator, Optional


class Fun:
    """多功能工具类，提供文件操作、视频转换和网络请求功能"""

    def __init__(self):
        """初始化类"""
        pass

    def save_data(self, data: str, url: str) -> None:
        """将数据以 JSON 格式上传到服务器

        Args:
            data: 要上传的数据（通常为 JSON 字符串）
            url: 目标服务器的 URL
        """
        headers = {'Content-Type': 'application/json'}
        try:
            response = requests.post(url, data=data, headers=headers, timeout=10)
            response.raise_for_status()  # 检查请求是否成功
            print("记录上传成功！")
        except requests.RequestException as e:
            print(f"记录上传失败: {str(e)}")

    def convert_avi_to_mp4(self, input_path: str, output_path: str = './runs/video/output.mp4') -> Generator[float, None, None]:
        """使用 FFmpeg 将 AVI 文件转换为 MP4，并生成转换进度

        Args:
            input_path: 输入的 AVI 文件路径
            output_path: 输出的 MP4 文件路径，默认为 'output.mp4'

        Yields:
            float: 转换进度百分比 (0-100)
        """
        ffmpeg_command = ["ffmpeg", "-i", input_path, "-vcodec", "libx264", output_path, "-y"]

        # 使用 subprocess 替代 shell=True，提高安全性
        process = subprocess.Popen(ffmpeg_command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        total_duration = self.get_video_duration(input_path)

        for line in process.stderr:
            if "time=" in line:
                try:
                    time_str = line.split("time=")[1].split(" ")[0]
                    h, m, s = map(float, time_str.split(":"))
                    processed_time = h * 3600 + m * 60 + s
                    if total_duration > 0:
                        yield min((processed_time / total_duration) * 100, 100)
                except (ValueError, IndexError) as e:
                    print(f"解析进度时发生错误: {e}")
                    yield 0  # 返回 0 表示解析失败

        process.wait()
        if process.returncode == 0:
            yield 100  # 转换完成
        else:
            print(f"FFmpeg 转换失败，返回码: {process.returncode}")

    def get_video_duration(self, path: str) -> float:
        """获取视频文件的总时长（秒）

        Args:
            path: 视频文件路径

        Returns:
            float: 视频时长（秒），如果失败则返回 0
        """
        try:
            cap = cv2.VideoCapture(path)
            if not cap.isOpened():
                print(f"无法打开视频文件: {path}")
                return 0
            total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
            fps = cap.get(cv2.CAP_PROP_FPS)
            cap.release()
            return total_frames / fps if fps > 0 else 0
        except Exception as e:
            print(f"获取视频时长失败: {e}")
            return 0

    def get_file_names(self, directory: str) -> List[str]:
        """获取指定文件夹中的所有文件名

        Args:
            directory: 文件夹路径

        Returns:
            List[str]: 文件名列表，失败时返回空列表
        """
        try:
            return [f for f in os.listdir(directory) if os.path.isfile(os.path.join(directory, f))]
        except (FileNotFoundError, PermissionError) as e:
            print(f"读取目录失败: {e}")
            return []

    def upload(self, file_path: str, upload_url: str = "http://localhost:9999/files/upload") -> Optional[dict]:
        """将文件上传到远程服务器

        Args:
            file_path: 本地文件路径
            upload_url: 服务器上传地址，默认为 'http://localhost:9999/files/upload'

        Returns:
            Optional[dict]: 成功时返回服务器响应数据，失败时返回 None
        """
        print(file_path)
        try:
            with open(file_path, 'rb') as file:
                files = {'file': (os.path.basename(file_path), file)}
                response = requests.post(upload_url, files=files, timeout=30)
                response.raise_for_status()
                print("文件上传成功！")
                return response.json().get('data')
        except (FileNotFoundError, requests.RequestException) as e:
            print(f"文件上传失败: {str(e)}")
            return None

    def download(self, url: str, save_path: str) -> bool:
        """从指定 URL 下载文件并保存到本地

        Args:
            url: 文件的下载链接
            save_path: 本地保存路径

        Returns:
            bool: 下载是否成功
        """
        os.makedirs(os.path.dirname(save_path), exist_ok=True)
        try:
            with requests.get(url, stream=True, timeout=10) as response:
                response.raise_for_status()
                with open(save_path, 'wb') as file:
                    for chunk in response.iter_content(chunk_size=8192):
                        if chunk:
                            file.write(chunk)
            print(f"文件已成功下载并保存到 {save_path}")
            return True
        except requests.RequestException as e:
            print(f"下载失败: {e}")
            return False

    def cleanup_files(self, file_paths: List[str]) -> None:
        """删除指定的文件列表

        Args:
            file_paths: 要删除的文件路径列表
        """
        for path in file_paths:
            try:
                if os.path.exists(path):
                    os.remove(path)
                    print(f"已删除文件: {path}")
            except OSError as e:
                print(f"删除文件 {path} 失败: {e}")

    def cleanup_resources(self, cap: cv2.VideoCapture, video_writer: Optional[cv2.VideoWriter]) -> None:
        """释放视频捕获和写入资源

        Args:
            cap: OpenCV 视频捕获对象
            video_writer: OpenCV 视频写入对象（可能为 None）
        """
        try:
            if cap and cap.isOpened():
                cap.release()
            if video_writer is not None:
                video_writer.release()
            cv2.destroyAllWindows()
            print("资源已成功释放")
        except Exception as e:
            print(f"释放资源时发生错误: {e}")

    def download_folder(self, url, dest_folder):
        """
        下载压缩文件并解压到指定文件夹，所有文件展平到 dest_folder，不保留子文件夹结构

        参数:
        url (str): 文件的下载链接
        dest_folder (str): 目标解压文件夹路径
        """
        try:
            # 确保目标文件夹存在
            if not os.path.exists(dest_folder):
                os.makedirs(dest_folder)

            # 下载文件
            response = requests.get(url, stream=True)
            response.raise_for_status()  # 检查请求是否成功

            # 获取文件名（从URL中提取或使用默认名）
            filename = url.split('/')[-1] or 'downloaded_file.zip'
            temp_path = os.path.join(dest_folder, filename)

            # 保存下载的文件
            with open(temp_path, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)

            # 临时解压文件夹
            temp_extract_path = os.path.join(dest_folder, 'temp_extract')
            if os.path.exists(temp_extract_path):
                shutil.rmtree(temp_extract_path)  # 清空临时文件夹
            os.makedirs(temp_extract_path)

            # 解压文件到临时文件夹
            if zipfile.is_zipfile(temp_path):
                with zipfile.ZipFile(temp_path, 'r') as zip_ref:
                    zip_ref.extractall(temp_extract_path)
            else:
                raise ValueError("下载的文件不是有效的zip文件")

            # 展平文件夹结构：将所有文件移动到 dest_folder
            for root, _, files in os.walk(temp_extract_path):
                for file in files:
                    src_path = os.path.join(root, file)
                    dest_path = os.path.join(dest_folder, file)

                    # 如果文件名冲突，添加数字后缀
                    base, ext = os.path.splitext(file)
                    counter = 1
                    while os.path.exists(dest_path):
                        new_filename = f"{base}_{counter}{ext}"
                        dest_path = os.path.join(dest_folder, new_filename)
                        counter += 1

                    shutil.move(src_path, dest_path)

            # 删除临时文件和文件夹
            shutil.rmtree(temp_extract_path)
            os.remove(temp_path)

            print(f"文件已成功下载并解压到 {dest_folder}")

        except requests.exceptions.RequestException as e:
            print(f"下载失败: {e}")
        except zipfile.BadZipFile as e:
            print(f"解压失败: {e}")
        except Exception as e:
            print(f"发生错误: {e}")

    def process_list(self, input_list):
        # 去除重复元素并保持原顺序
        unique_list = []
        seen = set()
        for item in input_list:
            if item not in seen:
                seen.add(item)
                unique_list.append(item)

        # 判断是否需要删除'正常'
        if '正常' in unique_list and len(unique_list) > 1:
            unique_list = [item for item in unique_list if item != '正常']

        return unique_list


if __name__ == "__main__":
    # 示例用法
    fun = Fun()
    files = fun.get_file_names("./")
    print("当前目录文件:", files)