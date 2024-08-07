import top.zedo.ollama.Ollama;
import top.zedo.ollama.OllamaApi;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Chat {
    private static final OllamaApi api = new OllamaApi();
    private static final Ollama.MessageHistory history = new Ollama.MessageHistory();
    private static final Scanner scanner = new Scanner(System.in);
    private static String modelName = "qwen2:7b";

    /**
     * 测试聊天
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        api.setHostURL("http://192.168.1.100:11434");
        while (true) {
            System.out.print("User: ");
            String line = scanner.nextLine();
            if (line.startsWith("/")) {
                String[] command = line.toLowerCase().split(" ");
                switch (command[0].substring(1)) {
                    case "list", "l" -> {
                        for (var m : api.getTags()) {
                            System.out.println(m);
                        }
                    }
                    case "clear", "c" -> {
                        history.clear();
                    }
                    case "setModel", "sm" -> {
                        modelName = command[1];
                    }
                    case "addSystem", "as" -> {
                        history.addSystem(line);
                    }
                    case "help", "?" -> {
                        System.out.print("/clear //清除\n/setModel [模型名] //设置模型\n/addSystem [系统提示词] //增加系统提示词\n/list //列出模型列表\n/help //帮助列表\n");
                    }
                    case "history", "h" -> {
                        System.out.println(history);
                    }
                    default -> {
                        System.out.println("未知指令。");
                    }
                }
            } else {
                history.addUser(line);
                System.out.print("AI: ");
                //推理并等待
                api.chat(new OllamaApi.PrintGenerateMessage(), new Ollama.Options().setTemperature(0.4f).setNum_thread(16).setSeed(new Random().nextInt()), null, modelName, "120h", history).get();
            }
        }
    }
}
