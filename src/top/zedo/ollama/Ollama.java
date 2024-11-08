package top.zedo.ollama;

import com.google.gson.annotations.SerializedName;

import java.util.*;

public class Ollama {

    public static class BaseMessage {
        /**
         * 模型名称
         */
        public String model;
        /**
         * 创建于时间
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

    public static class Tool {
        /**
         * 工具类型
         */
        public String type = "function";
        /**
         * 工具功能
         */
        public Function function;

        /**
         * @param type     工具类型 function
         * @param function 函数
         */
        public Tool(String type, Function function) {
            this.type = type;
            this.function = function;
        }

        /**
         * 函数工具
         *
         * @param name        函数名
         * @param description 函数描述
         * @param type        函数参数类型
         * @param required    必要参数名
         */
        public Tool(String name, String description, String type, String... required) {
            this.type = type;
            this.function = new Function(name, description, type, required);
        }

        /**
         * 增加函数参数属性
         *
         * @param name        参数名
         * @param type        参数类型
         * @param description 参数描述
         * @param enumValues  参数可选值
         */
        public Tool addProperty(String name, String type, String description, String... enumValues) {
            function.parameters.addProperty(name, type, description, enumValues);
            return this;
        }

        /**
         * 工具
         */
        public static class Function {
            /**
             * 函数名
             */
            public String name;
            /**
             * 函数描述
             */
            public String description;
            /**
             * 函数接受的参数
             */
            public Parameters parameters;

            /**
             * @param name        函数名
             * @param description 函数描述
             * @param type        函数参数类型  object
             * @param required    必要参数名
             */
            public Function(String name, String description, String type, String... required) {
                this.name = name;
                this.description = description;
                parameters = new Parameters(type, required);
            }

            /**
             * 增加参数属性
             *
             * @param name        参数名
             * @param type        参数类型
             * @param description 参数描述
             * @param enumValues  参数可选值
             */
            public void addProperty(String name, String type, String description, String... enumValues) {
                parameters.addProperty(name, type, description, enumValues);
            }

            /**
             * 函数接受的参数
             */
            public static class Parameters {

                /**
                 * 参数的类型
                 */
                public String type = "object";
                /**
                 * 函数所需的具体参数
                 */
                public Map<String, Property> properties;
                /**
                 * 函数调用时必须提供的参数
                 */
                public List<String> required;

                public Parameters(String type, String... required) {
                    this.type = type;
                    properties = new HashMap<>();
                    this.required = List.of(required);
                }


                /**
                 * 增加参数属性
                 *
                 * @param name        参数名
                 * @param type        参数类型
                 * @param description 参数描述
                 * @param enumValues  参数可选值
                 */
                public void addProperty(String name, String type, String description, String... enumValues) {
                    properties.put(name, new Property(type, description, List.of(enumValues)));
                }

                /**
                 * 参数属性
                 */
                public static class Property {
                    /**
                     * 参数的类型 string
                     */
                    public String type;
                    /**
                     * 参数的描述
                     */
                    public String description;
                    /**
                     * 参数的可选值
                     */
                    @SerializedName("enum")
                    public List<String> enumValues;

                    public Property(String type, String description, List<String> enumValues) {
                        this.type = type;
                        this.description = description;
                        this.enumValues = enumValues;
                    }
                }
            }
        }
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
        public Integer mirostat = null;

        /**
         * 影响算法响应生成文本反馈的速度。较低的学习率将导致调整速度较慢，而较高的学习率将使算法更具响应性。（默认：0.1）
         */
        public Float mirostat_eta = null;

        /**
         * 控制输出的一致性与多样性之间的平衡。较低的值将导致文本更加集中和一致。（默认：5.0）
         */
        public Float mirostat_tau = null;

        /**
         * 设置用于生成下一个令牌的上下文窗口大小。（默认：2048）
         */
        public Integer num_ctx = null;

        /**
         * 设置模型向后查看的距离，以防止重复。（默认：64，0=禁用，-1=num_ctx）
         */
        public Integer repeat_last_n = null;

        /**
         * 设置对重复的惩罚强度。较高的值（例如 1.5）将更强烈地惩罚重复，而较低的值（例如 0.9）将更宽容。（默认：1.1）
         */
        public Float repeat_penalty = null;

        /**
         * 模型的温度。增加温度将使模型回答更具创造性。（默认：0.8）
         */
        public Float temperature = null;

        /**
         * 设置用于生成的随机数种子。将此设置为特定数字将使模型对同一提示生成相同的文本。（默认：随机）
         */
        public Integer seed = null;

        /**
         * 设置用于停止的序列。当遇到此模式时，ollama.LLM 将停止生成文本并返回。可以通过在 modelfile 中指定多个单独的 stop 参数来设置多个停止模式。
         */
        public String stop = null;

        /**
         * 尾部自由采样用于减少输出中不太可能的令牌的影响。较高的值（例如 2.0）将更多地减少影响，而值为 1.0 时禁用此设置。（默认：1）
         */
        public Float tfs_z = null;

        /**
         * 生成文本时预测的最大令牌数。（默认：128，-1=无限生成，-2=填充上下文）
         */
        public Integer num_predict = null;

        /**
         * 降低生成无意义文本的概率。较高的值（例如 100）将提供更多样的回答，而较低的值（例如 10）将更为保守。（默认：40）
         */
        public Integer top_k = null;

        /**
         * 与 top-k 一起工作。较高的值（例如 0.95）将导致文本更多样化，而较低的值（例如 0.5）将生成更集中和保守的文本。（默认：0.9）
         */
        public Float top_p = null;

        /**
         * 与 top_p 的替代参数，确保质量和多样性。p 表示相对最可能令牌的最小概率。（默认：0.0）
         */
        public Float min_p = null;

        /**
         * 使用的线程数量，0表示自动（默认：0）
         */
        public Integer num_thread = null;

        /**
         * 使用的 GPU 数量。（默认：1）
         */
        public Integer num_gpu = null;

        /**
         * 主要使用的 GPU。（默认：0）
         */
        public Integer main_gpu = null;

        /**
         * 是否启用低显存模式，适用于显存有限的 GPU。（默认：false）
         */
        public Boolean low_vram = null;


        /**
         * 设置低显存模式（适用于显存有限的 GPU）。
         *
         * @param low_vram 是否启用低显存模式
         */
        public Options setLow_vram(boolean low_vram) {
            this.low_vram = low_vram;
            return this;
        }

        /**
         * 设置使用的 GPU 数量。
         *
         * @param num_gpu 使用的 GPU 数量
         */
        public Options setNum_gpu(int num_gpu) {
            this.num_gpu = num_gpu;
            return this;
        }

        /**
         * 设置主要使用的 GPU。
         *
         * @param main_gpu 主要 GPU 的编号
         */
        public Options setMain_gpu(int main_gpu) {
            this.main_gpu = main_gpu;
            return this;
        }

        /**
         * 设置使用的线程数量。0 表示自动选择线程数。
         *
         * @param num_thread 使用的线程数量
         */
        public Options setNum_thread(int num_thread) {
            this.num_thread = num_thread;
            return this;
        }

        /**
         * 设置启用 Mirostat 采样的模式以控制复杂度。
         * 启用Mirostat采样以控制困惑度。（默认：0, 0 = 禁用, 1 = Mirostat, 2 = Mirostat 2.0）
         *
         * @param mirostat Mirostat 采样模式（0 = 禁用，1 = Mirostat，2 = Mirostat 2.0）
         */
        public Options setMirostat(int mirostat) {
            this.mirostat = mirostat;
            return this;
        }

        /**
         * 设置算法响应生成文本反馈的速度。较高的学习率使算法更具响应性。
         * 影响算法响应生成文本反馈的速度。较低的学习率使调整较慢，较高的学习率会使算法更快响应。（默认：0.1）
         *
         * @param mirostat_eta 学习率
         */
        public Options setMirostat_eta(float mirostat_eta) {
            this.mirostat_eta = mirostat_eta;
            return this;
        }

        /**
         * 设置输出的一致性与多样性之间的平衡，较低的值使文本更加集中。
         * 控制输出的连贯性与多样性之间的平衡。较低的值生成的文本更聚焦连贯。（默认：5.0）
         *
         * @param mirostat_tau 输出一致性与多样性的平衡值
         */
        public Options setMirostat_tau(float mirostat_tau) {
            this.mirostat_tau = mirostat_tau;
            return this;
        }

        /**
         * 设置用于生成下一个令牌的上下文窗口大小。
         * 设置用于生成下一个词元的上下文窗口大小。（默认：2048）
         *
         * @param num_ctx 上下文窗口大小
         */
        public Options setNum_ctx(int num_ctx) {
            this.num_ctx = num_ctx;
            return this;
        }

        /**
         * 设置模型向后查看的距离，以防止重复。
         * 设置模型防止重复时回溯的距离。（默认：64, 0 = 禁用, -1 = num_ctx）
         *
         * @param repeat_last_n 模型查看的距离
         */
        public Options setRepeat_last_n(int repeat_last_n) {
            this.repeat_last_n = repeat_last_n;
            return this;
        }

        /**
         * 设置对重复的惩罚强度。较高的值更强烈地惩罚重复。
         * 设置重复惩罚的强度。较高的值（例如1.5）会更强烈地惩罚重复，而较低的值（例如0.9）会较为宽松。（默认：1.1）
         *
         * @param repeat_penalty 对重复的惩罚强度
         */
        public Options setRepeat_penalty(float repeat_penalty) {
            this.repeat_penalty = repeat_penalty;
            return this;
        }

        /**
         * 设置模型的温度值。增加温度使模型的回答更具创造性。
         * 模型的温度值。提高温度会使模型生成更具创意的回答。（默认：0.8）
         *
         * @param temperature 模型温度
         */
        public Options setTemperature(float temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * 设置生成的随机数种子，确保对相同提示生成相同的文本。
         * 设置生成的随机数种子。设定特定数值将使模型在相同的提示下生成相同的文本。（默认：0）
         *
         * @param seed 随机数种子
         */
        public Options setSeed(int seed) {
            this.seed = seed;
            return this;
        }

        /**
         * 设置用于停止的序列。当遇到此模式时，生成过程将停止。
         * 设置停止序列。当遇到此模式时，LLM将停止生成文本并返回结果。可以通过在模型文件中设置多个stop参数来指定多个停止模式。
         *
         * @param stop 停止序列
         */
        public Options setStop(String stop) {
            this.stop = stop;
            return this;
        }

        /**
         * 设置尾部自由采样的参数，用于减少低概率令牌的影响。
         * 尾部自由采样用于减少低概率词元的影响。较高的值（例如2.0）会更大程度减少影响，而1.0则禁用此设置。（默认：1）
         *
         * @param tfs_z 尾部自由采样的参数
         */
        public Options setTfs_z(float tfs_z) {
            this.tfs_z = tfs_z;
            return this;
        }

        /**
         * 设置生成文本时预测的最大令牌数。
         * 设置生成文本时要预测的最大词元数。（默认：128, -1 = 无限生成, -2 = 填满上下文）
         *
         * @param num_predict 最大令牌数
         */
        public Options setNum_predict(int num_predict) {
            this.num_predict = num_predict;
            return this;
        }

        /**
         * 设置生成文本时的 Top-K 值，控制样本的多样性。
         * 降低生成无意义文本的概率。较高的值（如100）会生成更为多样的回答，而较低的值（如10）则更保守。（默认：40）
         *
         * @param top_k Top-K 值
         */
        public Options setTop_k(int top_k) {
            this.top_k = top_k;
            return this;
        }

        /**
         * 设置与 Top-K 一起工作的 Top-P 值，控制样本的多样性。
         * 与top-k参数配合使用。较高的值（如0.95）会生成更为多样的文本，而较低的值（如0.5）会生成更聚焦保守的文本。（默认：0.9）
         *
         * @param top_p Top-P 值
         */
        public Options setTop_p(float top_p) {
            this.top_p = top_p;
            return this;
        }

        /**
         * 设置最小概率值，用于控制文本的质量和多样性。
         * 与top_p的替代参数，旨在平衡质量与多样性。参数p表示相对于最高概率词元的最小概率。例如，p=0.05时，若最高概率词元为0.9，则低于0.045的词元会被过滤掉。
         *
         * @param min_p 最小概率值
         */
        public Options setMin_p(float min_p) {
            this.min_p = min_p;
            return this;
        }
    }

    public static class MessageHistory extends ArrayList<Message> {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size(); i++) {
                if (i == 0) sb.append("┌");
                else if (i == size() - 1) sb.append("└");
                else sb.append("├");
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

    public static class ToolCall {
        public Function function;

        public static class Function {
            public String name;
            public Map<String, String> arguments;
        }
    }

    public static class Message {
        private final String role;
        private final String content;
        private final List<String> images;
        private final List<ToolCall> tool_calls;

        public Message(String role, String content, List<ToolCall> tool_calls) {
            this.role = role;
            this.content = content;
            this.tool_calls = tool_calls;
            images = null;
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
            images = null;
            tool_calls = null;
        }

        public Message(String role, String content, byte[]... images) {
            this.role = role;
            this.content = content;
            this.images = new ArrayList<>();
            for (byte[] img : images) {
                this.images.add(Base64.getEncoder().encodeToString(img));
            }
            tool_calls = null;
        }

        public List<ToolCall> getToolCalls() {
            return tool_calls;
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
