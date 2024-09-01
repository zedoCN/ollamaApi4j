import top.zedo.ollama.Ollama;
import top.zedo.ollama.OllamaApi;

import java.nio.file.Files;
import java.nio.file.Path;

public class Image {
    public static void main(String[] args) throws Exception {
        OllamaApi api = new OllamaApi();
        Ollama.MessageHistory history = new Ollama.MessageHistory();
        history.addUser("这张图片有什么内容？", Files.readAllBytes(Path.of("./res/ctrl.jpg")));
        api.setHostURL("http://192.168.1.100:11434");
        api.chat(new OllamaApi.PrintGenerateMessage(), new Ollama.Options().setNum_thread(16), null, "llava-llama3:latest", null, history,null).get();
    }
}
