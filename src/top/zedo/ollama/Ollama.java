package top.zedo.ollama;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Ollama {

    public static class BaseMessage {
        /**
         * 模型名称
         */
        public String model;
        /**
         *创建于时间
         */
        public String created_at;
        /**
         * 生成响应的时间
         */
        public long total_duration;
        /**
         * 加载模型的时间（纳秒）
         */
        public long load_duration;
        /**
         * 评估提示的时间（纳秒）
         */
        public long prompt_eval_duration;
        /**
         * 响应中的令牌数量
         */
        public long eval_count;
        /**
         * 生成响应所花费的时间（纳秒）
         */
        public long eval_duration;
        public boolean done;

        /**
         * 每秒生成的令牌速度（token/s）
         */
        public float getTokenSpeed() {
            return (float) eval_count / eval_duration * 1e9f;
        }
    }

    public static class GenerateMessage extends BaseMessage {
        /**
         * 如果响应是流式的，则为空；如果不是流式的，则会包含完整响应
         */
        public String response;
        public String done_reason;
        /**
         * 此响应中使用的会话的编码，这可以在下一个请求中发送以保持会话记忆
         */
        public List<Integer> context;
    }

    public static class ChatMessage extends BaseMessage {
        public Message message;
        /**
         * 提示中的令牌数量
         */
        public int prompt_eval_count;
    }

    public static class Options {
        /**
         * 启用 Mirostat 采样以控制复杂度。（默认：0，0=禁用，1=Mirostat，2=Mirostat 2.0）
         */
        public int mirostat = 0;
        /**
         * 影响算法响应生成文本反馈的速度。较低的学习率将导致调整速度较慢，而较高的学习率将使算法更具响应性。（默认：0.1）
         */
        public float mirostat_eta = 0.1f;
        /**
         * 控制输出的一致性与多样性之间的平衡。较低的值将导致文本更加集中和一致。（默认：5.0）
         */
        public float mirostat_tau = 5.f;
        /**
         * 设置用于生成下一个令牌的上下文窗口大小。（默认：2048）
         */
        public int num_ctx = 2048;
        /**
         * 设置模型向后查看的距离，以防止重复。（默认：64，0=禁用，-1=num_ctx）
         */
        public int repeat_last_n = 64;
        /**
         * 设置对重复的惩罚强度。较高的值（例如 1.5）将更强烈地惩罚重复，而较低的值（例如 0.9）将更宽容。（默认：1.1）
         */
        public float repeat_penalty = 1.1f;
        /**
         * 模型的温度。增加温度将使模型回答更具创造性。（默认：0.8）
         */
        public float temperature = 0.8f;
        /**
         * 设置用于生成的随机数种子。将此设置为特定数字将使模型对同一提示生成相同的文本。（默认：0）
         */
        public int seed = 0;
        /**
         * 设置用于停止的序列。当遇到此模式时，ollama.LLM 将停止生成文本并返回。可以通过在 modelfile 中指定多个单独的 stop 参数来设置多个停止模式。
         */
        public String stop = null;
        /**
         * 尾部自由采样用于减少输出中不太可能的令牌的影响。较高的值（例如 2.0）将更多地减少影响，而值为 1.0 时禁用此设置。（默认：1）
         */
        public float tfs_z = 1f;
        /**
         * 生成文本时预测的最大令牌数。（默认：128，-1=无限生成，-2=填充上下文）
         */
        public int num_predict = 128;
        /**
         * 降低生成无意义文本的概率。较高的值（例如 100）将提供更多样的回答，而较低的值（例如 10）将更为保守。（默认：40）
         */
        public int top_k = 40;
        /**
         * 与 top-k 一起工作。较高的值（例如 0.95）将导致文本更多样化，而较低的值（例如 0.5）将生成更集中和保守的文本。（默认：0.9）
         */
        public float top_p = 0.9f;


        /**
         * 使用的线程数量
         */
        public int num_thread = 16;
        /**
         * 使用的gpu数量
         */
        public int num_gpu = 1;
        /**
         * 主要的gpu
         */
        public int main_gpu = 0;
        /**
         * 低显存
         */
        public boolean low_vram = false;

        /**
         * 设置低显存
         */
        public Options setLow_vram(boolean low_vram) {
            this.low_vram = low_vram;
            return this;
        }

        /**
         * 使用的gpu数量
         */
        public Options setNum_gpu(int num_gpu) {
            this.num_gpu = num_gpu;
            return this;
        }

        /**
         * 主要的gpu
         */
        public Options setMain_gpu(int main_gpu) {
            this.main_gpu = main_gpu;
            return this;
        }

        /**
         * 使用的线程数量
         */
        public Options setNum_thread(int num_thread) {
            this.num_thread = num_thread;
            return this;
        }

        /**
         * 启用 Mirostat 采样以控制复杂度。（默认：0，0=禁用，1=Mirostat，2=Mirostat 2.0）
         */
        public Options setMirostat(int mirostat) {
            this.mirostat = mirostat;
            return this;
        }

        /**
         * 影响算法响应生成文本反馈的速度。较低的学习率将导致调整速度较慢，而较高的学习率将使算法更具响应性。（默认：0.1）
         */
        public Options setMirostat_eta(float mirostat_eta) {
            this.mirostat_eta = mirostat_eta;
            return this;
        }

        /**
         * 控制输出的一致性与多样性之间的平衡。较低的值将导致文本更加集中和一致。（默认：5.0）
         */
        public Options setMirostat_tau(float mirostat_tau) {
            this.mirostat_tau = mirostat_tau;
            return this;
        }

        /**
         * 设置用于生成下一个令牌的上下文窗口大小。（默认：2048）
         */
        public Options setNum_ctx(int num_ctx) {
            this.num_ctx = num_ctx;
            return this;
        }

        /**
         * 设置模型向后查看的距离，以防止重复。（默认：64，0=禁用，-1=num_ctx）
         */
        public Options setRepeat_last_n(int repeat_last_n) {
            this.repeat_last_n = repeat_last_n;
            return this;
        }

        /**
         * 设置对重复的惩罚强度。较高的值（例如 1.5）将更强烈地惩罚重复，而较低的值（例如 0.9）将更宽容。（默认：1.1）
         */
        public Options setRepeat_penalty(float repeat_penalty) {
            this.repeat_penalty = repeat_penalty;
            return this;
        }

        /**
         * 模型的温度。增加温度将使模型回答更具创造性。（默认：0.8）
         */
        public Options setTemperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * 设置用于生成的随机数种子。将此设置为特定数字将使模型对同一提示生成相同的文本。（默认：0）
         */
        public Options setSeed(int seed) {
            this.seed = seed;
            return this;
        }

        /**
         * 设置用于停止的序列。当遇到此模式时，ollama.LLM 将停止生成文本并返回。可以通过在 modelfile 中指定多个单独的 stop 参数来设置多个停止模式。
         */
        public Options setStop(String stop) {
            this.stop = stop;
            return this;
        }

        /**
         * 尾部自由采样用于减少输出中不太可能的令牌的影响。较高的值（例如 2.0）将更多地减少影响，而值为 1.0 时禁用此设置。（默认：1）
         */
        public Options setTfs_z(float tfs_z) {
            this.tfs_z = tfs_z;
            return this;
        }

        /**
         * 生成文本时预测的最大令牌数。（默认：128，-1=无限生成，-2=填充上下文）
         */
        public Options setNum_predict(int num_predict) {
            this.num_predict = num_predict;
            return this;
        }

        /**
         * 降低生成无意义文本的概率。较高的值（例如 100）将提供更多样的回答，而较低的值（例如 10）将更为保守。（默认：40）
         */
        public Options setTop_k(int top_k) {
            this.top_k = top_k;
            return this;
        }

        /**
         * 与 top-k 一起工作。较高的值（例如 0.95）将导致文本更多样化，而较低的值（例如 0.5）将生成更集中和保守的文本。（默认：0.9）
         */
        public Options setTop_p(float top_p) {
            this.top_p = top_p;
            return this;
        }
    }


    public static class MessageHistory extends ArrayList<Message> {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size(); i++) {
                if (i == 0)
                    sb.append("┌");
                else if (i == size() - 1)
                    sb.append("└");
                else
                    sb.append("├");
                sb.append(get(i).toString()).append("\n");
            }
            return sb.toString();
        }

        public void addMessage(String role, String content) {
            add(new Message(role, content));
        }

        public void addUser(String content) {
            add(new Message("user", content));
        }

        public void addUser(String content, byte[]... images) {
            add(new Message("user", content, images));
        }

        public void addObservation(String content) {
            add(new Message("observation", content));
        }

        public void addAssistant(String content) {
            add(new Message("assistant", content));
        }

        public void addSystem(String content) {
            add(new Message("system", content));
        }
    }

    public static class Message {
        private final String role;
        private final String content;
        private final List<String> images;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
            images = null;
        }

        public Message(String role, String content, byte[]... images) {
            this.role = role;
            this.content = content;
            this.images = new ArrayList<>();
            for (byte[] img : images) {
                this.images.add(Base64.getEncoder().encodeToString(img));
            }
        }

        @Override
        public String toString() {
            return "[" + role + "]: " + (images == null ? "" : "(附" + images.size() + "张图片)") + content.replaceAll("\r\n", " ");
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }

        public List<String> getImages() {
            return images;
        }

        public List<byte[]> getRawImages() {
            List<byte[]> imgData = new ArrayList<>();
            for (var img : images) {
                imgData.add(Base64.getDecoder().decode(img));
            }
            return imgData;
        }
    }

    public static class ModelShow {
        public String modelfile;
        public String parameters;
        /**
         * TEMPLATE是传递给模型的完整提示模板。它可能包括（可选的）系统消息、用户的消息和模型的响应。注意：语法可能是模型特定的。模板使用 Go 模板语法。
         */
        public String template;
        public Details details;

        @Override
        public String toString() {
            return "(show) " + modelfile.length() + " - " + parameters.length() + " - " + template.length();
        }

    }

    public static class Model {
        /**
         * 模型名称
         */
        public String name;
        public String model;
        public String modified_at;
        /**
         * 模型大小 (字节)
         */
        public long size;
        public String digest;
        /**
         * 模型细节
         */
        public Details details;
        public String expires_at;

        @Override
        public String toString() {
            return "(model " + details.parameter_size + ") " + name;
        }

    }

    public static class Details {
        public String parent_model;
        /**
         * 格式
         */
        public String format;
        public String family;
        public List<String> families;
        /**
         * 参数大小
         */
        public String parameter_size;
        /**
         * 量化
         */
        public String quantization_level;
    }

}
