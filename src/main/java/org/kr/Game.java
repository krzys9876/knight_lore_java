package org.kr;

import java.util.Timer;
import java.util.TimerTask;

public class Game implements Runnable {
    private final ScreenPanel mainPanel;
    private final ScreenPanel shadowPanel;
    private final DebugPanel debugPanel1;
    private final DebugPanel debugPanel2;

    // $4000-$57FF - spectrum video memory
    // $5800-$5AFF - spectrum attribute memory
    private final VideoMemory mainMemory;
    // $D8F3-$F0F2 - video buffer
    private final VideoMemory shadowMemory;
    // $5BA0-$6107 - variables
    // NOTE: variables and other memory locations are treated as ints, not bytes due to lack of unsigned byte type in java
    private final int[] variables = new int[0x6107 - 0x5BA0 + 1];
    private final int[] lookupTable = new int[0xFFFF - 0xF100 + 1];

    // Repaint every fixed interval
    final long repaintIntervalMs = 50;
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            updateMainMemory();
            updateShadowMemory();
        }
    };

    public Game(ScreenPanel mainPanel, ScreenPanel shadowPanel, DebugPanel debugPanel1, DebugPanel debugPanel2) {
        this.mainPanel = mainPanel;
        this.shadowPanel = shadowPanel;
        this.debugPanel1 = debugPanel1;
        this.debugPanel2 = debugPanel2;

        this.debugPanel1.append("Start");
        this.debugPanel2.append("Start");

        mainMemory = new VideoMemory();
        shadowMemory = new VideoMemory();

        timer.scheduleAtFixedRate(task, repaintIntervalMs * 2, repaintIntervalMs);

    }

    public void updateMainMemory() {
        mainPanel.setPixelData(mainMemory.toPixels());
        mainPanel.repaint();
    }
    public void updateShadowMemory() {
        shadowPanel.setPixelData(shadowMemory.toPixels());
        shadowPanel.repaint();
    }


    @Override
    public void run() {
        /*while (true) {
            debugPanel1.append(DateTimeFormatter.ofPattern("HH:mm:ss.SSS").format(LocalDateTime.now()));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        // Just for testing
        mainMemory.setByteAt(0x1800,  Color.getAttribute(Color.BLUE, Color.RED, Color.BRIGHT, Color.FLASH));
        mainMemory.setByteAt(0x181F,  Color.getAttribute(Color.BLUE, Color.RED, Color.BRIGHT, Color.FLASH));
        shadowMemory.setByteAt(0x1800,  Color.getAttribute(Color.RED, Color.BLUE, Color.BRIGHT, Color.FLASH));
        shadowMemory.setByteAt(0x181F,  Color.getAttribute(Color.RED, Color.BLUE, Color.BRIGHT, Color.FLASH));

        start_AF6C();
    }

    private int getVariableAt(int address) { return variables[address - 0x5BA0]; }
    private void setVariableAt(int address, int value) { variables[address - 0x5BA0] = value; }
    private int getLookup(int address) { return lookupTable[address - 0xF100]; }
    private void setLookup(int address, int value) {
        //debugPanel2.append(Integer.toHexString(address) + ": "+ Integer.toHexString(value));
        lookupTable[address - 0xF100] = value;
    }

    private void start_AF6C() {
        debugPanel1.append("start_AF6C");
        int v5C78 = getVariableAt(0x5C78); // FRAMES system variable (LSB)
        clear_mem_D53A(0x5BA0, 0x0568); // clear all variables
        setVariableAt(0x5BA0, v5C78);
        main_AF88();
    }

    private void clear_mem_D53A(int address, int cells) {
        debugPanel1.append("clear_mem_D53A");
        for (int i = address; i < address + cells; i++) { setVariableAt(i, (byte)0);}
    }

    private void main_AF88() {
        debugPanel1.append("main_AF88");
        build_lookup_tables_D69E();
    }

    private void build_lookup_tables_D69E() {
        debugPanel1.append("build_lookup_tables_D69E");
        // NOTE: registers are treated as ints, not bytes due to lack of unsigned byte type in java

        // E is not reinitialized between the two loops
        int e=0;
        // F200 - FFFF
        for(int l = 0; l<=0xFF; l++) {
            int d=0;
            e=l;
            int h = 0xFF;
            for (int b = 7; b > 0; b--) {
                // SLA E
                boolean cy = (e & 0x80) > 0;
                e = (e << 1) & 0xFF;
                // RL D
                d = (d << 1) & 0xFF;
                if(cy) d++;
                // LD A, E
                int a = e;
                // CPL
                a = a ^ 0xFF;
                // LD (HL), A
                setLookup(h * 256 + l, a);
                // DEC H
                h--;
                // LD A, D
                a = d;
                // CPL
                a = a ^ 0xFF;
                setLookup(h * 256 + l, a);
                // DEC H
                h--;
                // DJNZ $D6A7
            }
        }
        // F100 - F1FF
        for(int l = 0; l<=0xFF; l++) {
            int d=l;
            for(int b=8; b > 0; b--) {
                // SRL D
                boolean cy = (d & 1) > 0;
                d = (d >> 1);
                // RL E
                e = (e << 1) & 0xFF;
                if(cy) e++;
                // DJNZ $D6BE
            }
            // LD (HL), e
            setLookup(0xF100+l, e);
        }

        // Lookup table values verified with KL memory dump
        /*StringBuilder t= new StringBuilder();
        for(int i = 0; i < lookupTable.length; i++) {
            if((i % 16) == 0) {
                debugPanel2.append(t.toString());
                t = new StringBuilder(String.format("%04x:", i + 0xF100));
            }
            t.append(" ").append(String.format("%02x", lookupTable[i]));
        }
        debugPanel2.append(t.toString());*/
    }



}
