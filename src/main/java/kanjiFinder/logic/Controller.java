package kanjiFinder.logic;

import kanjiFinder.domain.Anki;
import kanjiFinder.domain.TextParser;
import kanjiFinder.domain.StringProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {
    private final StringProcessor stringProcessor;
    private final TextParser textParser;
    private Map<Path,Set<String>> uniqueKanjiByFile;
    private Set<String> uniqueKanjiInAnki;
    private final String path;

    public Controller(String path) {
        this.stringProcessor = new StringProcessor();
        this.textParser = new TextParser();
        this.uniqueKanjiByFile = new HashMap<Path, Set<String>>();
        this.uniqueKanjiInAnki = new HashSet<>();
        this.path = path;
    }

    public Map<Path,Set<String>> extractUniqueKanjiNotInAnkiFromPath(Anki anki) {
        String text = textParser.parseStringFromAnkiDeck(anki);
        this.uniqueKanjiByFile = new HashMap<Path, Set<String>>();
        this.uniqueKanjiInAnki = stringProcessor.extractUniqueKanji(text);
        return extractUniqueKanjiFromPath();
    }

    public Map<Path,Set<String>> extractUniqueKanjiFromPath() {
        Set<Path> filePaths = listFilesRecursiveFromPath();
        for(Path file : filePaths) {
            System.out.println("Extracting kanji from file: " + file.toString());
            Set<String> uniqueKanji = extractUniqueKanjiFromFile(file,this.uniqueKanjiInAnki);
            this.uniqueKanjiByFile.put(file, uniqueKanji);
        }
        return this.uniqueKanjiByFile;
    }

    public void exportUniqueKanjiWithSentence() {
        this.uniqueKanjiByFile.forEach((file, uniqueKanji) -> {
            String text = extractTextFromFile(file);
            StringBuilder builder = new StringBuilder();
            stringProcessor.extractKanjiWithSentences(text,uniqueKanji).forEach((kanji, sentence) -> {
                builder.append(kanji).append("：").append(sentence).append("\n");
            });
            Path exportPath = constructExportPath(file);
            System.out.println("Writing sentence to: " + exportPath);
            try {
                Files.write(exportPath,builder.toString().getBytes());
            } catch(Exception e) {
                System.out.println("Failed to write file:" + exportPath.toString());
            }
        });
    }

    private Path constructExportPath(Path filePath) {
        String fileName = filePath.getFileName().toString();
        fileName = String.join(".",fileName.substring(0,fileName.lastIndexOf(".")),"txt");
        Path rootPath = Paths.get(this.path).getParent();
        filePath = filePath.subpath(rootPath.getNameCount(), filePath.getNameCount()).getParent();
        if(filePath==null) filePath = Paths.get("");
        Path dir = rootPath.resolve(Paths.get("kanji")).resolve(filePath);
        try {
            Files.createDirectories(dir);
            return dir.resolve(fileName);
        } catch(Exception e) {
            System.out.println("Failed to create directory for" + dir);
            return null;
        }
    }

    public Set<Path> listFilesRecursiveFromPath() {
        try (Stream<Path> stream = Files.walk(Paths.get(this.path))) {
            return stream
                    .filter(this::isValidFile)
                    .collect(Collectors.toSet());
        } catch(IOException e) {
            System.out.println("Failed to access directory");
            return null;
        }
    }

    private boolean isValidFile(Path path) {
        File file = path.toFile();

        return file.isFile() && file.getName().endsWith(".epub") || file.getName().endsWith(".txt");
    }

    private String extractTextFromFile(Path path) {
        String text = "";
        File file = path.toFile();

        try {
            if (file.getName().endsWith(".epub")) {
                text = this.textParser.parseStringFromEpub(file.toPath());
            }
            if (file.getName().endsWith(".txt")) {
                text = this.textParser.parseStringFromTextFile(file.toPath());
            }
        } catch(IOException e) {
            System.out.println("Failed to extract kanji from: " + path.getFileName().toString());
        }
        return text;
    }

    private Set<String> extractUniqueKanjiFromFile(Path path) {
        String text = extractTextFromFile(path);
        Set<String> uniqueKanji = stringProcessor.extractUniqueKanji(text);
        return uniqueKanji;
    }

    private Set<String> extractUniqueKanjiFromFile(Path path, Set<String> uniqueKanjiInAnki) {
        Set<String> uniqueKanji = extractUniqueKanjiFromFile(path);
        uniqueKanji.removeAll(uniqueKanjiInAnki);
        return uniqueKanji;
    }

    public Map<Path,Set<String>> getUniqueKanjiByFile() {
        return this.uniqueKanjiByFile;
    }

    public Set<String> getUniqueKanjiInAnki() {
        return this.uniqueKanjiInAnki;
    }

}
