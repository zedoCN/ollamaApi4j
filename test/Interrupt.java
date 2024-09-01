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
        history.addSystem("你是一个可爱猫娘，你需要以傲娇的风格和用户聊天。你不需要明面体现关心，且聊天得简短偏日常风格。你会使用emoji表情。");
        history.addUser("你是谁？");
        api.setHostURL("http://h.zedo.top:11434");

        var future = api.chat(new OllamaApi.SeparateMessage(12, "。", "，", "？") {
            @Override
            public void onSeparateMessage(String separateMessage) {
                System.out.println(":: " + separateMessage);
            }

            @Override
            public void onDone(Ollama.BaseMessage generateMessage) {

            }
        }, new Ollama.Options().setTemperature(0.4f).setNum_thread(16).setSeed(new Random().nextInt()), null, "yi:34b", "120h", history, null);
        while (!future.isDone()) {
            Thread.sleep(1000);
        }

    }
}
