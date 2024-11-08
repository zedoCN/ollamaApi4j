import top.zedo.ollama.Ollama;
import top.zedo.ollama.OllamaApi;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Image {
    public static void main(String[] args) throws Exception {
        OllamaApi api = new OllamaApi();

        Ollama.MessageHistory history = new Ollama.MessageHistory();
        history.addSystem("""
                    你是一个图像分析AI。你只返回图像信息，格式为JSON，不附带任何额外的描述。
                    ## 格式模板
                    {
                        "title": "", //描述图片标题，要求精确且简短，必填。
                        "description": "", //图片的简短描述，必填。
                        "type": "", //用于图片分类用的类型，需要尽可能通用，必填。二次元插画、家庭合照、私人照片、色情照片、生活记录
                        "tags": [""], //描述图片的标签列表，标签需要常见、通用且准确，标签数量控制在5到10个左右，尽可能多，必填。
                        "rating": "", //图片的适宜年龄分级，是“all”、“r15”、“r18”中的其中的一个，必填。
                        "text": "", //图片中出现的文字，可以省略此字段。
                        "characters": [""], //描述图片中出现的人物列表，如“女孩”、“男人”、“一群学生”，可以省略此字段。
                        "location": "", //图片出现或所在的位置，如“室内”、“景区”，可以省略此字段。
                        "mood": "", //描述图片的情绪或氛围，如“温馨”、“忧郁”、“可爱”，必填。
                        "hue": "" //描述图片的整体色调，如“粉色”、“蓝色”，必填。
                    }
                """);

        history.addUser("", getImage(Path.of("C:\\Users\\zedoC\\Pictures\\1666349035847_1638338713731_梦梦奈立绘.png"), 512));
        api.setHostURL("http://127.0.0.1:11434");
        api.chat(new OllamaApi.PrintGenerateMessage(), new Ollama.Options().setMirostat_tau(4).setNum_predict(-1).setTemperature(0.5f), null, "minicpm-v:latest", "24h", history, null).get();
    }


    public static byte[] getImage(Path imagePath, int maxSize) throws IOException {
        BufferedImage image = ImageIO.read(Files.newInputStream(imagePath));

        float ratio = (float) image.getWidth() / image.getHeight();

        int newWidth = image.getWidth();
        int newHeight = image.getHeight();

        if (newWidth > maxSize) {
            newWidth = maxSize;
            newHeight = (int) (newWidth / ratio);
        }

        if (newHeight > maxSize) {
            newHeight = maxSize;
            newWidth = (int) (newHeight * ratio);
        }

        BufferedImage target = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        target.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(target, "jpg", out);
        return out.toByteArray();
    }
}
