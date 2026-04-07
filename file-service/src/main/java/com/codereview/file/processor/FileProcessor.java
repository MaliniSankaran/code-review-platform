package com.codereview.file.processor;

import java.util.Map;

public interface  FileProcessor {

    //Returns which file type this processor handles
    String getSupportedExtension();

    //Reads the code and extracts useful info (line count, class names, imports). Returns a Map so each language can return different metadata.
    Map<String, Object> extractMetadata(String fileContent);

    boolean isValidFile(String fileContent);
}
