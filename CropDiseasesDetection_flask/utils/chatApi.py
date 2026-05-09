from openai import OpenAI
import json
import requests
class ChatAPI:
    def __init__(self, deepseek_api_key,qwen_api_key):
        self.deepseek_client = OpenAI(
            api_key=deepseek_api_key,
            base_url="https://api.deepseek.com"
        )
        self.qwen_headers = {
            "Authorization": f"Bearer {qwen_api_key}",
            "Content-Type": "application/json"
        }
        self.qwen_url = "https://api.siliconflow.cn/v1/chat/completions"
    def deepseek_request(self, messages, model="deepseek-chat", stream=False):
        """DeepSeek API请求方法"""
        response = self.deepseek_client.chat.completions.create(
            model=model,
            messages=messages,
            stream=stream
        )
        return response.choices[0].message.content
    def qwen_request(self, messages, model="Qwen/Qwen2.5-14B-Instruct",
                     max_tokens=512, temperature=0.7):
        """Qwen API请求方法"""
        payload = {
            "model": model,
            "messages": messages,
            "stream": False,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "top_p": 0.7,
            "top_k": 50,
            "frequency_penalty": 0.5,
            "response_format": {"type": "text"}
        }

        try:
            response = requests.post(
                self.qwen_url,
                json=payload,
                headers=self.qwen_headers
            )
            response.raise_for_status()
            data = response.json()
            return data['choices'][0]['message']['content']
        except requests.exceptions.RequestException as e:
            return f"Request Error: {str(e)}"
        except KeyError:
            return "Error parsing API response"

# 使用示例
if __name__ == "__main__":
    # 初始化实例
    chat = ChatAPI(
        deepseek_api_key="sk-ae11edc8f7d442b6843003a7f54a7453",
        qwen_api_key="sk-jutikhhwhnuuqhqegyrrzfvacqurjhuukwgacdqyjnfljudi"
    )

    # 构造通用消息
    messages = [
        {"role": "user", "content": "我用yolo初步检测出脑膜瘤。请生成实质性建议，包括检查项目、手术方案、药物选择等"}
    ]

    # 调用DeepSeek
    print("DeepSeek Response:")
    print(chat.deepseek_request([{"role": "system", "content": "You are a helpful assistant"}] + messages))

    # 调用Qwen
    # print("\nQwen Response:")
    # print(chat.qwen_request(messages))