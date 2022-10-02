package xyz.ctsk.luckycow;

import io.javalin.Javalin;
import io.javalin.http.ContentType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

public class Main {
    private final byte[] beforeTemplate;
    private final byte[] afterTemplate;

    private static final String BEFORE_TEMPLATE_RES = "index-before.html";
    private static final String AFTER_TEMPLATE_RES = "index-after.html";
    private static final String[] FORTUNE_COMMAND = {"sh", "-c", "fortune -a | cowsay"};

    private static final int DEFAULT_PORT = 7070;

    private Main(byte[] beforeTemplate, byte[] afterTemplate) {
        this.beforeTemplate = beforeTemplate;
        this.afterTemplate = afterTemplate;
    }

    private InputStream run() throws IOException {
        var process = Runtime.getRuntime().exec(FORTUNE_COMMAND);
        return process.getInputStream();
    }

    private InputStream runAndFormat() throws IOException {
        return new SequenceInputStream(
                new ByteArrayInputStream(beforeTemplate),
                new SequenceInputStream(
                        run(),
                        new ByteArrayInputStream(afterTemplate)));
    }

    private static byte[] resToBytes(String res) throws IOException {
        try (var inputStream = Main.class.getClassLoader().getResourceAsStream(res)) {
            assert inputStream != null;
            return inputStream.readAllBytes();
        }
    }

    public static void main(String[] args) throws IOException {
        var beforeTemplate = Main.resToBytes(BEFORE_TEMPLATE_RES);
        var afterTemplate = Main.resToBytes(AFTER_TEMPLATE_RES);

        Main main = new Main(beforeTemplate, afterTemplate);

        var port = DEFAULT_PORT;
        var envPort = System.getenv("PORT");
        if (envPort != null) {
            port = Integer.parseInt(envPort);
        }

        var app = Javalin.create().start(port);
        app.get("/", ctx -> ctx.contentType(ContentType.HTML).result(main.runAndFormat()));
    }
}
