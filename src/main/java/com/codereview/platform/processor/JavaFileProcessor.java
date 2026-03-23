package com.codereview.platform.processor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JavaFileProcessor implements FileProcessor{


    @Override
    public String getSupportedExtension() {
        return ".java";
    }

    @Override
    public Map<String, Object> extractMetadata(String fileContent) {

        Map<String, Object> metadata = new HashMap<>();

        String[] lines = fileContent.split("\n");
        metadata.put("lineCount", lines.length);

        long importCount = 0;
        String className = null;

        for(String line : lines) {
            String trimmed = line.trim();
            if(trimmed.startsWith("import ")) {
                importCount++;
            }
            if(trimmed.startsWith("public class ") || trimmed.startsWith("class ")) {
                className = trimmed.split("\\s+")[2];
                if (className.contains("{")) {
                    className = className.replace("{", "");
                }
            }
        }
        metadata.put("importCount", importCount);
        metadata.put("className", className);
        metadata.put("language", "Java");

        return metadata;
    }

    @Override
    public boolean isValidFile(String fileContent) {
        return fileContent != null
                && !fileContent.isBlank()
                && (fileContent.contains("class ") || fileContent.contains("interface "));
    }
}
