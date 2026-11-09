package org.kr.kl.utils;

import java.awt.event.KeyEvent;

public class KeySequenceEvent {
    private KeySequenceEventType type;
    private int code;

    KeySequenceEvent(KeySequenceEventType type, int code) {
        this.type = type;
        this.code = code;
    }

    public KeySequenceEventType getType() { return type; }
    public int getCode() { return code; }

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
