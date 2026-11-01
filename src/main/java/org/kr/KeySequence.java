package org.kr;

import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KeySequence {
    private Map<Long, List<KeySequenceEvent>> keySequenceMap = new HashMap<>();
    KeySequenceMode mode;

    public KeySequence(KeySequenceMode mode) {
        this.mode = mode;
        if(mode == KeySequenceMode.REPLAY) loadFromResource();
    }

    public void loadFromResource() {
        String resourceFile = "/key_sequence.csv";
        try (InputStream in = InitialData.class.getResourceAsStream(resourceFile)) {
            if (in == null) throw new IllegalStateException("resource not found on classpath: " + resourceFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            List<Integer> data = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if(!line.startsWith("#")) {
                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    if (tokenizer.countTokens() != 3) {
                        throw new IllegalStateException("invalid key sequence: " + line);
                    }
                    long tick = Long.parseLong(tokenizer.nextToken());
                    String type = tokenizer.nextToken();
                    String key = tokenizer.nextToken();
                    List<KeySequenceEvent> events = keySequenceMap.get(tick);
                    if(events == null) { events = new ArrayList<>(); }
                    events.add(new KeySequenceEvent(type, key));
                    keySequenceMap.put(tick, events);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to read " + resourceFile, e);
        }
    }

    public boolean exists(long t) {
        return mode == KeySequenceMode.REPLAY && keySequenceMap.containsKey(t);
    }

    public List<KeySequenceEvent> get(long tick) {
        return keySequenceMap.get(tick);
    }

    public void putPressed(long tick, int code) {
        if(mode == KeySequenceMode.SAVE) put(tick, KeySequenceEventType.PRESSED, code);
    }

    public void putReleased(long tick, int code) {
        if(mode == KeySequenceMode.SAVE) put(tick, KeySequenceEventType.RELEASED, code);
    }

    private void put(long tick, KeySequenceEventType type, int code) {
        List<KeySequenceEvent> events = keySequenceMap.get(tick);
        if(events == null) { events = new ArrayList<>(); }
        events.add(new KeySequenceEvent(type, code));
        keySequenceMap.put(tick, events);
    }

    public void save(String file) {
        keySequenceMap.keySet().stream().sorted().forEach(t -> {
            List<KeySequenceEvent> events = keySequenceMap.get(t);
            events.forEach(event -> IO.println(String.format("%d,%s", t, event.toCsvString())));
        });
    }
}

class KeySequenceEvent {
    KeySequenceEventType type;
    int code;

    KeySequenceEvent(KeySequenceEventType type, int code) {
        this.type = type;
        this.code = code;
    }

    KeySequenceEvent(String typeText, String codeText) {
        type = typeText.equalsIgnoreCase("P") ? KeySequenceEventType.PRESSED : KeySequenceEventType.RELEASED;
        code = switch (codeText) {
            case "1" -> KeyEvent.VK_1; // pickup / drop
            case "Q" -> KeyEvent.VK_Q; // jump
            case "A" -> KeyEvent.VK_A; // forward
            case "Z" -> KeyEvent.VK_Z; // turn left
            case "X" -> KeyEvent.VK_X; // turn right
            default -> KeyEvent.VK_ESCAPE;
        };
    }

    public String toCsvString() {
        return (type == KeySequenceEventType.PRESSED ? "P" : "R") + "," + switch (code) {
            case KeyEvent.VK_1 -> "1";
            case KeyEvent.VK_Q -> "Q";
            case KeyEvent.VK_A -> "A";
            case KeyEvent.VK_Z -> "Z";
            case KeyEvent.VK_X -> "X";
            default -> "_";
        };
    }
}

enum KeySequenceEventType {
    PRESSED,
    RELEASED
}

enum KeySequenceMode {
    REPLAY,
    SAVE
}