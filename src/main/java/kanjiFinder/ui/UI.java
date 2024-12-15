package kanjiFinder.ui;

import kanjiFinder.domain.Anki;
import kanjiFinder.logic.Controller;

import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class UI {
    private Anki anki;
    private Controller controller;
    private final Scanner sc;
    private boolean useAnki;

    public UI() {
        this.anki = new Anki();
        this.controller = null;
        this.sc = new Scanner(System.in);
    }

    public void start() {
        promptDirectory();
        promptAnki();
        printUnknownKanji();
        promptExport();
    }

    private void promptDirectory() {
        System.out.println("Enter directory for extracting: ");
        String path = this.sc.nextLine();
        this.controller = new Controller(path);
    }

    private void promptAnki() {
        String answer;
        do {
            System.out.println("Extract only kanji not in Anki? (y/n): ");
            answer = this.sc.nextLine();
        } while(!answer.equals("y") && !answer.equals("n"));
        if(answer.equals("n")) {
            this.useAnki = false;
            return;
        }

        System.out.println("Decks available: ");
        String[] deckNames = this.anki.getDeckNames();
        for(int i=0; i<deckNames.length; i++) {
            System.out.println(String.join(": ", Integer.toString(i+1), deckNames[i]));
        }

        int number;
        while(true) {
            System.out.println("Choose deck number to extract from");
            if(!this.sc.hasNext()) continue;
            number = this.sc.nextInt();
            if(number>=0 && number<deckNames.length) break;
        }
        this.sc.nextLine();
        System.out.println("Enter deck fields to extract, multiple fields separated by , (Eg: Word,Sentence). Default field is Word");
        String fields = this.sc.nextLine();
        this.anki = new Anki(deckNames[number-1],fields);
        this.useAnki = true;
    }

    private void printUnknownKanji() {
        Map<Path, Set<String>> kanji = this.controller.extractUniqueKanjiFromPath();
        Map<Path, Set<String>> kanjiNotInAnki;
        if(this.useAnki) {
            kanjiNotInAnki = this.controller.extractUniqueKanjiNotInAnkiFromPath(this.anki);
        } else {
            kanjiNotInAnki = null;
        }
        System.out.println("--------------------------------");
        System.out.println();
        kanji.forEach((path, kanjiList) -> {
            System.out.println("Extracted result from file: " + path.toString());
            System.out.println("Number of unique kanji in list: " + kanjiList.size());
            if(!(kanjiNotInAnki==null)) System.out.println("Number of unknown unique kanji: " + kanjiNotInAnki.get(path).size());
        });
    }

    private void promptExport() {
        System.out.println("Export kanji with sentence to files? (y)");
        if(this.sc.nextLine().equals("y")) {
            this.controller.exportUniqueKanjiWithSentence();
        }
    }
}
