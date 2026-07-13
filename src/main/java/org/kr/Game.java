package org.kr;

import java.time.LocalDateTime;
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
    private final DataBlock variables = new DataBlock(0x5BA0, 0x6107 - 0x5BA0 + 1);
    private final DataBlock lookupTable = new DataBlock(0xF100, 0xFFFF - 0xF100 + 1);
    private final DataBlock flags12_1_D16D = InitialData.flags12_1_D16D.copy();
    private final DataBlock menu_colours_BDA2 = InitialData.menu_colours_BDA2.copy();
    private final DataBlock menu_xy_BDAA = InitialData.menu_xy_BDAA.copy();
    private final DataBlock menu_text_BDBA = InitialData.menu_text_BDBA.copy();

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

        mainMemory = new VideoMemory(0x4000);
        shadowMemory = new VideoMemory(0xD8F3);

        timer.scheduleAtFixedRate(task, repaintIntervalMs * 2, repaintIntervalMs);

    }

    public void updateMainMemory() {
        mainPanel.setPixelData(mainMemory.toPixels(LocalDateTime.now()));
        mainPanel.repaint();
    }
    public void updateShadowMemory() {
        shadowPanel.setPixelData(shadowMemory.toPixels(LocalDateTime.now()));
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
        mainMemory.setByteAt(0x5800,  Color.getAttribute(Color.BLUE, Color.RED, Color.BRIGHT, Color.FLASH));
        mainMemory.setByteAt(0x5801,  Color.getAttribute(Color.BLUE, Color.RED, Color.BRIGHT, Color.NONE));
        mainMemory.setByteAt(0x581E,  Color.getAttribute(Color.BLUE, Color.RED, Color.BRIGHT, Color.NONE));
        mainMemory.setByteAt(0x581F,  Color.getAttribute(Color.BLUE, Color.RED, Color.BRIGHT, Color.FLASH));
        shadowMemory.setByteAt(0xD8F3+0x1800,  Color.getAttribute(Color.RED, Color.BLUE, Color.BRIGHT, Color.FLASH));
        shadowMemory.setByteAt(0xD8F3+0x1801,  Color.getAttribute(Color.RED, Color.BLUE, Color.BRIGHT, Color.NONE));
        shadowMemory.setByteAt(0xD8F3+0x181E,  Color.getAttribute(Color.RED, Color.BLUE, Color.BRIGHT, Color.NONE));
        shadowMemory.setByteAt(0xD8F3+0x181F,  Color.getAttribute(Color.RED, Color.BLUE, Color.BRIGHT, Color.FLASH));

        start_AF6C();
    }

    private void start_AF6C() {
        debugPanel1.append("start_AF6C");

        // initialize variables (TODO: split variables to separate data blocks)
        // @label=seed_1
        // b$5BA0 DEFB $53,$00
        variables.set(0x5BA0, 0x53);
        //; Data block at 5BA2
        // @label=seed_2
        // b$5BA2 DEFS $02
        variables.set(0x5BA2, 2);

        int v5C78 = 0x65; // originally taken from 5C78 (LSB of FRAMES 3-byte system variable). It is incremented by ROM interrupt routine, servers as random seed
        // PUSH AF       ;
        // CALL $D53A    ;
        // POP AF        ;
        for (int i = 0x5BA0; i < 0x5BA0 + 0x0568; i++) { variables.set(i, 0);}
        variables.set(0x5BA0, v5C78);
        main_AF88();
    }

    private void main_AF88() {
        debugPanel1.append("main_AF88");
        build_lookup_tables_D69E();
        // XOR A
        // LD ($5BB2),A
        variables.set(0x5BB2, 0);
        // LD ($D16D),A  ; plyr_spr_1_scratchpad
        flags12_1_D16D.set(0xD16D, 0);
        // LD A,$05      ; 5 lives to start
        // LD ($5BBA),A  ;
        variables.set(0x5BBA, 5);
        // LD HL,$5BA0   ;
        // LD A,($5BA2)  ;
        // ADD A,(HL)    ; seed_1 += seed_2
        // LD (HL),A     ; update seed
        variables.set(0x5BA0, (variables.get(0x5BA0) + variables.get(0x5BA2)) & 0xFF);
        // CALL $D55F    ; {colour is bright yellow on black
        clear_scrn_D55F();
        // CALL $BD0C    ;
        do_menu_selection_BD0C();


        printVariables();
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
                lookupTable.set(h * 256 + l, a);
                // DEC H
                h--;
                // LD A, D
                a = d;
                // CPL
                a = a ^ 0xFF;
                lookupTable.set(h * 256 + l, a);
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
            lookupTable.set(0xF100+l, e);
        }

        // Lookup table values verified with KL memory dump
        //printLookupTable();
        //printShadowMemory();
    }

    private void clear_scrn_D55F() {
        debugPanel1.append("clear_scrn_D55F");
        // NOTE: we ignore sound
        // XOR A         ; border colour BLACK, activate MIC
        // OUT ($FE),A   ; ULA
        // CALL $D54C    ;

        // LD HL,$5800   ; colour data
        // LD BC,$0300   ; # bytes to clear
        // LD E,$46      ; bright yellow on black
        // JR $D53C      ;
        for(int hl = 0x5800; hl < 0x5800+0x0300; hl++) { mainMemory.setByteAt(hl, 0x46); }
        // JR $D544      ;
        for(int hl = 0x4000; hl < 0x4000+0x1800; hl++) { mainMemory.setByteAt(hl, 0); }
    }

    private void do_menu_selection_BD0C() {
        debugPanel1.append("do_menu_selection_BD0C");
        // XOR A         ;
        // LD ($5BB8),A  ;
        variables.set(0x5BB8, 0);
        // LD HL,$BDA2   ;
        // LD B,$08      ; # menu entries

        //; reset flashing attribute
        //@label=loc_BD15
        // RES 7,(HL)    ;
        // INC HL        ; next menu colour entry
        // DJNZ $BD15    ; {loop until done
        for (int hl = 0xBDA2; hl < 0xBDA2 + 8; hl++) { menu_colours_BDA2.set(hl, menu_colours_BDA2.get(hl) | 0x7F); }
        //CALL $D567    ;
        for (int hl = 0xD8F3; hl < 0xD8F3 + 0x1800; hl++) { shadowMemory.setByteAt(hl, 0); }
        // CALL $BEB3    ;
        display_menu_BEB3();
    }

    private void display_menu_BEB3() {
        debugPanel1.append("display_menu_BEB3");
        for(int de1 = 0xBDA2; de1 < 0xBDA2+8; de1++) {
            int hl2 = 0xBDAA;
            int de2 = 0xBDBA;
            variables.set(0x5BB6, menu_colours_BDA2.get(de1));
            int h = menu_xy_BDAA.get(hl2);
            int l = menu_xy_BDAA.get(hl2+1);
            hl2 += 2;

        }
    }


    private void printLookupTable() {
        debugTable("Lookup table:", 0xF100, lookupTable.getCopy());
    }

    private void printVariables() {
        debugTable("Variables:", 0x5BA0, variables.getCopy());
    }

    private void printVideoMemory() {
        debugTable("Video (main):", 0x4000, mainMemory.getCopy());
    }

    private void printShadowMemory() {
        debugTable("Video (shadow):", 0x4000, shadowMemory.getCopy());
    }

    private void debugTable(String title, int offset, int[] table) {
        StringBuilder t= new StringBuilder();
        t.append(title);
        for(int i = 0; i < table.length; i++) {
            if((i % 16) == 0) {
                debugPanel2.append(t.toString());
                t = new StringBuilder(String.format("%04x:", i + offset));
            }
            t.append(" ").append(String.format("%02x", table[i]));
        }
        debugPanel2.append(t.toString());
    }
}

class InitialData {
    public static final DataBlock flags12_1_D16D = new DataBlock(0xD16D, new int[] {0, 0, 0, 0});
    public static final DataBlock menu_colours_BDA2 = new DataBlock(0xBDA2, new int[] {0x43,0xC4,0x44,0x44,0x44,0x45,0x47,0x47});
    public static final DataBlock menu_xy_BDAA = new DataBlock(0xBDAA, new int[] {0x58,0x9F,0x30,0x8F,0x30,0x7F,0x30,0x6F,0x30,0x5F,0x30,0x4F,0x30,0x3F,0x50,0x27});
    public static final DataBlock menu_text_BDBA = new DataBlock(0xBDBA, new int[] {
            0x14,0x17,0x12,0x10,0x11,0x1D,0x26,0x15,0x18,0x1B,0x8E,
            0x01,0x26,0x14,0x0E,0x22,0x0B,0x18,0x0A,0x1B,0x8D,
            0x02,0x26,0x14,0x0E,0x16,0x19,0x1C,0x1D,0x18,0x17,0x26,0x13,0x18,0x22,0x1C,0x1D,0x12,0x0C,0x94,
            0x03,0x26,0x0C,0x1E,0x1B,0x1C,0x18,0x1B,0x26,0x26,0x26,0x13,0x18,0x22,0x1C,0x1D,0x12,0x0C,0x94,
            0x04,0x26,0x12,0x17,0x1D,0x0E,0x1B,0x0F,0x0A,0x0C,0x0E,0x26,0x12,0x92,
            0x05,0x26,0x0D,0x12,0x1B,0x0E,0x0C,0x1D,0x12,0x18,0x17,0x0A,0x15,0x26,0x0C,0x18,0x17,0x1D,0x1B,0x18,0x95,
            0x00,0x26,0x1C,0x1D,0x0A,0x1B,0x1D,0x26,0x10,0x0A,0x16,0x8E,
            0x25,0x26,0x01,0x09,0x08,0x04,0x26,0x0A,0x24,0x0C,0x24,0x10,0xA4});
}
