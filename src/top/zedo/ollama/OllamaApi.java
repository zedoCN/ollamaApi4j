package top.zedo.ollama;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OllamaApi {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final TypeToken<List<Ollama.Model>> listTypeToken = new TypeToken<>() {
    };
    private static final String[] headers = new String[]{"Content-Type", "application/json", "Accept", "application/json"};
    private HttpClient client = HttpClient.newHttpClient();
    private URI hostURL = URI.create("http://localhost:11434");

    /**
     * 设置HttpClient
     *
     * @param client HttpClient
     */
    public void setClient(HttpClient client) {
        this.client = client;
    }

    /**
     * 设置主机地址
     *
     * @param hostURL 主机地址
     */
    public void setHostURL(String hostURL) {
        this.hostURL = URI.create(hostURL);
    }

    /**
     * 发起HTTP POST请求以发送JSON数据，并处理响应
     * 此方法构建一个HTTP POST请求，使用提供的URL将JSON对象作为请求体发送，并异步处理接收到的响应
     *
     * @param url                请求的URL路径，将与基础URL组合以形成完整的请求URL
     * @param requestJson        要发送的JSON数据，作为请求体的一部分
     * @param jsonStreamResponse 一个回调接口，用于处理从响应流中读取的每个JSON元素
     * @return
     */
    private CompletableFuture<Void> httpPostAsync(String url, JsonElement requestJson, Callback<JsonElement> jsonStreamResponse) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(hostURL.resolve(url))
                .headers(headers)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        try (InputStream err = response.body()) {
                            System.err.println("错误请求: " + response.statusCode() + " " + new String(err.readAllBytes()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty())
                                continue;
                            JsonElement json = gson.fromJson(line, JsonElement.class);
                            jsonStreamResponse.call(json);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private JsonElement httpPost(String url, JsonElement requestJson) {
        final JsonElement[] result = new JsonElement[1];
        var response = httpPostAsync(url, requestJson, jsonElement -> result[0] = jsonElement);
        try {
            response.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result[0];
    }

    private JsonElement httpGet(String url) {
        return httpGet(url, JsonElement.class);
    }

    private <T> T httpGet(String url, Class<T> tClass) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(hostURL.resolve(url))
                .headers(headers)
                .GET()
                .build();
        try {
            return gson.fromJson(client.send(request, HttpResponse.BodyHandlers.ofString()).body(), tClass);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成补全
     * 生成给定提示的响应，该响应将使用所提供的模型。
     */
    public CompletableFuture<Void> generate(GenerateMessageCallback callback, Ollama.Options options, String template, String model, String keepAlive, String system, List<Integer> context, String prompt, byte[]... image) {
        JsonObject json = new JsonObject();
        json.addProperty("model", model);
        if (options != null)
            json.add("options", gson.toJsonTree(options));
        json.addProperty("prompt", prompt);
        if (system != null)
            json.addProperty("system", system);
        if (template != null)
            json.addProperty("template", template);
        if (context != null) {
            TypeToken<List<Integer>> listTypeToken = new TypeToken<>() {
            };
            json.add("context", gson.toJsonTree(context, listTypeToken.getType()));
        }
        if (keepAlive != null)
            json.addProperty("keep_alive", keepAlive);
        if (image != null) {
            JsonArray images = new JsonArray();
            for (byte[] img : image) {
                images.add(Base64.getEncoder().encodeToString(img));
            }
            json.add("images", images);
        }

        StringBuilder stringBuilder = new StringBuilder();
        final Ollama.GenerateMessage[] generateMessage = new Ollama.GenerateMessage[1];
        return httpPostAsync("/api/generate", json, jsonElement -> {
            generateMessage[0] = gson.fromJson(jsonElement, Ollama.GenerateMessage.class);
            if (!generateMessage[0].done) {
                stringBuilder.append(generateMessage[0].response);
                callback.onMessage(stringBuilder.toString(), generateMessage[0]);
            } else {
                callback.onDone(stringBuilder.toString(), generateMessage[0]);
            }
        });
    }

    /**
     * 对话补全
     * 使用所提供的模型生成聊天中的下一条消息。
     */
    public CompletableFuture<Void> chat(ChatMessageCallback callback, Ollama.Options options, String template, String model, String keepAlive, Ollama.MessageHistory history, List<Ollama.Tool> tools) {
        JsonObject json = new JsonObject();
        json.addProperty("model", model);
        if (options != null)
            json.add("options", gson.toJsonTree(options));
        if (template != null)
            json.addProperty("template", template);
        if (keepAlive != null)
            json.addProperty("keep_alive", keepAlive);
        TypeToken<List<Ollama.Message>> listTypeToken = new TypeToken<>() {
        };
        if (tools != null) {
            json.add("tools", gson.toJsonTree(tools));
            json.addProperty("stream", false);
        }
        json.add("messages", gson.toJsonTree(history, listTypeToken.getType()));

        StringBuilder stringBuilder = new StringBuilder();
        final Ollama.ChatMessage[] chatMessage = new Ollama.ChatMessage[1];
        return httpPostAsync("/api/chat", json, jsonResponse -> {
            chatMessage[0] = gson.fromJson(jsonResponse, Ollama.ChatMessage.class);
            if (!chatMessage[0].done) {
                stringBuilder.append(chatMessage[0].message.getContent());
                callback.onMessage(stringBuilder.toString(), chatMessage[0]);
            } else {
                history.addAssistant(stringBuilder.toString());
                callback.onDone(stringBuilder.toString(), chatMessage[0]);
            }
        });
    }

    /**
     * 列出本地模型
     * 列出可在本地获取的模型。
     */
    public List<Ollama.Model> getTags() {
        return gson.fromJson(httpGet("/api/tags").getAsJsonObject().get("models"), listTypeToken.getType());
    }

    /**
     * 显示模型信息
     * 显示有关模型的信息，包括详细信息、模型文件、模板、参数、许可证和系统提示。
     */
    public Ollama.ModelShow getModeInfo(String name) {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        return gson.fromJson(httpPost("/api/show", json), Ollama.ModelShow.class);
    }

    private interface Callback<T> {
        void call(T t) throws Exception;
    }

    public interface GenerateMessageCallback {
        /**
         * 生成消息
         *
         * @param message         已生成的消息
         * @param generateMessage 生成消息片段
         */
        void onMessage(String message, Ollama.GenerateMessage generateMessage);


        /**
         * 生成消息完成
         *
         * @param message         完整的消息
         * @param generateMessage 生成消息片段
         */
        void onDone(String message, Ollama.GenerateMessage generateMessage);
    }

    public interface ChatMessageCallback {
        /**
         * 生成消息
         *
         * @param message     已生成的消息
         * @param chatMessage 生成消息片段
         */
        void onMessage(String message, Ollama.ChatMessage chatMessage);

        /**
         * 生成消息完成
         *
         * @param message     完整的消息
         * @param chatMessage 生成消息片段
         */
        void onDone(String message, Ollama.ChatMessage chatMessage);
    }


    /**
     * 分隔消息内容
     */
    public static abstract class SeparateMessage implements ChatMessageCallback, GenerateMessageCallback {
        private final StringBuilder sb = new StringBuilder();
        private final Pattern pattern;
        private final int minLength;

        public SeparateMessage(int minLength, String... delimiters) {
            this.minLength = minLength;
            pattern = Pattern.compile("[" + String.join("", delimiters) + "]");
        }

        @Override
        public final void onMessage(String message, Ollama.ChatMessage chatMessage) {
            sb.append(chatMessage.message.getContent());
            handle();
        }

        private void handle() {
            String text = sb.toString();
            if (text.isEmpty())
                return;
            Matcher matcher = pattern.matcher(text);
            int lastIndex = 0;

            while (matcher.find()) {
                int index = matcher.start() + 1;
                String content = text.substring(0, index);

                // 检查是否满足最小长度要求
                if (content.length() >= minLength) {
                    sb.delete(0, index);
                    onSeparateMessage(content);
                    lastIndex = index; // 更新最后分隔符的位置
                }
            }

            // 如果所有匹配都不满足minLength要求，且最后有剩余的内容，但又没有更长的匹配，更新sb
            if (lastIndex > 0) {
                sb.delete(0, lastIndex);
            }
        }

        @Override
        public final void onDone(String message, Ollama.ChatMessage chatMessage) {
            if (!sb.isEmpty()) {
                onSeparateMessage(sb.toString());
                sb.setLength(0);
            }

            onDone(chatMessage);
        }

        @Override
        public final void onMessage(String message, Ollama.GenerateMessage generateMessage) {
            sb.append(generateMessage.response);

            handle();
        }

        @Override
        public final void onDone(String message, Ollama.GenerateMessage generateMessage) {
            if (!sb.isEmpty()) {
                onSeparateMessage(sb.toString());
                sb.setLength(0);
            }

            onDone(generateMessage);
        }

        public abstract void onSeparateMessage(String separateMessage);

        public abstract void onDone(Ollama.BaseMessage generateMessage);
    }


    /**
     * 打印生成消息
     */
    public static final class PrintGenerateMessage implements GenerateMessageCallback, ChatMessageCallback {

        @Override
        public void onMessage(String message, Ollama.GenerateMessage generateMessage) {
            System.out.print(generateMessage.response);
        }

        @Override
        public void onDone(String message, Ollama.GenerateMessage generateMessage) {
            System.out.println(" ❖");
        }

        @Override
        public void onMessage(String message, Ollama.ChatMessage chatMessage) {
            System.out.print(chatMessage.message.getContent());
            System.out.flush();
        }

        @Override
        public void onDone(String message, Ollama.ChatMessage chatMessage) {
            var toolCalls = chatMessage.message.getToolCalls();
            if (toolCalls != null) {
                for (var tool : toolCalls) {
                    System.out.print("\t[" + tool.function.name + " " + tool.function.arguments + "]\t");
                    System.out.flush();
                }
            }
            System.out.format(" ❖(%.2f token/s)\n", chatMessage.getTokenSpeed());
        }
    }

}
