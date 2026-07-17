package org.kr;

import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Game implements Runnable {
    private final ScreenPanel mainPanel;
    private final ScreenPanel shadowPanel;
    private final DebugPanel debugPanel1;
    private final DebugPanel debugPanel2;
    private final ConcurrentLinkedQueue<Integer> keyQueue;

    // $4000-$57FF - spectrum video memory
    // $5800-$5AFF - spectrum attribute memory
    private final VideoMemoryScreen mainMemory;
    // $D8F3-$F0F2 - video buffer
    private final VideoMemoryLinear shadowMemory;
    // $5BA0-$6107 - variables
    // NOTE: variables and other memory locations are treated as ints, not bytes due to lack of unsigned byte type in java
    private final DataBlock variables = new DataBlock(0x5BA0, 0x6107 - 0x5BA0 + 1);
    private final DataBlock lookupTable = new DataBlock(0xF100, 0xFFFF - 0xF100 + 1);
    private final DataBlock menu_colours_BDA2 = InitialData.block("menu_colours_BDA2");
    private final DataBlock menu_xy_BDAA = InitialData.block("menu_xy_BDAA");
    private final DataBlock menu_text_BDBA = InitialData.block("menu_text_BDBA");
    private final DataBlock font_6108 = InitialData.block("font_6108");
    private final DataBlock sprite_scratchpad_BFDB = InitialData.block("sprite_scratchpad_BFDB");
    private final DataBlock border_data_D2CF = InitialData.block("border_data_D2CF");
    private final DataBlock sprite_tbl_7112 = InitialData.block("sprite_tbl_7112");
    private final DataBlock sprite_graphics_data_728A = InitialData.block("sprite_graphics_data_728A");
    private final DataBlock objects_required_C27D =  InitialData.block("objects_required_C27D");
    private final DataBlock plyr_spr_1_scratchpad_D161 =  InitialData.block("plyr_spr_1_scratchpad_D161");
    private final DataBlock start_loc_1_D169 =  InitialData.block("start_loc_1_D169");
    private final DataBlock flags12_1_D16D =  InitialData.block("flags12_1_D16D");
    private final DataBlock byte_D171 =  InitialData.block("byte_D171");
    private final DataBlock plyr_spr_2_scratchpad_D181 =  InitialData.block("plyr_spr_2_scratchpad_D181");
    private final DataBlock start_loc_2_D189 =  InitialData.block("start_loc_2_D189");
    private final DataBlock byte_D191 =  InitialData.block("byte_D191");
    private final DataBlock plyr_spr_init_data_D1A1 =  InitialData.block("plyr_spr_init_data_D1A1");
    private final DataBlock start_locations_D1E2 =  InitialData.block("start_locations_D1E2");



    // Repaint every fixed interval
    final long repaintIntervalMs = 50;
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            updateMainMemory();
            updateShadowMemory();
            //printKeys();
        }
    };

    public Game(ScreenPanel mainPanel, ScreenPanel shadowPanel, DebugPanel debugPanel1, DebugPanel debugPanel2,
                ConcurrentLinkedQueue<Integer> keyQueue) {
        this.mainPanel = mainPanel;
        this.shadowPanel = shadowPanel;
        this.debugPanel1 = debugPanel1;
        this.debugPanel2 = debugPanel2;
        this.keyQueue = keyQueue;

        this.debugPanel1.append("Start");
        this.debugPanel2.append("Start");

        mainMemory = new VideoMemoryScreen(0x4000);
        shadowMemory = new VideoMemoryLinear(0xD8F3);

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

    private void printKeys() {
        if(keyQueue.isEmpty()) return;

        Integer key = keyQueue.poll();
        IO.println("Key: " + key);
    }


    @Override
    public void run() {
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
        //; Data block at 5BC7
        // @label=gfxbase_8x8
        // b$5BC7 DEFB $08,$61
        variables.set(0x5BC7, 0x08);
        variables.set(0x5BC8, 0x61);
        // @label=user_input_method
        // b$5BA4 DEFS $01
        variables.set(0x5BA4, 1);
        //; Data block at 5BB8
        //@label=suppress_border
        //b$5BB8 DEFB $01
        variables.set(0x5BB8, 1);
        //@label=old_input_method
        //b$5BA6 DEFS $01
        //$5BA7 DEFS $01
        variables.set(0x5BA6, 1);
        variables.set(0x5BA7, 1);


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
        menu_loop_BD23(); // returns when game starts

        debugPanel2.append("START THE GAME");

        // LD DE,$B20E   ; }
        // CALL $B2CF    ; play tune // ignore audio
        // CALL $B544    ; randomise order of required objects
        shuffle_objects_required_B544();
        // CALL $D1B1    ; {randomise player start location
        init_start_location_D1B1();

    //printVariables();
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
        for(int hl = 0x5800; hl < 0x5800+0x0300; hl++) {
            mainMemory.setByteAt(hl, 0x46);
            shadowMemory.setByteAt(hl - mainMemory.start + shadowMemory.start, 0x46);
        }
        // JR $D544      ;
        for(int hl = 0x4000; hl < 0x4000+0x1800; hl++) {
            mainMemory.setByteAt(hl, 0);
            shadowMemory.setByteAt(hl - mainMemory.start + shadowMemory.start, 0);
        }
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
        for (int hl = 0xBDA2; hl < 0xBDA2 + 8; hl++) { menu_colours_BDA2.set(hl, menu_colours_BDA2.get(hl) & 0x7F); }
        //CALL $D567    ;
        for (int hl = 0xD8F3; hl < 0xD8F3 + 0x1800; hl++) { shadowMemory.setByteAt(hl, 0); }
        // CALL $BEB3    ;
        display_menu_BEB3();
        // CALL $BD89
        flash_menu_BD89();

        //@label=menu_loop
        // CALL $BEB3    ;
        display_menu_BEB3();
        // LD DE,$B253   ;
        // CALL $B2B6    ; ignore audio


    }

    private void display_menu_BEB3() {
        debugPanel1.append("display_menu_BEB3");
        int hl2 = 0xBDAA; // menu xy
        int de2 = 0xBDBA; // menu text
        for(int de1 = 0xBDA2; de1 < 0xBDA2+8; de1++) {
            variables.set(0x5BB6, menu_colours_BDA2.get(de1));
            debugPanel2.append("menu attrib: %02x".formatted(variables.get(0x5BB6))) ;
            int l = menu_xy_BDAA.get(hl2); // x - menu position
            int h = menu_xy_BDAA.get(hl2+1); // y
            hl2 += 2;
            de2 = print_text_single_colour_BE31(h, l, de1, de2);
        }
        int a = variables.get(0x5BB8); // suppress border
        if(a == 0) {
            variables.set(0x5BB8,1);
            print_border_D296();
            update_screen_D56F();
        }
    }

    private int print_text_single_colour_BE31(int h, int l,int de1, int de2) {
        // h: y, l: x, de1: attribute address, de2: text address (first)
        debugPanel1.append(String.format("print_text_single_colour_BE31 ( y: %02x, x: %02x, attr_addr: %02x, text_addr: %04x)", h, l, de1, de2));
        variables.set(0x5BC7, 0x08);
        variables.set(0x5BC8, 0x61);
        int bc = calc_vidbuf_addr_D811(h, l);
        debugPanel1.append(String.format("Video addr: %04x", bc));

        // for testing only
        //shadowMemory.setByteAt(bc, 255);
        //for(int i=0; i < 32*192; i++) mainMemory.setByteAt(i + mainMemory.start, 0xFF);
        //for(int i=0; i < 768; i++) shadowMemory.setByteAt(i + shadowMemory.start + 0x1800, Color.getAttribute(Color.WHITE, Color.BLUE, Color.NONE, Color.NONE));

        int hl = calc_attrib_addr_D848(h, l);

        // for testing only
        //mainMemory.setByteAt(hl, Color.getAttribute(Color.WHITE, Color.GREEN, Color.NONE, Color.NONE));

        boolean textDone = false;
        while(!textDone) {
            print_8x8_BE7F(menu_text_BDBA.get(de2), bc);
            mainMemory.setByteAt(hl, variables.get(0x5BB6)); // Color.getAttribute(Color.WHITE, Color.GREEN, Color.NONE, Color.NONE));
            shadowMemory.setByteAt(hl-mainMemory.start+shadowMemory.start, variables.get(0x5BB6));
            textDone = (menu_text_BDBA.get(de2) & 0x80) > 0;
            hl ++;
            de2 ++;
            bc ++;
        }
        return de2;
    }

    private int calc_vidbuf_addr_D811(int b, int c) {
        // b: y, c: x
        int xy = (c + b*256) >> 3;
        return xy + 0xD8F3;
    }

    private int calc_attrib_addr_D848(int h, int l) {
        // h: y, l: x
        int y = (h ^ 0xFF) >> 3;
        int xy = (y * 256 + l) >> 3;
        return xy + 0x5700;
    }

    private void print_8x8_BE7F(int a, int bc) {
        // a: character, bc: shadow memory address
        int ch = a & 0x7F;
        //debugPanel1.append("print_8x8_BE7F");
        int baseChar = variables.get(0x5BC7) + variables.get(0x5BC8)*256;
        int de2 = ch  * 8 + baseChar; // font address
        for(int b = 8; b>0; b--) {
            shadowMemory.setByteAt(bc, font_6108.get(de2));
            de2++;
            bc-=32;
        }
    }

    private void flash_menu_BD89() {
        debugPanel1.append("flash_menu_BD89");
        int hl = 0xBDA3; // first menu entry
        int a = variables.get(0x5BA4); // input method
        a = (a >> 1) & 0x3; // joystick / keyboard flag
        // CALL $BEA3
        for(int i = 0; i < 4; i++) {
            if(a == i) menu_colours_BDA2.set(hl+i, menu_colours_BDA2.get(hl+i) | 0x80 );
            else menu_colours_BDA2.set(hl+i, menu_colours_BDA2.get(hl+i) & 0x7F );
        }
        if((variables.get(0x5BA4) & 0x08)>0) menu_colours_BDA2.set(hl+4, menu_colours_BDA2.get(hl+4) | 0x80 );
        else menu_colours_BDA2.set(hl+5, menu_colours_BDA2.get(hl+5) & 0x7F );
    }

    private void print_border_D296() {
        debugPanel1.append("print_border_D296");
        int ix = 0xBFDB;
        int hl = 0xD2CF;
        // CALL $D24C x4
        hl = transfer_sprite_and_print_D24C(ix, hl, border_data_D2CF); // corners
        hl = transfer_sprite_and_print_D24C(ix, hl, border_data_D2CF);
        hl = transfer_sprite_and_print_D24C(ix, hl, border_data_D2CF);
        hl = transfer_sprite_and_print_D24C(ix, hl, border_data_D2CF);
        hl = transfer_and_multiple_print_sprite(8, 0, 0x18, ix, hl, border_data_D2CF); // horizontal lines
        hl = transfer_and_multiple_print_sprite(8, 0, 0x18, ix, hl, border_data_D2CF);
        hl = transfer_and_multiple_print_sprite(0, 1, 0x80, ix, hl, border_data_D2CF); // vertical lines
        transfer_and_multiple_print_sprite(0, 1, 0x80, ix, hl, border_data_D2CF);
    }

    private int transfer_sprite_and_print_D24C(int ix, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        debugPanel1.append("transfer_sprite_and_print_D24C");
        transfer_sprite_D237(ix, hl, source);
        print_sprite_D718(ix, hl);

        debugTable("Sprite scratchpad:", 0xBFD8, sprite_scratchpad_BFDB.getCopy());

        return hl+4;
    }

    // Populate sprite metadata
    private void transfer_sprite_D237(int ix, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        debugPanel1.append("transfer_sprite_D237 (hl: %02x, ix: %04x)".formatted(hl, ix));
        sprite_scratchpad_BFDB.set(ix, source.get(hl)); // sprite index
        sprite_scratchpad_BFDB.set(ix+0x07, source.get(hl+1)); // flags
        sprite_scratchpad_BFDB.set(ix+0x1A, source.get(hl+2)); // pixel X
        sprite_scratchpad_BFDB.set(ix+0x1B, source.get(hl+3)); // pixel Y
    }

    private void print_sprite_D718(int ix, int hl) {
        debugPanel1.append("print_sprite_D718 (hl: %02x, ix: %04x)".formatted(hl, ix));
        int de = flip_sprite_D6EF(ix);
        if(de == 0) return; // spr_null used as a flag to return from routine

        int x = sprite_scratchpad_BFDB.get(ix + 0x1A);
        int y = sprite_scratchpad_BFDB.get(ix + 0x1B);
        int xOffset = (x & 7);
        debugPanel2.append(xOffset == 0 ? "aligned" : "NOT aligned by %02x".formatted(xOffset));
        // JR Z,$D76F    ; {no, skip

        // LD ($D7AD),A   ; - self modifying relative address to unrolled loop depending on a (width in bytes)
        // we skip this part entirely, using inner for loop instead
        int a = sprite_graphics_data_728A.get(de);
        a = (a & 0x7); // width_bytes, ignore rotation flags
        de ++;
        sprite_scratchpad_BFDB.set(ix + 0x19,   sprite_graphics_data_728A.get(de)); // height_lines
        // off bottom of screen?
        if(y + sprite_scratchpad_BFDB.get(ix + 0x19)>0xC0) sprite_scratchpad_BFDB.set(ix + 0x19, 0xC0);
        de ++;
        int bc = calc_vidbuf_addr_D811(y, x);

        if(xOffset == 0) {
            //@label=loc_D76F
            sprite_scratchpad_BFDB.set(ix + 0x18, a);
            for(int lineNo = 0; lineNo<sprite_scratchpad_BFDB.get(ix + 0x19); lineNo++) {
                for (int i = 0; i < a; i++) {
                    int bufByte = shadowMemory.getByteAt(bc);
                    int e_mask = sprite_graphics_data_728A.get(de);
                    de++;
                    int d_spriteByte = sprite_graphics_data_728A.get(de);
                    de++;
                    bufByte = (bufByte & (e_mask ^ 0xFF));
                    bufByte = (bufByte | d_spriteByte) & 0xFF;
                    shadowMemory.setByteAt(bc, bufByte);
                    bc++;
                }
                bc+=(32-a);
            }
        } else {
            sprite_scratchpad_BFDB.set(ix + 0x18, a + 1);
            int lookupBase = 0xF000 + (xOffset << 9);
            for(int lineNo = 0; lineNo<sprite_scratchpad_BFDB.get(ix + 0x19); lineNo++) {
                // Shift each byte across two buffer bytes (left and right), use lookup tables to reflect the original logic
                for (int i = 0; i < a; i++) {
                    int bufByteLeft = shadowMemory.getByteAt(bc);
                    int bufByteRight = shadowMemory.getByteAt(bc+1);
                    int e_mask = sprite_graphics_data_728A.get(de);
                    de++;
                    int d_spriteByte = sprite_graphics_data_728A.get(de);
                    de++;
                    int e_maskLeft = (lookupTable.get(lookupBase + e_mask)) ^ 0xFF;
                    int e_maskRight = (lookupTable.get(lookupBase + 0x0100 + e_mask)) ^ 0xFF;
                    int d_spriteByteLeft = (lookupTable.get(lookupBase + d_spriteByte)) ^ 0xFF;
                    int d_spriteByteRight = (lookupTable.get(lookupBase + 0x0100 + d_spriteByte)) ^ 0xFF;
                    bufByteLeft = (bufByteLeft & (e_maskLeft ^ 0xFF));
                    bufByteLeft = (bufByteLeft | d_spriteByteLeft) & 0xFF;
                    shadowMemory.setByteAt(bc, bufByteLeft);
                    // Do not modify next line
                    if(((bc - shadowMemory.start) & 0x001F)!=0x001F) {
                        bufByteRight = (bufByteRight & (e_maskRight ^ 0xFF));
                        bufByteRight = (bufByteRight | d_spriteByteRight) & 0xFF;
                        shadowMemory.setByteAt(bc+1, bufByteRight);
                    }
                    bc++;
                }
                bc+=(32-a);
            }
        }
    }

    // Returns address of sprite graphics data or 0 if null sprite
    private int flip_sprite_D6EF(int ix) {
        debugPanel1.append("flip_sprite_D6EF (ix: %04x)".formatted(ix));
        int l = sprite_scratchpad_BFDB.get(ix);
        int hl = l * 2 + 0x7112; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
        int de = sprite_tbl_7112.get(hl) + sprite_tbl_7112.get(hl+1)*256; // sprite actual address
        debugPanel2.append("sprite address (DE): %04x".formatted(de));
        int width = sprite_graphics_data_728A.get(de);
        // returns sprite address or 0 if sprite is spr_null
        int flagsScratch = sprite_scratchpad_BFDB.get(ix + 0x07);
        boolean flipVScratch = (flagsScratch & 0x80) > 0;
        boolean flipHScratch = (flagsScratch & 0x40) > 0;
        int flagsSpriteData = sprite_graphics_data_728A.get(de) & 0xC0;
        boolean flipVData = (flagsSpriteData & 0x80) > 0;
        boolean flipHData = (flagsSpriteData & 0x40) > 0;

        // Flip vertically (in-place) and set flag in data
        if(flipVScratch != flipVData) {
            int lines = sprite_graphics_data_728A.get(de+1);
            int bytesInLine = (sprite_graphics_data_728A.get(de) & 0x07) * 2;
            for(int line = 0; line<(lines >> 1); line++) {
                for(int b = 0; b < bytesInLine; b++) {
                    int topByteAddress = de + 2 + line * bytesInLine + b;
                    int bottomByteAddress = de + 2 + (lines - line - 1) * bytesInLine + b;
                    int buffer = sprite_graphics_data_728A.get(topByteAddress);
                    sprite_graphics_data_728A.set(topByteAddress, sprite_graphics_data_728A.get(bottomByteAddress));
                    sprite_graphics_data_728A.set(bottomByteAddress, buffer);
                }
            }
            sprite_graphics_data_728A.set(de, sprite_graphics_data_728A.get(de) ^ 0x80);
        }
        // Flip horizontally (in-place) and set flag in data
        if(flipHScratch != flipHData) {
            int lines = sprite_graphics_data_728A.get(de+1);
            int bytesInLine = (sprite_graphics_data_728A.get(de) & 0x07) * 2;
            for(int line = 0; line<lines; line++) {
                for(int b = 0; b < (bytesInLine >> 1); b++) {
                    int leftByteAddress = de + 2 + line * bytesInLine + b;
                    int rightByteAddress = de + 2 + line * bytesInLine + (bytesInLine - b -1);
                    int leftBuffer = sprite_graphics_data_728A.get(leftByteAddress);
                    leftBuffer = lookupTable.get(0xF100 + leftBuffer); // F1xx - byte flip table
                    int rightBuffer = sprite_graphics_data_728A.get(rightByteAddress);
                    rightBuffer = lookupTable.get(0xF100 + rightBuffer);
                    sprite_graphics_data_728A.set(leftByteAddress, rightBuffer);
                    sprite_graphics_data_728A.set(rightByteAddress, leftBuffer);
                }
            }
            sprite_graphics_data_728A.set(de, sprite_graphics_data_728A.get(de) ^ 0x40);
        }
        return width == 0 ? 0 : de;
    }

    private int transfer_and_multiple_print_sprite(int dx, int dy, int times, int ix, int hl, DataBlock dataBlock) {
        transfer_sprite_D237(ix, hl, dataBlock);
        return multiple_print_sprite_BEE4(dx, dy, times, ix, hl);
    }

    private int multiple_print_sprite_BEE4(int dx, int dy, int times, int ix, int hl) {
        for(int i=0; i<times; i++) {
            print_sprite_D718(ix, hl);
            sprite_scratchpad_BFDB.set(ix + 0x1A, sprite_scratchpad_BFDB.get(ix + 0x1A) + dx);
            sprite_scratchpad_BFDB.set(ix + 0x1B, sprite_scratchpad_BFDB.get(ix + 0x1B) + dy);
        }
        return hl+4;
    }

    private void update_screen_D56F() {
        int bytesX = VideoMemory.WIDTH / 8;
        for(int bufferY = 0; bufferY<VideoMemory.HEIGHT; bufferY++) {
            int screenY = ((bufferY & 0b111) <<3) + ((bufferY & 0b111000) >> 3) + (bufferY & 0b11000000);
            int screenBase = screenY * bytesX + mainMemory.start;
            int bufferBase = (VideoMemory.HEIGHT - bufferY -1) * bytesX + shadowMemory.start;
            for(int b = 0; b<VideoMemory.WIDTH / 8; b++) {
                mainMemory.setByteAt(screenBase+b, shadowMemory.getByteAt(bufferBase+b));
                shadowMemory.setByteAt(bufferBase+b, 0); // wipe buffer
            }
        }
        // wipe buffer attributes (non-existent in original game)
        for(int attr = shadowMemory.start + VideoMemory.PIXEL_MEM_SIZE;
            attr<shadowMemory.start + VideoMemory.PIXEL_MEM_SIZE + VideoMemory.HEIGHT /8 * VideoMemory.WIDTH / 8; attr++) {
            shadowMemory.setByteAt(attr, 0);
        }
    }

    // exiting means game starts
    private void menu_loop_BD23() {
        debugPanel1.append("menu_loop_BD23");
        // @label=menu_loop

        boolean startGame = false;
        while(!startGame) {
            if (!keyQueue.isEmpty()) {
                int a = variables.get(0x5BA4);
                variables.set(0x5BA6, a);
                Integer key = keyQueue.poll();
                IO.println("Key: " + key);
                // 1,2,3,4,5
                if (key == KeyEvent.VK_1) a &= 0xF9;
                if (key == KeyEvent.VK_2) {
                    a &= 0xF9;
                    a |= 2;
                }
                if (key == KeyEvent.VK_3) {
                    a &= 0xF9;
                    a |= 4;
                }
                if (key == KeyEvent.VK_4) a |= 6;
                //variables.set(0x5BA4, a);
                if (key == KeyEvent.VK_5) a ^= 0x08; //; toggle directional
                variables.set(0x5BA4, a);
                // CALL NZ,$B4A3 ; yes // ignore audio
                do_menu_selection_BD0C();
                if (key == KeyEvent.VK_0) startGame = true;
                //Do not change seed to make game deterministic during development
                //variables.set(0x5BA0, variables.get(0x5BA0)+1); // increase seed
            }
        }
    }

    private void shuffle_objects_required_B544() {
        debugTable("Required objects:", objects_required_C27D.start, objects_required_C27D.getCopy());
        int a = variables.get(0x5BA0); //seed 1
        a = (a & 3) | 4; // random number (assuming seed is random)
        for(int c = a; c>0; c--) {
            int iy = objects_required_C27D.start;
            for (int b = 0x0D; b > 0; b--) {
                int buf = objects_required_C27D.get(iy);
                objects_required_C27D.set(iy, objects_required_C27D.get(iy+1));
                objects_required_C27D.set(iy+1, buf);
                iy++;
            }
        }
        //debugTable("Required objects:", objects_required_C27D.start, objects_required_C27D.getCopy());
    }

    private void init_start_location_D1B1() {
        for(int i=0; i<8; i++)
            plyr_spr_1_scratchpad_D161.set(plyr_spr_1_scratchpad_D161.start+i,
                    plyr_spr_init_data_D1A1.get(plyr_spr_init_data_D1A1.start+i));
        for(int i=0; i<8; i++)
            plyr_spr_2_scratchpad_D181.set(plyr_spr_2_scratchpad_D181.start+i,
                    plyr_spr_init_data_D1A1.get(plyr_spr_init_data_D1A1.start+i+8));
        // LD A,$12      ; graphic_no (player top half)
        // LD ($D171),A  ; plyr_spr_1_scratchpad (byte 16)
        byte_D171.set(0xD171, 0x12);
        // LD A,$22      ; graphic_no (player bottom half)
        // LD ($D191),A  ; {plyr_spr_2_scratchpad (byte 16)
        byte_D191.set(0xD191, 0x22);
        int a = variables.get(0x5BA0) & 0x3; // random
        int randomLoc = start_locations_D1E2.get(0xD1E2 + a);
        start_loc_1_D169.set(0xD169, randomLoc);
        start_loc_2_D189.set(0xD189, randomLoc);
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

