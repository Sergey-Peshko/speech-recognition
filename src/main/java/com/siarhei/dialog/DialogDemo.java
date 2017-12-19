package com.siarhei.dialog;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;


public class DialogDemo {

    private static final String ACOUSTIC_MODEL =
        "resource:/edu/cmu/sphinx/models/en-us/en-us";
    private static final String DICTIONARY_PATH =
        "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static final String GRAMMAR_PATH =
        "resource:/gram/";

    private static final Map<String, Integer> DIGITS =
        new HashMap<String, Integer>();

    static {
        DIGITS.put("zero", 0);
        DIGITS.put("one", 1);
        DIGITS.put("two", 2);
        DIGITS.put("three", 3);
        DIGITS.put("four", 4);
        DIGITS.put("five", 5);
        DIGITS.put("six", 6);
        DIGITS.put("seven", 7);
        DIGITS.put("eight", 8);
        DIGITS.put("nine", 9);
    }

    private static double parseNumber(String[] tokens) {
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < tokens.length; ++i) {
            if (tokens[i].equals("point"))
                sb.append(".");
            else
                sb.append(DIGITS.get(tokens[i]));
        }

        return Double.parseDouble(sb.toString());
    }
    private static void recognizeDigits(LiveSpeechRecognizer recognizer) {
        System.out.println("Digits recognition (using GrXML)");
        System.out.println("--------------------------------");
        System.out.println("Example: one two three");
        System.out.println("Say \"101\" to exit");
        System.out.println("--------------------------------");

        recognizer.startRecognition(true);
        while (true) {
            String utterance = recognizer.getResult().getHypothesis();
            if (utterance.equals("one zero one"))
                break;
            else
                System.out.println(utterance);
        }
        recognizer.stopRecognition();
    }

    private static void recognizerBankAccount(LiveSpeechRecognizer recognizer) {
        System.out.println("This is bank account voice menu");
        System.out.println("-------------------------------");
        System.out.println("Example: balance");
        System.out.println("Example: withdraw zero point five");
        System.out.println("Example: deposit one two three");
        System.out.println("Example: back");
        System.out.println("-------------------------------");

        double savings = .0;
        recognizer.startRecognition(true);

        while (true) {
            String utterance = recognizer.getResult().getHypothesis();
            if (utterance.equals("back") || utterance.endsWith("back")) {
                break;
            } else if (utterance.startsWith("deposit")) {
                double deposit = parseNumber(utterance.split("\\s"));
                savings += deposit;
                System.out.format("Deposited: $%.2f\n", deposit);
            } else if (utterance.startsWith("withdraw")) {
                double withdraw = parseNumber(utterance.split("\\s"));
                savings -= withdraw;
                System.out.format("Withdrawn: $%.2f\n", withdraw);
            } else if (!utterance.endsWith("balance")) {
                System.out.println("Unrecognized command: " + utterance);
            }

            System.out.format("Your savings: $%.2f\n", savings);
        }

        recognizer.stopRecognition();
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setUseGrammar(true);

        configuration.setGrammarName("dialog");
        LiveSpeechRecognizer jsgfRecognizer =
            new LiveSpeechRecognizer(configuration);

        configuration.setGrammarName("digits.grxml");
        LiveSpeechRecognizer grxmlRecognizer =
                new LiveSpeechRecognizer(configuration);

        jsgfRecognizer.startRecognition(true);
        while (true) {
            System.out.println("Choose menu item:");
            System.out.println("Example: go to the bank account");
            System.out.println("Example: digits");
            System.out.println("Example: exit the program\n");

            String utterance = jsgfRecognizer.getResult().getHypothesis();
            System.out.println("You said: " + utterance);

            if (utterance.startsWith("exit"))
                break;

            if (utterance.equals("digits")) {
                jsgfRecognizer.stopRecognition();
                recognizeDigits(grxmlRecognizer);
                jsgfRecognizer.startRecognition(true);
            }

            if (utterance.equals("bank account") || utterance.endsWith("bank account")) {
                jsgfRecognizer.stopRecognition();
                recognizerBankAccount(jsgfRecognizer);
                jsgfRecognizer.startRecognition(true);
            }


        }

        jsgfRecognizer.stopRecognition();
    }
}