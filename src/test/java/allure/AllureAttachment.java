package allure;

import io.qameta.allure.Allure;

import java.nio.charset.StandardCharsets;

public class AllureAttachment {
    private final static StringBuilder builderLog = new StringBuilder();

    public static void attachText(String title, String text) {
        Allure.getLifecycle().addAttachment(title, "text/plain", "txt", text.getBytes(StandardCharsets.UTF_8));
    }

    public static void logCollector(String line) {
        builderLog.append(line).append("\n");
        if (line.contains("END HTTP")) {
            attachText("log", builderLog.toString());
            builderLog.setLength(0);
        }
    }
}
