package org.kr;

import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KeySequence {
    private Map<Long, KeySequenceEvent> keySequenceMap = new HashMap<>();

    public KeySequence() {
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
                    keySequenceMap.put(tick, new KeySequenceEvent(type, key));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to read " + resourceFile, e);
        }
    }

    public boolean exists(long t) {
        return keySequenceMap.containsKey(t);
    }

    public KeySequenceEvent get(long tick) {
        return keySequenceMap.get(tick);
    }
}

class KeySequenceEvent {
    KeySequenceEventType type;
    int code;
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
}

enum KeySequenceEventType {
    PRESSED,
    RELEASED
}