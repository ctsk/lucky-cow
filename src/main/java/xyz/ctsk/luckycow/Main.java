package xyz.ctsk.luckycow;

import io.javalin.Javalin;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Main {
    private final String beforeTemplate;
    private final String afterTemplate;

    private static final String BEFORE_TEMPLATE_RES = "index-before.html";
    private static final String AFTER_TEMPLATE_RES = "index-after.html";
    private static final String[] FORTUNE_COMMAND = {"sh", "-c", "fortune -a | cowsay"};

    private static final int DEFAULT_PORT = 7070;

    private Main(String beforeTemplate, String afterTenplate) {
        this.beforeTemplate = beforeTemplate;
        this.afterTemplate = afterTenplate;
    }

    private String executeCommand(String[] command) throws IOException {
        var process = Runtime.getRuntime().exec(command);
        return IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
    }

    private String runAndFormat(String[] command) throws IOException {
        return beforeTemplate + executeCommand(command) + afterTemplate;
    }

    private static String resToString(String res) throws IOException {
        var inputStream = Main.class.getClassLoader().getResourceAsStream(res);
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    }

    public static void main(String[] args) throws IOException {
        var beforeTemplate = Main.resToString(BEFORE_TEMPLATE_RES);
        var afterTemplate = Main.resToString(AFTER_TEMPLATE_RES);

        Main main = new Main(beforeTemplate, afterTemplate);

        Javalin app = Javalin.create().start(DEFAULT_PORT);
        app.get("/", ctx -> ctx.html(main.runAndFormat(FORTUNE_COMMAND)));
    }
}
