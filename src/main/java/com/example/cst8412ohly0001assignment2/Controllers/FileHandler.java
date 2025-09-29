package com.example.cst8412ohly0001assignment2.Controllers;

import javafx.collections.ObservableList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;

import org.w3c.dom.*;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class FileHandler
{
    public static final FileHandler INSTANCE = new FileHandler();
    private final Logger LOGGER = Logger.getLogger(FileHandler.class.getName());
    private LinkedList<LinkedHashMap<String,String>> fileContents = new LinkedList<>();
    private LinkedList<String> fileSchema = new LinkedList<>();

    private File currentFile = null;
    private final Deque<File> previousFiles = new ArrayDeque<>();
    private final int maxPreviousFiles = 20;

    private FileHandler(){}

    public LinkedList<LinkedHashMap<String,String>> getContents()
    {
        return fileContents;
    }

    public void setContents(LinkedList<LinkedHashMap<String,String>> fileContents)
    {
        this.fileContents = fileContents;
    }

    public LinkedList<String> getSchema()
    {
        return fileSchema;
    }

    public void setSchema(LinkedList<String> fileSchema) { this.fileSchema = fileSchema; }

    private Deque<File> getPreviousFiles()
    {
        return previousFiles;
    }

    public File getCurrentFile()
    {
        return currentFile;
    }
    
    private void readCSV(File file) throws Exception {
        // Updated builder-style CSVFormat
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()         // uses first row as header
                .setSkipHeaderRecord(true) // skip the header row in iteration
                .build();

        try (CSVParser parser = new CSVParser(new FileReader(file), format)) {
            parser.forEach(record -> fileContents.add(new LinkedHashMap<>(record.toMap())));
        }
    }

    private void readJSON(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        fileContents = mapper.readValue(file, new TypeReference<>() {});
    }

    private void readXML(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                LinkedHashMap<String, String> row = new LinkedHashMap<>();
                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node child = childNodes.item(j);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        row.put(child.getNodeName(), child.getTextContent());
                    }
                }
                fileContents.add(row);
            }
        }
    }

    public void readYAML(File file) throws Exception {
        Yaml yaml = new Yaml(); // default constructor
        try (FileReader reader = new FileReader(file)) {
            Object loaded = yaml.load(reader);
            fileContents = new LinkedList<>();
            if (loaded instanceof List<?>) {
                for (Object obj : (List<?>) loaded) {
                    if (obj instanceof LinkedHashMap<?, ?> map) {
                        // cast keys/values to String
                        LinkedHashMap<String, String> row = new LinkedHashMap<>();
                        map.forEach((k,v) -> row.put(String.valueOf(k), v != null ? String.valueOf(v) : ""));
                        fileContents.add(row);
                    }
                }
            }
        }
    }

    public String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot == -1) return ""; // no extension
        return name.substring(lastDot + 1).toLowerCase();
    }

    public LinkedList<String> extractSchema()
    {
        //assumes schema is csv-like / tabularly consistent (may not work for all json and xml files)
        LinkedHashMap<String,String> firstRow = fileContents.getFirst();
        LinkedList<String> schema = new LinkedList<>();
        //ensures order is preserved
        firstRow.forEach((key, _) -> schema.add(key));
        return schema;
    }

    public void readFile(File file) {
        try {
            if (file.exists() && file.isFile() && file.canRead()) {
                fileContents.clear();
                switch (getFileExtension(file)) {
                    case "csv" -> readCSV(file);
                    case "json" -> readJSON(file);
                    case "yaml" -> readYAML(file);
                    case "xml" -> readXML(file);
                }
                setCurrentFile(file);
                fileSchema = extractSchema();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reading file: " + file.getAbsolutePath(), e);
        }
    }

    private void writeCSV(File file) throws Exception {
        // Extract headers from the first row
        Set<String> headers = fileContents.getFirst().keySet();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(new String[0]))
                .build();

        try (var writer = new FileWriter(file);
             var csvPrinter = new org.apache.commons.csv.CSVPrinter(writer, format)) {
            fileContents.forEach(row -> {
                try {
                    csvPrinter.printRecord(headers.stream().map(key -> row.getOrDefault(key, "")).toList());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void writeJSON(File file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, fileContents);
    }

    private void writeXML(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        // Root element
        Element root = doc.createElement("records");
        doc.appendChild(root);

        fileContents.forEach(row -> {
            Element recordElement = doc.createElement("record");
            root.appendChild(recordElement);
            row.forEach((key, value) -> {
                Element field = doc.createElement(key);
                field.appendChild(doc.createTextNode(value != null ? value : ""));
                recordElement.appendChild(field);
            });
        });

        // Write to file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    private void writeYAML(File file) throws Exception {
        if (fileContents == null || fileContents.isEmpty()) return;

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(fileContents, writer);
        }
    }

    public void writeFile(File file) {
        if (fileContents.isEmpty()) return;
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.exists()) {
                    LOGGER.warning("Could not create parent directories: " + parent.getAbsolutePath());
                    return;
                }
            }
            setCurrentFile(file);
            switch (getFileExtension(file)) {
                case "csv" -> writeCSV(file);
                case "json" -> writeJSON(file);
                case "yaml" -> writeYAML(file);
                case "xml" -> writeXML(file);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error writing file: " + file.getAbsolutePath(), e);
        }
    }

    public void reloadCurrentFile()
    {
        readFile(currentFile);
    }

    public void overwriteCurrentFile()
    {
        writeFile(currentFile);
    }

    public void closeCurrentFile() {
        currentFile = null;
        fileContents.clear();
        fileSchema.clear();
    }

    public void reorderColumns(ObservableList<String> items) {
        fileSchema = new LinkedList<>(items); //faster then clear + putAll()
        for (int i = 0; i < fileContents.size(); i++) {
            LinkedHashMap<String, String> oldRow = fileContents.get(i);
            LinkedHashMap<String, String> newRow = new LinkedHashMap<>();
            //reorders rows, drops removed ones and adds new ones, acting as a schema sync
            fileSchema.forEach(key -> newRow.put(key, oldRow.getOrDefault(key, "")));
            fileContents.set(i, newRow);
        }
    }

    public void setCurrentFile(File currentFile) {
        if (previousFiles.size() >= maxPreviousFiles)  {
            previousFiles.removeFirst();
        }
        if (!previousFiles.contains(currentFile))
            this.previousFiles.addLast(currentFile);
        this.currentFile = currentFile;
    }

    public void clearContents() {
        fileContents.clear();
    }
}