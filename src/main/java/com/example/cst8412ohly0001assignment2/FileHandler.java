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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;

public final class FileHandler
{
    private static final Logger LOGGER = Logger.getLogger(FileHandler.class.getName());

    private static List<HashMap<String, String>> readCSV(File file) throws Exception {
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

    private static List<HashMap<String, String>> readJSON(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, new TypeReference<>() {
        });
    }

    private static List<HashMap<String, String>> readXML(File file) throws Exception {
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

    public static List<HashMap<String,String>> readFile(String filePath) {
        if (filePath == null) return List.of();
        File file = new File(filePath);
        try {
            if (file.exists() && file.isFile() && file.canRead()) {
                return switch (getFileExtension(file)) {
                    case "csv" -> readCSV(file);
                    case "json" -> readJSON(file);
                    case "xml" -> readXML(file);
                    default -> List.of();
                };
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading file: " + file.getAbsolutePath(), e);
        }
        return List.of();
    }

    private static boolean writeCSV(File file, List<HashMap<String, String>> data) throws Exception {
        if (data.isEmpty()) return false;

        // Extract headers from the first row
        Set<String> headers = data.get(0).keySet();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(new String[0]))
                .build();

        try (var writer = new FileWriter(file);
             var csvPrinter = new org.apache.commons.csv.CSVPrinter(writer, format)) {

            for (HashMap<String, String> row : data) {
                List<String> record = new ArrayList<>();
                for (String header : headers) {
                    record.add(row.getOrDefault(header, ""));
                }
                csvPrinter.printRecord(record);
            }
        }

        return true;
    }

    private static boolean writeJSON(File file, List<HashMap<String, String>> data) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        return true;
    }

    public static boolean writeXML(File file, List<HashMap<String, String>> data) throws Exception {
        if (data.isEmpty()) return false;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Root element
        Element root = doc.createElement("records");
        doc.appendChild(root);

        for (HashMap<String, String> row : data) {
            Element recordElement = doc.createElement("record");
            root.appendChild(recordElement);

            for (Map.Entry<String, String> entry : row.entrySet()) {
                Element field = doc.createElement(entry.getKey());
                field.appendChild(doc.createTextNode(entry.getValue() != null ? entry.getValue() : ""));
                recordElement.appendChild(field);
            }
        }

        // Write to file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);

        return true;
    }

    public static boolean writeFile(String filePath, List<HashMap<String,String>> data) {
        if (filePath == null || data == null || data.isEmpty()) return false;

        File file = new File(filePath);
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.exists()) {
                    LOGGER.warning("Could not create parent directories: " + parent.getAbsolutePath());
                    return false;
                }
            }
            return switch (getFileExtension(file)) {
                case "csv" -> writeCSV(file, data);
                case "json" -> writeJSON(file, data);
                case "xml" -> writeXML(file, data);
                default -> false;
            };
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error writing file: " + file.getAbsolutePath(), e);
            return false;
        }
    }
}