import top.zedo.ollama.Ollama;
import top.zedo.ollama.OllamaApi;

import java.util.Random;

public class Interrupt {
    /**
     * 测试中断
     */
    public static void main(String[] args) throws InterruptedException {
        OllamaApi api = new OllamaApi();
        Ollama.MessageHistory history = new Ollama.MessageHistory();
        history.addUser("详细的介绍一下你自己。");
        api.setHostURL("http://192.168.1.100:11434");
        var future = api.chat(new OllamaApi.PrintGenerateMessage(), new Ollama.Options().setTemperature(0.4f).setNum_thread(16).setSeed(new Random().nextInt()), null, "qwen2:7b", "120h", history);
        Thread.sleep(5000);
        future.cancel(true);
        while (!future.isDone()) {
            Thread.sleep(1000);
        }
    }
}
