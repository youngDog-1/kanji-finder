package kanjiFinder.domain;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TextParser {

    public TextParser() {}

    private String readText(BufferedReader reader) throws IOException {
        int intValueOfChar;
        StringBuilder buffer = new StringBuilder();
        while((intValueOfChar = reader.read()) != -1) {
            buffer.append((char) intValueOfChar);
        }
        while(reader.read() != -1) {
            char c = (char) reader.read();
            if(c!=' ') buffer.append(c);
        }
        return buffer.toString();
    }

    public String parseStringFromTextFile(Path path) throws IOException {
        BufferedReader reader = Files.newBufferedReader(path);
        return readText(reader);
    }


    public String parseStringFromEpub(Path path) {
        EpubReader epubReader = new EpubReader();
        Book book;
        try {
            book = epubReader.readEpub(new FileInputStream(path.toFile()));
        } catch (IOException e) {
            System.out.println("File format is not epub");
            return null;
        }

        MediaType xhtmlMediaType = new MediaType("application/xhtml+xml", ".xhtml");
        List<Resource> resourceCollection = book.getResources().getAll()
                .stream()
                .filter(r -> r.getMediaType().equals(xhtmlMediaType))
                .toList();

        StringBuilder stringBuilder = new StringBuilder();
        for(Resource r : resourceCollection) {
            try(BufferedReader reader = new BufferedReader(r.getReader());) {
                stringBuilder.append(extractTextFromXhtml(reader));
            } catch(IOException e) {
                System.out.println("Failed to get 1 resource");
            }
        }
        return stringBuilder.toString();
    }

    private String extractTextFromXhtml(BufferedReader reader) throws IOException {
        Document doc = Jsoup.parse(readText(reader));
        return doc.body().text();
    }

    public String parseStringFromAnkiDeck(Anki anki) {
        StringProcessor stringProcessor = new StringProcessor();
        StringBuilder builder = new StringBuilder();

        String[] notesInfo = anki.getSelectedFieldsOfNotesFromDeck();

        for(String note : notesInfo) {
            JSONObject noteJson = new JSONObject(note);
            noteJson.keySet().forEach(key -> builder.append(noteJson.optString(key)));
        }
        return builder.toString();
    }
}
