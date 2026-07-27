package org.kr;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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
    private final DataBlock variables = new DataBlock(0x5BA0, 0x5BE8 - 0x5BA0 + 1);
    private final DataBlock scrn_visited_5BE8 = new DataBlock(0x5BE8, 0x20);
    private final DataBlock lookupTable = new DataBlock(0xF100, 0xFFFF - 0xF100 + 1);
    private final DataBlock menu_colours_BDA2 = InitialData.block("menu_colours_BDA2");
    private final DataBlock menu_xy_BDAA = InitialData.block("menu_xy_BDAA");
    private final DataBlock menu_text_BDBA = InitialData.block("menu_text_BDBA");
    private final DataBlock font_6108 = InitialData.block("font_6108");
    private final DataBlock sprite_scratchpad_BFDB = InitialData.block("sprite_scratchpad_BFDB");
    private final DataBlock border_data_D2CF = InitialData.block("border_data_D2CF");
    private final DataBlock special_objs_tbl_6FF2 = InitialData.block("special_objs_tbl_6FF2");
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
    private final DataBlock sun_moon_scratchpad_C44D =  InitialData.block("sun_moon_scratchpad_C44D");
    private final DataBlock graphic_objs_tbl_5C08 = new DataBlock(0x5C08, 0x40);
    private final DataBlock special_objs_here_5C48 = new DataBlock(0x5C48, 0x40);
    //TODO: replace data_block_5CA8 with other_objs_here_5C88
    private final DataBlock other_objs_here_5C88 = new DataBlock(0x5C88, 0x20 + 0x0460);
    private final DataBlock data_block_5CA8 = new DataBlock(0x5CA8, 0x0460);
    private final DataBlock location_tbl_6251 = InitialData.block("location_tbl_6251");
    private final DataBlock room_size_tbl_6248 = InitialData.block("room_size_tbl_6248");
    //private final DataBlock background_type_tbl_6CE2 = InitialData.block("background_type_tbl_6CE2");
    private final DataBlock[] backgroundObjects = new DataBlock[]{
            InitialData.block("arch_n_6D12"),
            InitialData.block("arch_e_6D23"),
            InitialData.block("arch_s_6D45"),
            InitialData.block("arch_w_6D67"),
            InitialData.block("tree_arch_n_6D78"),
            InitialData.block("tree_arch_e_6D89"),
            InitialData.block("tree_arch_s_6D9A"),
            InitialData.block("tree_arch_w_6DAB"),
            InitialData.block("gate_n_6DBC"),
            InitialData.block("gate_e_6DC5"),
            InitialData.block("gate_s_6DCE"),
            InitialData.block("gate_w_6DD7"),
            InitialData.block("wall_size_1_6DE0"),
            InitialData.block("wall_size_2_6E49"),
            InitialData.block("wall_size_3_6EBA"),
            InitialData.block("tree_room_size_1_6F2B"),
            InitialData.block("tree_filler_w_6F8C"),
            InitialData.block("tree_filler_n_6F9D"),
            InitialData.block("wizard_6FAE"),
            InitialData.block("cauldron_6FBF"),
            InitialData.block("high_arch_e_6D34"),
            InitialData.block("high_arch_s_6D56"),
            InitialData.block("high_arch_e_base_6FD0"),
            InitialData.block("high_arch_s_base_6FE1")
    };
    //private final DataBlock block_type_tbl_6BD1 = InitialData.block("block_type_tbl_6BD1");
    private final DataBlock[] foregroundObjects = new DataBlock[]{
            InitialData.block("block_6C0B"),
            InitialData.block("fire_6C3C"),
            InitialData.block("ball_ud_y_6C43"),
            InitialData.block("rock_6C66"),
            InitialData.block("gargoyle_6C6D"),
            InitialData.block("spike_6C74"),
            InitialData.block("chest_6C90"),
            InitialData.block("table_6C97"),
            InitialData.block("guard_ew_6C9E"),
            InitialData.block("ghost_6CB8"),
            InitialData.block("fire_ns_6CBF"),
            InitialData.block("block_high_6C12"),
            InitialData.block("ball_ud_xy_6C4A"),
            InitialData.block("guard_square_6CAB"),
            InitialData.block("block_ew_6C19"),
            InitialData.block("block_ns_6C20"),
            InitialData.block("moveable_block_6C27"),
            InitialData.block("spike_high_6C7B"),
            InitialData.block("spike_ball_fall_6C82"),
            InitialData.block("spike_ball_high_fall_6C89"),
            InitialData.block("fire_ew_6CC6"),
            InitialData.block("dropping_block_6C2E"),
            InitialData.block("collapsing_block_6C35"),
            InitialData.block("ball_bounce_6C5F"),
            InitialData.block("ball_ud_6C51"),
            InitialData.block("repel_spell_6CCD"),
            InitialData.block("gate_ud_1_6CD4"),
            InitialData.block("gate_ud_2_6CDB"),
            InitialData.block("ball_ud_x_6C58")
    };

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
        variables.set(0x5BA2, 0);
        variables.set(0x5BA3, 0);
        //; Data block at 5BC7
        // @label=gfxbase_8x8
        // b$5BC7 DEFB $08,$61
        variables.set(0x5BC7, 0x08);
        variables.set(0x5BC8, 0x61);
        // @label=user_input_method
        // b$5BA4 DEFS $01
        variables.set(0x5BA4, 0);
        //; Data block at 5BB8
        //@label=suppress_border
        //b$5BB8 DEFB $01
        variables.set(0x5BB8, 1);
        //@label=old_input_method
        //b$5BA6 DEFS $01
        //$5BA7 DEFS $01
        variables.set(0x5BA6, 0);
        variables.set(0x5BA7, 0);
        // @label=transform_flag_graphic
        // b$5BB1 DEFS $01
        variables.set(0x5BB1, 0);



        int v5C78 = 0x65; // originally taken from 5C78 (LSB of FRAMES 3-byte system variable). It is incremented by ROM interrupt routine, servers as random seed
        // PUSH AF       ;
        // CALL $D53A    ;
        // POP AF        ;
        variables.reset();
        scrn_visited_5BE8.reset();
        graphic_objs_tbl_5C08.reset();
        special_objs_here_5C48.reset();
        other_objs_here_5C88.reset();

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
        menu_loop_BD23(true); // returns when game starts

        debugPanel2.append("START THE GAME");

        // LD DE,$B20E   ; }
        // CALL $B2CF    ; play tune // ignore audio
        // CALL $B544    ; randomise order of required objects
        shuffle_objects_required_B544();
        // CALL $D1B1    ; {randomise player start location

        int a = location_tbl_6251.start;
        while(a < location_tbl_6251.endExcl()) {
            int id = location_tbl_6251.get(a);
            IO.println("Room id: "+id);


            init_start_location_D1B1(id);
            // CALL $C46D    ; }
            init_sun_C46D();
            // CALL $C47E    ; randomise special object locations
            init_special_objects_C47E();
            // @label=player_dies
            // CALL $D12A    ;
            player_dies_AFB7();
            game_loop_AFBA();

            try {
                updateShadowMemory();
                shadowPanel.saveImage("images/location_%03d_%02x.png".formatted(start_loc_1_D169.get(0xD169),start_loc_1_D169.get(0xD169)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            int size = location_tbl_6251.get(a+1);
            a+=size+1;
        }

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
        clear_scrn_buffer_D567();
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

    private void clear_scrn_buffer_D567() {
        for (int hl = 0xD8F3; hl < 0xD8F3 + 0x1800; hl++) { shadowMemory.setByteAt(hl, 0); }
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
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, ix, hl, border_data_D2CF); // corners
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, ix, hl, border_data_D2CF);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, ix, hl, border_data_D2CF);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, ix, hl, border_data_D2CF);
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 8, 0, 0x18, ix, hl, border_data_D2CF); // horizontal lines
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 8, 0, 0x18, ix, hl, border_data_D2CF);
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 0, 1, 0x80, ix, hl, border_data_D2CF); // vertical lines
        transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 0, 1, 0x80, ix, hl, border_data_D2CF);
    }

    private int transfer_sprite_and_print_D24C(DataBlock metadata, int ix, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        debugPanel1.append("transfer_sprite_and_print_D24C");
        transfer_sprite_D237(metadata, ix, hl, source);
        print_sprite_D718(metadata, ix);

        return hl+4;
    }

    // Populate sprite metadata
    private void transfer_sprite_D237(DataBlock medatada, int ix, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        debugPanel1.append("transfer_sprite_D237 (hl: %02x, ix: %04x)".formatted(hl, ix));
        medatada.set(ix, source.get(hl)); // sprite index
        medatada.set(ix+0x07, source.get(hl+1)); // flags
        medatada.set(ix+0x1A, source.get(hl+2)); // pixel X
        medatada.set(ix+0x1B, source.get(hl+3)); // pixel Y
    }

    private void print_sprite_D718(DataBlock metadata, int ix) {
        int de = flip_sprite_D6EF(metadata, ix);
        if(de == 0) return; // spr_null used as a flag to return from routine

        int x = metadata.get(ix + 0x1A);
        int y = metadata.get(ix + 0x1B);
        int xOffset = (x & 7);
        debugPanel2.append(xOffset == 0 ? "aligned" : "NOT aligned by %02x".formatted(xOffset));
        // JR Z,$D76F    ; {no, skip

        // LD ($D7AD),A   ; - self modifying relative address to unrolled loop depending on a (width in bytes)
        // we skip this part entirely, using inner for loop instead
        int a = sprite_graphics_data_728A.get(de);
        a = (a & 0x7); // width_bytes, ignore rotation flags
        de ++;
        metadata.set(ix + 0x19,   sprite_graphics_data_728A.get(de)); // height_lines
        // off bottom of screen?
        if(y + metadata.get(ix + 0x19)>0xC0) metadata.set(ix + 0x19, 0xC0 - y);
        de ++;
        int bc = calc_vidbuf_addr_D811(y, x);

        if(xOffset == 0) {
            //@label=loc_D76F
            metadata.set(ix + 0x18, a);
            for(int lineNo = 0; lineNo<metadata.get(ix + 0x19); lineNo++) {
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
            metadata.set(ix + 0x18, a + 1);
            int lookupBase = 0xF000 + (xOffset << 9);
            for(int lineNo = 0; lineNo<metadata.get(ix + 0x19); lineNo++) {
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
    private int flip_sprite_D6EF(DataBlock metadata, int ix) {
        debugPanel1.append("flip_sprite_D6EF (ix: %04x)".formatted(ix));
        int l = metadata.get(ix);
        int hl = l * 2 + 0x7112; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
        int de = sprite_tbl_7112.get(hl) + sprite_tbl_7112.get(hl+1)*256; // sprite actual address
        debugPanel2.append("sprite address (DE): %04x".formatted(de));
        int width = sprite_graphics_data_728A.get(de);
        // returns sprite address or 0 if sprite is spr_null
        int flagsScratch = metadata.get(ix + 0x07);
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
                int firstByteAddress = de + 2 + line * bytesInLine;
                int lastByteAddress = firstByteAddress + bytesInLine - 1;
                int bytePairs = bytesInLine >> 1;
                for(int b = 0; b < bytePairs; b++) {
                    boolean isMask = (b & 1) == 0;
                    // Middle pair means that we have off number of data bytes so we must only flip bits and leave bytes in place
                    boolean isMiddlePair = ((bytePairs & 1) == 1) && (b == bytePairs - 1);
                    int shift = isMiddlePair ? 0 : (isMask ? -1 : 1);
                    // NOTE: the data bytes and mask bytes must not be mixed
                    int leftByteAddress = firstByteAddress + b;
                    int rightByteAddress = lastByteAddress - b + shift;
                    int leftBuffer = sprite_graphics_data_728A.get(leftByteAddress);
                    leftBuffer = lookupTable.get(0xF100 + leftBuffer); // F1xx - byte flip table
                    int rightBuffer = sprite_graphics_data_728A.get(rightByteAddress);
                    rightBuffer = lookupTable.get(0xF100 + rightBuffer);
                    sprite_graphics_data_728A.set(leftByteAddress, isMiddlePair ? leftBuffer : rightBuffer);
                    sprite_graphics_data_728A.set(rightByteAddress, isMiddlePair ? rightBuffer : leftBuffer);
                }
            }
            sprite_graphics_data_728A.set(de, sprite_graphics_data_728A.get(de) ^ 0x40);
        }
        return width == 0 ? 0 : de;
    }

    private int transfer_and_multiple_print_sprite(DataBlock metadata, int dx, int dy, int times, int ix, int hl, DataBlock dataBlock) {
        transfer_sprite_D237(metadata, ix, hl, dataBlock);
        return multiple_print_sprite_BEE4(metadata, dx, dy, times, ix, hl);
    }

    private int multiple_print_sprite_BEE4(DataBlock metadata, int dx, int dy, int times, int ix, int hl) {
        for(int i=0; i<times; i++) {
            print_sprite_D718(metadata, ix);
            metadata.set(ix + 0x1A, metadata.get(ix + 0x1A) + dx);
            metadata.set(ix + 0x1B, metadata.get(ix + 0x1B) + dy);
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
    private void menu_loop_BD23(boolean skip) {
        debugPanel1.append("menu_loop_BD23");
        // @label=menu_loop

        boolean startGame = false;
        if(skip) keyQueue.add(KeyEvent.VK_0);
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
        //debugTable("Required objects:", objects_required_C27D.start, objects_required_C27D.getCopy());
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

    private void init_start_location_D1B1(int override) {
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
        // For testing only
        if(override != -1) randomLoc = override;

        start_loc_1_D169.set(0xD169, randomLoc);
        start_loc_2_D189.set(0xD189, randomLoc);
    }

    private void init_sun_C46D() {
        sun_moon_scratchpad_C44D.set(0xC44D, 0x58); // ; sprite index
        sun_moon_scratchpad_C44D.set(0xC44D+0x1A, 0xB0); // ; pixel X
        sun_moon_scratchpad_C44D.set(0xC44D+0x1B, 0x09); // ; pixel Y
    }

    private void init_special_objects_C47E() {
        int rnd = variables.get(0x5BA0) & 0x07; // random
        int hl = special_objs_tbl_6FF2.start;
        while(hl < special_objs_tbl_6FF2.endExcl()) {
            rnd = (rnd & 0x07) | 0x60; // set numbers between 0x60 and 0x67 starting from random number to special object indexes
            special_objs_tbl_6FF2.set(hl, rnd);
            hl++;
            // copy coords from "start" to "current"
            for(int i=0; i<4; i++) special_objs_tbl_6FF2.set(hl+i+4, special_objs_tbl_6FF2.get(hl+i));
            hl+=8;
            rnd++;
        }
        debugTable("Special objects:", special_objs_tbl_6FF2.start, special_objs_tbl_6FF2.getCopy());
    }

    private void player_dies_AFB7() {
        lose_life_$D12A();
    }

    private void lose_life_$D12A() {
        int de = graphic_objs_tbl_5C08.start;
        for(int i=0; i<plyr_spr_1_scratchpad_D161.size; i++) graphic_objs_tbl_5C08.set(de+i, plyr_spr_1_scratchpad_D161.get(plyr_spr_1_scratchpad_D161.start+i));
        de+=plyr_spr_1_scratchpad_D161.size;
        for(int i=0; i<start_loc_1_D169.size; i++) graphic_objs_tbl_5C08.set(de+i, start_loc_1_D169.get(start_loc_1_D169.start+i));
        de+=start_loc_1_D169.size;
        for(int i=0; i<flags12_1_D16D.size; i++) graphic_objs_tbl_5C08.set(de+i, flags12_1_D16D.get(flags12_1_D16D.start+i));
        de+=flags12_1_D16D.size;
        for(int i=0; i<byte_D171.size; i++) graphic_objs_tbl_5C08.set(de+i, byte_D171.get(byte_D171.start+i));
        de+=byte_D171.size;
        for(int i=0; i<plyr_spr_2_scratchpad_D181.size; i++) graphic_objs_tbl_5C08.set(de+i, plyr_spr_2_scratchpad_D181.get(plyr_spr_2_scratchpad_D181.start+i));
        de+=plyr_spr_2_scratchpad_D181.size;
        for(int i=0; i<start_loc_2_D189.size; i++) graphic_objs_tbl_5C08.set(de+i, start_loc_2_D189.get(start_loc_2_D189.start+i));
        de+=start_loc_2_D189.size;
        for(int i=0; i<byte_D191.size; i++) graphic_objs_tbl_5C08.set(de+i, byte_D191.get(byte_D191.start+i));

        variables.set(0x5BB1, 0);
        int livesLeft = variables.get(0x5BBA) - 1;
        variables.set(0x5BBA, livesLeft);
        if(livesLeft < 0) {
            game_over_BA22();
            return;
        }
        int a = sun_moon_scratchpad_C44D.get(0xC44D);
        int dayNight = (a >> 3) & 0x20; // ; day/night?
        int playerGraphicsNo = graphic_objs_tbl_5C08.get(0x5C08+0x10);
        playerGraphicsNo = (playerGraphicsNo & 0x1F) + dayNight;
        graphic_objs_tbl_5C08.set(0x5C08+0x10, playerGraphicsNo);
        int playerGraphicsNoTop = graphic_objs_tbl_5C08.get(0x5C08+0x30);
        playerGraphicsNoTop = (playerGraphicsNoTop & 0xF0) + dayNight + 0x20;
        graphic_objs_tbl_5C08.set(0x5C08+0x30, playerGraphicsNoTop);
    }

    private void game_over_BA22() {
        debugPanel2.append("Game over BA22\n");
        //TODO: implement
    }

    private void game_loop_AFBA() {
        debugPanel2.append("Game loop AFBA\n");
        build_screen_objects_D1E6();
        // @label=onscreen_loop
        variables.set(0x5BA2, variables.get(0x5BBC));
        update_sprite_loop_AFC7(other_objs_here_5C88);
        /*int ix = graphic_objs_tbl_5C08.start; // NOTE: player's sprites data
        save_2d_info_CE49(ix, graphic_objs_tbl_5C08);

        print_sprite_D718(graphic_objs_tbl_5C08, ix);*/
        // This is all just to verify if objects render at all
        //renderAllSprites(graphic_objs_tbl_5C08, 0, 2);
        //renderAllSprites(special_objs_here_5C48, 0,2);
        renderAllSprites(other_objs_here_5C88, 0,36);
        //debugTable("Other objects (after render):", other_objs_here_5C88.start, other_objs_here_5C88.getCopy());
        // set attributes so the buffer contents are visible
        for(int i=0; i < 768; i++) shadowMemory.setByteAt(i + shadowMemory.start + 0x1800, Color.getAttribute(Color.WHITE, Color.BLUE, Color.NONE, Color.NONE));

    }

    private void update_sprite_loop_AFC7(DataBlock block) {
        // block contains sprite metadata (32 bytes each)
        int ix = block.start;
        while(ix < block.endExcl()) {
            updateOneSprite(block, ix);
            ix+=32;
        }
    }

    private void updateOneSprite(DataBlock block, int ix) {
        save_2d_info_CE49(ix, block);
        // @label=upd_sprite_jmp_tbl
        switch(block.get(ix)) {
            case 0x00, 0x01, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0xBA: break;
            case 0x02, 0x04: upd_2_4_C73C(block, ix); break;
            case 0x03, 0x05: upd_3_5_C722(block, ix); break;
            case 0x06, 0x07: upd_6_7_C4E3(block, ix); break;
            case 0x08: upd_8_C65E(block, ix); break;
            case 0x09: upd_9_C6BD(block, ix); break;
            case 0x0A: upd_10_C4E8(block, ix); break;
            case 0x0B: upd_11_C4ED(block, ix); break;
            case 0x0C, 0x0D, 0x0E, 0x0F: upd_12_to_15_C4F2(block, ix); break;
            case 0x16: upd_22_B7A3(block, ix); break;
            case 0x17: upd_23_B7E7(block, ix); break;
            case 0x1E, 0x1F, 0x9E, 0x9F: upd_30_31_158_159_B9A5(block, ix); break;
            case 0x36: upd_54_B6B9(block, ix); break;
            case 0x37: upd_55_B6B1(block, ix); break;
            case 0x3E: upd_62_C4AA(block, ix); break;
            case 0x3F: upd_63_B7A9(block, ix); break;
            case 0x52, 0x53, 0x54, 0x55: upd_80_to_83_C5C8(block, ix); break;
            case 0x56, 0x57: upd_86_87_B7ED(block, ix); break;
            case 0x5B: upd_91_B683(block, ix); break;
            case 0x80, 0x81, 0x82: upd_128_to_130_C4D3(block, ix); break;
            case 0x8D: upd_141_B99C(block, ix); break;
            case 0x8E: upd_142_B99F(block, ix); break;
            case 0x8F: upd_143_B6A2(block, ix); break;
            case 0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D: upd_144_to_149_152_to_157_B6F9(block, ix); break;
            case 0x96, 0x97: upd_150_151_B73C(block, ix); break;
            case 0xA4, 0xA5, 0xA6, 0xA7: upd_164_to_167_B92C(block, ix); break;
            case 0xB2, 0xB3: upd_178_179_B865(block, ix); break;
            case 0xB4, 0xB5: upd_180_181_B80F(block, ix); break;
            case 0xB6, 0xB7: upd_182_183_B5FF(block, ix); break;

            default: IO.println("Not updated: %02x (%d)".formatted(block.get(ix),block.get(ix))); break;
        }
    }

    private void upd_2_4_C73C(DataBlock block, int ix) {
        boolean hFlip = (block.get(ix + 7) & 0x40) > 0; // BIT 6,(IX+$07)
        if(hFlip) {
            // LD HL,$FEEF
            block.set(ix + 0x12, -17); // L=x - EF
            block.set(ix + 0x13, -2); // H=y - FE
            int x = block.get(ix + 1) - 0x0D;
            block.set(ix + 9, x); // dX
            int y = block.get(ix + 2);
            block.set(ix + 0x0A, y); // dY=Y
            // LD HL,$0F06   ; +15, +6
            int z = block.get(ix + 3);
            block.set(ix + 0x0B, z); // dZ=Z
            // TODO: implement chk_plyr_spec_near_arch_C7DB
            // TODO: implement $C785 (check special objects)
        } else {
            if(block.get(ix) == 4) {
                //@label=adj_m3_p1
                //c$C737 LD HL,$FD01   ;
                block.set(ix + 0x12, 1); // L=x - 01
                block.set(ix + 0x12, -3); // H=y - FD
            } else {
                // LD HL,$FDF9    ; -3, -7
                block.set(ix + 0x12, -7); // F9
                block.set(ix + 0x13, -3); // FD
            }
            int y = block.get(ix + 2) + 0x0D;
            block.set(ix + 0x0A, y); // dY
            int x = block.get(ix + 1);
            block.set(ix + 9, x); // dX
            // LD HL,$060F   ; +6, +15
            int z = block.get(ix + 3);
            block.set(ix + 0x0B, z); // dZ=Z
            // TODO: implement chk_plyr_spec_near_arch_C7DB
            // TODO: implement $C785 (check special objects)
        }
    }

    private void upd_3_5_C722(DataBlock block, int ix) {
        boolean hFlip = (block.get(ix + 7) & 0x40) > 0; // BIT 6,(IX+$07)
        if(hFlip) {
            //LD HL,$FEF9
            block.set(ix + 0x12, -7); // F9
            block.set(ix + 0x13, -2); // FE

        } else {
            // LD HL,$FDF7    ; -3, -9
            block.set(ix + 0x12, -9); // F7
            block.set(ix + 0x13, -3); // FD
        }
    }

    private void upd_8_C65E(DataBlock block, int ix) {
        // c$C4DD LD HL,$FAF4   ; -6, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -6); //FA
        // TODO: implement rest of routine
    }

    private void upd_9_C6BD(DataBlock block, int ix) {
        // c$C4DD LD HL,$FAF4   ; -6, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -6); //FA
        // TODO: implement rest of routine
    }

    private void upd_10_C4E8(DataBlock block, int ix) {
        //LD HL,$FFEC   ;
        block.set(ix + 0x12, -20); // EC
        block.set(ix + 0x13, -1); // FF
    }

    private void upd_11_C4ED(DataBlock block, int ix) {
        //LD HL,$FEF4   ; -2, -12
        block.set(ix + 0x12, -12); // F4
        block.set(ix + 0x13, -2); // FE
    }

    private void upd_12_to_15_C4F2(DataBlock block, int ix) {
        //LD HL,$FCF8   ; -4, -8
        block.set(ix + 0x12, -8); // F8
        block.set(ix + 0x13, -4); // FC
    }

    private void upd_6_7_C4E3(DataBlock block, int ix) {
        //LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
    }

    private void upd_128_to_130_C4D3(DataBlock block, int ix) {
        //LD HL,$FEF8   ; -2, -8
        block.set(ix + 0x12, -8); //F8
        block.set(ix + 0x13, -2); //FE
    }

    private void upd_22_B7A3(DataBlock block, int ix) {
        set_both_deadly_flags_B85C(block, ix);
        //c$C4FC LD HL,$F9F4   ; -7, -12
        // $C4FF JR $C4E0      ;
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -7); //F9
    }

    private void upd_23_B7E7(DataBlock block, int ix) {
        set_both_deadly_flags_B85C(block, ix);
        // c$C4E3 LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
    }

    private void upd_30_31_158_159_B9A5(DataBlock block, int ix) {
        //c$C510 LD HL,$03F4   ; +3, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, 3); //03
        // TODO: implement rest of routine
    }

    private void upd_54_B6B9(DataBlock block, int ix) {
        // c$C4E3 LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
        // TODO: implement rest of routine
    }

    private void upd_55_B6B1(DataBlock block, int ix) {
        // c$C4E3 LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
        // TODO: implement rest of routine
    }

    private void upd_62_C4AA(DataBlock block, int ix) {
        // c$C4E3 LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
        // TODO: implement rest of routine
    }

    private void upd_63_B7A9(DataBlock block, int ix) {
        set_both_deadly_flags_B85C(block, ix);
        // c$C4E3 LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
        // TODO: implement rest of routine
    }

    private void upd_80_to_83_C5C8(DataBlock block, int ix) {
        // c$C4DD LD HL,$FAF4   ; -6, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -6); //FA
        // TODO: implement rest of routine
    }

    private void upd_86_87_B7ED(DataBlock block, int ix) {
        // c$C4F2 LD HL,$FCF8   ; -4, -8
        block.set(ix + 0x12, -8); //F8
        block.set(ix + 0x13, -4); //FC
        // TODO: implement rest of routine
    }

    private void upd_91_B683(DataBlock block, int ix) {
        //LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
        // TODO: implement rest of routine
    }

    private void upd_141_B99C(DataBlock block, int ix) {
        //LD HL,$F4F0   ;
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -12); //F4
    }

    private void upd_142_B99F(DataBlock block, int ix) {
        //LD HL,$0CE8   ;
        block.set(ix + 0x12, -24); //E8
        block.set(ix + 0x13, 12); //0C
    }

    private void upd_143_B6A2(DataBlock block, int ix) {
        // c$C4E3 LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
        // TODO: implement rest of routine
    }

    private void upd_144_to_149_152_to_157_B6F9(DataBlock block, int ix) {
        // c$C4DD LD HL,$FAF4   ; -6, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -6); //FA
        // TODO: implement rest of routine
    }

    private void upd_150_151_B73C(DataBlock block, int ix) {
        //c$C50B LD HL,$07F4   ; +7, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, 7); //07
        // TODO: implement rest of routine
    }

    private void upd_164_to_167_B92C(DataBlock block, int ix) {
        // c$C4D8 LD HL,$FCF4   ; -4, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -4); // FC
        // TODO: implement rest of routine
    }

    private void upd_178_179_B865(DataBlock block, int ix) {
        // c$C4F2 LD HL,$FCF8   ; -4, -8
        block.set(ix + 0x12, -8); //F8
        block.set(ix + 0x13, -4); //FC
        // TODO: implement rest of routine
    }

    private void upd_180_181_B80F(DataBlock block, int ix) {
        // c$C4F2 LD HL,$FCF8   ; -4, -8
        block.set(ix + 0x12, -8); //F8
        block.set(ix + 0x13, -4); //FC
        // TODO: implement rest of routine
    }

    private void upd_182_183_B5FF(DataBlock block, int ix) {
        // c$C4F2 LD HL,$FCF8   ; -4, -8
        block.set(ix + 0x12, -8); //F8
        block.set(ix + 0x13, -4); //FC
        // TODO: implement rest of routine

    }


    private void renderAllSprites(DataBlock block, int from, int cnt) {
        for(int i=from;i<from+cnt;i++) {
            int ix = block.start + i*32;
            if(block.get(ix)!=0) {
                calc_pixel_XY_D6C9(ix, block);
                print_sprite_D718(block, ix);
            }
        }
    }

    private void build_screen_objects_D1E6() {
        if(variables.get(0x5BB2) != 0) {
            // update special objects if not the first screen of the game
            update_special_objs_C591();
        }
        clear_scrn_buffer_D567();
        retrieve_screen_D3C6();
        // TODO: implement
        find_special_objs_here_C525();
        adjust_plyr_xyz_for_room_size_D320();

        variables.set(0x5BAF, 0);
        variables.set(0x5BB0, 0);
        variables.set(0x5BBD, 0);
        variables.set(0x5BBF, 0);
        variables.set(0x5BB7, 1);
        variables.set(0x5BC0, graphic_objs_tbl_5C08.get(0x5C10) & 1);

        //flag_room_visited_D219();


    }

    private void update_special_objs_C591() {
        //TODO: implement
    }

    private void retrieve_screen_D3C6() {
        int de = 0x5C88; // target
        int bc = 0x6BD1; // location table end
        int hl = 0x6251; // location table start
        // $D12D LD DE,$5C08   ;
        // $D130 PUSH DE       ;
        // $D131 POP IX        ;
        int ix = 0x5C08; // set in lose_life_D12A

        boolean found = false;
        int currLocId = graphic_objs_tbl_5C08.get(ix + 8);

        while(hl < bc && !found) {
            int tableLocId = location_tbl_6251.get(hl);
            found = tableLocId == currLocId;
            if(!found) {
                hl++;
                int size = location_tbl_6251.get(hl);
                hl+=size;
            }

        }
        if(!found) {
            // This should be unreachable
            debugPanel2.append("Location not found: ERROR");
            for(int i = data_block_5CA8.start; i<data_block_5CA8.start+data_block_5CA8.size; i++) data_block_5CA8.set(i, 0);
            return;
        }
        // @label=found_screen
        // HL points to start of the location in location table
        int roomId = location_tbl_6251.get(hl);
        debugPanel2.append("Retrieved room: %d / %02x".formatted(roomId,roomId));
        hl++;
        int size = location_tbl_6251.get(hl);
        int roomEnd = hl + size - 1;
        hl++;
        int attrOrig = location_tbl_6251.get(hl);
        int attr = (attrOrig & 0x07) | 0x40;
        variables.set(0x5BAD, attr); // current room attributes (color)
        int roomSize = ((attrOrig >> 3) & 0x1F);
        variables.set(0x5BAB, room_size_tbl_6248.get(room_size_tbl_6248.start + roomSize*3)); // room size X
        variables.set(0x5BAC, room_size_tbl_6248.get(room_size_tbl_6248.start + roomSize*3 + 1)); // room size Y
        variables.set(0x5BAE, room_size_tbl_6248.get(room_size_tbl_6248.start + roomSize*3 + 2)); // room size Z
        debugPanel2.append("Retrieved room size: "+roomSize+" X:"+variables.get(0x5BAB)+" Y:"+variables.get(0x5BAC)+" Z:"+variables.get(0x5BAE));
        //IO.println("Retrieved room size: "+roomSize+" X:"+variables.get(0x5BAB)+" Y:"+variables.get(0x5BAC)+" Z:"+variables.get(0x5BAE));
        hl++; // background objects start

        // @label=next_bg_obj
        // decode all background objects
        // hl - iterates over room background objects (until end of room data or 0xFF separator)
        int targetAddr = other_objs_here_5C88.start;
        while(location_tbl_6251.get(hl) != 0xFF && hl<=roomEnd) {
            DataBlock bkgObj = backgroundObjects[location_tbl_6251.get(hl)];
            debugPanel2.append("Retrieved background object: %02x".formatted(location_tbl_6251.get(hl)));
            //IO.print("Background block type: %02x".formatted(location_tbl_6251.get(hl)));
            int bkgAddr =  bkgObj.start;
            while(bkgObj.get(bkgAddr) != 0) { // each object consists of 8-byte sprite info terminated by 0
                // 8 - byte sprite info
                debugPanel2.append("Retrieved sprite: %02x".formatted(bkgObj.get(bkgAddr)));
                for(int i=0; i<8; i++) other_objs_here_5C88.set(targetAddr+i, bkgObj.get(bkgAddr+i));
                bkgAddr+=8;
                // 9th byte
                other_objs_here_5C88.set(targetAddr+8, currLocId);
                for(int t=9; t<32; t++) other_objs_here_5C88.set(targetAddr+t, 0); // reset remaining info
                targetAddr+=32;
            }
            hl++;
            //IO.println();
        }

        hl ++;
        while(hl < roomEnd) {
            // @label=find_fg_objs
            int blockCtrl = location_tbl_6251.get(hl);
            int blockCnt = (blockCtrl & 0x07) + 1;
            int blockType = (blockCtrl >> 3) & 0x1F;
            DataBlock blockDef = foregroundObjects[blockType];
            //IO.print("Foreground block type: %02x, count: %d ".formatted(blockType, blockCnt));
            hl++;
            for(int i=0; i<blockCnt; i++) {
                int locByte = location_tbl_6251.get(hl+i);
                int x = (locByte & 0b00000111);
                int y = (locByte & 0b00111000) >> 3;
                int z = (locByte & 0b11000000) >> 6;
                //IO.print("(%d,%d,%d)".formatted(x, y, z));
                other_objs_here_5C88.set(targetAddr, blockDef.get(blockDef.start)); // object ID
                other_objs_here_5C88.set(targetAddr+4, blockDef.get(blockDef.start+1)); // width
                other_objs_here_5C88.set(targetAddr+5, blockDef.get(blockDef.start+2)); // depth
                other_objs_here_5C88.set(targetAddr+6, blockDef.get(blockDef.start+3)); // height
                other_objs_here_5C88.set(targetAddr+7, blockDef.get(blockDef.start+4)); // flags
                other_objs_here_5C88.set(targetAddr+8, currLocId); // screen
                int offsets = blockDef.get(blockDef.start+5);
                int x1 = ((offsets << 3) & 8);
                int y1 = ((offsets << 2) & 8);
                other_objs_here_5C88.set(targetAddr+1, x1 + x*16 + 0x48); // X
                other_objs_here_5C88.set(targetAddr+2, y1 + y*16 + 0x48); // Y
                other_objs_here_5C88.set(targetAddr+3, ((z*12+offsets) & 0xFC) + variables.get(0x5BAE)); // Y, variable stores room size Z
                //IO.print("/(%d,%d,%d)".formatted(other_objs_here_5C88.get(targetAddr+1), other_objs_here_5C88.get(targetAddr+2), other_objs_here_5C88.get(targetAddr+3)));
                for(int t=9; t<32; t++) other_objs_here_5C88.set(targetAddr+t,0);  // reset remaining info
                targetAddr+=32;
            }
            //IO.println();
            hl+=blockCnt;
        }


        // @label=zero_end_of_graphic_objs_tbl
        // clear rest of graphics objects table
        for(int i=targetAddr; i<other_objs_here_5C88.start + other_objs_here_5C88.size; i++) other_objs_here_5C88.set(i, 0);

        debugTable("Other objects data:", other_objs_here_5C88.start, other_objs_here_5C88.getCopy());
    }

    private void find_special_objs_here_C525() {
        // TODO: implement
        int currLocId = graphic_objs_tbl_5C08.get(graphic_objs_tbl_5C08.start + 8);
        int iy = special_objs_tbl_6FF2.start;
        while(iy<special_objs_tbl_6FF2.endExcl() && special_objs_tbl_6FF2.get(iy)!=0) {
            if(currLocId == special_objs_tbl_6FF2.get(iy+8)) {
                IO.println("Found special object");
            }
            iy+=9;
        }
    }

    private void adjust_plyr_xyz_for_room_size_D320() {
        debugTable("Graphic objects table:", graphic_objs_tbl_5C08.start, graphic_objs_tbl_5C08.getCopy());
        int roomSizeX = variables.get(0x5BAB) - 2;
        int roomSizeY = variables.get(0x5BAC) - 2;
        int ix = 0x5C08;
        int x = graphic_objs_tbl_5C08.get(ix + 1);
        if(x == 0) {
            // @label=enter_arch_e

        }
        //TODO: implement

    }

    private void flag_room_visited_D219() {
        int screenOrig = graphic_objs_tbl_5C08.get(0x5C10); // plyr_spr_1 screen
        int screen = (screenOrig >> 3) & 0x1F;

        //TODO: implement
    }

    private void save_2d_info_CE49(int ix, DataBlock block) {
        int widthBytes = block.get(ix + 0x18);
        block.set(ix + 0x1c, widthBytes);
        int heightLines = block.get(ix + 0x19);
        block.set(ix + 0x1d, heightLines);
        int pixelX = block.get(ix + 0x1a);
        block.set(ix + 0x1e, pixelX);
        int pixelY = block.get(ix + 0x1b);
        block.set(ix + 0x1f, pixelY);
    }

    private void calc_pixel_XY_D6C9(int ix, DataBlock block) {
        int x = block.get(ix + 0x01);
        x = x + block.get(ix + 0x02);
        x = x - 0x80;
        x = x + block.get(ix + 0x12);
        block.set(ix + 0x1a, x);
        int y = block.get(ix + 0x02);
        y = y - block.get(ix + 0x01);
        y = y + 0x80;
        y = y >> 1;
        y = y + block.get(ix + 0x03);
        y = y - 0x68;
        y = y + block.get(ix + 0x13);
        block.set(ix + 0x1b, y);
        // TODO: implement:
        // $D6EC CP $C0         ; bottom line of screen?
        // $D6EE RET            ;
    }

    private void set_both_deadly_flags_B85C(DataBlock block, int ix) {
        block.set(ix + 0x0D, block.get(ix + 0x0D) | 0xA0);
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

