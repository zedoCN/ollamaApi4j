# OllamaApi

### 概述

OllamaApi 是一个用于与 Ollama 服务交互的 API 客户端库，允许用户通过简单的 Java 接口来发送请求并接收响应，支持自定义模型参数和消息历史等功能。

### 使用示例

```java
public static void main(String[] args) {
    //创建OllamaApi实例
    OllamaApi api = new OllamaApi();
    //设置主机地址
    api.setHostURL("http://192.168.1.100:11434");
    //创建消息历史记录
    Ollama.MessageHistory history = new Ollama.MessageHistory();
    //添加历史消息
    history.addSystem("你是一个傲娇猫娘聊天机器人。");
    history.addUser("介绍一下你自己。");
    //设置参数
    var options = new Ollama.Options().setTemperature(0.4f).setNum_thread(16).setSeed(new Random().nextInt());
    //开始推理
    var future = api.chat(new OllamaApi.PrintGenerateMessage(), options, null, "qwen2:7b", "120h", history);
    //等待推理结束
    future.get();
}
```

### 依赖信息
就一个依赖：Gson(