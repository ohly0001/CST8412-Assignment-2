package com.example.cst8412ohly0001assignment2;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileReader;
import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;

public final class FileHandler
{
    private static final Logger LOGGER = Logger.getLogger(FileHandler.class.getName());

    private static List<HashMap<String, String>> parseCSV(File file) throws Exception {
        List<HashMap<String, String>> result = new ArrayList<>();

        // Updated builder-style CSVFormat
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()         // uses first row as header
                .setSkipHeaderRecord(true) // skip the header row in iteration
                .build();

        try (CSVParser parser = new CSVParser(new FileReader(file), format)) {
            for (CSVRecord record : parser) {
                HashMap<String, String> row = new HashMap<>(record.toMap());
                result.add(row);
            }
        }

        return result;
    }

    private static List<HashMap<String, String>> parseJSON(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, new TypeReference<>() {
        });
    }

    private static List<HashMap<String, String>> parseXML(File file) throws Exception {
        List<HashMap<String, String>> result = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                HashMap<String, String> row = new HashMap<>();
                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node child = childNodes.item(j);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        row.put(child.getNodeName(), child.getTextContent());
                    }
                }
                result.add(row);
            }
        }
        return result;
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) return ""; // no extension
        return name.substring(lastDot + 1).toLowerCase();
    }

    public static List<HashMap<String,String>> readFile(String filePath)
    {
        File file = new File(filePath);
        try {
            if (file.exists() && file.isFile() && file.canRead()) {
                return switch (getFileExtension(file)) {
                    case "csv" -> parseCSV(file);
                    case "json" -> parseJSON(file);
                    case "xml" -> parseXML(file);
                    default -> List.of();
                };
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading file: " + file.getAbsolutePath(), e);
        }
        return List.of();
    }
}