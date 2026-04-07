package com.codereview.file.processor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PythonFileProcessor implements FileProcessor {


    @Override
    public String getSupportedExtension() {
        return ".py";
    }

    @Override
    public Map<String, Object> extractMetadata(String fileContent) {

        Map<String, Object> metadata = new HashMap<>();

        String[] lines = fileContent.split("\n");
        metadata.put("lineCount", lines.length);

        long importCount = 0;
        long functionCount = 0;
        long classCount = 0;

        for(String line : lines) {

            String trimmed =  line.trim();
            if( trimmed.startsWith("import ") || trimmed.startsWith("from ")) {
                importCount++;
            }
            if( trimmed.startsWith("def ")) {
                functionCount++;
            }
            if( trimmed.startsWith("class ")) {
                classCount++;
            }
        }
        metadata.put("importCount", importCount);
        metadata.put("functionCount", functionCount);
        metadata.put("classCount", classCount);
        metadata.put("language", "Python");

        return metadata;
    }

    @Override
    public boolean isValidFile(String fileContent) {
        return fileContent != null
                && !fileContent.isBlank()
                && (fileContent.contains("def ") || fileContent.contains("class ") || fileContent.contains("import "));
    }
}
