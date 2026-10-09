package org.kr;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class Game implements Runnable {
    private final ScreenPanel mainPanel;
    private final ScreenPanel shadowPanel;
    private final DebugPanel debugPanel1;
    private final DebugPanel debugPanel2;
    private final ConcurrentSkipListSet<Integer> keyQueue;

    private long lastTick = 0;
    private int sunTick = 0;
    private long tickNo = 0;
    private final long REPAINT_MS = 50;
    private final long DELAY_MS = 100;
    private final int SUN_TICK_PER_GAME_TICK = 5;
    private final int MAX_DAYS_BCD = 0x40; // NOTE: number in BCD

    private final boolean skipDeath = true;

    // $4000-$57FF - spectrum video memory
    // $5800-$5AFF - spectrum attribute memory
    private final VideoMemoryScreen mainMemory;
    // $D8F3-$F0F2 - video buffer
    private final VideoMemoryLinear shadowMemory;
    // $5BA0-$6107 - variables
    // NOTE: variables and other memory locations are treated as ints, not bytes due to lack of unsigned byte type in java
    private final DataBlock variables = new DataBlock(0x5BA0, 0x5BE8 - 0x5BA0 + 1);
    private boolean exitingScreen = false;
    private final DataBlock scrn_visited_5BE8 = new DataBlock(0x5BE8, 0x20);
    private final DataBlock inventory_5BD8 = new DataBlock(0x5BD8, 4);
    private final DataBlock objects_carried_5BDC = new DataBlock(0x5BD8, 12);
    private final DataBlock object_attributes_BFD3 = InitialData.block("object_attributes_BFD3");
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
    private final DataBlock sun_moon_yoff_C440 = InitialData.block("sun_moon_yoff_C440");
    private final DataBlock sun_moon_scratchpad_C44D =  InitialData.block("sun_moon_scratchpad_C44D");
    // Includes special_objs_here and other_objs_here, 40 32-byte slots
    private final DataBlock graphic_objs_tbl_5C08 = new DataBlock(0x5C08, 0x40 + 0x40 + 0x20 + 0x0460);
    private final DataBlock objects_to_draw_CE8B = new DataBlock(0xCE8B, 0x2E); // 46, but there should be not more than 40 objects to draw
    private final DataBlock location_tbl_6251 = InitialData.block("location_tbl_6251");
    private final DataBlock room_size_tbl_6248 = InitialData.block("room_size_tbl_6248");
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
    private final DataBlock panel_data_D27E = InitialData.block("panel_data_D27E");
    private final DataBlock day_txt_BCE7 = InitialData.block("day_txt_BCE7");
    private final DataBlock day_font_BCEC = InitialData.block("day_font_BCEC"); // NOTE: this font looks the same but is shifted by half of byte

    // Repaint every fixed interval
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            updateMainMemory();
            updateShadowMemory();
        }
    };

    public Game(ScreenPanel mainPanel, ScreenPanel shadowPanel, DebugPanel debugPanel1, DebugPanel debugPanel2,
                ConcurrentSkipListSet<Integer> keyQueue) {
        this.mainPanel = mainPanel;
        this.shadowPanel = shadowPanel;
        this.debugPanel1 = debugPanel1;
        this.debugPanel2 = debugPanel2;
        this.keyQueue = keyQueue;

        this.debugPanel1.append("Start");
        this.debugPanel2.append("Start");

        mainMemory = new VideoMemoryScreen(0x4000);
        shadowMemory = new VideoMemoryLinear(0xD8F3);

        timer.scheduleAtFixedRate(task, REPAINT_MS * 2, REPAINT_MS);
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
        //; Data block at 5BA5
        //@label=seed_3
        //b$5BA5 DEFS $01
        variables.set(0x5BA5, 0);
        //; Data block at 5BC7
        // @label=gfxbase_8x8
        // b$5BC7 DEFB $08,$61
        //variables.set(0x5BC7, 0x08); // variables NOT USED (passing DataBlock with font data instead)
        //variables.set(0x5BC8, 0x61);
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
        //@label=all_objs_in_cauldron
        //b$5BC3 DEFS $01
        variables.set(0x5BC3, 0);
        //@label=days
        //b$5BB9 DEFS $01
        variables.set(0x5BB9, 0);
        //; #TABLE(default,centre,:w)
        //; { =h Bit(n) | =h Description }
        //; { b5 | ??? }
        //; { b4 | pickup/drop }
        //; { b3 | jump }
        //; { b2 | forward }
        //; { b1 | right }
        //; { b0 | left }
        //; TABLE#
        //@label=user_input
        //b$5BB5 DEFS $01
        variables.set(0x5BB5, 0);
        //@label=obj_dropping_into_cauldron
        //b$5BC4 DEFS $01
        variables.set(0x5BC4, 0);

        //; Data block at 5BC1
        //@label=tmp_dZ
        //b$5BC1 DEFS $01
        variables.set(0x5BC1, 0);



        int v5C78 = 0x65; // originally taken from 5C78 (LSB of FRAMES 3-byte system variable). It is incremented by ROM interrupt routine, servers as random seed
        // PUSH AF       ;
        // CALL $D53A    ;
        // POP AF        ;
        variables.reset();
        scrn_visited_5BE8.reset();
        inventory_5BD8.reset();
        objects_carried_5BDC.reset();
        // For testing only
        inventory_5BD8.set(inventory_5BD8.start, 0x60);
        inventory_5BD8.set(inventory_5BD8.start+1, 0x61);
        inventory_5BD8.set(inventory_5BD8.start+2, 0x62);
        inventory_5BD8.set(inventory_5BD8.start+3, 0x63);
        objects_carried_5BDC.set(objects_carried_5BDC.start, 0x60);
        objects_carried_5BDC.set(objects_carried_5BDC.start+4, 0x61);
        objects_carried_5BDC.set(objects_carried_5BDC.start+8, 0x62);
        graphic_objs_tbl_5C08.reset();
        //special_objs_here_5C48.reset();
        //other_objs_here_5C88.reset();

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
        variables.set(0x5BA0, (variables.getU(0x5BA0) + variables.getU(0x5BA2)) & 0xFF);
        // CALL $D55F    ; {colour is bright yellow on black
        clear_scrn_D55F();
        // CALL $BD0C    ;
        do_menu_selection_BD0C();
        menu_loop_BD23(true); // returns when game starts

        try {
            updateShadowMemory();
            updateMainMemory();
            mainPanel.saveImage("images/menu.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        debugPanel2.append("START THE GAME");
        debugPanel2.append("Input method: "+variables.getU(0x5BA4));

        // LD DE,$B20E   ; }
        // CALL $B2CF    ; play tune // ignore audio
        // CALL $B544    ; randomise order of required objects
        shuffle_objects_required_B544();
        // CALL $D1B1    ; {randomise player start location

        int a = location_tbl_6251.start;
        boolean specialObjectsInitialized = false;
        //while(a < location_tbl_6251.endExcl())
        {
            variables.set(0x5BBA, 5); // important only when looping over rooms
            //int id = location_tbl_6251.get(a);
            //IO.println("Room id: "+id);
            //int id = 1;
            //int id=-1;
            int id = 0xFD;


            init_start_location_D1B1(id);
            // CALL $C46D    ; }
            init_sun_C46D();
            // CALL $C47E    ; randomise special object locations
            if(!specialObjectsInitialized) {
                init_special_objects_C47E();
                specialObjectsInitialized = true;
            }
            // @label=player_dies
            // CALL $D12A    ;
            player_dies_AFB7();
            game_loop_AFBA();

            //saveImage(mainPanel, "images/location_%03d_%02x.png".formatted(start_loc_1_D169.get(0xD169),start_loc_1_D169.get(0xD169)));

            //int size = location_tbl_6251.getA(a+1);
            //a+=size+1;
        }

        //printVariables();
    }

    private void saveImage(ScreenPanel panel, String fileName) {
        try {
            updateShadowMemory();
            updateMainMemory();
            panel.saveImage(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        for (int hl = 0xBDA2; hl < 0xBDA2 + 8; hl++) { menu_colours_BDA2.set(hl, menu_colours_BDA2.getU(hl) & 0x7F); }
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
            variables.set(0x5BB6, menu_colours_BDA2.getU(de1));
            //debugPanel2.append("menu attrib: %02x".formatted(variables.get(0x5BB6))) ;
            int l = menu_xy_BDAA.getU(hl2); // x - menu position
            int h = menu_xy_BDAA.getU(hl2+1); // y
            hl2 += 2;
            de2 = print_text_single_colour_BE31(h, l, de1, de2);
        }
        int a = variables.getU(0x5BB8); // suppress border
        if(a == 0) {
            variables.set(0x5BB8,1);
            print_border_D296();
            update_screen_D56F(true);
        }
    }

    private int print_text_single_colour_BE31(int h, int l,int de1, int de2) {
        // h: y, l: x, de1: attribute address, de2: text address (first)
        debugPanel1.append(String.format("print_text_single_colour_BE31 ( y: %02x, x: %02x, attr_addr: %02x, text_addr: %04x)", h, l, de1, de2));
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
            print_8x8_BE7F(menu_text_BDBA.getU(de2), bc, font_6108);
            mainMemory.setByteAt(hl, variables.getU(0x5BB6)); // Color.getAttribute(Color.WHITE, Color.GREEN, Color.NONE, Color.NONE));
            shadowMemory.setByteAt(hl-mainMemory.start+shadowMemory.start, variables.getU(0x5BB6));
            textDone = (menu_text_BDBA.getU(de2) & 0x80) > 0;
            hl ++;
            de2 ++;
            bc ++;
        }
        return de2;
    }

    private int calc_vidbuf_addr_D811(int y, int x) {
        int xy = (x + y*256) >> 3;
        return xy + 0xD8F3;
    }

    private int calc_attrib_addr_D848(int h, int l) {
        // h: y, l: x
        int y = (h ^ 0xFF) >> 3;
        int xy = (y * 256 + l) >> 3;
        return xy + 0x5700;
    }

    private void print_8x8_BE7F(int a, int bc, DataBlock font) {
        // a: character, bc: shadow memory address
        int ch = a & 0x7F;
        //debugPanel1.append("print_8x8_BE7F");
        int baseChar = font.start;
        int de2 = ch  * 8 + baseChar; // font address
        for(int b = 8; b>0; b--) {
            shadowMemory.setByteAt(bc, font.getU(de2));
            de2++;
            bc-=32;
        }
    }

    private void flash_menu_BD89() {
        debugPanel1.append("flash_menu_BD89");
        int hl = 0xBDA3; // first menu entry
        int a = variables.getU(0x5BA4); // input method
        a = (a >> 1) & 0x3; // joystick / keyboard flag
        // CALL $BEA3
        for(int i = 0; i < 4; i++) {
            if(a == i) menu_colours_BDA2.set(hl+i, menu_colours_BDA2.getU(hl+i) | 0x80 );
            else menu_colours_BDA2.set(hl+i, menu_colours_BDA2.getU(hl+i) & 0x7F );
        }
        if((variables.getU(0x5BA4) & 0x08)>0) menu_colours_BDA2.set(hl+4, menu_colours_BDA2.getU(hl+4) | 0x80 );
        else menu_colours_BDA2.set(hl+5, menu_colours_BDA2.getU(hl+5) & 0x7F );
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
        //debugPanel1.append("transfer_sprite_and_print_D24C");
        transfer_sprite_D237(metadata, ix, hl, source);
        print_sprite_D718(metadata, ix);

        return hl+4;
    }

    // Populate sprite metadata
    private void transfer_sprite_D237(DataBlock medatada, int ix, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        //debugPanel1.append("transfer_sprite_D237 (hl: %02x, ix: %04x)".formatted(hl, ix));
        medatada.set(ix, source.getU(hl)); // sprite index
        medatada.set(ix+0x07, source.getU(hl+1)); // flags
        medatada.set(ix+0x1A, source.getU(hl+2)); // pixel X
        medatada.set(ix+0x1B, source.getU(hl+3)); // pixel Y
    }

    private void print_sprite_D718(DataBlock metadata, int ix) {
        int de = flip_sprite_D6EF(metadata, ix);
        if(de == 0) return; // spr_null used as a flag to return from routine

        int x = metadata.getU(ix + 0x1A);
        int y = metadata.getU(ix + 0x1B);
        int xOffset = (x & 7);
        //debugPanel2.append(xOffset == 0 ? "aligned" : "NOT aligned by %02x".formatted(xOffset));
        // JR Z,$D76F    ; {no, skip

        // LD ($D7AD),A   ; - self modifying relative address to unrolled loop depending on a (width in bytes)
        // we skip this part entirely, using inner for loop instead
        int a = sprite_graphics_data_728A.getU(de);
        a = (a & 0x7); // width_bytes, ignore rotation flags
        de ++;
        metadata.set(ix + 0x19,   sprite_graphics_data_728A.getU(de)); // height_lines
        // off bottom of screen?
        if(y + metadata.getU(ix + 0x19)>0xC0) metadata.set(ix + 0x19, 0xC0 - y);
        de ++;

        if(y > 191 || x > 255) return; // do not attempt do draw out of screen
        int bc = calc_vidbuf_addr_D811(y, x);

        if(xOffset == 0) {
            //@label=loc_D76F
            metadata.set(ix + 0x18, a);
            for(int lineNo = 0; lineNo<metadata.getU(ix + 0x19); lineNo++) {
                for (int i = 0; i < a; i++) {
                    int bufByte = shadowMemory.getByteAt(bc);
                    int e_mask = sprite_graphics_data_728A.getU(de);
                    de++;
                    int d_spriteByte = sprite_graphics_data_728A.getU(de);
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
            for(int lineNo = 0; lineNo<metadata.getU(ix + 0x19); lineNo++) {
                // Shift each byte across two buffer bytes (left and right), use lookup tables to reflect the original logic
                for (int i = 0; i < a; i++) {
                    int bufByteLeft = shadowMemory.getByteAt(bc);
                    int bufByteRight = shadowMemory.getByteAt(bc+1);
                    int e_mask = sprite_graphics_data_728A.getU(de);
                    de++;
                    int d_spriteByte = sprite_graphics_data_728A.getU(de);
                    de++;
                    int e_maskLeft = (lookupTable.getU(lookupBase + e_mask)) ^ 0xFF;
                    int e_maskRight = (lookupTable.getU(lookupBase + 0x0100 + e_mask)) ^ 0xFF;
                    int d_spriteByteLeft = (lookupTable.getU(lookupBase + d_spriteByte)) ^ 0xFF;
                    int d_spriteByteRight = (lookupTable.getU(lookupBase + 0x0100 + d_spriteByte)) ^ 0xFF;
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
        //debugPanel1.append("flip_sprite_D6EF (ix: %04x)".formatted(ix));
        int l = metadata.getU(ix);
        int hl = l * 2 + 0x7112; // sprite data index (index x 2 + start, 2 bytes per address) // sprite address location
        int de = sprite_tbl_7112.getU(hl) + sprite_tbl_7112.getU(hl+1)*256; // sprite actual address
        //debugPanel2.append("sprite address (DE): %04x".formatted(de));
        int width = sprite_graphics_data_728A.getU(de);
        // returns sprite address or 0 if sprite is spr_null
        int flagsScratch = metadata.getU(ix + 0x07);
        boolean flipVScratch = (flagsScratch & 0x80) > 0;
        boolean flipHScratch = (flagsScratch & 0x40) > 0;
        int flagsSpriteData = sprite_graphics_data_728A.getU(de) & 0xC0;
        boolean flipVData = (flagsSpriteData & 0x80) > 0;
        boolean flipHData = (flagsSpriteData & 0x40) > 0;

        // Flip vertically (in-place) and set flag in data
        if(flipVScratch != flipVData) {
            int lines = sprite_graphics_data_728A.getU(de+1);
            int bytesInLine = (sprite_graphics_data_728A.getU(de) & 0x07) * 2;
            for(int line = 0; line<(lines >> 1); line++) {
                for(int b = 0; b < bytesInLine; b++) {
                    int topByteAddress = de + 2 + line * bytesInLine + b;
                    int bottomByteAddress = de + 2 + (lines - line - 1) * bytesInLine + b;
                    int buffer = sprite_graphics_data_728A.getU(topByteAddress);
                    sprite_graphics_data_728A.set(topByteAddress, sprite_graphics_data_728A.getU(bottomByteAddress));
                    sprite_graphics_data_728A.set(bottomByteAddress, buffer);
                }
            }
            sprite_graphics_data_728A.set(de, sprite_graphics_data_728A.getU(de) ^ 0x80);
        }
        // Flip horizontally (in-place) and set flag in data
        if(flipHScratch != flipHData) {
            int lines = sprite_graphics_data_728A.getU(de+1);
            int bytesInLine = (sprite_graphics_data_728A.getU(de) & 0x07) * 2;
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
                    int leftBuffer = sprite_graphics_data_728A.getU(leftByteAddress);
                    leftBuffer = lookupTable.getU(0xF100 + leftBuffer); // F1xx - byte flip table
                    int rightBuffer = sprite_graphics_data_728A.getU(rightByteAddress);
                    rightBuffer = lookupTable.getU(0xF100 + rightBuffer);
                    sprite_graphics_data_728A.set(leftByteAddress, isMiddlePair ? leftBuffer : rightBuffer);
                    sprite_graphics_data_728A.set(rightByteAddress, isMiddlePair ? rightBuffer : leftBuffer);
                }
            }
            sprite_graphics_data_728A.set(de, sprite_graphics_data_728A.getU(de) ^ 0x40);
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
            metadata.set(ix + 0x1A, metadata.getU(ix + 0x1A) + dx);
            metadata.set(ix + 0x1B, metadata.getU(ix + 0x1B) + dy);
        }
        return hl+4;
    }

    private void update_screen_D56F(boolean clearBuffer) {
        int bytesX = VideoMemory.WIDTH / 8;
        for(int bufferY = 0; bufferY<VideoMemory.HEIGHT; bufferY++) {
            int screenY = ((bufferY & 0b111) <<3) + ((bufferY & 0b111000) >> 3) + (bufferY & 0b11000000);
            int screenBase = screenY * bytesX + mainMemory.start;
            int bufferBase = (VideoMemory.HEIGHT - bufferY -1) * bytesX + shadowMemory.start;
            for(int b = 0; b<VideoMemory.WIDTH / 8; b++) {
                mainMemory.setByteAt(screenBase+b, shadowMemory.getByteAt(bufferBase+b));
                if(clearBuffer) shadowMemory.setByteAt(bufferBase+b, 0); // wipe buffer
            }
        }
        // wipe buffer attributes (non-existent in original game)
        if(clearBuffer) {
            for (int attr = shadowMemory.start + VideoMemory.PIXEL_MEM_SIZE;
                 attr < shadowMemory.start + VideoMemory.PIXEL_MEM_SIZE + VideoMemory.HEIGHT / 8 * VideoMemory.WIDTH / 8; attr++) {
                shadowMemory.setByteAt(attr, 0);
            }
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
                int a = variables.getU(0x5BA4);
                variables.set(0x5BA6, a);
                Integer key = keyQueue.first();
                keyQueue.remove(key);
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
        int a = variables.getU(0x5BA0); //seed 1
        a = (a & 3) | 4; // random number (assuming seed is random)
        for(int c = a; c>0; c--) {
            int iy = objects_required_C27D.start;
            for (int b = 0x0D; b > 0; b--) {
                int buf = objects_required_C27D.getU(iy);
                objects_required_C27D.set(iy, objects_required_C27D.getU(iy+1));
                objects_required_C27D.set(iy+1, buf);
                iy++;
            }
        }
        //debugTable("Required objects:", objects_required_C27D.start, objects_required_C27D.getCopy());
    }

    private void init_start_location_D1B1(int override) {
        for(int i=0; i<8; i++)
            plyr_spr_1_scratchpad_D161.set(plyr_spr_1_scratchpad_D161.start+i,
                    plyr_spr_init_data_D1A1.getU(plyr_spr_init_data_D1A1.start+i));
        for(int i=0; i<8; i++)
            plyr_spr_2_scratchpad_D181.set(plyr_spr_2_scratchpad_D181.start+i,
                    plyr_spr_init_data_D1A1.getU(plyr_spr_init_data_D1A1.start+i+8));
        // LD A,$12      ; graphic_no (player top half)
        // LD ($D171),A  ; plyr_spr_1_scratchpad (byte 16)
        byte_D171.set(0xD171, 0x12);
        // LD A,$22      ; graphic_no (player bottom half)
        // LD ($D191),A  ; {plyr_spr_2_scratchpad (byte 16)
        byte_D191.set(0xD191, 0x22);
        int a = variables.getU(0x5BA0) & 0x3; // random
        int randomLoc = start_locations_D1E2.getU(0xD1E2 + a);
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
        int rnd = variables.getU(0x5BA0) & 0x07; // random
        int hl = special_objs_tbl_6FF2.start;
        while(hl < special_objs_tbl_6FF2.endExcl()) {
            rnd = (rnd & 0x07) | 0x60; // set numbers between 0x60 and 0x67 starting from random number to special object indexes
            special_objs_tbl_6FF2.set(hl, rnd);
            hl++;
            // copy coords from "start" to "current"
            for(int i=0; i<4; i++) special_objs_tbl_6FF2.set(hl+i+4, special_objs_tbl_6FF2.getU(hl+i));
            hl+=8;
            rnd++;
        }
        debugTable("Special objects:", special_objs_tbl_6FF2.start, special_objs_tbl_6FF2.getCopy());
    }

    private void player_dies_AFB7() {
        lose_life_$D12A();
    }

    private void initPlayerData() {
        int de = 0x5C08;
        for(int i=0; i<plyr_spr_1_scratchpad_D161.size; i++) graphic_objs_tbl_5C08.set(de+i, plyr_spr_1_scratchpad_D161.getU(plyr_spr_1_scratchpad_D161.start+i));
        de+=plyr_spr_1_scratchpad_D161.size;
        for(int i=0; i<start_loc_1_D169.size; i++) graphic_objs_tbl_5C08.set(de+i, start_loc_1_D169.getU(start_loc_1_D169.start+i));
        de+=start_loc_1_D169.size;
        for(int i=0; i<flags12_1_D16D.size; i++) graphic_objs_tbl_5C08.set(de+i, flags12_1_D16D.getU(flags12_1_D16D.start+i));
        de+=flags12_1_D16D.size;
        for(int i=0; i<byte_D171.size; i++) graphic_objs_tbl_5C08.set(de+i, byte_D171.getU(byte_D171.start+i));
        de+=byte_D171.size;
        for(int i=0; i<plyr_spr_2_scratchpad_D181.size; i++) graphic_objs_tbl_5C08.set(de+i, plyr_spr_2_scratchpad_D181.getU(plyr_spr_2_scratchpad_D181.start+i));
        de+=plyr_spr_2_scratchpad_D181.size;
        for(int i=0; i<start_loc_2_D189.size; i++) graphic_objs_tbl_5C08.set(de+i, start_loc_2_D189.getU(start_loc_2_D189.start+i));
        de+=start_loc_2_D189.size;
        for(int i=0; i<byte_D191.size; i++) graphic_objs_tbl_5C08.set(de+i, byte_D191.getU(byte_D191.start+i));
    }

    private void lose_life_$D12A() {
        initPlayerData();
        variables.set(0x5BB1, 0);
        int livesLeft = variables.getU(0x5BBA) - 1;
        variables.set(0x5BBA, livesLeft);
        if(livesLeft < 0) {
            game_over_BA22();
            return;
        }
        int a = sun_moon_scratchpad_C44D.getU(0xC44D);
        int dayNight = (a >> 3) & 0x20; // ; day/night?
        int playerGraphicsNo = graphic_objs_tbl_5C08.getU(0x5C08+0x10);
        playerGraphicsNo = (playerGraphicsNo & 0x1F) + dayNight;
        graphic_objs_tbl_5C08.set(0x5C08+0x10, playerGraphicsNo);
        int playerGraphicsNoTop = graphic_objs_tbl_5C08.getU(0x5C08+0x30);
        playerGraphicsNoTop = (playerGraphicsNoTop & 0x0F) + dayNight + 0x20;
        graphic_objs_tbl_5C08.set(0x5C08+0x30, playerGraphicsNoTop);
    }

    private void game_over_BA22() {
        debugPanel2.append("Game over BA22\n");
        //TODO: implement
    }

    private void game_loop_AFBA() {
        debugPanel2.append("Game loop AFBA\n");
        boolean exit = false;
        while(!exit) {
            exitingScreen = false;
            build_screen_objects_D1E6();
            while (!exit & !exitingScreen) {
                exit = onscreen_loop_AFBD();
                debugPanel2.setText("");
                for(int ix = graphic_objs_tbl_5C08.start; ix < graphic_objs_tbl_5C08.endExcl(); ix+=0x20) {
                    if(graphic_objs_tbl_5C08.getU(ix)!=0) {
                        debugPanel2.append(graphic_objs_tbl_5C08.objectInfo(ix));
                    }
                }
                delay();
            }
        }
    }

    private boolean onscreen_loop_AFBD() {
        // @label=onscreen_loop
        //IO.println("Onscreen loop AFBD");
        variables.set(0x5BA2, variables.getU(0x5BBC));
        update_sprite_loop_AFC7(graphic_objs_tbl_5C08);
        if(exitingScreen) return false;
        // loc_B000
        list_objects_to_draw_CE62();
        //debugTable("Objects to draw: ", 0, objects_to_draw_CE8B.getCopy());

        // set attributes so the buffer contents are visible
        for(int i=0; i < 768; i++) shadowMemory.setByteAt(i + shadowMemory.start + 0x1800, Color.getAttribute(Color.WHITE, Color.BLUE, Color.NONE, Color.NONE));

        clear_scrn_buffer_D567(); // For testing
        calc_display_order_and_render_CEBB();

        //print_sprite_D718(graphic_objs_tbl_5C08, ix);
        // This is all just to verify if objects render at all
        //renderAllSprites(graphic_objs_tbl_5C08, 0, 2);
        //renderAllSprites(other_objs_here_5C88, 2,36);
        //renderAllSprites(graphic_objs_tbl_5C08, 0,2 + 2 + 36);
        //debugTable("Other objects (after render):", other_objs_here_5C88.start, other_objs_here_5C88.getCopy());

        //@label=delay_loop
        //NOTE: we ignore delay loop (calculated), instead we will schedule game loop every game tick

        //@label=no_delay
        //TODO: implement checking if first render ($B042 AND A         ; rendered before?)


        variables.set(0x5BB7, 0);
        int a = variables.getU(0x5BAD); // screen attribute
        for(int addr = 0x5800; addr<0x5800+0x0300; addr++) {
            mainMemory.setByteAt(addr, a);
            //shadowMemory.setByteAt(addr - mainMemory.start + shadowMemory.start, a);
        }

        //TODO: implement
        //$B04F CALL $BF4E    ; {inventory
        display_objects_BF4E();

        // $B052 CALL $D2EF    ;
        //@label=colour_panel
        fill_window_C515(mainMemory, 0x5AB6, 1, 3, 0);
        fill_window_C515(mainMemory, 0x5ABD, 1, 3, 0);
        fill_window_C515(mainMemory, 0x5A97, 6, 4, 0x42);
        // $B055 CALL $D30D    ;
        // @label=colour_sun_moon
        boolean isSun = ((sun_moon_scratchpad_C44D.getU(0xC44D)) & 1) == 0;
        int attr = isSun ? 0x46 : 0x47;
        fill_window_C515(mainMemory, 0x5AB8, 4, 2, attr);

        display_panel_D255();
        display_sun_moon_frame_C3A4();
        display_day_BCCA();
        print_days_BC66();
        print_lives_gfx_BC7A();
        print_lives_BCA3();
        update_screen_D56F(false);
        // @label=reset_objs_wipe_flag
        for(int addr = graphic_objs_tbl_5C08.start+7; addr<graphic_objs_tbl_5C08.endExcl(); addr+=32)
            graphic_objs_tbl_5C08.setBit(5, addr); // c$B090 RES 5,(HL)    ;
        return false;
    }

    private void delay() {
        long currentTick = System.currentTimeMillis();
        if(lastTick == 0) lastTick = currentTick;

        long sleepMs = DELAY_MS - (currentTick - lastTick);
        if(sleepMs > 0) {
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            currentTick = currentTick + sleepMs;
        }
        lastTick = currentTick;
        sunTick = (sunTick+1) % SUN_TICK_PER_GAME_TICK;
        tickNo ++;
    }

    private void display_day_BCCA() {
        int attr = variables.getU(0x5BAD); // room attribute
        attr = (((attr ^ 0xFF) + 2) & 0x07) | 0x40;
        day_txt_BCE7.set(day_txt_BCE7.start, attr);
        int de = day_txt_BCE7.start;
        //@label=print_text
        int x = 0x70;
        int y = 0x0F;
        int videoAddr = calc_vidbuf_addr_D811(y, x);
        int attrAddr = calc_attrib_addr_D848(y, x);
        de++;

        boolean textDone = false;
        while(!textDone) {
            print_8x8_BE7F(day_txt_BCE7.getU(de), videoAddr, day_font_BCEC);
            mainMemory.setByteAt(attrAddr, attr);
            //shadowMemory.setByteAt(attrAddr-mainMemory.start+shadowMemory.start, attr);
            textDone = (day_txt_BCE7.getU(de) & 0x80) > 0;
            videoAddr ++;
            attrAddr ++;
            de ++;
        }
    }

    private void display_objects_BF4E() {
        int ix = sprite_scratchpad_BFDB.start;
        for(int b=0; b<3; b++) {
            if(objects_carried_5BDC.getU(objects_carried_5BDC.start + b*4)!=0) {
                int x = (2-b)*24+16;
                int y = 0;
                sprite_scratchpad_BFDB.set(ix + 0x1A, x);
                sprite_scratchpad_BFDB.set(ix + 0x1B, y);
                int hl = calc_vidbuf_addr_D811(y, x);
                fill_window_C515(shadowMemory, hl, 3, 24, 0);
                int spriteIndex = objects_carried_5BDC.getU(objects_carried_5BDC.start + b*4);
                sprite_scratchpad_BFDB.set(ix, spriteIndex);
                print_sprite_D718(sprite_scratchpad_BFDB, ix);
                // TODO: implement blit_to_screen
                // $BFA2 CALL $D67C    ;
                int attr = object_attributes_BFD3.getU(object_attributes_BFD3.start + (spriteIndex & 0x0F));
                hl = calc_attrib_addr_D848(y + 0x17, x);
                fill_window_C515(mainMemory, hl, 3, 3, attr);
            }
        }
    }

    private void print_days_BC66() {
        int videoAddress = 0xD9E2; // (120, 7)
        print_BCD_number_BCAE(videoAddress, new int[] {variables.getU(0x5BB9)});
    }

    private void print_BCD_number_BCAE(int bc, int[] values) {
        // NOTE: we assume that the number is already in BCD
        for(int i = 0; i<values.length; i++) {
            int digitH = (values[i] >> 4) & 0x0F;
            int digitL = values[i] & 0x0F;
            print_8x8_BE7F(digitH, bc, font_6108);
            print_8x8_BE7F(digitL, bc+1, font_6108);
        }
        mainMemory.setByteAt(0x5AEF, 0x47);
        mainMemory.setByteAt(0x5AEF+1, 0x47);
    }

    private void print_lives_gfx_BC7A() {
        sprite_scratchpad_BFDB.reset();
        sprite_scratchpad_BFDB.set(sprite_scratchpad_BFDB.start, 0x8C);
        sprite_scratchpad_BFDB.set(sprite_scratchpad_BFDB.start + 7, 0);
        sprite_scratchpad_BFDB.set(sprite_scratchpad_BFDB.start + 0x1A, 0x10);
        sprite_scratchpad_BFDB.set(sprite_scratchpad_BFDB.start + 0x1B, 0x20);
        print_sprite_D718(sprite_scratchpad_BFDB, sprite_scratchpad_BFDB.start);
        for(int i=0; i<2; i++) mainMemory.setByteAt(0x5A42+i, 0x47);
        for(int i=0; i<4; i++) mainMemory.setByteAt(0x5A62+i, 0x47);
    }

    private void print_lives_BCA3() {
        print_BCD_number_BCAE(0xDDD7, new int[] {variables.getU(0x5BBA)});
    }

    private void list_objects_to_draw_CE62() {
        objects_to_draw_CE8B.reset();
        int ix = 0x5C08;
        int hl = objects_to_draw_CE8B.start;
        final int cnt = 40;
        for(int i=0; i<cnt; i++) {
            if(graphic_objs_tbl_5C08.getU(ix + i*32) != 0 && (graphic_objs_tbl_5C08.getU(ix + i*32 + 7) & 0x10) > 0) {
                objects_to_draw_CE8B.set(hl, i);
                hl++;
            }
        }
        objects_to_draw_CE8B.set(hl, 0xFF); //flag end of list
    }

    private void calc_display_order_and_render_CEBB() {
        variables.set(0x5BBE, 0); // rendered objects cnt
        int de1 = objects_to_draw_CE8B.start;
        int i = de1;
        while(objects_to_draw_CE8B.getU(i) !=0xFF) i++;
        int cntToRender = i - de1;
        RenderStack renderList = new RenderStack();
        while(variables.getU(0x5BBE) < cntToRender) {
            boolean rendered1 = (objects_to_draw_CE8B.getU(de1) & 0x80) > 0;
            if(!rendered1) {
                boolean isLast = variables.getU(0x5BBE) >= cntToRender-1;
                // if only one object remains, we should render it and exit
                if(isLast)
                    renderOne(de1, renderList);
                else {
                    int de2 = objects_to_draw_CE8B.start;
                    boolean doneInnerLoop = false;
                    boolean rendered = false;
                    boolean savedToList = false;
                    while (objects_to_draw_CE8B.getU(de2) != 0xFF && !doneInnerLoop) {
                        boolean rendered2 = (objects_to_draw_CE8B.getU(de2) & 0x80) > 0;
                        if (!rendered2 && de1 != de2) {
                            boolean is2behind1 = is2behind1(de1, de2);
                            if (is2behind1) {
                                if (renderList.contains(objects_to_draw_CE8B.getU(de2))) {
                                    renderOne(de2, renderList);
                                    rendered = true;
                                } else {
                                    renderList.add(objects_to_draw_CE8B.getU(de2));
                                    savedToList = true;
                                }
                                doneInnerLoop = true;
                            }
                        }
                        if (!doneInnerLoop) de2++;
                    }
                    // Analyze inner loop results
                    if (savedToList)
                        de1 = de2; // since 2 is behind 1 we should now examine 2 if there is anything behind it. If not it should be rendered later
                    else if (rendered)
                        de1 = objects_to_draw_CE8B.start; // restart the loop after rendering (the rendered object will be ignored)
                    else {
                        // render if nothing changed since last iteration (either stack is empty or last added object is the analyzed object
                        if (renderList.isEmpty() || renderList.isLast(objects_to_draw_CE8B.getU(de1)))
                            renderOne(de1, renderList);
                        de1++;
                    }
                }
            } else de1++;
            // restart loop if not all objects are rendered
            if(objects_to_draw_CE8B.getU(de1) == 0xFF) de1 = objects_to_draw_CE8B.start;
        }
    }

    private void renderOne(int de, RenderStack renderList) {
        int objectLoc = objects_to_draw_CE8B.getU(de) * 32 + graphic_objs_tbl_5C08.start;
        if(calc_pixel_XY_D6C9(graphic_objs_tbl_5C08, objectLoc)) print_sprite_D718(graphic_objs_tbl_5C08, objectLoc);
        objects_to_draw_CE8B.set(de, objects_to_draw_CE8B.getU(de) | 0x80);
        variables.set(0x5BBE, variables.getU(0x5BBE) + 1);
        renderList.reset();
    }

    private boolean is2behind1(int de1, int de2) {
        if(de1==de2) return false;

        int objectLoc1 = objects_to_draw_CE8B.getU(de1) * 32 + graphic_objs_tbl_5C08.start;
        int objectLoc2 = objects_to_draw_CE8B.getU(de2) * 32 + graphic_objs_tbl_5C08.start;

        int x1 = graphic_objs_tbl_5C08.getU(objectLoc1 + 1);
        int y1 = graphic_objs_tbl_5C08.getU(objectLoc1 + 2);
        int z1 = graphic_objs_tbl_5C08.getU(objectLoc1 + 3);
        int w1 = graphic_objs_tbl_5C08.getU(objectLoc1 + 4);
        int d1 = graphic_objs_tbl_5C08.getU(objectLoc1 + 5);
        int h1 = graphic_objs_tbl_5C08.getU(objectLoc1 + 6);

        int x2 = graphic_objs_tbl_5C08.getU(objectLoc2 + 1);
        int y2 = graphic_objs_tbl_5C08.getU(objectLoc2 + 2);
        int z2 = graphic_objs_tbl_5C08.getU(objectLoc2 + 3);
        int w2 = graphic_objs_tbl_5C08.getU(objectLoc2 + 4);
        int d2 = graphic_objs_tbl_5C08.getU(objectLoc2 + 5);
        int h2 = graphic_objs_tbl_5C08.getU(objectLoc2 + 6);

        boolean overlap1 = z1 >= (z2+h2); // $CF05 JR NC,$CF16    ; no overlap (C+=0)
        boolean overlap2 = z2 >= (z1+h1); // $CF12 JR C,$CF15     ; overlap (C+=1)
        boolean overlap3 = (y1-d1) >= (y2+d2); // $CF23 SUB L          ; Y1-D1-(Y2+d2)
        boolean overlap4 = (y2-d2) >= (y1+d1); // $CF33 SUB L          ; Y2-D2-(Y1+D1)
        boolean overlap5 = (x1-w1) >= (x2+w2); // $CF49 SUB L          ; X1-W1-(X2+W2)
        boolean overlap6 = (x2-w2) >= (x1+w1); // $CF59 SUB L          ; X2-W2-(X1+W1)
        int c=0;

        if(!overlap1)
            if(!overlap2) c+=1;
            else c+=2;
        if(!overlap3)
            if(!overlap4) c+=3;
            else c+=6;
        if(!overlap5)
            if(!overlap6) c+=9;
            else c+=18;

        boolean isBehind = c==3 || c==4 || c==6 || c==7 || c==12 || c==15 || c==16;
        return isBehind;
    }

    private void display_panel_D255() {
        sprite_scratchpad_BFDB.reset();
        int ix = sprite_scratchpad_BFDB.start;
        int hl = panel_data_D27E.start;
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 16, -8, 5, ix, hl, panel_data_D27E);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, ix, hl, panel_data_D27E);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, ix, hl, panel_data_D27E);
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 16, 8, 5, ix, hl, panel_data_D27E);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, ix, hl, panel_data_D27E);
        transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, ix, hl, panel_data_D27E);
    }

    private void display_sun_moon_frame_C3A4() {
        if(variables.getU(0x5BC3)!=0) return;

        int ix = sun_moon_scratchpad_C44D.start;
        // @label=display_frame
        int x = sun_moon_scratchpad_C44D.getU(ix + 0x1A);
        if (x == 0xE1 - 1) toggle_day_night_C3FF();

        int y = ((x + 0x10) >> 2) & 0x0F;
        int hl = 0xC440 + y;
        sun_moon_scratchpad_C44D.set(ix + 0x1B, sun_moon_yoff_C440.getU(hl));
        if (sunTick == 0) sun_moon_scratchpad_C44D.set(ix + 0x1A, x + 1);

        // @label=display_frame
        fill_window_C515(shadowMemory, 0xD90A, 6, 0x1F, 0);
        print_sprite_D718(sun_moon_scratchpad_C44D, ix);

        ix = sprite_scratchpad_BFDB.start;
        sprite_scratchpad_BFDB.set(ix + 7, 0);
        sprite_scratchpad_BFDB.set(ix, 0x5A);
        sprite_scratchpad_BFDB.set(ix + 0x1A, 0xB8);
        sprite_scratchpad_BFDB.set(ix + 0x1B, 0);
        print_sprite_D718(sprite_scratchpad_BFDB, ix);
        sprite_scratchpad_BFDB.set(ix + 0x1A, 0xD0);
        sprite_scratchpad_BFDB.set(ix, 0xBA);
        print_sprite_D718(sprite_scratchpad_BFDB, ix);
    }

    // ; Input:HL starting location B  width (bytes) C  height (lines), A - value
    private void fill_window_C515(VideoMemory memory, int hl, int b, int c, int a) {
        for(int y = 0; y<c; y++) {
            for(int x = 0; x<b; x++) {
                memory.setByteAt(hl + x + y*0x20, a);
            }
        }
    }

    private void toggle_day_night_C3FF() {
        int ix = sun_moon_scratchpad_C44D.start;
        int currentSprite =  sun_moon_scratchpad_C44D.getU(ix);
        int nextSprite = currentSprite ^ 1; // flip last bit
        sun_moon_scratchpad_C44D.set(ix, nextSprite);
        sun_moon_scratchpad_C44D.set(ix+0x1A, 0xB0); // ; pixel X
        variables.set(0x5BB1, 1); // transform flag
        if((nextSprite & 1) == 0) {
            // DAA - number must be in BCD
            int incDays = incBCD(variables.getU(0x5BB9)) % 0xA0; // wrap days at 99
            variables.set(0x5BB9, incDays);
            if(incDays == MAX_DAYS_BCD+1) game_over_BA22();
            print_days_BC66();
        }
    }

    private int incBCD(int num) {
        int incNum = num+1;
        if((incNum & 0x0F) == 10) incNum = (incNum & 0xF0) + 0x10;
        return incNum;
    }


    private void update_sprite_loop_AFC7(DataBlock block) {
        // block contains sprite metadata (32 bytes each)
        int ix = block.start;
        while(ix < block.endExcl()) {
            updateOneSprite(block, ix);
            if(exitingScreen) return;
            ix+=32;
        }
    }

    private void updateOneSprite(DataBlock block, int ix) {
        save_2d_info_CE49(ix, block);
        // @label=upd_sprite_jmp_tbl
        switch(block.getU(ix)) {
            case 0x00, 0x01, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0xBA: break;
            case 0x02, 0x04: upd_2_4_C73C(block, ix); break;
            case 0x03, 0x05: upd_3_5_C722(block, ix); break;
            case 0x06, 0x07: upd_6_7_C4E3(block, ix); break;
            case 0x08: upd_8_C65E(block, ix); break;
            case 0x09: upd_9_C6BD(block, ix); break;
            case 0x0A: upd_10_C4E8(block, ix); break;
            case 0x0B: upd_11_C4ED(block, ix); break;
            case 0x0C, 0x0D, 0x0E, 0x0F: upd_12_to_15_C4F2(block, ix); break;
            case 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D: upd_16_to_21_24_to_29_C823(block, ix); break;
            case 0x16: upd_22_B7A3(block, ix); break;
            case 0x17: upd_23_B7E7(block, ix); break;
            case 0x1E, 0x1F, 0x9E, 0x9F: upd_30_31_158_159_B9A5(block, ix); break;
            case 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F: upd_32_to_47_CDDA(block, ix); break;
            case 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D: upd_48_to_53_56_to_61_C828(block, ix); break;
            case 0x36: upd_54_B6B9(block, ix); break;
            case 0x37: upd_55_B6B1(block, ix); break;
            case 0x3E: upd_62_C4AA(block, ix); break;
            case 0x3F: upd_63_B7A9(block, ix); break;
            case 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F: upd_64_to_79_CDDF(block, ix); break;
            case 0x52, 0x53, 0x54, 0x55: upd_80_to_83_C5C8(block, ix); break;
            case 0x56, 0x57: upd_86_87_B7ED(block, ix); break;
            case 0x58, 0x59, 0x5A: upd_88_to_90_C506(block, ix); break;
            case 0x5B: upd_91_B683(block, ix); break;
            case 0x5C, 0x5D, 0x5E, 0x5F: upd_92_to_95_C337(block, ix); break;
            case 0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66: upd_96_to_102_C28B(block, ix); break;
            case 0x67: upd_103_C1AB(block, ix); break;
            case 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E: upd_120_to_126_BEFE(block, ix); break;
            case 0x7F: upd_127_BF11(block, ix); break;
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

            default: IO.println("Not updated: %02x (%d)".formatted(block.getU(ix),block.getU(ix))); break;
        }
    }

    private void upd_2_4_C73C(DataBlock block, int ix) {
        boolean hFlip = (block.getU(ix + 0x07) & 0x40) > 0; // BIT 6,(IX+$07)
        int centreX, centreY;
        if(hFlip) {
            // LD HL,$FEEF
            block.set(ix + 0x12, -17); // L=x - EF
            block.set(ix + 0x13, -2); // H=y - FE
            int x = block.getU(ix + 1) - 0x0D;
            block.set(ix + 9, x); // dX
            int y = block.getU(ix + 2);
            block.set(ix + 0x0A, y); // dY=Y
            // LD HL,$0F06   ; +15, +6
            centreY = 15;
            centreX = 6;
        } else {
            if(block.getU(ix) == 4) {
                //@label=adj_m3_p1
                //c$C737 LD HL,$FD01   ;
                block.set(ix + 0x12, 1); // L=x - 01
                block.set(ix + 0x12, -3); // H=y - FD
            } else {
                // LD HL,$FDF9    ; -3, -7
                block.set(ix + 0x12, -7); // F9
                block.set(ix + 0x13, -3); // FD
            }
            int y = block.getU(ix + 2) + 0x0D;
            block.set(ix + 0x0A, y); // dY
            int x = block.getU(ix + 1);
            block.set(ix + 9, x); // dX
            // LD HL,$060F   ; +6, +15
            centreY = 6;
            centreX = 15;
        }
        int z = block.getU(ix + 3);
        block.set(ix + 0x0B, z); // dZ=Z
        chk_plyr_spec_near_arch_C7DB(block, ix, centreY, centreX);
        // TODO: implement $C785 (check special objects)
    }

    // centre Y -> H, centre x -> L
    private void chk_plyr_spec_near_arch_C7DB(DataBlock block, int ix, int centreY, int centreX) {
        int iy = 0x5C08; // start of graphics table, first two objects are player top and bottom
        for(int objToCheck = 0; objToCheck < 4; objToCheck++) {
            int grNo = graphic_objs_tbl_5C08.getU(iy);
            if(grNo!=0) {
                boolean autoAdjust = graphic_objs_tbl_5C08.isSet(3, iy + 7);
                if(autoAdjust) {
                    // NOTE: we compare unsigned values - the dX, dY and dZ values are set as temporary variables and contain x, y and z
                    int archCentreX = block.getU(ix + 0x09);
                    int archCentreY = block.getU(ix + 0x0A);
                    int archCentreZ = block.getU(ix + 0x0B);
                    int objX = graphic_objs_tbl_5C08.getU(iy + 0x01);
                    int objY = graphic_objs_tbl_5C08.getU(iy + 0x02);
                    int objZ = graphic_objs_tbl_5C08.getU(iy + 0x03);

                    //@label=is_near_to
                    if((Math.abs(archCentreX - objX) < centreX) && (Math.abs(archCentreY - objY) < centreY) &&
                            (Math.abs(archCentreZ - objZ) < 4)) {
                        // C7F5 SET 0,(IY+$07)
                        // IO.println("is near");
                        graphic_objs_tbl_5C08.setBit(0, iy + 0x07);
                    }
                }
            }
            iy += 0x20;
        }
    }

    private void upd_3_5_C722(DataBlock block, int ix) {
        boolean hFlip = (block.getU(ix + 0x07) & 0x40) > 0; // BIT 6,(IX+$07)
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

    private void upd_16_to_21_24_to_29_C823(DataBlock block, int ix) {
        // c$C4DD LD HL,$FAF4   ; -6, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -6); //FA
        upd_player_bottom_C82B(block, ix);
    }

    private void upd_player_bottom_C82B(DataBlock block, int ix) {
        boolean flag = block.isSet(6, ix + 0x0D);
        if(flag) {
            if (variables.getU(0x5BC3) == 0) {
                block.setBit(6, ix + 0x2D); // SET 6,(IX+$2D)
                init_death_sparkles_BF21(block, ix);
                return;
            }
        }
        // @label=loc_C83E
            // c$C83E CALL $C306    ;
        // Note: this behavior comes from increasing SP twice when transformation starts ($C316-$C317
        boolean transformStarted = chk_and_init_transform_C306(block, ix);
        if(!transformStarted) {
        // $C841 CALL $D022    ; check_user_input
            check_user_input_D022(); // NOTE: keys are in variable 0x5BB5
            // TODO: implement rest of the routine
            // $C844 CALL $C00E    ; handle_pickup_drop
            // $C847 CALL $C89F    ; handle_left_right
            handle_left_right_C89F(block, ix);
            // $C84A CALL $C948    ; handle_jump
            handle_jump_C948(block, ix);
            // $C84D CALL $C969    ; handle_forward
            handle_forward_C969(block, ix);
            // $C850 CALL $C87A    ; chk_plyr_OOB (out of bounds)
            boolean oob = chk_plyr_OOB_C87A(block, ix);
            // $C853 JR NC,$C86D   ; plyr_OOB
            // @label=plyr_OOB
            if(oob && block.getS(ix + 0x0B) > 0) block.set(ix + 0x0B, 0);
            loc_C855(block, ix) ;
        }
    }

    private boolean chk_plyr_OOB_C87A(DataBlock block, int ix) {
        int roomSizeX = variables.getU(0x5BAB);
        int roomSizeY = variables.getU(0x5BAC);
        int xMax = roomSizeX - block.getU(ix + 0x04);
        int yMax = roomSizeY - block.getU(ix + 0x05);

        int xCalc = Math.abs(block.getU(ix + 0x01) - 0x80);
        int yCalc = Math.abs(block.getU(ix + 0x02) - 0x80);

        // True if player is outside the room (entering or exitin
        return xCalc >= xMax || yCalc >= yMax;
    }

    private void loc_C855(DataBlock block, int ix) {
        block.setBit(1, ix + 0x27);
        move_player_C9A1(block, ix);
        block.resetBit(1,ix + 0x27);
        int flags = block.getU(ix + 0x0C);
        if(flags>=0x10) block.set(ix + 0x0C, flags-0x10); // decrement entering screen counter (upper 4 bits)
        set_wipe_and_draw_flags_C692(block, ix);
    }

    private void move_player_C9A1(DataBlock block, int ix) {
        if(variables.getU(0x5BC4)!=0) block.set(ix + 0x0B, 2); // D (IX+$0B),$02 ; dZ=2
        int flags = block.getU(ix + 0x0C); // LD A,(IX+$0C)  ; flags12
        boolean jumping = block.isSet(3, ix + 0x0C);
        if(jumping || ((flags & 0xF0) != 0) || variables.isSet(2, 0x5BB5)) calc_plyr_dXY_C9FB(block, ix);

        int dz = block.getS(ix + 0x0B);
        if(dz < 0) dz -= 2;
        else
            if(jumping) dz -= 1;
            else dz -= 2;
        block.set(ix + 0x0B, dz);
        variables.set(0x5BC1, dz);
        //$C9D6 CALL M,$B451   ; ignore audio

        //$C9D9 CALL $CB45     ;
        adj_for_out_of_bounds_CB45(block, ix);
        //$C9DC CALL $CA70     ;
        handle_exit_screen_CA70(block, ix);
        // NOTE: values 0xFF in X and Y are flags indicating which arch user exited the room
        if(!exitingScreen) {
            add_dXYZ_C706(block, ix);
            if (block.isSet(2, ix + 0x0C)) {
                int tmpDz = variables.getS(0x5BC1);
                if (tmpDz < 0) block.resetBit(3, ix + 0x0C);
            }
            block.set(ix + 0x09, 0);
            block.set(ix + 0x0A, 0);
        }
    }

    private void adj_for_out_of_bounds_CB45(DataBlock block, int ix) {
        if(block.isSet(1, ix + 0x07)) return;
        block.setBit(1, ix + 0x07);
        int flags2 = block.getU(ix + 0x0C);
        flags2 &= 0xF8; // ; clear X,Y,Z OOB
        block.set(ix + 0x0C, flags2);
        int dzAdj = block.getS(ix + 0x0B); // H
        if(dzAdj != 0) {
            dzAdj = adj_dZ_for_out_of_bounds_CA5A(block, ix);
            if(dzAdj!=0) dzAdj = adj_dZ_for_obj_intersect_CC38(block, ix, 0, 0, dzAdj);
        }
        int dxAdj = block.getS(ix + 0x09); // C
        if(dxAdj != 0) {
            dxAdj = adj_dX_for_out_of_bounds_CCDD(block, ix);
            if(dxAdj!=0) dxAdj = adj_dX_for_obj_intersect_CB9A(block, ix, dxAdj, 0, dzAdj);
        }
        int dyAdj = block.getS(ix + 0x0A); // L
        if(dyAdj != 0) {
            dyAdj = adj_dY_for_out_of_bounds_CD08(block, ix);
            if(dyAdj!=0) dyAdj = adj_dY_for_obj_intersect_CBE9(block, ix, dxAdj, dyAdj, dzAdj);
        }

        block.resetBit(1, ix + 0x07);

        block.set(ix + 0x09, dxAdj);
        block.set(ix + 0x0A, dyAdj);
        block.set(ix + 0x0B, dzAdj);
    }

    private void handle_exit_screen_CA70(DataBlock block, int ix) {
        int flags = block.getU(ix + 0x0C);
        boolean enteringRoom = (flags & 0xF0)>0;
        if(enteringRoom) return;
        boolean nearArch = block.isSet(0, ix + 0x07);
        if(!nearArch) return;

        block.resetBit(0, ix + 0x07);

        // $CA82 LD HL,($5BAB)  ;
        // $CA85 PUSH HL        ;
        int oldRoomSizeX = variables.getU(0x5BAB); // L
        int oldRoomSizeY = variables.getU(0x5BAC); // H

        //@label=screen_move_tbl
        //b$CA92 DEFW $CA9A
        //$CA94 DEFW $CAF3
        //$CA96 DEFW $CB0E
        //$CA98 DEFW $CB29
        int currentScreen = block.getU(ix + 0x08);
        int newScreen = switch(get_sprite_dir_CA1E(block, ix)) {
            case 0 -> screen_west_CA9A(block, ix, oldRoomSizeX, oldRoomSizeY);
            case 1 -> screen_east_CAF3(block, ix, oldRoomSizeX, oldRoomSizeY);
            case 2 -> screen_north_CB0E(block, ix, oldRoomSizeX, oldRoomSizeY);
            case 3 -> screen_south_CB29(block, ix, oldRoomSizeX, oldRoomSizeY);
            default -> currentScreen;
        };

        if(currentScreen!=newScreen) {
            // @label=exit_screen
            int grNo = block.getU(ix);
            if(grNo - 0x10 > 0x40) return;
            IO.println(String.format("EXIT: %d (%02X) -> %d (%02X)",currentScreen,currentScreen,newScreen,newScreen));
            exitingScreen = true; //TODO: rethink if this is the correct way to flag this
            block.set(ix + 0x08, newScreen);
            block.set(ix + 0x0C, block.getU(ix + 0x0C) | 0x30);
            copyPlayerData();
            byte_D171.set(0xD171, plyr_spr_1_scratchpad_D161.getU(0xD161));
            byte_D191.set(0xD191, plyr_spr_2_scratchpad_D181.getU(0xD181));
            plyr_spr_1_scratchpad_D161.set(0xD161, 0x78); // sparkly transform #1
            plyr_spr_2_scratchpad_D181.set(0xD181, 0x78);
        }
    }

    private void copyPlayerData() {
        // This is the opposite of the initPlayerData
        int de = 0x5C08;
        for(int i=0; i<plyr_spr_1_scratchpad_D161.size; i++) plyr_spr_1_scratchpad_D161.set(plyr_spr_1_scratchpad_D161.start+i, graphic_objs_tbl_5C08.getU(de+i));
        de+=plyr_spr_1_scratchpad_D161.size;
        for(int i=0; i<start_loc_1_D169.size; i++) start_loc_1_D169.set(start_loc_1_D169.start+i, graphic_objs_tbl_5C08.getU(de+i));
        de+=start_loc_1_D169.size;
        for(int i=0; i<flags12_1_D16D.size; i++) flags12_1_D16D.set(flags12_1_D16D.start+i, graphic_objs_tbl_5C08.getU(de+i));
        de+=flags12_1_D16D.size;
        for(int i=0; i<byte_D171.size; i++) byte_D171.set(byte_D171.start+i, graphic_objs_tbl_5C08.getU(de+i));
        de+=byte_D171.size;
        for(int i=0; i<plyr_spr_2_scratchpad_D181.size; i++) plyr_spr_2_scratchpad_D181.set(plyr_spr_2_scratchpad_D181.start+i, graphic_objs_tbl_5C08.getU(de+i));
        de+=plyr_spr_2_scratchpad_D181.size;
        for(int i=0; i<start_loc_2_D189.size; i++) start_loc_2_D189.set(start_loc_2_D189.start+i, graphic_objs_tbl_5C08.getU(de+i));
        de+=start_loc_2_D189.size;
        for(int i=0; i<byte_D191.size; i++) byte_D191.set(byte_D191.start+i, graphic_objs_tbl_5C08.getU(de+i));
    }


    private int screen_west_CA9A(DataBlock block, int ix, int oldRoomSizeX, int oldRoomSizeY) {
        int currentScreen = block.getU(ix + 0x08);
        oldRoomSizeX = 0x80 - oldRoomSizeX;
        int playerX = block.getU(ix + 0x01) + block.getS(ix + 0x09) + block.getU(ix + 0x04);
        if(oldRoomSizeX < playerX) return currentScreen;

        block.set(ix + 0x01, 0); // x=0 - used later when determining which arch the player enters the room
        //@label=screen_e_w
        int newScreen = ((currentScreen & 0x0F) - 1) | (currentScreen & 0xF0); // do not change row
        return newScreen;
    }

    private int screen_east_CAF3(DataBlock block, int ix, int oldRoomSizeX, int oldRoomSizeY) {
        int currentScreen = block.getU(ix + 0x08);
        oldRoomSizeX = 0x80 + oldRoomSizeX;
        int playerX = block.getU(ix + 0x01) + block.getS(ix + 0x09) - block.getU(ix + 0x04);
        if(oldRoomSizeX > playerX) return currentScreen;

        block.set(ix + 0x01, 0xFF); // x=0xFF
        //@label=screen_e_w
        int newScreen = ((currentScreen & 0x0F) + 1) | (currentScreen & 0xF0); // do not change row
        return newScreen;
    }

    private int screen_north_CB0E(DataBlock block, int ix, int oldRoomSizeX, int oldRoomSizeY) {
        int currentScreen = block.getU(ix + 0x08);
        oldRoomSizeY = 0x80 + oldRoomSizeY;
        int playerY = block.getU(ix + 0x02) + block.getS(ix + 0x0A) - block.getU(ix + 0x05);
        if(oldRoomSizeY > playerY) return currentScreen;

        block.set(ix + 0x02, 0xFF); // y=0xFF
        int newScreen = (currentScreen + 0x10) & 0xFF;
        return newScreen;
    }

    private int screen_south_CB29(DataBlock block, int ix, int oldRoomSizeX, int oldRoomSizeY) {
        int currentScreen = block.getU(ix + 0x08);
        oldRoomSizeY = 0x80 - oldRoomSizeY;
        int playerY = block.getU(ix + 0x02) + block.getS(ix + 0x0A) + block.getU(ix + 0x05);
        if(oldRoomSizeY < playerY) return currentScreen;

        block.set(ix + 0x02, 0); // y=0
        int newScreen = (currentScreen - 0x10) & 0xFF;
        return newScreen;
    }

    private boolean is_object_not_ignored_B538(DataBlock block, int iy) {
        return block.getU(iy) != 0 && !block.isSet(1, iy + 0x07);
    }

    //; abs(objX+newdX-thisX)-(objW+thisW)
    private boolean do_objs_intersect_on_x_CC9D(DataBlock block, int ix, int iy, int dxAdj) {
        // dxAdj - C register
        // ix: obj, iy: this
        int objW = block.getU(ix + 0x04);
        int thisW = block.getU(iy + 0x04);
        int objX = block.getU(ix + 0x01);
        int thisX = block.getU(iy + 0x01);
        return Math.abs(objX + dxAdj - thisX) - (objW + thisW) < 0;
    }

    //; (objY+l-thisY)-(objD+thisD) - note: there is an error in comment in original source
    private boolean do_objs_intersect_on_y_CCB2(DataBlock block, int ix, int iy, int dyAdj) {
        // dyAdj - L register
        // ix: obj, iy: this
        int objD = block.getU(ix + 0x05);
        int thisD = block.getU(iy + 0x05);
        int objY = block.getU(ix + 0x02);
        int thisY = block.getU(iy + 0x02);
        return Math.abs(objY + dyAdj - thisY) - (objD + thisD) < 0;
    }

    //; (objZ+H-thisZ)-(objH or thisH)
    private boolean do_objs_intersect_on_z_CCC7(DataBlock block, int ix, int iy, int dzAdj) {
        // dzAdj - H register
        // ix: obj, iy: this
        int objZ = block.getU(ix + 0x03);
        int thisZ = block.getU(iy + 0x03);
        int objH = block.getU(ix + 0x06);
        int thisH = block.getU(iy + 0x06);
        return Math.abs(objZ + dzAdj - thisZ) - (Math.min(objH, thisH)) < 0;
    }

    private void setDestroyedFlags(DataBlock block, int ix, int iy) {
        // TODO: replace this with proper configuration
        if(skipDeath) return;

        int flagsObj1 = block.getU(ix + 0x0D);
        int flagsObj2 = block.getU(iy + 0x0D);
        int obj2DestroyedFlags = ((flagsObj1 >> 1) & 0x40) | flagsObj2; // bit 7->6
        block.set(iy + 0x0D, obj2DestroyedFlags);
        int obj1DestroyedFlags = (obj2DestroyedFlags << 1) & 0x40 | flagsObj1; // bit 5->6
        block.set(ix + 0x0D, obj1DestroyedFlags);
    }

    private int adj_dZ_for_obj_intersect_CC38(DataBlock block, int ix, int dxAdj, int dyAdj, int dzAdj) {
        int iy = graphic_objs_tbl_5C08.start;
        for(int i=0; i<0x28; i++) {
            if(is_object_not_ignored_B538(block, iy)) {
                //IO.println("Analyze: "+i+":"+block.getU(iy));
                if (do_objs_intersect_on_x_CC9D(block, ix, iy, dxAdj) && do_objs_intersect_on_y_CCB2(block, ix, iy, dyAdj)) {
                    while (do_objs_intersect_on_z_CCC7(block, ix, iy, dzAdj)) {
                        //IO.println("Intersects " + i + ":" + block.getU(iy));
                        block.setBit(2, ix + 0x0C); // set Z OOB

                        setDestroyedFlags(block, ix, iy);

                        block.setBit(3, iy + 0x0D); //triggered (falling, collapsing blocks)
                        boolean movable = block.isSet(2, ix + 0x07);
                        if (movable) {
                            if (block.getS(ix + 0x09) == 0) block.set(ix + 0x09, block.getS(iy + 0x09)); //copy dX
                            if (block.getS(ix + 0x0A) == 0) block.set(ix + 0x0A, block.getS(iy + 0x0A)); //copy dY
                        }
                        dzAdj = adj_d_for_out_of_bounds_CA89(dzAdj);
                        if (dzAdj == 0) return dzAdj;
                    }
                }
            }
            iy += 0x20;
        }
        return dzAdj;
    }

    private int adj_dX_for_obj_intersect_CB9A(DataBlock block, int ix, int dxAdj, int dyAdj, int dzAdj) {
        int iy = graphic_objs_tbl_5C08.start;
        for(int i=0; i<0x28; i++) {
            if(is_object_not_ignored_B538(block, iy)) {
                //IO.println("Analyze: "+i+":"+block.getU(iy));
                if(do_objs_intersect_on_y_CCB2(block, ix, iy, dyAdj) && do_objs_intersect_on_z_CCC7(block, ix, iy, dzAdj)) {
                    while(do_objs_intersect_on_x_CC9D(block, ix, iy, dxAdj)) {
                        //IO.println("Intersects " + i + ":" + block.getU(iy));
                        block.setBit(0, ix + 0x0C); // set X OOB

                        setDestroyedFlags(block, ix, iy);

                        boolean movable = block.isSet(2, ix + 0x07);
                        if(movable) {
                            block.set(iy + 0x09, block.getS(ix + 0x09)); //copy dX
                        }
                        dxAdj = adj_d_for_out_of_bounds_CA89(dxAdj);
                        if (dxAdj == 0) return dxAdj;
                    }
                }

            }
            iy += 0x20;
        }
        return dxAdj;
    }

    private int adj_dY_for_obj_intersect_CBE9(DataBlock block, int ix, int dxAdj, int dyAdj, int dzAdj) {
        int iy = graphic_objs_tbl_5C08.start;
        for(int i=0; i<0x28; i++) {
            if(is_object_not_ignored_B538(block, iy)) {
                //IO.println("Analyze: "+i+":"+block.getU(iy));
                if(do_objs_intersect_on_x_CC9D(block, ix, iy, dxAdj) && do_objs_intersect_on_z_CCC7(block, ix, iy, dzAdj)) {
                    while(do_objs_intersect_on_y_CCB2(block, ix, iy, dyAdj)) {
                        //IO.println("Intersects " + i + ":" + block.getU(iy));
                        block.setBit(1, ix + 0x0C); // set Y OOB

                        setDestroyedFlags(block, ix, iy);

                        boolean movable = block.isSet(2, ix + 0x07);
                        if(movable) {
                            block.set(iy + 0x0A, block.getS(ix + 0x0A)); //copy dY
                        }
                        dyAdj = adj_d_for_out_of_bounds_CA89(dyAdj);
                        if (dyAdj == 0) return dyAdj;

                    }
                }
            }
            iy += 0x20;
        }
        return dyAdj;
    }

    private int adj_dZ_for_out_of_bounds_CA5A(DataBlock block, int ix) {
        int roomSize = variables.getU(0x5BAE);
        int dz = block.getS(ix + 0x0B);
        int z = block.getU(ix + 0x03);
        while(true) {
            if (z + dz >= roomSize) return dz;
            block.setBit(2, ix + 0x0C);
            dz = adj_d_for_out_of_bounds_CA89(dz);
            if (dz == 0) return dz;
        }
    }

    private int adj_dX_for_out_of_bounds_CCDD(DataBlock block, int ix) {
        int dx = block.getS(ix + 0x09);
        int flags = block.getU(ix + 0x0C);
        boolean enteringRoom = (flags & 0xF0)>0;
        if(enteringRoom) return dx;
        boolean nearArch = block.isSet(0, ix + 0x07);
        if(nearArch) return dx;

        int roomSizeX = variables.getU(0x5BAB);

        int x = block.getU(ix + 0x01);
        while(true) {
            // TODO: verify why there is infinite loop after changing rooms
            int a = x + dx - 0x80;
            if (a <= 0) a = -a;
            a += block.getU(ix + 0x04);
            if(a < roomSizeX) return dx;
            block.setBit(0, ix + 0x0C);
            dx = adj_d_for_out_of_bounds_CA89(dx);
        }
    }

    private int adj_dY_for_out_of_bounds_CD08(DataBlock block, int ix) {
        int dy = block.getS(ix + 0x0A);
        int flags = block.getU(ix + 0x0C);
        boolean enteringRoom = (flags & 0xF0)>0;
        if(enteringRoom) return dy;
        boolean nearArch = block.isSet(0, ix + 0x07);
        if(nearArch) return dy;

        int roomSizeY = variables.getU(0x5BAC);

        int y = block.getU(ix + 0x02);
        while(true) {
            int a = y + dy - 0x80;
            if (a <= 0) a = -a;
            a += block.getU(ix + 0x05);
            if(a < roomSizeY) return dy;
            block.setBit(1, ix + 0x0C);
            dy = adj_d_for_out_of_bounds_CA89(dy);
        }
    }

    private int adj_d_for_out_of_bounds_CA89(int a) {
        if(a==0) return 0;
        if(a<0) return a+1;
        return a-1;
    }

    private void add_dXYZ_C706(DataBlock block, int ix) {
        // skip this when entering room
        if(block.getU(ix + 0x01)==0xFF || block.getU(ix + 0x02)==0xFF) return;

        block.set(ix + 0x01, block.getU(ix + 0x01) + block.getS(ix + 0x09));
        block.set(ix + 0x02, block.getU(ix + 0x02) + block.getS(ix + 0x0A));
        block.set(ix + 0x03, block.getU(ix + 0x03) + block.getS(ix + 0x0B));
    }

    private void calc_plyr_dXY_C9FB(DataBlock block, int ix) {
        block.set(ix + 0x09, block.getS(ix + 0x09) + block.getS(ix + 0x0E)); // dX
        block.set(ix + 0x0A, block.getS(ix + 0x0A) + block.getS(ix + 0x0F)); // dY
        block.set(ix + 0x0E, 0); // dX_adj
        block.set(ix + 0x0F, 0); // dY_adj
        switch(get_sprite_dir_CA1E(block, ix)) {
            case 0: block.set(ix + 0x09, (block.getS(ix + 0x09) - 3)); break;
            case 1: block.set(ix + 0x09, (block.getS(ix + 0x09) + 3)); break;
            case 2: block.set(ix + 0x0A, (block.getS(ix + 0x0A) + 3)); break;
            case 3: block.set(ix + 0x0A, (block.getS(ix + 0x0A) - 3)); break;
        }
    }

    private int get_sprite_dir_CA1E(DataBlock block, int ix) {
        int flags = block.getU(ix + 0x07);
        flags = (flags >> 2) & 0x10;
        int grNo = (block.getU(ix) & 8) | flags;
        grNo = (grNo >> 3) & 3;
        return grNo;
    }

    private boolean chk_and_init_transform_C306(DataBlock block, int ix) {
        boolean transforming = variables.getU(0x5BB1) > 0;
        if(!transforming) return false;
        int a =  block.getU(ix + 0x0C) & 0xF0; // counter when entering the room
        if (a > 0) return false;
        boolean jumping = block.isSet(3, ix + 0x0C);
        if (jumping) return false;

        int sprite = block.getU(ix);
        variables.set(0x5BB1, sprite);
        block.set(ix + 0x10, 8); // transform counter

        block.set(ix + 0x20, 1); // update player top
        set_wipe_and_draw_flags_C692(block, ix + 0x20);
        //@label=rand_legs_sprite
        int rnd = new Random().nextInt(0xFF); //variables.get(0x5BA5);
        int newSprite = (rnd & 0x03) | 0x5C;
        if(block.getU(ix) == newSprite) newSprite^=1; // change if same as current
        block.set(ix, newSprite);
        int flip = (block.getU(ix + 0x07)) ^ 0x40;
        block.set(ix + 0x07, flip);

        return true;
    }

    private void check_user_input_D022() {
        // NOTE: we ignore other methods than keyboard
        // NOTE2: we do not poll from the queue here, we simply read what's in the queue. The main class takes care of key press/release event
        int c = 0;

        if(keyQueue.stream().anyMatch(k -> k == KeyEvent.VK_Z || k == KeyEvent.VK_C || k == KeyEvent.VK_B || k == KeyEvent.VK_M))
            c |= 0b00001;
        if(keyQueue.stream().anyMatch(k -> k == KeyEvent.VK_X || k == KeyEvent.VK_V || k == KeyEvent.VK_N))
            c |= 0b00010;
        if(keyQueue.stream().anyMatch(k -> k == KeyEvent.VK_A || k == KeyEvent.VK_S || k == KeyEvent.VK_D || k == KeyEvent.VK_F || k == KeyEvent.VK_G || k == KeyEvent.VK_H || k == KeyEvent.VK_J || k == KeyEvent.VK_K || k == KeyEvent.VK_L))
            c |= 0b00100;
        if(keyQueue.stream().anyMatch(k -> k == KeyEvent.VK_Q || k == KeyEvent.VK_W || k == KeyEvent.VK_E || k == KeyEvent.VK_R || k == KeyEvent.VK_T || k == KeyEvent.VK_Y || k == KeyEvent.VK_U || k == KeyEvent.VK_I || k == KeyEvent.VK_O || k == KeyEvent.VK_P))
            c |= 0b01000;
        if(keyQueue.stream().anyMatch(k -> k == KeyEvent.VK_1 || k == KeyEvent.VK_2 || k == KeyEvent.VK_3 || k == KeyEvent.VK_4 || k == KeyEvent.VK_5 || k == KeyEvent.VK_6 || k == KeyEvent.VK_7 || k == KeyEvent.VK_8 || k == KeyEvent.VK_9 || k == KeyEvent.VK_0))
            c |= 0b10000;
        if(c!=variables.getU(0x5BB5)) {
            variables.set(0x5BB5, c);
            //IO.println("Pressed: "+Integer.toBinaryString(c));
        }
    }

    private void handle_left_right_C89F(DataBlock block, int ix) {
        //NOTE: we ignore input method and react to keyboard only
        // @label=left_right_rotational
        int counter = block.getU(ix + 0x0D);
        if((counter & 0x07) > 0) { // ; too soon to turn again?
            block.set(ix + 0x0D, (counter & 0x07)-1);
            return;
        }
        int keys = variables.getU(0x5BB5);
        if((keys & 3) == 0) return; // ; left or right?

        int flags = block.getU(ix + 0x0C);
        if((flags & 0xF0) >0) return; // ; entering screen?
        if(DataBlock.isSetS(3, flags)) return; // ; already jumping?
        // $C911 CALL $B4C1     ;
        // ignore audio
        // @label=loc_C915
        counter |= 2; // ; init turning delay counter
        block.set(ix + 0x0D, counter);
        boolean right = (keys & 1)>0;
        boolean left = (keys & 2)>0;
        int flip = block.getU(ix + 0x07);
        boolean hflip = !DataBlock.isSetS(6, flip);
        if((right & hflip) || (left & !hflip)) {
            int sprite = block.getU(ix);
            sprite ^= 8;
            block.set(ix, sprite);
        }
        flip^= 0x40; // ; toggle hflip
        block.set(ix + 0x07, flip);
        int sprite = block.getU(ix);
        sprite += 0x10; // ; top half
        block.set(ix + 0x20, sprite); // ; set sprite for top half
    }

    private void handle_jump_C948(DataBlock block, int ix) {
        int flags = block.getU(ix + 0x0C);
        boolean enteringScreen = (flags & 0xF0) > 0;
        boolean jumping = DataBlock.isSetS(3, flags);
        boolean jump = DataBlock.isSetS(3, variables.getU(0x5BB5));
        if(!jump || enteringScreen || jumping) return;
        int dz = block.getS(ix + 0x0B);
        dz++;
        if(dz < -1) return;
        block.setBit(3, ix + 0x0C); // ; flag jumping
        block.set(ix + 0x0B, 8);
    }

    private void handle_forward_C969(DataBlock block, int ix) {
        int flags = block.getU(ix + 0x0C);
        boolean enteringScreen = (flags & 0xF0) > 0;
        boolean jumping = DataBlock.isSetS(3, flags);
        boolean forward = DataBlock.isSetS(2, variables.getU(0x5BB5));
        // ignore audio: @label=loc_C97A
        if(!enteringScreen && !jumping & !forward) {
            //@label=loc_C994
            int sprite = block.getU(ix) & 0x07;
            if(sprite == 2 || sprite == 4) return;
        }
        // @label=animate_human_legs
        int sprite = block.getU(ix);
        int nextSprite = ((sprite+1) & 7) % 6; // ; wrap?
        // @label=loc_C98B
        block.set(ix, (sprite & 0xF8) | nextSprite); //  ; update sprite - NOTE we update only bottom half
    }

    private void init_death_sparkles_BF21(DataBlock block, int ix) {
        //TODO: implement
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

    private void upd_32_to_47_CDDA(DataBlock block, int ix) {
        // c$C4F7 LD HL,$F8F4   ; -8, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -8); //F8
        upd_player_top_CDE2(block, ix);
    }

    private void upd_player_top_CDE2(DataBlock block, int ix) {
        if(variables.getU(0x5BC3)!=0) return;

        boolean flag = block.isSet(6,ix + 0x0D);
        if(flag) {
            init_death_sparkles_BF21(block, ix);
            return;
        }

        // Player top is +32 from block start, player bottom is at block start
        // ; copy x,y,z,w,d,h,flags
        for(int i=0; i<7; i++) block.set(ix+1+i, block.getU(ix-32+1+i));
        block.set(ix + 6, 0);
        block.setBit(1, ix + 0x07);
        int a =  block.getU(ix + 0x0D) & 0x0F;
        if(a == 0) { // look around again
            int rnd = new Random().nextInt(0xFF); // variables.get(0x5BA5);
            int sprite = block.getU(ix - 32); // bottom half
            if(rnd == 2) {
                sprite =  (sprite & 0xF8) | 6; // ; look one way
                block.set(ix + 0x0D, 8);
            } else if(rnd == 0xFE) {
                sprite = (sprite & 0xF8) | 7; // ; look the other way
                block.set(ix + 0x0D, 8);
            }
            //IO.println("upd_player_top_CDE2 sprite = " + sprite);
            block.set(ix, sprite + 0x10);
        } else {
            block.set(ix + 0x0D, a - 1);
        }
        int bottomY = block.getU(ix -32 + 3);
        block.set(ix + 3, bottomY + 0x0C);
    }

    private void upd_48_to_53_56_to_61_C828(DataBlock block, int ix) {
        // c$C4FC LD HL,$F9F4   ; -7, -12
        block.set(ix + 0x12, -12); // F4
        block.set(ix + 0x13, -7); // F9
        upd_player_bottom_C82B(block, ix);
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

    private void upd_64_to_79_CDDF(DataBlock block, int ix) {
        // c$C501 LD HL,$F4F4   ; -12, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -12); //F4
        upd_player_top_CDE2(block, ix);
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

    private void upd_88_to_90_C506(DataBlock block, int ix) {
        // c$C506 LD HL,$F4F0   ;
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -16); //F0
    }

    private void upd_91_B683(DataBlock block, int ix) {
        //LD HL,$F8F0   ; -8, -16
        block.set(ix + 0x12, -16); //F0
        block.set(ix + 0x13, -8); //F8
        // TODO: implement rest of routine
    }

    private void upd_92_to_95_C337(DataBlock block, int ix) {
        //c$C4ED LD HL,$FEF4   ; -2, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -2); //FE
        boolean flag = block.isSet(6, ix + 0x0D);
        if(flag) {
            if (variables.getU(0x5BC3) == 0) {
                init_death_sparkles_BF21(block, ix);
                return;
            }
        }
        //int rnd = new Random().nextInt(0xFF) & 3; //variables.get(0x5BA2);
        //if(rnd != 0) return;
        // No randomness
        if((tickNo % 3) != 0) return;
        int counter = block.getU(ix + 0x10) -1;
        block.set(ix + 0x10, counter);
        //IO.println("upd_92_to_95_C337 sprite = "+block.get(ix));
        //debugTableConsole("Graphics while transforming:", graphic_objs_tbl_5C08.start, graphic_objs_tbl_5C08.getCopy());

        if(counter == 0) {
            // End transformation
            int a = variables.getU(0x5BB1) ^ 0x20;
            block.set(ix, a);
            a += 0x10;
            block.set(ix + 0x20, a);
            variables.set(0x5BB1, 0);
            block.set(ix + 0x12, -12); //F4
            block.set(ix + 0x13, -6); //FA
            boolean spriteFlag = block.isSet(5, ix);
            if (spriteFlag) block.set(ix + 0x13, block.getS(ix + 0x13) - 1);
        } else {
            // Continue transformation
            int nextSprite = (new Random().nextInt(0xFF) & 3) | 0x5C;
            if(nextSprite != block.getU(ix)) nextSprite = nextSprite ^ 1;
            block.set(ix, nextSprite);
            block.set(ix + 7, block.getU(ix + 0x07) ^ 0x40);
        }
    }

    private void upd_96_to_102_C28B(DataBlock block, int ix) {
        //c$C4D8 LD HL,$FCF4   ; -4, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -4); //FC
        // TODO: implement rest of routine
    }

    private void upd_103_C1AB(DataBlock block, int ix) {
        //c$C4D3 LD HL,$FEF8   ; -2, -8
        block.set(ix + 0x12, -8); //F8
        block.set(ix + 0x13, -2); //FE
        // TODO: implement rest of routine
    }

    private void upd_120_to_126_BEFE(DataBlock block, int ix) {
        //c$C4D8 LD HL,$FCF4   ; -4, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -4); //FC
        block.set(ix, block.getU(ix)+1); // next sprite
        set_wipe_and_draw_flags_C692(block, ix);
        // TODO: implement rest of routine
    }

    private void set_wipe_and_draw_flags_C692(DataBlock block, int ix) {
        block.set(ix + 7, block.getU(ix + 0x07) | 0x30);
    }

    private void upd_127_BF11(DataBlock block, int ix) {
        //c$C4D8 LD HL,$FCF4   ; -4, -12
        block.set(ix + 0x12, -12); //F4
        block.set(ix + 0x13, -4); //FC
        block.resetBit(6,ix + 0x0D);
        block.set(ix, block.getU(ix + 0x10));
        updateOneSprite(block, ix); // update after sparkes change to player
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
            if(block.getU(ix)!=0) {
                if(calc_pixel_XY_D6C9(block, ix)) print_sprite_D718(block, ix);
            }
        }
    }

    private void build_screen_objects_D1E6() {
        if(variables.getU(0x5BB2) != 0) {
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
        variables.set(0x5BC0, graphic_objs_tbl_5C08.getU(0x5C10) & 1);

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
        int currLocId = graphic_objs_tbl_5C08.getU(ix + 8);

        while(hl < bc && !found) {
            int tableLocId = location_tbl_6251.getU(hl);
            found = tableLocId == currLocId;
            if(!found) {
                hl++;
                int size = location_tbl_6251.getU(hl);
                hl+=size;
            }

        }
        if(!found) {
            // This should be unreachable
            debugPanel2.append("Location not found: ERROR");
            graphic_objs_tbl_5C08.reset();
            return;
        }
        // @label=found_screen
        // HL points to start of the location in location table
        int roomId = location_tbl_6251.getU(hl);
        debugPanel2.append("Retrieved room: %d / %02x".formatted(roomId,roomId));
        hl++;
        int size = location_tbl_6251.getU(hl);
        int roomEnd = hl + size - 1;
        hl++;
        int attrOrig = location_tbl_6251.getU(hl);
        int attr = (attrOrig & 0x07) | 0x40;
        variables.set(0x5BAD, attr); // current room attributes (color)
        int roomSize = ((attrOrig >> 3) & 0x1F);
        variables.set(0x5BAB, room_size_tbl_6248.getU(room_size_tbl_6248.start + roomSize*3)); // room size X
        variables.set(0x5BAC, room_size_tbl_6248.getU(room_size_tbl_6248.start + roomSize*3 + 1)); // room size Y
        variables.set(0x5BAE, room_size_tbl_6248.getU(room_size_tbl_6248.start + roomSize*3 + 2)); // room size Z
        debugPanel2.append("Retrieved room size: "+roomSize+" X:"+variables.getU(0x5BAB)+" Y:"+variables.getU(0x5BAC)+" Z:"+variables.getU(0x5BAE));
        hl++; // background objects start

        // @label=next_bg_obj
        // decode all background objects
        // hl - iterates over room background objects (until end of room data or 0xFF separator)
        int targetAddr = 0x5C88;
        while(location_tbl_6251.getU(hl) != 0xFF && hl<=roomEnd) {
            DataBlock bkgObj = backgroundObjects[location_tbl_6251.getU(hl)];
            debugPanel2.append("Retrieved background object: %02x".formatted(location_tbl_6251.getU(hl)));
            int bkgAddr =  bkgObj.start;
            while(bkgObj.getU(bkgAddr) != 0) { // each object consists of 8-byte sprite info terminated by 0
                // 8 - byte sprite info
                debugPanel2.append("Retrieved sprite: %02x".formatted(bkgObj.getU(bkgAddr)));
                for(int i=0; i<8; i++) graphic_objs_tbl_5C08.set(targetAddr+i, bkgObj.getU(bkgAddr+i));
                bkgAddr+=8;
                // 9th byte
                graphic_objs_tbl_5C08.set(targetAddr+8, currLocId);
                for(int t=9; t<32; t++) graphic_objs_tbl_5C08.set(targetAddr+t, 0); // reset remaining info
                targetAddr+=32;
            }
            hl++;
        }

        hl ++;
        while(hl < roomEnd) {
            // @label=find_fg_objs
            int blockCtrl = location_tbl_6251.getU(hl);
            int blockCnt = (blockCtrl & 0x07) + 1;
            int blockType = (blockCtrl >> 3) & 0x1F;
            DataBlock blockDef = foregroundObjects[blockType];
            hl++;
            for(int i=0; i<blockCnt; i++) {
                int locByte = location_tbl_6251.getU(hl+i);
                int x = (locByte & 0b00000111);
                int y = (locByte & 0b00111000) >> 3;
                int z = (locByte & 0b11000000) >> 6;
                graphic_objs_tbl_5C08.set(targetAddr, blockDef.getU(blockDef.start)); // object ID
                graphic_objs_tbl_5C08.set(targetAddr+4, blockDef.getU(blockDef.start+1)); // width
                graphic_objs_tbl_5C08.set(targetAddr+5, blockDef.getU(blockDef.start+2)); // depth
                graphic_objs_tbl_5C08.set(targetAddr+6, blockDef.getU(blockDef.start+3)); // height
                graphic_objs_tbl_5C08.set(targetAddr+7, blockDef.getU(blockDef.start+4)); // flags
                graphic_objs_tbl_5C08.set(targetAddr+8, currLocId); // screen
                int offsets = blockDef.getU(blockDef.start+5);
                int x1 = ((offsets << 3) & 8);
                int y1 = ((offsets << 2) & 8);
                graphic_objs_tbl_5C08.set(targetAddr+1, x1 + x*16 + 0x48); // X
                graphic_objs_tbl_5C08.set(targetAddr+2, y1 + y*16 + 0x48); // Y
                graphic_objs_tbl_5C08.set(targetAddr+3, ((z*12+offsets) & 0xFC) + variables.getU(0x5BAE)); // Y, variable stores room size Z
                for(int t=9; t<32; t++) graphic_objs_tbl_5C08.set(targetAddr+t,0);  // reset remaining info
                targetAddr+=32;
            }
            hl+=blockCnt;
        }


        // @label=zero_end_of_graphic_objs_tbl
        // clear rest of graphics objects table
        for(int i=targetAddr; i<graphic_objs_tbl_5C08.start + graphic_objs_tbl_5C08.size; i++) graphic_objs_tbl_5C08.set(i, 0);

        debugTable("Other objects data:", graphic_objs_tbl_5C08.start, graphic_objs_tbl_5C08.getCopy());
    }

    private void find_special_objs_here_C525() {
        // TODO: implement
        int currLocId = graphic_objs_tbl_5C08.getU(0x5C08 + 8);
        for(int i=0x5C08 + 2*0x20; i<0x5C08 + 4*0x20; i++) graphic_objs_tbl_5C08.set(i, 0);
        int iy = special_objs_tbl_6FF2.start;
        int ix = 0x5C48;
        while(iy<special_objs_tbl_6FF2.endExcl() && special_objs_tbl_6FF2.getU(iy)!=0) {
            if(currLocId == special_objs_tbl_6FF2.getU(iy+8)) {
                graphic_objs_tbl_5C08.set(ix, special_objs_tbl_6FF2.getU(iy));
                graphic_objs_tbl_5C08.set(ix+1, special_objs_tbl_6FF2.getU(iy+5));
                graphic_objs_tbl_5C08.set(ix+2, special_objs_tbl_6FF2.getU(iy+6));
                graphic_objs_tbl_5C08.set(ix+3, special_objs_tbl_6FF2.getU(iy+7));
                graphic_objs_tbl_5C08.set(ix+4, 5);
                graphic_objs_tbl_5C08.set(ix+5, 5);
                graphic_objs_tbl_5C08.set(ix+6, 0xC);
                graphic_objs_tbl_5C08.set(ix+7, 0x14);
                graphic_objs_tbl_5C08.set(ix+8, currLocId);
                //for(int i=9; i<16; i++) graphic_objs_tbl_5C08.set(ix+i, 0);
                graphic_objs_tbl_5C08.set(ix+16, iy & 0xFF);
                graphic_objs_tbl_5C08.set(ix+17, iy & (0xFF00 >> 8));
                //for(int i=18; i<32; i++) graphic_objs_tbl_5C08.set(ix+i, 0);
                ix+=32;
            }
            iy+=9;
        }
    }

    private void adjust_plyr_xyz_for_room_size_D320() {
        debugTable("Graphic objects table:", 0x5C08, graphic_objs_tbl_5C08.getCopy());
        int roomSizeX = variables.getU(0x5BAB) - 2;
        int roomSizeY = variables.getU(0x5BAC) - 2;
        int ix = 0x5C08;
        int x = graphic_objs_tbl_5C08.getU(ix + 1);
        int y = graphic_objs_tbl_5C08.getU(ix + 2);
        if(x == 0) {
            // @label=enter_arch_e
            adjust_plyr_Z_for_arch_D38C(graphic_objs_tbl_5C08, ix,0x37);
            int a = roomSizeX + 0x80 + graphic_objs_tbl_5C08.getU(ix + 4);
            graphic_objs_tbl_5C08.set(ix + 1, a);
            copy_spr_1_xy_2_D34D(ix);
        } else if(x == 0xFF) {
            // @label=enter_arch_w
            adjust_plyr_Z_for_arch_D38C(graphic_objs_tbl_5C08, ix,0xAE);
            int a = 0x80 - roomSizeX - graphic_objs_tbl_5C08.getU(ix + 4);
            graphic_objs_tbl_5C08.set(ix + 1, a);
            copy_spr_1_xy_2_D34D(ix);
        } else if(y == 0) {
            // @label=enter_arch_n
            adjust_plyr_Z_for_arch_D38C(graphic_objs_tbl_5C08, ix,0x51);
            int a = roomSizeY + 0x80 + graphic_objs_tbl_5C08.getU(ix + 5);
            graphic_objs_tbl_5C08.set(ix + 2, a);
            copy_spr_1_xy_2_D34D(ix);
        } else if(y == 0xFF) {
            // @label=enter_arch_s
            adjust_plyr_Z_for_arch_D38C(graphic_objs_tbl_5C08, ix,0xC8);
            int a = 0x80 - roomSizeY - graphic_objs_tbl_5C08.getU(ix + 5);
            graphic_objs_tbl_5C08.set(ix + 2, a);
            copy_spr_1_xy_2_D34D(ix);
        }
    }

    private void copy_spr_1_xy_2_D34D(int ix) {
        graphic_objs_tbl_5C08.setBit(4, ix + 0x07);
        graphic_objs_tbl_5C08.setBit(4, ix + 0x27);
        graphic_objs_tbl_5C08.set(ix + 0x21, graphic_objs_tbl_5C08.getU(ix + 1));
        graphic_objs_tbl_5C08.set(ix + 0x22, graphic_objs_tbl_5C08.getU(ix + 2));
    }

    private void adjust_plyr_Z_for_arch_D38C(DataBlock block, int ix, int c) {
        int iy = 0x5C88;
        int de = 0x40;
        for(int b=0; b<4; b++) {
            if(graphic_objs_tbl_5C08.getU(iy) < 0x06) { // is arch
                // Arches are recognized by sum of their coordinates (8-bit unsigned, meaning & 0xFF or module 256)
                int xy = (graphic_objs_tbl_5C08.getU(iy + 0x01)+graphic_objs_tbl_5C08.getU(iy + 0x02)) & 0xFF;
                if(xy == c) {
                    // @label=adj_plyr_Z
                    int archZ = graphic_objs_tbl_5C08.getU(iy + 0x03);
                    block.set(ix + 0x03, archZ);
                    block.set(ix + 0x23, archZ + 0x0C);
                }
            }
            iy+=de;
        }
    }

    private void flag_room_visited_D219() {
        int screenOrig = graphic_objs_tbl_5C08.getU(0x5C10); // plyr_spr_1 screen
        int screen = (screenOrig >> 3) & 0x1F;

        //TODO: implement
    }

    private void save_2d_info_CE49(int ix, DataBlock block) {
        int widthBytes = block.getU(ix + 0x18);
        block.set(ix + 0x1c, widthBytes);
        int heightLines = block.getU(ix + 0x19);
        block.set(ix + 0x1d, heightLines);
        int pixelX = block.getU(ix + 0x1a);
        block.set(ix + 0x1e, pixelX);
        int pixelY = block.getU(ix + 0x1b);
        block.set(ix + 0x1f, pixelY);
    }

    private boolean calc_pixel_XY_D6C9(DataBlock block, int ix) {
        int x = block.getU(ix + 0x01);
        x = x + block.getU(ix + 0x02);
        x = x - 0x80;
        x = (x + block.getS(ix + 0x12)) & 0xFF;
        block.set(ix + 0x1a, x);
        int y = block.getU(ix + 0x02);
        y = y - block.getU(ix + 0x01);
        y = y + 0x80;
        y = y >> 1;
        y = y + block.getU(ix + 0x03);
        y = y - 0x68;
        y = y + block.getS(ix + 0x13);
        block.set(ix + 0x1b, y);
        // $D6EC CP $C0         ; bottom line of screen?
        return y < 0xC0; // 192
    }

    private void set_both_deadly_flags_B85C(DataBlock block, int ix) {
        block.set(ix + 0x0D, block.getU(ix + 0x0D) | 0xA0);
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

    private void debugTableConsole(String title, int offset, int[] table) {
        StringBuilder t= new StringBuilder();
        t.append(title);
        for(int i = 0; i < table.length; i++) {
            if((i % 16) == 0) {
                IO.println(t.toString());
                t = new StringBuilder(String.format("%04x:", i + offset));
            }
            t.append(" ").append(String.format("%02x", table[i]));
        }
        debugPanel2.append(t.toString());
    }
}

