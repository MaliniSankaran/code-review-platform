package com.codereview.file.processor;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FileProcessorFactory {

    private final Map<String, FileProcessor> processorMap = new HashMap<>();

    public FileProcessorFactory( List<FileProcessor> processors ) {
        for( FileProcessor processor : processors ) {
            processorMap.put(processor.getSupportedExtension(),  processor);
        }
    }

    public FileProcessor getFileProcessor(String filename) {
        String extension = filename.substring(filename.lastIndexOf("."));
        return processorMap.getOrDefault(extension, null);
    }

    public Boolean isSupported(String filename){
        if(!filename.contains(".")) return false;
        String extension = filename.substring(filename.lastIndexOf("."));
        return processorMap.containsKey(extension);
    }
}
