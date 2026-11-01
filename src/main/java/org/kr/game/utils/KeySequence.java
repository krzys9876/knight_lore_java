package org.kr.game.utils;

import org.kr.InitialData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KeySequence {
    private final Map<Long, List<KeySequenceEvent>> keySequenceMap = new HashMap<>();
    private KeySequenceMode mode;
    private long lastTick;

    public KeySequence(KeySequenceMode mode) {
        this.mode = mode;
        this.lastTick = -1;
        if (mode == KeySequenceMode.REPLAY) loadFromResource();
    }

    public void loadFromResource() {
        String resourceFile = "/key_sequence.csv";
        try (InputStream in = InitialData.class.getResourceAsStream(resourceFile)) {
            if (in == null) throw new IllegalStateException("resource not found on classpath: " + resourceFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    if (tokenizer.countTokens() != 3) {
                        throw new IllegalStateException("invalid key sequence: " + line);
                    }
                    long tick = Long.parseLong(tokenizer.nextToken());
                    String type = tokenizer.nextToken();
                    String key = tokenizer.nextToken();
                    List<KeySequenceEvent> events = keySequenceMap.get(tick);
                    if (events == null) {
                        events = new ArrayList<>();
                    }
                    events.add(new KeySequenceEvent(type, key));
                    keySequenceMap.put(tick, events);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to read " + resourceFile, e);
        }

        lastTick = keySequenceMap.keySet().stream().mapToLong(Long::longValue).max().orElse(-1);
    }

    public void switchMode(long tick) {
        if(mode == KeySequenceMode.REPLAY && tick > lastTick) mode = KeySequenceMode.SAVE;
    }

    public boolean exists(long t) {
        return mode == KeySequenceMode.REPLAY && keySequenceMap.containsKey(t);
    }

    public List<KeySequenceEvent> get(long tick) {
        return keySequenceMap.get(tick);
    }

    public boolean isSaving() { return mode == KeySequenceMode.SAVE; }
    public boolean isReplaying() { return mode == KeySequenceMode.REPLAY; }

    public void putPressed(long tick, int code) {
        if (mode == KeySequenceMode.SAVE) put(tick, KeySequenceEventType.PRESSED, code);
    }

    public void putReleased(long tick, int code) {
        if (mode == KeySequenceMode.SAVE) put(tick, KeySequenceEventType.RELEASED, code);
    }

    private void put(long tick, KeySequenceEventType type, int code) {
        List<KeySequenceEvent> events = keySequenceMap.get(tick);
        if (events == null) {
            events = new ArrayList<>();
        }
        events.add(new KeySequenceEvent(type, code));
        keySequenceMap.put(tick, events);
    }

    public void save(String file) {
        try {
            FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8, false);
            keySequenceMap.keySet().stream().sorted().forEach(t -> {
                List<KeySequenceEvent> events = keySequenceMap.get(t);
                events.forEach(event -> {
                    String line = String.format("%d,%s\n", t, event.toCsvString());
                    //IO.print(line);
                    try {
                        writer.append(line);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException("failed to save " + file, e);
        }
    }
}

