import top.zedo.ollama.Ollama;
import top.zedo.ollama.OllamaApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Shell {
    private static final OllamaApi api = new OllamaApi();
    private static final Ollama.MessageHistory history = new Ollama.MessageHistory();
    private static final List<Ollama.Tool> tools = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        api.setHostURL("http://h.zedo.top:11434");
        history.addSystem("你是一个能使用Windows PowerShell的人工智能助手。");
        history.addUser("当前目录有哪些文件？");

        tools.add(new Ollama.Tool("exec", "执行PowerShell指令", "object", "command")
                .addProperty("command", "string", "要被执行的指令")
        );
        tools.add(new Ollama.Tool("tell", "给用户发送消息", "object", "message")
                .addProperty("message", "string", "要被发送的消息")
        );


        inference();
    }

    public static void inference() throws ExecutionException, InterruptedException {
        api.chat(new OllamaApi.ChatMessageCallback() {
            @Override
            public void onMessage(String message, Ollama.ChatMessage chatMessage) {

            }

            @Override
            public void onDone(String message, Ollama.ChatMessage chatMessage) {
                for (var call : chatMessage.message.getToolCalls()) {
                    switch (call.function.name) {
                        case "exec" -> {
                            System.out.println(call.function.arguments.get("command"));
                        }
                        case "tell" -> {
                            System.out.println(call.function.arguments);
                        }
                    }
                }
            }
        }, new Ollama.Options(), null, "qwen2.5:7b", "120h", history, tools).get();
    }

}
