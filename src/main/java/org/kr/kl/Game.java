package org.kr.kl;

import org.kr.*;
import org.kr.kl.objects.*;
import org.kr.kl.ui.GamePanel;
import org.kr.kl.ui.DebugPanel;
import org.kr.kl.utils.*;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class Game implements Runnable {
    private final GamePanel mainPanel;
    private final GamePanel shadowPanel;
    private final DebugPanel debugPanel1;
    private final DebugPanel debugPanel2;
    private final ConcurrentSkipListSet<Integer> keyQueue;

    private final GameParams params;

    private final int SUN_TICK_PER_GAME_TICK = 8;
    private final long CAULDRON_ATTR_DELAY_MS = 120;

    private long lastTick = 0;
    private int sunTick = 0;
    private long tickNo = 0;

    // $4000-$57FF - spectrum video memory
    // $5800-$5AFF - spectrum attribute memory
    private final VideoMemoryScreen mainMemory;
    // $D8F3-$F0F2 - video buffer
    private final VideoMemoryLinear shadowMemory;
    // $5BA0-$6107 - variables
    // NOTE: variables and other memory locations are treated as ints, not bytes due to lack of unsigned byte type in java
    private final Variables variables = new Variables();
    private int gameCounter = 0;
    private String gameId = "";
    private final DataBlock scrn_visited_5BE8 = new DataBlock(0x5BE8, 0x20);
    private final DataBlock inventory_5BD8 = new DataBlock(0x5BD8, 16);
    private final DataBlock cant_drop_5BD3 = new DataBlock(0x5BD3, 5);
    //private final DataBlock objects_carried_5BDC = new DataBlock(0x5BD8, 12); // contains unk and objects_carried_2
    private final DataBlock object_attributes_BFD3 = InitialData.block("object_attributes_BFD3");
    private final LookupTable lookupTable = new LookupTable();
    private final DataBlock menu_colours_BDA2 = InitialData.block("menu_colours_BDA2");
    private final DataBlock menu_xy_BDAA = InitialData.block("menu_xy_BDAA");
    private final DataBlock menu_text_BDBA = InitialData.block("menu_text_BDBA");
    private final DataBlock font_6108 = InitialData.block("font_6108");
    private final GraphicObject sprite_scratchpad_BFDB = new GraphicObject();
    private final DataBlock border_data_D2CF = InitialData.block("border_data_D2CF");
    //private final DataBlock special_objs_tbl_6FF2 = InitialData.block("special_objs_tbl_6FF2");
    private final SpecialObjects specialObjects = new SpecialObjects();
    private final SpriteData spriteData = new SpriteData();
    private final DataBlock objects_required_C27D =  InitialData.block("objects_required_C27D");
    private final GraphicObject plyr_spr_1_scratchpad_D161 =  new GraphicObject();
    private final GraphicObject plyr_spr_2_scratchpad_D181 =  new GraphicObject();
    private final DataBlock plyr_spr_init_data_D1A1 =  InitialData.block("plyr_spr_init_data_D1A1");
    private final DataBlock start_locations_D1E2 =  InitialData.block("start_locations_D1E2");
    private final DataBlock sun_moon_yoff_C440 = InitialData.block("sun_moon_yoff_C440");
    private final GraphicObject sun_moon_scratchpad_C44D = new GraphicObjects(0xC44D, 1).o(0);
    // Includes special_objs_here and other_objs_here, 40 32-byte slots
    private final GraphicObjects graphicObjects = new GraphicObjects(0x5C08, 40);
    //private final DataBlock graphic_objs_tbl_5C08 = new DataBlock(0x5C08, 0x40 + 0x40 + 0x20 + 0x0460);
    private final DataBlock objects_to_draw_CE8B = new DataBlock(0xCE8B, 0x2E); // 46, but there should be not more than 40 objects to draw
    private final DataBlock location_tbl_6251 = InitialData.block("location_tbl_6251");
    private final DataBlock room_size_tbl_6248 = InitialData.block("room_size_tbl_6248");
    private final DataBlock cauldron_bubbles_B8C8 =  InitialData.block("cauldron_bubbles_B8C8");
    private final DataBlock complete_colours_BAD2 = InitialData.block("complete_colours_BAD2");
    private final DataBlock complete_xy_BAD8 = InitialData.block("complete_xy_BAD8");
    private final DataBlock the_potion_casts_BAE4 = InitialData.block("the_potion_casts_BAE4");
    private final DataBlock gameover_colours_BB4C = InitialData.block("gameover_colours_BB4C");
    private final DataBlock gameover_xy_BB52 = InitialData.block("gameover_xy_BB52");
    private final DataBlock a_GAME_OVER_BB5E = InitialData.block("a_GAME_OVER_BB5E");
    private final DataBlock rating_tbl_BBB7 = InitialData.block("rating_tbl_BBB7");
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

    private final KeySequence keySequence;
    private Runnable tickCallback = () -> {};

    public void setTickCallback(Runnable r) { tickCallback = r; }

    private void updateMemory() {
        updateMainMemory();
        updateShadowMemory();
    }

    public Game(GamePanel mainPanel, GamePanel shadowPanel, DebugPanel debugPanel1, DebugPanel debugPanel2,
                ConcurrentSkipListSet<Integer> keyQueue, GameParams params, KeySequence keySequence) {
        this.mainPanel = mainPanel;
        this.shadowPanel = shadowPanel;
        this.debugPanel1 = debugPanel1;
        this.debugPanel2 = debugPanel2;
        this.keyQueue = keyQueue;
        this.params = params;
        this.keySequence = keySequence;

        this.debugPanel1.append("Start");
        this.debugPanel2.append("Start");

        mainMemory = new VideoMemoryScreen(0x4000);
        shadowMemory = new VideoMemoryLinear(0xD8F3);
    }

    public void updateMainMemory() {
        mainPanel.setPixelData(mainMemory.toPixels(keySequence.isReplaying() ? -1 : tickNo));
    }
    public void updateShadowMemory() {
        shadowPanel.setPixelData(shadowMemory.toPixels(keySequence.isReplaying() ? -1 : tickNo));
    }

    @Override
    public void run() {
        start_AF6C();
    }

    private void start_AF6C() {
        debugPanel1.append("start_AF6C");

        gameCounter = 0;
        while(!variables.shouldQuit()) {
            variables.init();

            // PUSH AF       ;
            // CALL $D53A    ;
            // POP AF        ;
            scrn_visited_5BE8.reset();
            inventory_5BD8.reset();
            cant_drop_5BD3.reset();
            //objects_carried_5BDC.reset();
            // For testing only

            // 65 14 F2 6F - bottle
            // 66 14 FB 6F - bottle
            /*inventory_5BD8.set(inventory_5BD8.start + 12, 0x65);
            inventory_5BD8.set(inventory_5BD8.start + 12 + 1, 0x14);
            inventory_5BD8.set(inventory_5BD8.start + 12 + 2, 0xF2);
            inventory_5BD8.set(inventory_5BD8.start + 12 + 3, 0x6F);
            inventory_5BD8.set(inventory_5BD8.start + 8, 0x66);
            inventory_5BD8.set(inventory_5BD8.start + 8 + 1, 0x14);
            inventory_5BD8.set(inventory_5BD8.start + 8 + 2, 0xFB);
            inventory_5BD8.set(inventory_5BD8.start + 8 + 3, 0x6F);*/

            //graphic_objs_tbl_5C08.reset();
            //special_objs_here_5C48.reset();
            //other_objs_here_5C88.reset();

            //variables.data.set(0x5BA0, v5C78);
            variables.resetGameOver();;
            gameId = nowAsString();
            if(!keySequence.isInactive()) new File("saves/"+gameId).mkdir();
            main_AF88();
            gameCounter++;
        }
    }

    private void main_AF88() {
        debugPanel1.append("main_AF88");
        //build_lookup_tables_D69E();

        // XOR A
        // LD ($5BB2),A
        variables.set_5BB2(0);
        // LD ($D16D),A  ; plyr_spr_1_scratchpad
        plyr_spr_1_scratchpad_D161.setFlagsC(0);
        // LD A,$05      ; 5 lives to start
        // LD ($5BBA),A  ;
        variables.set_5BBA(byteToBCD(params.getInitialLives()+1)); // NOTE: the game starts with lose life
        // LD HL,$5BA0   ;
        // LD A,($5BA2)  ;
        // ADD A,(HL)    ; seed_1 += seed_2
        // LD (HL),A     ; update seed
        params.getRnd().updateSeed1();
        // CALL $D55F    ; {colour is bright yellow on black
        clear_scrn_D55F();
        // CALL $BD0C    ;
        do_menu_selection_BD0C();
        menu_loop_BD23(params.isSkipMenu()); // returns when game starts

            /*try {
                updateShadowMemory();
                updateMainMemory();
                mainPanel.saveImage("images/menu.png");
            } catch (IOException e) {
                e.printStackTrace();
            }*/

        debugPanel2.append("START THE GAME");
        debugPanel2.append("Input method: " + variables.get_5BA4());

        // LD DE,$B20E   ; }
        // CALL $B2CF    ; play tune // ignore audio
        // CALL $B544    ; randomise order of required objects
        if(gameCounter==0) shuffle_objects_required_B544();
        // CALL $D1B1    ; {randomise player start location

        int a = location_tbl_6251.start;
        boolean specialObjectsInitialized = false;
        //while(a < location_tbl_6251.endExcl())
        {
            //variables.data.set(0x5BBA, 5); // important only when looping over rooms
            //int id = location_tbl_6251.get(a);
            //IO.println("Room id: "+id);
            int overrideRoomId=-1;
            //int overrideRoomId = 0xA8;
            int overrideWE = 0; // -W +E : -60 / +60 for large room (entrance to entrance)
            int overrideNS = 0; // -S +N
            int overrideZ = 0; // + only : 60 is a little above upper entrance

            init_start_location_D1B1(overrideRoomId, overrideWE, overrideNS, overrideZ);
            // CALL $C46D    ; }
            init_sun_C46D();
            // CALL $C47E    ; randomise special object locations
            if (!specialObjectsInitialized) {
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
    }

    private void saveImage(GamePanel panel, String filePath, String fileName) {
        try {
            updateShadowMemory();
            updateMainMemory();
            panel.saveImage(filePath, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        variables.set_5BB8(0);
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
            variables.set_5BB6(menu_colours_BDA2.getU(de1));
            //debugPanel2.append("menu attrib: %02x".formatted(variables.get(0x5BB6))) ;
            int x = menu_xy_BDAA.getU(hl2); // x - menu position
            int y = menu_xy_BDAA.getU(hl2+1); // y
            hl2 += 2;
            de2 = print_text_single_colour_BE31(menu_text_BDBA, x, y, de2);
        }
        int a = variables.get_5BB8(); // suppress border
        if(a == 0) {
            variables.set_5BB8(1);
            print_border_D296();
            update_screen_D56F(true);
        }
    }

    private int print_text_single_colour_BE31(DataBlock text, int x, int y, int de) {
        // h: y, l: x, de2: text address (first)
        debugPanel1.append(String.format("print_text_single_colour_BE31 ( x: %02x, y: %02x, text_addr: %04x)", x, y, de));
        int bc = calc_vidbuf_addr_D811(y, x);
        debugPanel1.append(String.format("Video addr: %04x", bc));

        // for testing only
        //shadowMemory.setByteAt(bc, 255);
        //for(int i=0; i < 32*192; i++) mainMemory.setByteAt(i + mainMemory.start, 0xFF);
        //for(int i=0; i < 768; i++) shadowMemory.setByteAt(i + shadowMemory.start + 0x1800, Color.getAttribute(Color.WHITE, Color.BLUE, Color.NONE, Color.NONE));

        int hl = calc_attrib_addr_D848(y, x);

        // for testing only
        //mainMemory.setByteAt(hl, Color.getAttribute(Color.WHITE, Color.GREEN, Color.NONE, Color.NONE));

        de = printText(text, de, bc, hl, variables.get_5BB6());
        return de;
    }

    private int printText(DataBlock text, int de, int bc, int hl, int attr) {
        boolean textDone = false;
        while(!textDone) {
            print_8x8_BE7F(text.getU(de), bc, font_6108);
            mainMemory.setByteAt(hl, attr); // Color.getAttribute(Color.WHITE, Color.GREEN, Color.NONE, Color.NONE));
            shadowMemory.setByteAt(hl -mainMemory.start+shadowMemory.start, attr);
            textDone = (text.getU(de) & 0x80) > 0;
            hl++;
            de++;
            bc++;
        }
        return de;
    }

    private int print_text_std_font_BE45(DataBlock text, int x, int y, int de) {
        // h: y, l: x, de2: text address (first)
        debugPanel1.append(String.format("print_text_single_colour_BE31 ( x: %02x, y: %02x, text_addr: %04x)", x, y, de));
        int bc = calc_vidbuf_addr_D811(y, x);
        debugPanel1.append(String.format("Video addr: %04x", bc));

        int attr = text.getU(de); // first byte is the attribute
        int hl = calc_attrib_addr_D848(y, x);

        de = printText(text, de+1, bc, hl, attr);

        return de;
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
        int a = variables.get_5BA4(); // input method
        a = (a >> 1) & 0x3; // joystick / keyboard flag
        // CALL $BEA3
        for(int i = 0; i < 4; i++) {
            if(a == i) menu_colours_BDA2.set(hl+i, menu_colours_BDA2.getU(hl+i) | 0x80 );
            else menu_colours_BDA2.set(hl+i, menu_colours_BDA2.getU(hl+i) & 0x7F );
        }
        if((variables.get_5BA4() & 0x08)>0) menu_colours_BDA2.set(hl+4, menu_colours_BDA2.getU(hl+4) | 0x80 );
        else menu_colours_BDA2.set(hl+5, menu_colours_BDA2.getU(hl+5) & 0x7F );
    }

    private void print_border_D296() {
        debugPanel1.append("print_border_D296");
        int hl = 0xD2CF;
        // CALL $D24C x4
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, border_data_D2CF); // corners
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, border_data_D2CF);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, border_data_D2CF);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, border_data_D2CF);
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 8, 0, 0x18, hl, border_data_D2CF); // horizontal lines
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 8, 0, 0x18, hl, border_data_D2CF);
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 0, 1, 0x80, hl, border_data_D2CF); // vertical lines
        transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 0, 1, 0x80, hl, border_data_D2CF);
    }

    private int transfer_sprite_and_print_D24C(GraphicObject o, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        transfer_sprite_D237(o, hl, source);
        print_sprite_D718(o);
        return hl+4;
    }

    // Populate sprite metadata
    private void transfer_sprite_D237(GraphicObject o, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        o.setGrNo(source.getU(hl)); // sprite index
        o.setFlags7(source.getU(hl+1)); // flags
        o.setPixelX(source.getU(hl+2)); // pixel X
        o.setPixelY(source.getU(hl+3)); // pixel Y
    }

    private void print_sprite_D718(GraphicObject o) {
        Optional<Sprite> spriteO = spriteData.flip_sprite_D6EF(o);
        if(spriteO.isEmpty()) return; // spr_null used as a flag to return from routine
        Sprite sprite = spriteO.get();

        int x = o.getPixelX();
        int y = o.getPixelY();
        int xOffset = (x & 7);

        // LD ($D7AD),A   ; - self modifying relative address to unrolled loop depending on a (width in bytes)
        // we skip this part entirely, using inner for loop instead
        o.setSpriteHeight(sprite.lines);
        // off bottom of screen?
        if(y + o.getSpriteHeight()>0xC0) o.setSpriteHeight(0xC0 - y);

        int de = 0;

        if(y > 191 || x > 255) return; // do not attempt do draw out of screen
        int bc = calc_vidbuf_addr_D811(y, x);

        if(xOffset == 0) {
            //@label=loc_D76F
            o.setSpriteWidthB(sprite.width);
            for(int lineNo = 0; lineNo<o.getSpriteHeight(); lineNo++) {
                for (int i = 0; i < sprite.width; i++) shadowMemory.setByteAt(bc+i, sprite.applyToByte(shadowMemory.getByteAt(bc+i), de+i));
                bc += 0x20;
                de += sprite.width;
            }
        } else {
            o.setSpriteWidthB(sprite.width + 1);
            for(int lineNo = 0; lineNo<o.getSpriteHeight(); lineNo++) {
                // Shift each byte across two buffer bytes (left and right), use lookup tables to reflect the original logic
                for (int i = 0; i < sprite.width; i++) {
                    int bufByteLeft = shadowMemory.getByteAt(bc+i);
                    int bufByteRight = shadowMemory.getByteAt(bc+i+1);
                    shadowMemory.setByteAt(bc+i, sprite.applyToByteLeft(bufByteLeft, de+i, xOffset));
                    if(((bc - shadowMemory.start) & 0x001F)!=0x001F)
                        shadowMemory.setByteAt(bc+i+1, sprite.applyToByteRight(bufByteRight, de+i, xOffset));
                }
                bc += 0x20;
                de += sprite.width;
            }
        }
    }

    private int transfer_and_multiple_print_sprite(GraphicObject o, int dx, int dy, int times, int hl, DataBlock dataBlock) {
        transfer_sprite_D237(o, hl, dataBlock);
        return multiple_print_sprite_BEE4(o, dx, dy, times, hl);
    }

    private int multiple_print_sprite_BEE4(GraphicObject o, int dx, int dy, int times, int hl) {
        for(int i=0; i<times; i++) {
            print_sprite_D718(o);
            o.setPixelX(o.getPixelX() + dx);
            o.setPixelY(o.getPixelY() + dy);
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
                int a = variables.get_5BA4();
                variables.set_5BA6(a);
                Integer key = keyQueue.first();
                keyQueue.remove(key);
                //IO.println("Key: " + key);
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
                //variables.data.set(0x5BA4, a);
                if (key == KeyEvent.VK_5) a ^= 0x08; //; toggle directional
                variables.set_5BA4(a);
                // CALL NZ,$B4A3 ; yes // ignore audio
                do_menu_selection_BD0C();
                if (key == KeyEvent.VK_0) startGame = true;
                //Do not change seed to make game deterministic during development
                //variables.data.set(0x5BA0, variables.get(0x5BA0)+1); // increase seed
                params.getRnd().updateSeed1();
            }
            delay();
            if(variables.shouldQuit()) return;
        }
    }

    private void shuffle_objects_required_B544() {
        //debugTable("Required objects:", objects_required_C27D.start, objects_required_C27D.getCopy());
        int a = params.getRnd().getSeed1();
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
        //debugTableConsole("Required: ", objects_required_C27D.start, objects_required_C27D.getCopy());
        //debugTable("Required objects:", objects_required_C27D.start, objects_required_C27D.getCopy());
    }

    private void init_start_location_D1B1(int override, int overrideWE, int overrideNS, int overrideZ) {
        IO.println("init_start_location_D1B1");

        if(overrideWE != 0) {
            plyr_spr_init_data_D1A1.set(plyr_spr_init_data_D1A1.start + 1, (128 + overrideWE) & 0xFF);
            plyr_spr_init_data_D1A1.set(plyr_spr_init_data_D1A1.start + 1 + 8, (128 + overrideWE) & 0xFF);
        }
        if(overrideNS != 0) {
            plyr_spr_init_data_D1A1.set(plyr_spr_init_data_D1A1.start + 2, (128 + overrideNS) & 0xFF);
            plyr_spr_init_data_D1A1.set(plyr_spr_init_data_D1A1.start + 2 + 8, (128 + overrideNS) & 0xFF);
        }
        if(overrideZ != 0) {
            plyr_spr_init_data_D1A1.set(plyr_spr_init_data_D1A1.start + 3, (128+overrideZ) & 0xFF); // negative convention
            plyr_spr_init_data_D1A1.set(plyr_spr_init_data_D1A1.start + 3 + 8, 12 + (128+overrideZ) & 0xFF); // negative convention
        }

        plyr_spr_1_scratchpad_D161.copyFrom(plyr_spr_init_data_D1A1, plyr_spr_init_data_D1A1.start, 8);
        plyr_spr_2_scratchpad_D181.copyFrom(plyr_spr_init_data_D1A1, plyr_spr_init_data_D1A1.start+8, 8);

        // LD A,$12      ; graphic_no (player top half)
        // LD ($D171),A  ; plyr_spr_1_scratchpad (byte 16)
        plyr_spr_1_scratchpad_D161.setTemp10(0x12);
        //byte_D171.set(0xD171, 0x12);
        // LD A,$22      ; graphic_no (player bottom half)
        // LD ($D191),A  ; {plyr_spr_2_scratchpad (byte 16)
        plyr_spr_2_scratchpad_D181.setTemp10(0x22);
        int a = params.getRnd().getSeed1() & 0x3; // random
        int randomLoc = start_locations_D1E2.getU(0xD1E2 + a);
        // For testing only
        if(override != -1) randomLoc = override;

        //start_loc_1_D169.set(0xD169, randomLoc);
        plyr_spr_1_scratchpad_D161.setScreen(randomLoc);
        plyr_spr_2_scratchpad_D181.setScreen(randomLoc);
    }

    private void init_sun_C46D() {
        sun_moon_scratchpad_C44D.setGrNo(0x58); // ; sprite index
        sun_moon_scratchpad_C44D.setPixelX(0xB0); // ; pixel X
        sun_moon_scratchpad_C44D.setPixelY(0x09); // ; pixel Y
    }

    private void init_special_objects_C47E() {
        int rnd1 = params.getRnd().getSeed1() & 0x07; // random
        for(int spi = 0; spi < specialObjects.size; spi++) {
            rnd1 = (rnd1 & 0x07) | 0x60; // set numbers between 0x60 and 0x67 starting from random number to special object indexes
            specialObjects.setGraphicNo(spi, rnd1);
            specialObjects.copyStartToCurrent(spi);
            rnd1++;
        }
        debugTable2("Special objects:", specialObjects.start(), specialObjects.getCopy());
    }

    private void player_dies_AFB7() {
        lose_life_D12A();
    }

    private void lose_life_D12A() {
        graphicObjects.playerBottom().copyFrom(plyr_spr_1_scratchpad_D161);
        graphicObjects.playerTop().copyFrom(plyr_spr_2_scratchpad_D181);
        variables.set_5BB1(0);
        int livesLeft = variables.get_5BBA();
        if(livesLeft < 1) {
            variables.set_5BBA(-1);
            game_over_BA22();
            return;
        }
        int newLivesLeft = params.isDisableLoseLife() ? livesLeft : decBCD(livesLeft);
        variables.set_5BBA(newLivesLeft);
        int scratch = sun_moon_scratchpad_C44D.getGrNo();
        scratch = ((scratch << 5)) & 0x20; // 3 x RRCA & 0x20 effectively means bit 0 shifted to bit 5
        int playerGraphicsNo = graphicObjects.playerBottom().getTemp10();
        playerGraphicsNo = (playerGraphicsNo & 0x1F) + scratch;
        graphicObjects.playerBottom().setTemp10(playerGraphicsNo);
        int playerGraphicsNoTop = graphicObjects.playerTop().getTemp10();
        playerGraphicsNoTop = (playerGraphicsNoTop & 0x0F) + scratch + 0x20;
        graphicObjects.playerTop().setTemp10(playerGraphicsNoTop);
    }

    private void game_over_BA22() {
        debugPanel2.append("Game over BA22\n");
        IO.println("Game over BA22\n");

        clear_scrn_buffer_D567();
        clear_scrn_D55F();

        if(variables.get_5BC3() != 0)  game_complete_msg_BAAB(); // all objects in cauldron

        clear_scrn_buffer_D567();
        clear_scrn_D55F();

        gameOverScreen();
        variables.setGameOver();
    }

    private void gameOverScreen() {
        variables.set_5BB8(0);
        display_text_list_BEBF(gameover_colours_BB4C, gameover_xy_BB52, a_GAME_OVER_BB5E);
        int days = variables.get_5BB9();
        if(days > byteToBCD(params.getMaxDays())) days --; // game over due to timeout
        int daysAddr = 0xE8E2;
        if(days < 0x10) print_BCD_lsd_BCC0(daysAddr + 1, days);
        else print_BCD_number_BCAE(daysAddr, new int[] { days });
        calc_and_display_percent_BC10();

        int objectsInCauldron = variables.get_5BBB();
        int objBCD = byteToBCD(objectsInCauldron);
        int objAddr = 0xE2EA;
        if(objBCD < 0x10) print_BCD_lsd_BCC0(objAddr + 1, objBCD);
        else print_BCD_number_BCAE(objAddr, new int[] { objBCD });

        int screens = variables.get_5BC6();
        int allObjInCauldron = variables.get_5BC3();

        int a = screens;
        int c = ((a << 1) & 0xFF) | ((a >> 7) & 1);
        c = c & 0xC0;
        a = allObjInCauldron;
        a = a & 1;
        a = a | c;
        a = ((a << 3) & 0xFF) | ((a >> 5) & 7);
        a = a & 0x0E;
        IO.println("rating "+a);
        int rating = a;

        int ratingAddrL = rating_tbl_BBB7.getU(rating_tbl_BBB7.start + rating);
        int ratingAddrH = rating_tbl_BBB7.getU(rating_tbl_BBB7.start + rating + 1);
        int ratingAddr = ratingAddrH * 256 + ratingAddrL;

        IO.println(String.format("rating address %04X",ratingAddr));

        print_text_std_font_BE45(rating_tbl_BBB7, 0x58, 0x27, ratingAddr);

        update_screen_D56F(true);
        waitForKey();
    }

    private int calcScreensVisited() {
        int screens = 0;
        for(int i=0; i<scrn_visited_5BE8.size; i++) {
            screens += calcBits(scrn_visited_5BE8.getU(scrn_visited_5BE8.start+i));
        }
        return screens;
    }

    private void calc_and_display_percent_BC10() {
        int screens = calcScreensVisited();
        //IO.println("screens = " + screens);
        int objInCauldron = variables.get_5BBB();
        int percent = 100 * (screens + 2*objInCauldron) / 156; // 156 = 128 + 2*14, where 14 is maximum number of charms
        screens --; // don't count first room
        variables.set_5BC6(screens);
        //IO.println("percent = " + percent + " BCD " + String.format("%03X", byteToBCD(percent)));
        int percentAddr = 0xE4E6;
        int percentBCD = byteToBCD(percent);
        if(percentBCD < 0x10) {
            print_BCD_lsd_BCC0(percentAddr + 1, percentBCD);
        } else {
            print_BCD_number_BCAE(percentAddr, new int[] {percentBCD});
            if(percentBCD >= 0x100) {
                print_BCD_lsd_BCC0(percentAddr - 1, (percentBCD >> 8) & 0x0F);
            }
        }
    }

    private int byteToBCD(int value) {
        int bcd3 = (value / 100) % 10;
        value -= bcd3 * 100;
        int bcd2 = (value / 10) % 10;
        value -= bcd2 * 10;
        int bcd1 = value;
        return bcd1 + bcd2 * 16 + bcd3 * 256;

    }

    private int calcBits(int toCalc) {
        int res = 0;
        for(int i=0; i<8; i++) {
            if((toCalc & (1 << i))>0) res++;
        }
        return res;
    }

    private void game_complete_msg_BAAB() {
        variables.set_5BB8(0);
        display_text_list_BEBF(complete_colours_BAD2, complete_xy_BAD8, the_potion_casts_BAE4);
        // $BAC4 LD DE,$B239   ;
        // $BAC7 CALL $B2CF    ; // ignore audio
        update_screen_D56F(true);
        if(keySequence.isSaving()) saveKeySequence();
        waitForKey();
    }

    private void waitForKey() {
        while(keyQueue.isEmpty() && !variables.shouldQuit()) {
            delay();
        }
        keyQueue.clear();
        delay();
    }

    private void waitForSpace() {
        while(!keyQueue.contains(KeyEvent.VK_SPACE)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        while(keyQueue.contains(KeyEvent.VK_SPACE)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void display_text_list_BEBF(DataBlock colors, DataBlock xy, DataBlock text) {
        int de = text.start;
        for(int i = 0; i < colors.size; i++) {
            variables.set_5BB6(colors.getU(colors.start + i));
            int x = xy.getU(xy.start + i*2);
            int y = xy.getU(xy.start + i*2+1);
            de = print_text_single_colour_BE31(text, x, y, de);
        }
        int a = variables.get_5BB8(); // suppress border
        if(a == 0) {
            variables.set_5BB8(1);
            print_border_D296();
        }
        // update_screen_D56F(true); // called after the routine
    }

    private void game_loop_AFBA() {
        debugPanel2.append("Game loop AFBA\n");
        boolean exit = false;
        while(!exit && !variables.shouldQuit()) {
            variables.resetExitingScreen();
            build_screen_objects_D1E6();
            while (!exit && !variables.isExitingScreen() && !variables.shouldQuit()) {
                exit = onscreen_loop_AFBD();
                debugPanel1.setText("");
                debugPanel2.setText("");
                for(int i = 0; i < graphicObjects.size; i++)
                    if(graphicObjects.o(i).getGrNo() !=0) debugPanel2.append(graphicObjects.objectInfo(i, graphicObjects.o(i).ix));

                debugTable2("Inventory", inventory_5BD8.start, inventory_5BD8.getCopy());
                debugPanel1.append(String.format("Current room: %02X (hex)", graphicObjects.playerBottom().getScreen()));
                debugPanel1.append(String.format("Objects collected: %d of %d", variables.get_5BBB(), params.getCauldronRequiredObjects()));
                int hl = ret_next_obj_required_C274();
                if(hl < objects_required_C27D.endExcl()) {
                    int required = objects_required_C27D.getU(hl) | 0x60;
                    debugPanel1.append(String.format("Next required object: %s", DataBlock.GRAPHIC_NAMES[required]));
                }
                debugPanel1.append(String.format("Rooms visited: %d", calcScreensVisited()));
                debugPanel1.append(String.format("Max days: %d", params.getMaxDays()));
                debugPanel1.append("Special objects (name / location):");
                for(int i = 0; i < specialObjects.size; i++) {
                    int grNo = specialObjects.getGraphicNo(i);
                    if(grNo!=0) {
                        String name = DataBlock.GRAPHIC_NAMES[grNo];
                        int location = specialObjects.getScreen(i);
                        debugPanel1.append(String.format("%s / %02X", name, location));
                    }
                }
                debugPanel1.append(String.format("Random seeds: %d %d %d",
                        params.getRnd().getSeed1(), params.getRnd().getSeed2(), params.getRnd().getSeed3()));
                debugPanel1.append(String.format("Tick: %d", tickNo));
                debugPanel1.append(String.format("Mode: %s", keySequence.isReplaying() ? "Replaying" : "Saving"));

                delay();
            }
        }
    }

    private boolean onscreen_loop_AFBD() {
        // @label=onscreen_loop
        //IO.println("Onscreen loop AFBD");
        params.getRnd().updateSeed2();
        update_sprite_loop_AFC7();

        if(variables.isExitingScreen()) return false;
        // loc_B000
        variables.setBit_5BB2(0); // ; update special objects table next time
        init_cauldron_bubbles_B8A9();
        list_objects_to_draw_CE62();
        //debugTable("Objects to draw: ", 0, objects_to_draw_CE8B.getCopy());

        // set attributes so the buffer contents are visible
        for(int i=0; i < 768; i++) shadowMemory.setByteAt(i + shadowMemory.start + 0x1800, Color.getAttribute(Color.WHITE, Color.BLUE, Color.NONE, Color.NONE));

        clear_scrn_buffer_D567(); // For testing
        calc_display_order_and_render_CEBB();

        //@label=no_delay
        //TODO: implement checking if first render ($B042 AND A         ; rendered before?)

        variables.set_5BB7(0);
        int a = variables.get_5BAD(); // screen attribute
        for(int addr = 0x5800; addr<0x5800+0x0300; addr++) {
            mainMemory.setByteAt(addr, a);
            //shadowMemory.setByteAt(addr - mainMemory.start + shadowMemory.start, a);
        }

        display_objects_BF4E();

        //@label=colour_panel
        fill_window_C515(mainMemory, 0x5AB6, 1, 3, 0);
        fill_window_C515(mainMemory, 0x5ABD, 1, 3, 0);
        fill_window_C515(mainMemory, 0x5A97, 6, 4, 0x42);
        // @label=colour_sun_moon
        boolean isSun = ((sun_moon_scratchpad_C44D.getGrNo()) & 1) == 0;
        int attr = isSun ? 0x46 : 0x47;
        fill_window_C515(mainMemory, 0x5AB8, 4, 2, attr);

        display_panel_D255();
        display_sun_moon_frame_C3A4();
        display_day_BCCA();
        print_days_BC66();
        print_lives_gfx_BC7A();
        print_lives_BCA3();
        if(variables.isGameOver()) return true;
        update_screen_D56F(false);

        // @label=loc_B074
        variables.set_5BC5(0);
        if(graphicObjects.playerBottom().getGrNo()==0 && graphicObjects.playerTop().getGrNo()==0) {
            player_dies_AFB7();
            variables.setExitingScreen();
            return variables.isGameOver();
        }

        // @label=reset_objs_wipe_flag
        // Mot used - all objects are redrawn
        for(int addr = graphicObjects.start(); addr<graphicObjects.endExcl(); addr+=0x20)
            graphicObjects.resetWipe(addr); // c$B090 RES 5,(HL)    ;

        return variables.isGameOver();
    }

    private void init_cauldron_bubbles_B8A9() {
        if(!graphicObjects.playerBottom().isCauldronRoom()) return;

        // NOTE: first special object is cauldron bubbles
        GraphicObject secondSpecObj = graphicObjects.firstSpecObj().next(); // ; special_objs_here[1]
        if(secondSpecObj.getGrNo() != 0) return;
        if(variables.get_5BC3() != 0) return;

        secondSpecObj.copyFrom(cauldron_bubbles_B8C8, cauldron_bubbles_B8C8.start, 0x12); // 18 bytes to copy
        secondSpecObj.setXYAdj(-12, -4);
    }

    private long getDelayMs() {
        return keySequence.isReplaying() ? params.getDelayMsReplay() : params.getDelayMs();
    }

    private void delay() {
        long currentTick = System.currentTimeMillis();
        if(lastTick == 0) lastTick = currentTick;

        long sleepMs = getDelayMs() - (currentTick - lastTick);
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

        nextTick();

        if(keySequence.isSaving()) {
            saveImage(mainPanel, "saves/" + gameId, String.format("tick_%d.png", tickNo));
        }

        if(params.getTickLimit() > 0 && tickNo >= params.getTickLimit()) {
            IO.println("tick: " + tickNo + ", should quit");
            variables.setShouldQuit();
        }

        keySequence.switchMode(tickNo);

        if(keySequence.isReplaying() && keySequence.exists(tickNo)) {
            List<KeySequenceEvent> events = keySequence.get(tickNo);
            events.forEach(event -> {
                if(event.getType() == KeySequenceEventType.PRESSED)
                    keyQueue.add(event.getCode());
                else keyQueue.remove(event.getCode());
            });
        }
    }

    private void nextTick() {
        tickNo++;
        updateMemory();
        tickCallback.run();
    }

    public long getTickNo() { return tickNo; }

    private void display_day_BCCA() {
        int attr = variables.get_5BAD(); // room attribute
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
        for(int b=0; b<3; b++) {
            if(inventory_5BD8.getU(inventory_5BD8.start + 4 + b*4)!=0) {
                int x = (2-b)*24+16;
                int y = 0;
                sprite_scratchpad_BFDB.setPixelX(x);
                sprite_scratchpad_BFDB.setPixelY(y);
                int hl = calc_vidbuf_addr_D811(y, x);
                fill_window_C515(shadowMemory, hl, 3, 24, 0);
                int spriteIndex = inventory_5BD8.getU(inventory_5BD8.start + 4 + b*4);
                sprite_scratchpad_BFDB.setGrNo(spriteIndex);
                print_sprite_D718(sprite_scratchpad_BFDB);
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
        print_BCD_number_BCAE(videoAddress, new int[] {variables.get_5BB9()});
    }

    private void print_BCD_number_BCAE(int bc, int[] values) {
        // NOTE: we assume that the number is already in BCD
        for(int i = 0; i<values.length; i++) {
            int digitH = (values[i] >> 4) & 0x0F;
            int digitL = values[i] & 0x0F;
            print_8x8_BE7F(digitH, bc, font_6108);
            print_8x8_BE7F(digitL, bc+1, font_6108);
        }
        //mainMemory.setByteAt(0x5AEF, 0x47);
        //mainMemory.setByteAt(0x5AEF+1, 0x47);
    }

    private void print_BCD_lsd_BCC0(int bc, int value) {
        int digitL = value & 0x0F;
        print_8x8_BE7F(digitL, bc, font_6108);
        //mainMemory.setByteAt(0x5AEF, 0x47);
    }

    private void print_lives_gfx_BC7A() {
        sprite_scratchpad_BFDB.reset();
        sprite_scratchpad_BFDB.setGrNo(0x8C);
        sprite_scratchpad_BFDB.setFlags7(0);
        sprite_scratchpad_BFDB.setPixelX(0x10);
        sprite_scratchpad_BFDB.setPixelY(0x20);
        print_sprite_D718(sprite_scratchpad_BFDB);
        for(int i=0; i<2; i++) mainMemory.setByteAt(0x5A42+i, 0x47);
        for(int i=0; i<4; i++) mainMemory.setByteAt(0x5A62+i, 0x47);
    }

    private void print_lives_BCA3() {
        print_BCD_number_BCAE(0xDDD7, new int[] {variables.get_5BBA()});
    }

    private void list_objects_to_draw_CE62() {
        objects_to_draw_CE8B.reset();
        int hl = objects_to_draw_CE8B.start;
        for(int i = 0; i < graphicObjects.size; i++) {
            if(graphicObjects.o(i).getGrNo() != 0/* && (!graphicObjects.isWipe(ix + i*0x20))*/) {
                objects_to_draw_CE8B.set(hl, i);
                hl++;
            }
        }
        objects_to_draw_CE8B.set(hl, 0xFF); //flag end of list
    }

    private void calc_display_order_and_render_CEBB() {
        variables.set_5BBE(0); // rendered objects cnt
        int de1 = objects_to_draw_CE8B.start;
        int i = de1;
        while(objects_to_draw_CE8B.getU(i) !=0xFF) i++;
        int cntToRender = i - de1;
        RenderStack renderList = new RenderStack();
        while(variables.get_5BBE() < cntToRender) {
            boolean rendered1 = (objects_to_draw_CE8B.getU(de1) & 0x80) > 0;
            if(!rendered1) {
                boolean isLast = variables.get_5BBE() >= cntToRender-1;
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
                            int relativePosition = analyzeRelativePosition(de1, de2);
                            boolean objCoincide = overlaps(relativePosition);
                            boolean is2behind1 = false;
                            if(objCoincide) {
                                // @label=objs_coincide
                                int objectLoc1 = objects_to_draw_CE8B.getU(de1) * 32 + graphicObjects.start();
                                int grNo1 = graphicObjects.getGrNo(objectLoc1);
                                boolean isSpecial1 = grNo1 >= 0x60 && grNo1 <= 0x66;
                                int objectLoc2 = objects_to_draw_CE8B.getU(de2) * 32 + graphicObjects.start();
                                int grNo2 = graphicObjects.getGrNo(objectLoc2);
                                boolean isSpecial2 = grNo2 >= 0x60 && grNo2 <= 0x66;
                                if(isSpecial1) graphicObjects.setGrNo(objectLoc1, 0xBB); //; set to twinkle sprite
                                else {
                                    if(isSpecial2) graphicObjects.setGrNo(objectLoc2, 0xBB); //; set to twinkle sprite
                                    // The first object on the list is behind the second
                                    // (e.g. spell is behind cauldron, so it is invisible when inside).
                                    // This makes rendering of coinciding object deterministic.
                                    else is2behind1 = de2 < de1;
                                }
                            }
                            is2behind1 |= is2behind1(relativePosition);
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
        GraphicObject o = graphicObjects.o(objects_to_draw_CE8B.getU(de));
        if(calc_pixel_XY_D6C9(o)) print_sprite_D718(o);
        objects_to_draw_CE8B.set(de, objects_to_draw_CE8B.getU(de) | 0x80);
        variables.set_5BBE(variables.get_5BBE() + 1);
        renderList.reset();
    }

    // Returns number of case from jump table at $CF69
    private int analyzeRelativePosition(int de1, int de2) {
        if(de1==de2) return 0;

        int objectLoc1 = objects_to_draw_CE8B.getU(de1) * 0x20 + graphicObjects.start();
        int objectLoc2 = objects_to_draw_CE8B.getU(de2) * 0x20 + graphicObjects.start();

        int x1 = graphicObjects.getX(objectLoc1);
        int y1 = graphicObjects.getY(objectLoc1);
        int z1 = graphicObjects.getZ(objectLoc1);
        int w1 = graphicObjects.getW(objectLoc1);
        int d1 = graphicObjects.getD(objectLoc1);
        int h1 = graphicObjects.getH(objectLoc1);

        int x2 = graphicObjects.getX(objectLoc2);
        int y2 = graphicObjects.getY(objectLoc2);
        int z2 = graphicObjects.getZ(objectLoc2);
        int w2 = graphicObjects.getW(objectLoc2);
        int d2 = graphicObjects.getD(objectLoc2);
        int h2 = graphicObjects.getH(objectLoc2);

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

        return c;
    }

    private boolean is2behind1(int c) {
        boolean isBehind = c==3 || c==4 || c==6 || c==7 || c==12 || c==15 || c==16;
        return isBehind;
    }

    private boolean overlaps(int c) {
        return c == 13;
    }

    private void display_panel_D255() {
        sprite_scratchpad_BFDB.reset();
        int hl = panel_data_D27E.start;
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 16, -8, 5, hl, panel_data_D27E);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, panel_data_D27E);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, panel_data_D27E);
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 16, 8, 5, hl, panel_data_D27E);
        hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, panel_data_D27E);
        transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, panel_data_D27E);
    }

    private void display_sun_moon_frame_C3A4() {
        if(variables.get_5BC3()!=0) return;

        int x = sun_moon_scratchpad_C44D.getPixelX();
        if (x == 0xE1 - 1) toggle_day_night_C3FF();

        int y = ((x + 0x10) >> 2) & 0x0F;
        int hl = 0xC440 + y;
        sun_moon_scratchpad_C44D.setPixelY(sun_moon_yoff_C440.getU(hl));
        if (sunTick == 0) sun_moon_scratchpad_C44D.setPixelX(x + 1);

        fill_window_C515(shadowMemory, 0xD90A, 6, 0x1F, 0);
        print_sprite_D718(sun_moon_scratchpad_C44D);

        sprite_scratchpad_BFDB.setGrNo(0x5A);
        sprite_scratchpad_BFDB.setFlags7(0);
        sprite_scratchpad_BFDB.setPixelX(0xB8);
        sprite_scratchpad_BFDB.setPixelY(0);
        print_sprite_D718(sprite_scratchpad_BFDB);
        sprite_scratchpad_BFDB.setPixelX(0xD0);
        sprite_scratchpad_BFDB.setGrNo(0xBA);
        print_sprite_D718(sprite_scratchpad_BFDB);
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
        int currentSprite =  sun_moon_scratchpad_C44D.getGrNo();
        int nextSprite = currentSprite ^ 1; // flip last bit
        sun_moon_scratchpad_C44D.setGrNo(nextSprite);
        sun_moon_scratchpad_C44D.setPixelX(0xB0); // ; pixel X
        variables.set_5BB1(1); // transform flag
        if((nextSprite & 1) == 0) {
            // DAA - number must be in BCD
            int currDays = variables.get_5BB9();
            int incDays = params.isDisableIncDays() ? currDays : incBCD(currDays) % 0xA0; // wrap days at 99
            variables.set_5BB9(incDays);
            if(incDays == byteToBCD(params.getMaxDays())+1) {
                game_over_BA22();
                return;
            }
            print_days_BC66();
        }
    }

    private int incBCD(int num) {
        int incNum = num+1;
        if((incNum & 0x0F) == 10) incNum = (incNum & 0xF0) + 0x10;
        return incNum;
    }

    private int decBCD(int num) {
        int decNum = num-1;
        if((decNum & 0x0F) == 15) decNum = decNum - 6;
        return decNum;
    }

    private void update_sprite_loop_AFC7() {
        // block contains sprite metadata (32 bytes each)
        for(int i=0; i<graphicObjects.size; i++) {
            updateOneSprite(graphicObjects.o(i));
            if(variables.isExitingScreen() || variables.isGameOver()) return;
            params.getRnd().updateSeed3();
        }
    }

    private void updateOneSprite(GraphicObject o) {
        o.save2DInfo();
        // @label=upd_sprite_jmp_tbl
        switch(o.getGrNo()) {
            case 0x00, 0x01, 0x86, 0x87, 0x88, 0x89, 0x8A, 0x8B, 0x8C, 0xBA: break;
            case 0x02, 0x04: upd_2_4_C73C(o); break;
            case 0x03, 0x05: upd_3_5_C722(o); break;
            case 0x06, 0x07: upd_6_7_C4E3(o); break;
            case 0x08: upd_8_C65E(o); break;
            case 0x09: upd_9_C6BD(o); break;
            case 0x0A: upd_10_C4E8(o); break;
            case 0x0B: upd_11_C4ED(o); break;
            case 0x0C, 0x0D, 0x0E, 0x0F: upd_12_to_15_C4F2(o); break;
            case 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D: upd_16_to_21_24_to_29_C823(o); break;
            case 0x16: upd_22_B7A3(o); break;
            case 0x17: upd_23_B7E7(o); break;
            case 0x1E, 0x1F, 0x9E, 0x9F: upd_30_31_158_159_B9A5(o); break;
            case 0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F: upd_32_to_47_CDDA(o); break;
            case 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D: upd_48_to_53_56_to_61_C828(o); break;
            case 0x36: upd_54_B6B9(o); break;
            case 0x37: upd_55_B6B1(o); break;
            case 0x3E: upd_62_C4AA(o); break;
            case 0x3F: upd_63_B7A9(o); break;
            case 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F: upd_64_to_79_CDDF(o); break;
            case 0x50, 0x51, 0x52, 0x53: upd_80_to_83_C5C8(o); break;
            case 0x54: upd_84_C4C3(o); break;
            case 0x55: upd_85_C4B6(o); break;
            case 0x56, 0x57: upd_86_87_B7ED(o); break;
            case 0x58, 0x59, 0x5A: upd_88_to_90_C506(o); break;
            case 0x5B: upd_91_B683(o); break;
            case 0x5C, 0x5D, 0x5E, 0x5F: upd_92_to_95_C337(o); break;
            case 0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66: upd_96_to_102_C28B(o); break;
            case 0x67: upd_103_C1AB(o); break;
            case 0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E: upd_104_to_110_C1F1(o); break;
            case 0x6F: upd_111_B95E(o); break;
            case 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0xB8: upd_112_to_118_184_BF2B(o); break;
            case 0x77: upd_119_BF3F(o); break;
            case 0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E: upd_120_to_126_BEFE(o); break;
            case 0x7F: upd_127_BF11(o); break;
            case 0x80, 0x81, 0x82: upd_128_to_130_C4D3(o); break;
            case 0x83, 0x84, 0x85: upd_131_to_133_B566(o); break;
            case 0x8D: upd_141_B99C(o); break;
            case 0x8E: upd_142_B99F(o); break;
            case 0x8F: upd_143_B6A2(o); break;
            case 0x90, 0x91, 0x92, 0x93, 0x94, 0x95, 0x98, 0x99, 0x9A, 0x9B, 0x9C, 0x9D: upd_144_to_149_152_to_157_B6F9(o); break;
            case 0x96, 0x97: upd_150_151_B73C(o); break;
            case 0xA0, 0xA1, 0xA2, 0xA3: upd_160_to_163_B8DA(o); break;
            case 0xA4, 0xA5, 0xA6, 0xA7: upd_164_to_167_B92C(o); break;
            case 0xA8, 0xA9, 0xAA, 0xAB, 0xAC, 0xAD, 0xAE, 0xAF: upd_168_to_175_B923(o); break;
            case 0xB0, 0xB1: upd_176_177_B83F(o); break;
            case 0xB2, 0xB3: upd_178_179_B865(o); break;
            case 0xB4, 0xB5: upd_180_181_B80F(o); break;
            case 0xB6, 0xB7: upd_182_183_B5FF(o); break;
            case 0xB9, 0xBB: upd_185_187_BF37(o); break;

            default: IO.println("Not updated: %02x (%d)".formatted(o.getGrNo(),o.getGrNo())); break;
        }
    }

    private void upd_2_4_C73C(GraphicObject o) {
        int centreX, centreY;
        if (o.isHFlip()) {
            // LD HL,$FEEF
            o.setXYAdj(-17, -2);
            int x = o.getX() - 0x0D;
            o.setDX(x); // dX
            int y = o.getY();
            o.setDY(y); // dY=Y
            // LD HL,$0F06   ; +15, +6
            centreY = 15;
            centreX = 6;
        } else {
            if (o.getGrNo() == 4) o.setXYAdj(1, -3);
            else o.setXYAdj(-7, -3);
            int y = o.getY() + 0x0D;
            o.setDY(y); // dY
            int x = o.getX();
            o.setDX(x); // dX
            // LD HL,$060F   ; +6, +15
            centreY = 6;
            centreX = 15;
        }
        o.setDZ(o.getZ());
        chk_plyr_spec_near_arch_C7DB(o, centreY, centreX);

        // @label=loc_C785
        for (int posToCheck = 0; posToCheck < 2; posToCheck++) {
            GraphicObject checkPlayer = graphicObjects.o(posToCheck);
            if (checkPlayer.isAutoAdjust() && is_near_to_C7FE(o, checkPlayer, 0x0F, 0x0F)) {
                switch (o.getSpriteDir()) {
                    case 0, 1: adj_ew_C7B1(o, checkPlayer); break;
                    case 2, 3: adj_ns_C7C4(o, checkPlayer); break;
                }
            }
        }
    }

    private void adj_ew_C7B1(GraphicObject arch, GraphicObject player) {
        int dyArch = arch.getDYu();
        int yPlayer = player.getY();
        if(dyArch == yPlayer) return;
        int newDy = (dyArch > yPlayer) ? 1 : -1;
        player.setDYAdj(newDy);
    }

    private void adj_ns_C7C4(GraphicObject arch, GraphicObject player) {
        int dxArch = arch.getDXu();
        int xPlayer = player.getX();
        if(dxArch == xPlayer) return;
        int newDx = (dxArch > xPlayer) ? 1 : -1;
        player.setDXAdj(newDx);
    }

    // centre Y -> H, centre x -> L
    private void chk_plyr_spec_near_arch_C7DB(GraphicObject o, int centreY, int centreX) {
        for(int posToCheck = 0; posToCheck < 4; posToCheck++) {
            GraphicObject objToCheck = graphicObjects.o(posToCheck);
            if(objToCheck.getGrNo() != 0 && objToCheck.isAutoAdjust() &&
                    is_near_to_C7FE(o, objToCheck, centreY, centreX)) objToCheck.setNearArch();
        }
    }

    private boolean is_near_to_C7FE(GraphicObject o, GraphicObject objToCheck, int centreY, int centreX) {
        //@label=is_near_to
        // ix - arch, iy - object (player), centreY - H, centreX - L
        // NOTE: we compare unsigned values - the dX, dY and dZ values are set as temporary variables and contain x, y and z
        int archCentreX = o.getDXu();
        int archCentreY = o.getDYu();
        int archCentreZ = o.getDZu();
        int objX = objToCheck.getX();
        int objY = objToCheck.getY();
        int objZ = objToCheck.getZ();

        return (Math.abs(archCentreX - objX) < centreX) && (Math.abs(archCentreY - objY) < centreY) &&
                (Math.abs(archCentreZ - objZ) < 4);
    }

    private void upd_3_5_C722(GraphicObject o) {
        if(o.isHFlip()) o.setXYAdj(-7, -2);
        else o.setXYAdj(-9, -3);
    }

    private void upd_6_7_C4E3(GraphicObject o) {
        o.setXYAdj(-16, -8);
    }

    private void upd_8_C65E(GraphicObject o) {
        // c$C4DD LD HL,$FAF4   ; -6, -12
        o.setXYAdj(-12, -6); //FA

        boolean moving = variables.get_5BAF() > 0;
        if(moving) return;
        int roomSizeZ = variables.get_5BAE();
        int z = o.getZ();
        if(z == roomSizeZ || z <= (roomSizeZ + 0x1F)) {
            // @label=init_portcullis_up
            //int seed3 = variables.get_5BA5);
            if((params.getRnd().getSeed3() & 0x1F) != 0) return; // ; 1in32 chance?

            o.setBitGrNo(0); // ; graphic_no=9
            o.setDZ(1); // ; dZ=1 (up)
        } else {
            int moveCnt = variables.get_5BB0();
            if(moveCnt >= 4) {
                if((params.getRnd().getSeed3() & 0x1F) != 0) return;
                moveCnt = 0x80; // ; arbitrarily large count
            }
            // @label=init_portcullis_down
            variables.set_5BB0(moveCnt + 1);
            o.setBitGrNo(0); // ; graphic_no=9
            o.setDZ(-1); // ; dZ=-1 (down)
        }
        variables.set_5BAF(variables.get_5BAF() + 1);
        o.setWipeAndDraw();
    }

    private void upd_9_C6BD(GraphicObject o) {
        // c$C4DD LD HL,$FAF4   ; -6, -12
        o.setXYAdj(-12, -6);

        o.setDeadlyIfObjectHitsPlayer();
        boolean enteringRoom = o.enteringScreen();
        if(enteringRoom) return;
        int dZ =  o.getDZ();
        if(dZ >= 0) {
            // @label=move_portcullis_up
            o.setDZ(2);
            // $C6EE CALL $B467      ; // ignore audio
            dec_dZ_and_update_XYZ_C700(o);
            int roomSizeZ = variables.get_5BAE();
            int z = o.getZ();
            if(z > roomSizeZ + 0x1F) stop_portcullis_C6E0(o);
            o.setWipeAndDraw();
        } else {
            o.setDZ(dZ - 1); // ; accelerate speed?
            dec_dZ_and_update_XYZ_C700(o);
            boolean oob = o.isOobZ();
            if(oob) stop_portcullis_C6E0(o);
            o.setWipeAndDraw();
        }
    }

    private void stop_portcullis_C6E0(GraphicObject o) {
        // @label=stop_portcullis
        variables.set_5BAF(0);
        o.resetBitGrNo(0); // graphic_no=8
    }

    private void upd_10_C4E8(GraphicObject o) { o.setXYAdj(-20, -1); }

    private void upd_11_C4ED(GraphicObject o) { o.setXYAdj(-12, -2); }

    private void upd_12_to_15_C4F2(GraphicObject o) { o.setXYAdj(-8, -4); }

    private void upd_128_to_130_C4D3(GraphicObject o) { o.setXYAdj(-8, -2); }

    private void upd_131_to_133_B566(GraphicObject o) {
        o.setXYAdj(-12, -4);

        int z = o.getZ();
        if(z < 0xA4) {
            o.setDZ(3); // ; dZ=3
            int x = o.getX();
            int y = o.getY();
            int xBit1 = (x & 0x80) >> 6;
            int yBit0 = (y & 0x80) >> 7;
            int xy = xBit1 | yBit0;
            int dX = 0;
            int dY = 0;
            // @label=dX_dY_tbl
            switch(xy) {
                case 0: dY = 4; dX = -4; break;
                case 1: dY = 4; dX = 4; break;
                case 2: dY = -4; dX = -4; break;
                case 3: dY = -4; dX = 4; break;
            }
            // @label=save_dX_dY
            o.setDX(dX);
            o.setDY(dY);

            int a = params.getRnd().getSeed3() & 0x03;
            if(a==0) a++;
            a+=0x82;
            o.setGrNo(a); // ; set graphic no.
        } else {
            // @label=loc_B5C4
            int playerX = graphicObjects.playerBottom().getX();
            int x = o.getX();
            int deltaX = Math.abs(playerX - x);
            if(deltaX < 0x06) {
                int playerY = graphicObjects.playerBottom().getY();
                int y = o.getY();
                int deltaY = Math.abs(playerY - y);
                if(deltaY < 0x06) {
                    game_over_BA22();
                    return;
                }
            }
            o.setDeadlyIfObjectHitsPlayer();
            o.setIgnore3D(); // ; ignore object in 3D calculations
            o.setDZ(1); // ; dZ=1
            move_towards_plyr_B965(o, 4, 4);
        }
        variables.set_5BC5(o.getZ());
        dec_dZ_and_update_XYZ_C700(o);
        o.setWipeAndDraw();
    }

    private void upd_16_to_21_24_to_29_C823(GraphicObject o) {
        // c$C4DD LD HL,$FAF4   ; -6, -12
        o.setXYAdj(-12, -6);
        upd_player_bottom_C82B(o);
    }

    private void upd_player_bottom_C82B(GraphicObject o) {
        if(o.isDead()) {
            if (variables.get_5BC3() == 0) {
                o.next().setDead(); // SET 6,(IX+$2D)
                init_death_sparkles_BF21(o);
                return;
            }
        }
        // @label=loc_C83E
            // c$C83E CALL $C306    ;
        // Note: this behavior comes from increasing SP twice when transformation starts ($C316-$C317
        boolean transformStarted = chk_and_init_transform_C306(o);
        if(!transformStarted) {
            check_user_input_D022(); // NOTE: keys are in variable 0x5BB5
            handle_pickup_drop_C00E(o);
            handle_left_right_C89F(o);
            handle_jump_C948(o);
            handle_forward_C969(o);
            boolean oob = chk_plyr_OOB_C87A(o);
            if(oob && o.getDZ() > 0) o.setDZ(0);
            loc_C855(o);
        }
    }

    private void handle_pickup_drop_C00E(GraphicObject o) {
        boolean pressed = variables.get_5BB3() != 0;
        if(!pressed) {
            if(!chk_pickup_drop_BFFB() || chk_plyr_OOB_C87A(o) || o.isJumping() || !o.isOobZ()) return;
            cant_drop_5BD3.set(0x5BD3, 0);
            int zOrig = o.getZ();
            o.setZ(zOrig + 12); // ; Z+12
            boolean intersects = do_any_objs_intersect_B4FD(o);
            o.setZ(zOrig); // ; restore original Z
            if(intersects) cant_drop_5BD3.set(0x5BD3, 1); //; we can't drop anything
            // @label=loc_C041
            variables.set_5BB3(1);
            variables.set_5BB4(1);
            int origW = o.getW();
            int origD = o.getD();
            int origH = o.getH();
            o.setW(origW + 4); // ; width+4
            o.setD(origD + 4); // ; depth+4
            o.setH(origH + 4); // ; height+4
            GraphicObject specObjP = graphicObjects.firstSpecObj();
            boolean canPickup = false;
            int b=2;
            while(b>0 && !canPickup) {
                canPickup = can_pickup_spec_obj_C172(o, specObjP);
                if(!canPickup) {
                    b--;
                    specObjP = specObjP.next();
                }
            }
            if(canPickup) pickup_object_C141(o, specObjP);
            else {
                if(chk_pickup_drop_BFFB()) {
                    b=2;
                    if(graphicObjects.playerBottom().isCauldronRoom()) b=1; // ; only check 1st special object
                    GraphicObject slotD = graphicObjects.firstSpecObj();
                    boolean foundPlace = false;
                    while(b>0 && !foundPlace) {
                        foundPlace = slotD.getGrNo() == 0;
                        if(!foundPlace) {
                            b--;
                            slotD = slotD.next();
                        }
                    }
                    if(foundPlace) room_to_drop_C0B2(o, slotD);
                };
            }
            o.setW(origW);
            o.setD(origD);
            o.setH(origH);
        } else {
            // @label=loc_C0A9
            if(chk_pickup_drop_BFFB()) return;
            variables.set_5BB3(0);
        }
    }

    private void room_to_drop_C0B2(GraphicObject player, GraphicObject toDrop) {
        int hl = inventory_5BD8.start + 0x0C;
        if(inventory_5BD8.getU(hl) == 0) {
            adjust_carried_C12B();
        } else {
            if(cant_drop_5BD3.getU(cant_drop_5BD3.start) == 0) {
                int grNo = inventory_5BD8.getU(hl);
                toDrop.setGrNo(grNo);
                if(player.isCauldronRoom()) {
                    int z = player.getZ();
                    if(z >= 0x98) {
                        toDrop.setBitGrNo(3);
                        variables.set_5BC4(1);
                    }
                }
                toDrop.setX(player.getX());
                toDrop.setY(player.getY());
                toDrop.setZ(player.getZ());
                player.setZ(player.getZ() + 0x0C);
                player.next().setZ(player.next().getZ() + 0x0C);
                calc_pixel_XY_D6C9(toDrop);
                drop_object_C106(player, toDrop);
            }
        }
    }

    private void pickup_object_C141(GraphicObject o, GraphicObject obj) {
        variables.set_5BC0(0);
        int grNo = obj.getGrNo();
        int hl = inventory_5BD8.start;
        inventory_5BD8.set(hl, grNo);
        hl++;
        int flags = obj.getFlags7();
        inventory_5BD8.set(hl, flags);
        hl++;
        int e = obj.getTemp10();
        int d = obj.getTemp11();
        int ptr =  d * 256 + e; // ; ptr graphic object
        int spi = specialObjects.getIndex(ptr);
        specialObjects.clearGraphicNo(spi);
        inventory_5BD8.set(hl, e);
        hl++;
        inventory_5BD8.set(hl, d);
        obj.setWipeAndDraw();
        obj.setGrNo(1);

        hl = inventory_5BD8.start + 0x0C;
        int a = inventory_5BD8.getU(hl);
        if(a == 0) {
            adjust_carried_C12B();
        } else {
            obj.setGrNo(a);
            drop_object_C106(o, obj);
        }
    }

    private void drop_object_C106(GraphicObject player, GraphicObject obj) {
        int hl = inventory_5BD8.start + 0x0C; // current slot
        int flags = inventory_5BD8.getU(hl + 1);
        int d = inventory_5BD8.getU(hl + 3); // 2-byte pointer to special objects table
        int e = inventory_5BD8.getU(hl + 2);
        obj.setW(5); // width=5
        obj.setD(5); // depth=5
        obj.setH(0x0C); // height=12
        obj.setFlags7(flags); //flags7
        obj.setScreen(player.getScreen()); // ; scrn
        obj.setTemp10(e); // set ptr to obj_tbl entry
        obj.setTemp11(d);
        obj.setJustDropped(); // flag just dropped
        adjust_carried_C12B();
    }

    private void adjust_carried_C12B() {
        int hl = inventory_5BD8.start + 0x0B; // unk
        int de = inventory_5BD8.last();
        for(int bc = 0; bc<0x0c; bc++) { // LDDR
            inventory_5BD8.set(de, inventory_5BD8.getU(hl));
            hl--;
            de--;
        }
        de = inventory_5BD8.start;
        for(int b = 0; b<4; b++) {
            inventory_5BD8.set(de, 0);
            de++;
        }
    }

    private boolean can_pickup_spec_obj_C172(GraphicObject player, GraphicObject obj) {
        int grNo = obj.getGrNo();
        if (((grNo - 0x60) & 0xFF) >= 0x07) return false; // special objects base no.
        return is_on_or_near_obj_C17A(player, obj);
    }

    private boolean is_on_or_near_obj_C17A(GraphicObject player, GraphicObject obj) {
        if(!player.intersectOnX(obj, 0)) return false;
        if(!player.intersectOnY(obj, 0)) return false;
        int z = player.getZ();
        player.setZ(z - 4);
        boolean intersectsZ = player.intersectOnZ(obj, 0);
        player.setZ(z);
        return intersectsZ;
    }

    private boolean do_any_objs_intersect_B4FD(GraphicObject o) {
        o.setIgnore3D(); // ; ignore this object
        for(int b = 0; b<graphicObjects.size; b++) {
            GraphicObject objToCheck = graphicObjects.o(b);
            if(objToCheck.isNotIgnored() &&
                    o.intersectOnX(objToCheck, 0) &&
                    o.intersectOnY(objToCheck, 0) &&
                    o.intersectOnZ(objToCheck, 0)) {
                o.resetIgnore3D(); // ; ignore this object
                return true;
            }
        }
        o.resetIgnore3D(); // ; ignore this object
        return false;
    }

    private boolean chk_pickup_drop_BFFB() {
        int keys = variables.get_5BB5();
        return (keys & 0x10) > 0; // only standard keyboard input
    }

    private boolean chk_plyr_OOB_C87A(GraphicObject p) {
        int roomSizeX = variables.get_5BAB();
        int roomSizeY = variables.get_5BAC();
        int xMax = roomSizeX - p.getW();
        int yMax = roomSizeY - p.getD();

        int xCalc = Math.abs(p.getX() - 0x80);
        int yCalc = Math.abs(p.getY() - 0x80);

        // True if player is outside the room (entering or exiting)
        return xCalc >= xMax || yCalc >= yMax;
    }

    private void loc_C855(GraphicObject player) {
        player.next().setIgnore3D();
        move_player_C9A1(player);
        player.next().resetIgnore3D();
        player.decEnterCounter(); // decrement entering screen counter (upper 4 bits)
        //player.setWipeAndDraw();
    }

    private void move_player_C9A1(GraphicObject player) {
        if(variables.get_5BC4()!=0) player.setDZ(2);
        boolean forward = variables.isSet_5BB5(2);
        if(player.isJumping() || player.enteringScreen() || forward) calc_plyr_dXY_C9FB(player);

        int dz = player.getDZ();
        if(dz < 0) dz -= 2;
        else {
            boolean jumpingKey = variables.isSet_5BB5(3);
            if (jumpingKey) dz -= 1; else dz -= 2;
        }
        player.setDZ(dz);
        variables.set_5BC1(dz);
        //$C9D6 CALL M,$B451   ; ignore audio

        adj_for_out_of_bounds_CB45(player);
        handle_exit_screen_CA70(player);
        // NOTE: values 0xFF in X and Y are flags indicating which arch user exited the room
        if(!variables.isExitingScreen()) {
            add_dXYZ_C706(player);
            if (player.isOobZ()) {
                int tmpDz = variables.get_5BC1();
                if (tmpDz < 0) player.resetJumping();
            }
            player.setDX(0);
            player.setDY(0);
        }
    }

    private void adj_for_out_of_bounds_CB45(GraphicObject o) {
        if(o.ignore3D()) return;
        o.setIgnore3D();
        o.clearOob();
        int dzAdj = o.getDZ(); // H
        if(dzAdj != 0) {
            dzAdj = adj_dZ_for_out_of_bounds_CA5A(o);
            if(dzAdj!=0) dzAdj = adj_dZ_for_obj_intersect_CC38(o, dzAdj);
        }
        int dxAdj = o.getDX(); // C
        if(dxAdj != 0) {
            dxAdj = adj_dX_for_out_of_bounds_CCDD(o);
            if(dxAdj!=0) dxAdj = adj_dX_for_obj_intersect_CB9A(o, dxAdj, dzAdj);
        }
        int dyAdj = o.getDY(); // L
        if(dyAdj != 0) {
            dyAdj = adj_dY_for_out_of_bounds_CD08(o);
            if(dyAdj!=0) dyAdj = adj_dY_for_obj_intersect_CBE9(o, dxAdj, dyAdj, dzAdj);
        }
        o.resetIgnore3D();
        o.setDX(dxAdj);
        o.setDY(dyAdj);
        o.setDZ(dzAdj);
    }

    private void handle_exit_screen_CA70(GraphicObject player) {
        if(player.enteringScreen() || !player.isNearArch()) return;

        player.resetNearArch();
        int oldRoomSizeX = variables.get_5BAB(); // L
        int oldRoomSizeY = variables.get_5BAC(); // H

        int currentScreen = player.getScreen();
        int newScreen = switch(player.getSpriteDir()) {
            case 0 -> screen_west_CA9A(player, oldRoomSizeX);
            case 1 -> screen_east_CAF3(player, oldRoomSizeX);
            case 2 -> screen_north_CB0E(player, oldRoomSizeY);
            case 3 -> screen_south_CB29(player, oldRoomSizeY);
            default -> currentScreen;
        };

        if(currentScreen!=newScreen) {
            if(player.getGrNo() - 0x10 > 0x40) return;
            variables.setExitingScreen();
            player.setScreen(newScreen);
            player.initEnterCounter();
            plyr_spr_1_scratchpad_D161.copyFrom(graphicObjects.playerBottom());
            plyr_spr_2_scratchpad_D181.copyFrom(graphicObjects.playerTop());
            plyr_spr_1_scratchpad_D161.setTemp10(plyr_spr_1_scratchpad_D161.getGrNo());
            plyr_spr_2_scratchpad_D181.setTemp10(plyr_spr_2_scratchpad_D181.getGrNo());
            plyr_spr_1_scratchpad_D161.setGrNo(0x78); // sparkly transform #1
            plyr_spr_2_scratchpad_D181.setGrNo(0x78);
        }
    }

    private int screenEastWest(GraphicObject player, int oldRoomSizeX, boolean east) {
        int currentScreen = player.getScreen();
        oldRoomSizeX = 0x80 + (east ? oldRoomSizeX : -oldRoomSizeX);
        int playerX = player.getX() + player.getDX() + (east ? -player.getW() : player.getW());
        if((east & oldRoomSizeX > playerX) || (!east && oldRoomSizeX < playerX)) return currentScreen;

        player.setX(east ? 0xFF : 0); // used later when determining which arch the player enters the room
        return ((currentScreen & 0x0F) + (east ? 1 : -1)) | (currentScreen & 0xF0); // do not change row;
    }

    private int screen_west_CA9A(GraphicObject player, int oldRoomSizeX) { return screenEastWest(player, oldRoomSizeX, false); }
    private int screen_east_CAF3(GraphicObject player, int oldRoomSizeX) { return screenEastWest(player, oldRoomSizeX, true); }

    private int screenNorthSouth(GraphicObject player, int oldRoomSizeY, boolean north) {
        int currentScreen = player.getScreen();
        oldRoomSizeY = 0x80 + (north ? oldRoomSizeY : -oldRoomSizeY);
        int playerY = player.getY() + player.getDY() + (north ? -player.getD() : player.getD());
        if((north && oldRoomSizeY > playerY) || (!north && oldRoomSizeY < playerY)) return currentScreen;

        player.setY(north ? 0xFF : 0); // used later when determining which arch the player enters the room
        return (currentScreen + (north ? 0x10 : -0x10)) & 0xFF;
    }

    private int screen_north_CB0E(GraphicObject player, int oldRoomSizeY) { return screenNorthSouth(player, oldRoomSizeY, true); }
    private int screen_south_CB29(GraphicObject player, int oldRoomSizeY) { return screenNorthSouth(player, oldRoomSizeY, false); }

    private void setDestroyedFlags(int objectIx, int playerIx) {
        if(params.isDisableDeath()) return;

        if(graphicObjects.getDeadlyIfObjectHitsPlayer(objectIx)) graphicObjects.setDead(playerIx); // bit 7->6
        if(graphicObjects.getDeadlyIfPlayerHitsObject(playerIx)) graphicObjects.setDead(objectIx); // bit 5->6
    }

    private int adj_dZ_for_obj_intersect_CC38(GraphicObject o, int dzAdj) {
        GraphicObject obj = graphicObjects.playerBottom();
        for(int i=0; i<graphicObjects.size; i++) {
            if(obj.isNotIgnored()) {
                if (o.intersectOnX(obj, 0) && o.intersectOnY(obj, 0)) {
                    while (o.intersectOnZ(obj, dzAdj)) {
                        o.setOobZ();
                        setDestroyedFlags(o.ix, obj.ix);
                        obj.setTriggered(); //triggered (falling, collapsing blocks)
                        if (o.isMoveable()) {
                            if (o.getDX() == 0) o.setDX(obj.getDX()); //copy dX
                            if (o.getDY() == 0) o.setDY(obj.getDY()); //copy dY
                        }
                        dzAdj = adj_d_for_out_of_bounds_CA89(dzAdj);
                        if (dzAdj == 0) return dzAdj;
                    }
                }
            }
            if(i < graphicObjects.size - 1) obj = obj.next();
        }
        return dzAdj;
    }

    private int adj_dX_for_obj_intersect_CB9A(GraphicObject o, int dxAdj, int dzAdj) {
        GraphicObject obj = graphicObjects.playerBottom();
        for(int i=0; i<graphicObjects.size; i++) {
            if(obj.isNotIgnored()) {
                if(o.intersectOnY(obj, 0) && o.intersectOnZ(obj, dzAdj)) {
                    while(o.intersectOnX(obj, dxAdj)) {
                        o.setOobX(); // set X OOB
                        setDestroyedFlags(o.ix, obj.ix);
                        if(obj.isMoveable()) obj.setDX(o.getDX()); //copy dX
                        dxAdj = adj_d_for_out_of_bounds_CA89(dxAdj);
                        if (dxAdj == 0) return dxAdj;
                    }
                }
            }
            if(i < graphicObjects.size - 1) obj = obj.next();
        }
        return dxAdj;
    }

    private int adj_dY_for_obj_intersect_CBE9(GraphicObject o, int dxAdj, int dyAdj, int dzAdj) {
        GraphicObject obj = graphicObjects.playerBottom();
        for(int i=0; i<graphicObjects.size; i++) {
            if(obj.isNotIgnored()) {
                if(o.intersectOnX(obj, dxAdj) && o.intersectOnZ(obj, dzAdj)) {
                    while(o.intersectOnY(obj, dyAdj)) {
                        o.setOobY(); // set Y OOB
                        setDestroyedFlags(o.ix, obj.ix);
                        if(obj.isMoveable()) obj.setDY(o.getDY()); //copy dY
                        dyAdj = adj_d_for_out_of_bounds_CA89(dyAdj);
                        if (dyAdj == 0) return dyAdj;
                    }
                }
            }
            if(i < graphicObjects.size - 1) obj = obj.next();
        }
        return dyAdj;
    }

    private int adj_dZ_for_out_of_bounds_CA5A(GraphicObject o) {
        int roomSize = variables.get_5BAE();
        int dz = o.getDZ();
        int z = o.getZ();
        while(true) {
            if (z + dz >= roomSize) return dz;
            o.setOobZ();
            dz = adj_d_for_out_of_bounds_CA89(dz);
            if (dz == 0) return dz;
        }
    }

    private int adj_dX_for_out_of_bounds_CCDD(GraphicObject o) {
        int dx = o.getDX();
        if(o.enteringScreen()) return dx;
        if(o.isNearArch()) return dx;

        int roomSizeX = variables.get_5BAB();

        int x = o.getX();
        while(true) {
            int a = Math.abs(((x + dx) & 0xFF) - 0x80) + o.getW();
            if(a < roomSizeX) return dx;
            o.setOobX();
            dx = adj_d_for_out_of_bounds_CA89(dx);
            if(dx == 0) return dx;
        }
    }

    private int adj_dY_for_out_of_bounds_CD08(GraphicObject o) {
        int dy = o.getDY();
        if(o.enteringScreen()) return dy;
        if(o.isNearArch()) return dy;

        int roomSizeY = variables.get_5BAC();

        int y = o.getY();
        while(true) {
            int a = Math.abs(((y + dy) & 0xFF) - 0x80) + o.getD();
            if(a < roomSizeY) return dy;
            o.setOobY();
            dy = adj_d_for_out_of_bounds_CA89(dy);
            if(dy == 0) return dy;
        }
    }

    private int adj_d_for_out_of_bounds_CA89(int a) {
        if(a==0) return 0;
        if(a<0) return a+1;
        return a-1;
    }

    private void add_dXYZ_C706(GraphicObject o) {
        // skip this when entering room
        if(o.getX()==0xFF || o.getY()==0xFF) return;

        o.setX(o.getX() + o.getDX());
        o.setY(o.getY() + o.getDY());
        o.setZ(o.getZ() + o.getDZ());
    }

    private void calc_plyr_dXY_C9FB(GraphicObject o) {
        o.setDX(o.getDX() + o.getDXAdj());
        o.setDY(o.getDY() + o.getDYAdj());
        o.setDXAdj(0);
        o.setDYAdj(0);
        switch(o.getSpriteDir()) {
            case 0: o.setDX(o.getDX() - 3); break;
            case 1: o.setDX(o.getDX() + 3); break;
            case 2: o.setDY(o.getDY() + 3); break;
            case 3: o.setDY(o.getDY() - 3); break;
        }
    }

    private boolean chk_and_init_transform_C306(GraphicObject o) {
        boolean transforming = variables.get_5BB1() > 0;
        if(!transforming || o.getEnterCounter() > 0 || o.isJumping()) return false;

        variables.set_5BB1(o.getGrNo());
        o.setTemp10(8); // transform counter

        o.next().setGrNo(1); // update player top
        //set_wipe_and_draw_flags_C692(o.ix + 0x20);
        //@label=rand_legs_sprite
        int newSprite = (params.getRnd().getSeed3() & 0x03) | 0x5C;
        if(o.getGrNo() == newSprite) newSprite^=1; // change if same as current
        o.setGrNo(newSprite);
        o.toggleHFlip();

        return true;
    }

    private String nowAsString() {
        return DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
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
        if(keyQueue.contains(KeyEvent.VK_SPACE)) {
            keyQueue.remove(KeyEvent.VK_SPACE);
            waitForSpace();
        }
        if(keySequence.isSaving() && keyQueue.stream().anyMatch(k -> k == KeyEvent.VK_ESCAPE)) {
            saveKeySequence();
            System.exit(0);
        }
        int prevC = variables.get_5BB5();
        if(c != prevC) {
            boolean leftCurr = (c & 0b00001) > 0;
            boolean leftPrev = (prevC & 0b00001) > 0;
            boolean rightCurr = (c & 0b00010) > 0;
            boolean rightPrev = (prevC & 0b00010) > 0;
            boolean forwardCurr = (c & 0b00100) > 0;
            boolean forwardPrev = (prevC & 0b00100) > 0;
            boolean jumpCurr = (c & 0b01000) > 0;
            boolean jumpPrev = (prevC & 0b01000) > 0;
            boolean pickupDropCurr = (c & 0b10000) > 0;
            boolean pickupDropPrev = (prevC & 0b10000) > 0;
            if(leftCurr && !leftPrev) keySequence.putPressed(tickNo, KeyEvent.VK_Z);
            if(!leftCurr && leftPrev) keySequence.putReleased(tickNo, KeyEvent.VK_Z);
            if(rightCurr && !rightPrev) keySequence.putPressed(tickNo, KeyEvent.VK_X);
            if(!rightCurr && rightPrev) keySequence.putReleased(tickNo, KeyEvent.VK_X);
            if(forwardCurr && !forwardPrev) keySequence.putPressed(tickNo, KeyEvent.VK_A);
            if(!forwardCurr && forwardPrev) keySequence.putReleased(tickNo, KeyEvent.VK_A);
            if(jumpCurr && !jumpPrev) keySequence.putPressed(tickNo, KeyEvent.VK_Q);
            if(!jumpCurr && jumpPrev) keySequence.putReleased(tickNo, KeyEvent.VK_Q);
            if(pickupDropCurr && !pickupDropPrev) keySequence.putPressed(tickNo, KeyEvent.VK_1);
            if(!pickupDropCurr && pickupDropPrev) keySequence.putReleased(tickNo, KeyEvent.VK_1);

            variables.set_5BB5(c);
            //IO.println("Pressed: "+Integer.toBinaryString(c));
        }
    }

    private void saveKeySequence() {
        keySequence.save(String.format("saves/%s/key_sequence.csv", gameId));
    }

    private void handle_left_right_C89F(GraphicObject o) {
        //NOTE: we ignore input method and react to keyboard only
        // @label=left_right_rotational
        if(o.getTurnCounter() > 0) { // ; too soon to turn again?
            o.decTurnCounter();
            return;
        }
        int keys = variables.get_5BB5();
        if((keys & 3) == 0) return; // ; left or right?

        if(o.getTurnCounter() > 0 || o.isJumping()) return; // ; already jumping?
        // $C911 CALL $B4C1     ;
        // ignore audio
        // @label=loc_C915
        o.initTurnCounter();
        boolean right = (keys & 1)>0;
        boolean left = (keys & 2)>0;
        if((right & !o.isHFlip()) || (left & o.isHFlip())) o.toggleBitGrNo(3);
        o.toggleHFlip();
        o.next().setGrNo(o.getGrNo() + 0x10); // ; set sprite for top half
    }

    private void handle_jump_C948(GraphicObject o) {
        boolean jump = DataBlock.isSetS(3, variables.get_5BB5());
        if(!jump || o.enteringScreen() || o.isJumping()) return;
        if(o.getDZ() < -1) return;
        o.setJumping();
        o.setDZ(8);
    }

    private void handle_forward_C969(GraphicObject o) {
        boolean forward = DataBlock.isSetS(2, variables.get_5BB5());
        // ignore audio: @label=loc_C97A
        if(!o.enteringScreen() && !o.isJumping() & !forward) {
            //@label=loc_C994
            int sprite = o.getGrNo() & 0x07;
            if(sprite == 2 || sprite == 4) return;
        }
        animate_human_legs_C97F(o);
    }

    private void animate_human_legs_C97F(GraphicObject o) {
        int sprite = o.getGrNo();
        int nextSprite = ((sprite+1) & 7) % 6; // ; wrap?
        o.setGrNo((sprite & 0xF8) | nextSprite); //  ; update sprite - NOTE we update only bottom half
    }

    private void init_death_sparkles_BF21(GraphicObject o) {
        o.setGrNo(0x70);
        o.setIgnore3D();
        // c$BF31 CALL $B403    ; // ignore audio
        o.setWipeAndDraw();
    }

    private void upd_22_B7A3(GraphicObject o) {
        o.setBothDeadlyFlags();
        o.setXYAdj(-12, -7);
    }

    private void upd_23_B7E7(GraphicObject o) {
        o.setBothDeadlyFlags();
        o.setXYAdj(-16, -8);
    }

    private void upd_30_31_158_159_B9A5(GraphicObject o) {
        o.setXYAdj(-12, 3);

        dec_dZ_and_update_XYZ_C700(o);
        move_guard_wizard_NSEW_B9CC(o);
    }

    private void move_guard_wizard_NSEW_B9CC(GraphicObject o) {
        int dydx = switch(o.getDirection()) {
            case 0 -> guard_W_B9E0(o);
            case 1 -> guard_N_B9FB(o);
            case 2 -> guard_E_BA08(o);
            case 3 -> guard_S_BA15(o);
            default -> 0;
        };
        int dx = dydx & 0xFF;
        o.setDX(dx);
        o.next().setDX(dx); // ; next obj dX
        int dy = (dydx & 0xFF00) >> 8;
        o.setDY(dy);
        o.next().setDY(dy); // ; next obj dY
        o.next().setX(o.getX()); // ; copy to next obj X
        o.next().setY(o.getY()); // ; copy to next obj Y
        set_guard_wizard_sprite_B76C(o);
        o.setBothDeadlyFlags();
        //o.setWipeAndDraw();
    }

    // returns HL: H - dY, L - dX
    private int guard_W_B9E0(GraphicObject o) {
        if(!o.isOobX()) return 0x00FE; // ; dY=0, dX=-2
        o.incDirection();
        return 0x0200; // ; dY=2, dX=0
    }

    private int guard_N_B9FB(GraphicObject o) {
        if(!o.isOobY()) return 0x0200; // ; dY=+2, dX=0
        o.incDirection();
        return 0x0002; // ; dY=0, dX=+2
    }

    private int guard_E_BA08(GraphicObject o) {
        if(!o.isOobX()) return 0x0002; // ; dY=0, dX=+2
        o.incDirection();
        return 0xFE00; // ; dY=-2, dX=0
    }

    private int guard_S_BA15(GraphicObject o) {
        if(!o.isOobY()) return 0xFE00; // ; dY=-2, dX=0
        o.incDirection();
        return 0x00FE; // ; dY=0, dX=-2
    }

    private void upd_32_to_47_CDDA(GraphicObject o) {
        o.setXYAdj(-12, -8);
        upd_player_top_CDE2(o);
    }

    private void upd_player_top_CDE2(GraphicObject o) {
        if(o.isDead() && variables.get_5BC3()==0) {
            init_death_sparkles_BF21(o);
            return;
        }

        // Player top is the second object, player bottom is at block start
        // ; copy x,y,z,w,d,h,flags
        graphicObjects.copyFrom(graphicObjects, o.prev().ix + 1, o.ix + 1, 7);
        o.setH(0);
        o.setIgnore3D();
        if(o.getLookAroundCounter() == 0) { // look around again
            int sprite = o.prev().getGrNo(); // bottom half
            if(params.getRnd().getSeed3() < 2) {
                sprite =  (sprite & 0xF8) | 6; // ; look one way
                o.initLookAroundCounter();
            } else if(params.getRnd().getSeed3() >= 0xFE) {
                sprite = (sprite & 0xF8) | 7; // ; look the other way
                o.initLookAroundCounter();
            }
            o.setGrNo(sprite + 0x10);
        } else {
            o.decLookAroundCounter();
        }
        int bottomZ = o.prev().getZ();
        o.setZ(bottomZ + 0x0C);
    }

    private void upd_48_to_53_56_to_61_C828(GraphicObject o) {
        o.setXYAdj(-12, -7);
        upd_player_bottom_C82B(o);
    }

    private void upd_54_B6B9(GraphicObject o) {
        loc_B6BF(o, true);
    }

    private void upd_55_B6B1(GraphicObject o) {
        // c$B6B1 CALL $B462    ; // ignore audio
        loc_B6BF(o, false);
    }

    private void loc_B6BF(GraphicObject o, boolean isX) {
        o.setXYAdj(-16, -8);

        // A random number derived from object index (only starting point is random)
        int blockSeed = ((o.ix >> 1) & 0x10);
        int cycle = blockSeed + params.getRnd().getSeed2(); // seed 2 increases every tick
        if((cycle & 0x10) > 0) cycle ^= 0x0F; // bit 4 flips C's direction: 0->15 then 15->0
        cycle &= 0x0F; // ensure 0-15, which is the size of a block - this limits block movement

        int xy = isX ? o.getX() : o.getY();
        xy = (xy + 0x08) & 0x0F;
        // These two numbers always differ except for points for cycle = 0 or 14 (and initial random number)
        // When they are equal, the block stops for one cycle and then moves the opposite direction
        if(cycle != xy) {
            int dXY = xy < cycle ? 1 : -1; // flip direction
            if(isX) o.setDX(dXY); else o.setDY(dXY);
            o.setDZ(1);
            dec_dZ_and_update_XYZ_C700(o);
        }
        o.setWipeAndDraw();
    }

    private void upd_62_C4AA(GraphicObject o) {
        o.setXYAdj(-16, -8);
        o.setDX(0);
        o.setDY(0);
        // $C4B0 CALL $B3E9    ; // ignore audio

        dec_dZ_and_update_XYZ_C700(o);
        o.setWipeAndDraw();
    }

    private void upd_63_B7A9(GraphicObject o) {
        o.setBothDeadlyFlags();
        // c$C4E3 LD HL,$F8F0   ; -8, -16
        o.setXYAdj(-16, -8);

        // This flag is set in every odd room and is cleared after user picks up an object -
        // these are deliberate traps
        boolean disableSpikeBallDrop = variables.get_5BC0() != 0;
        if(disableSpikeBallDrop) return;
        if(o.isDropping()) {
            dec_dZ_and_update_XYZ_C700(o);
            if(o.isOobZ()) { //; Z OOB? (hit the floor?)
                o.setRising();
                variables.set_5BBF(0); // ; flag no spiked balls dropping
            }
            // $B7D6 CALL $B451     ; } // ignore audio
        } else {
            boolean isOtherDropping = variables.get_5BBF() != 0;
            if(!isOtherDropping) {
                // ; 1-in-16 chance
                if((params.getRnd().getSeed3() & 0xF0) > 0) {
                    o.setDropping(); // ; flag this one as dropping
                    variables.set_5BBF(1); // ; flag a spike ball dropping
                }
            }
        }
        o.setWipeAndDraw();
    }

    private void upd_64_to_79_CDDF(GraphicObject o) {
        o.setXYAdj(-12, -12);
        upd_player_top_CDE2(o);
    }

    private void upd_80_to_83_C5C8(GraphicObject o) {
        o.setXYAdj(-12, -6);

        dec_dZ_and_update_XYZ_C700(o);
        if(!o.isMovingXY() || o.isOobXY()) {
            // @label=loc_C5DD
            int rnd3 = (params.getRnd().getSeed3() & 0x03) + 4;
            int dx = get_delta_from_tbl_C645(rnd3);
            o.setDX(dx);
            int rnd2 = (params.getRnd().getSeed2() & 0x03) + 4;
            int dy = get_delta_from_tbl_C645(rnd2);
            o.setDY(dy);
            calc_ghost_sprite_C603(o);
            // $C5FA CALL $B467    ; // ignore audio
        }
        o.toggleNextPrevSprite();
        o.setBothDeadlyFlags();
        o.setWipeAndDraw();
    }

    private void calc_ghost_sprite_C603(GraphicObject o) {
        int dx = o.getDX();
        int dy = o.getDY();
        if(Math.abs(dy) < Math.abs(dx)) {
            if(dx > 0) o.resetBitGrNo(1); // ; sprite=80/81
            else o.setBitGrNo(1); // ; sprite=80/81
            o.setHFlip();
        } else {
            if(dy > 0) o.setBitGrNo(1);
            else o.resetBitGrNo(1);
            o.resetHFlip();
        }
    }

    private int get_delta_from_tbl_C645(int a) {
        final int[] delta_tbl = { 0xFF, 1, 0xFE, 2, 0xFD, 3, 0xFC, 4, 0xFB, 5, 0xFA, 6, 0xF9, 7, 0xF8, 8};
        return delta_tbl[a];
    }

    private void upd_84_C4C3(GraphicObject o) {
        o.setXYAdj(-16, -8);
        dec_dZ_and_update_XYZ_C700(o);
        if(o.isMoving()) {
            o.setDX(0);
            o.setDY(0);
        }
    }

    private void upd_85_C4B6(GraphicObject o) {
        o.setXYAdj(-16, -8);
        dec_dZ_and_update_XYZ_C700(o);
        // $C4C0 JP $C232      ;// ignore audio
    }

    private void upd_86_87_B7ED(GraphicObject o) {
        o.setXYAdj(-8, -4);

        o.setDZ(1); // ; pre-load dZ=1
        o.setDX(o.isEast() ? 2 : -2);
        // $B801 CALL $B45D     ; // ignore audio
        dec_dZ_and_update_XYZ_C700(o);
        loc_B82F(o, true);
    }

    // Toggle direction if OOB
    private void loc_B82F(GraphicObject o, boolean isX) {
        if(isX && o.isOobX()) o.toggleEastWest();
        else if(!isX && o.isOobY()) o.toggleNorthSouth();

        o.toggleNextPrevSprite();
        o.setBothDeadlyFlags();
        //o.setWipeAndDraw();
    }

    private void upd_88_to_90_C506(GraphicObject o) {
        o.setXYAdj(-12, -16);
    }

    private void upd_91_B683(GraphicObject o) {
        o.setXYAdj(-16, -8);

        if(!o.isTriggered()) return;
        o.resetTriggered();
        o.setDZ(0); // ; dZ=0
        dec_dZ_and_update_XYZ_C700(o);
        // $B69C CALL $B451      ; } // ignore audio
    }

    private void dec_dZ_and_update_XYZ_C700(GraphicObject o) {
        o.setDZ(o.getDZ()-1);
        adj_for_out_of_bounds_CB45(o);
        add_dXYZ_C706(o);
    }

    private void upd_92_to_95_C337(GraphicObject o) {
        // NOTE: this applies only to player sprite
        o.setXYAdj(-12, -2);
        if(o.isDead()) {
            if (variables.get_5BC3() == 0) {
                init_death_sparkles_BF21(o);
                return;
            }
        }
        //if((rnd.getSeed2() & 3) != 0) return;
        // No randomness - transformation looks more dynamic
        if((tickNo % 3) != 0) return;
        int counter = o.getTemp10() -1;
        o.setTemp10(counter);

        if(counter == 0) {
            // End transformation
            int a = variables.get_5BB1() ^ 0x20;
            o.setGrNo(a);
            a += 0x10;
            graphicObjects.playerTop().setGrNo(a);
            variables.set_5BB1(0);
            o.setXYAdj(-12, -6);
            boolean spriteFlag = o.isBitSetGrNo(5);
            if (spriteFlag) o.setYAdj(o.getYAdj() - 1);
        } else {
            // Continue transformation
            int nextSprite = (params.getRnd().getSeed3() & 3) | 0x5C;
            if(nextSprite != o.getGrNo()) nextSprite = nextSprite ^ 1;
            o.setGrNo(nextSprite);
            o.toggleHFlip();
        }
    }

    private void upd_96_to_102_C28B(GraphicObject o) {
        o.setXYAdj(-12, -4);

        dec_dZ_and_update_XYZ_C700(o);
        if(!o.justDropped() && !o.isMoving()) return;

        o.resetJustDropped();
        o.setDX(0);
        o.setDY(0);
        // c$C232 CALL $B467    ; // ignore audio
        o.setWipeAndDraw();
    }

    private void upd_103_C1AB(GraphicObject o) {
        // extra life object
        o.setXYAdj(-8, -2);

        GraphicObject p = graphicObjects.playerBottom();
        int width = p.getW();
        int depth = p.getD();
        p.setW(width+1);
        p.setD(depth+1);
        boolean isNear = is_on_or_near_obj_C17A(p, o);
        p.setW(width); //; restore width
        p.setD(depth); //; restore depth
        if(isNear) {
            o.setBitGrNo(3); // ; graphic_no: 103 + 8 = 111, see upd_111
            o.setXYAdj(-12, -4);
            int l = o.getTemp10();
            int h = o.getTemp11();
            int ptr = h * 256 + l;
            int spi = specialObjects.getIndex(ptr);
            specialObjects.clearGraphicNo(spi); // ; zap ptr object table entry
            int lives = variables.get_5BBA();
            variables.set_5BBA(incBCD(lives));
            variables.set_5BC0(0);
            // $C1E2 CALL $B4A3     ; // ignore audio
            print_lives_BCA3();
        }
        dec_dZ_and_update_XYZ_C700(o);
        if(o.isMoving()) {
            o.setDX(0);
            o.setDY(0);
        }
        o.setWipeAndDraw();
    }

    private void upd_104_to_110_C1F1(GraphicObject o) {
        o.setXYAdj(-12, -4);

        int x = o.getX();
        int dX = o.getX() - 0x80;
        if(dX < 0) dX = 1;
        else if(dX > 0) dX = -1;
        o.setDX(dX);

        int y = o.getY();
        int dY = y - 0x80;
        if(dY < 0) dY = 1;
        else if(dY > 0) dY = -1;
        o.setDY(dY);

        boolean center = x == 0x80 && y == 0x80;
        int z = o.getZ();
        if(!center) {
            int dZ = 1;
            if(z < 0x98) dZ++;
            o.setDZ(dZ);
        } else {
            if(z <= 0x80) {
                add_obj_to_cauldron_C245(o);
                return;
            } else o.setIgnore3D(); // ; ignore in 3D calcs
        }
        dec_dZ_and_update_XYZ_C700(o);
        // c$C232 CALL $B467    ; // ignore audio
        // set_wipe_and_draw_flags_C692(block, ix);
    }

    private void add_obj_to_cauldron_C245(GraphicObject o) {
        o.setZ(0x80);
        int hl = ret_next_obj_required_C274();
        int grNo = o.getGrNo();
        boolean sameAsRequired = objects_required_C27D.getU(hl) == (grNo & 0x07);
        if(sameAsRequired) {
            variables.set_5BBB(variables.get_5BBB() + 1);
            cycle_colours_with_sound_C2A5();
            boolean gotAllObjects = variables.get_5BBB() == params.getCauldronRequiredObjects();
            if(gotAllObjects) prepare_final_animation_C2CC();
        }
        variables.set_5BC4(0);
        int l = o.getTemp10();
        int h = o.getTemp11();
        int ptr = h * 256 + l;
        int spi = specialObjects.getIndex(ptr);
        specialObjects.clearGraphicNo(spi);
        upd_111_B95E(o);
    }

    private void prepare_final_animation_C2CC() {
        IO.println("prepare_final_animation_C2CC");

        variables.set_5BC3(1);
        int objIx = graphicObjects.specialObjStartIx + 1;
        for(int i=0; i< 0x0B; i++) { // ; wipe 11 objects in the room (1 special, 7 bg, 3 fg)
            graphicObjects.o(objIx).setGrNo(1); // ; set graphic no. = INVALID
            //set_wipe_and_draw_flags_C692(graphic_objs_tbl_5C08, ix);
            objIx++;
        }
        while(objIx < graphicObjects.size) {
            int grNo = graphicObjects.o(objIx).getGrNo();
            if (grNo == 7)  // block
                graphicObjects.o(objIx).setGrNo(0x83); // ; set to twinkly sprite
            objIx++;
        }
    }

    private void cycle_colours_with_sound_C2A5() {
        IO.println("cycle_colours_with_sound_C2A5");

        for(int d = 0; d<0x10; d++) {
            int hl = 0x5800;
            for (int i = 0; i < 0x0300; i++) {
                int attr = mainMemory.getByteAt(hl);
                int newAttr = (attr & 0xF8) + ((attr + 1) & 0x07);
                mainMemory.setByteAt(hl, newAttr);
                hl++;
                // $C2BD CALL $B403    ; // ignore audio
            }
            try {
                if(!keySequence.isReplaying()) Thread.sleep(CAULDRON_ATTR_DELAY_MS);
                updateMemory();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void upd_111_B95E(GraphicObject o) {
        // ; invalid
        o.setGrNo(1);
        // ignore audio
    }

    private void upd_112_to_118_184_BF2B(GraphicObject o) {
        o.setXYAdj(-12, -4);
        o.setGrNo(o.getGrNo()+1);
        // ignore audio
    }

    private void upd_119_BF3F(GraphicObject o) {
        o.setXYAdj(-12, -4);
        upd_111_B95E(o); // Fix bug/feature in the source (writing to ROM)
    }

    private void upd_120_to_126_BEFE(GraphicObject o) {
        o.setXYAdj(-12, -4);
        if(((params.getRnd().getSeed2() ^ 0xFF) & 0x01) == 0) o.setGrNo(o.getGrNo() + 1); // next sprite on odd frames
        o.setWipeAndDraw();
    }

    private void upd_127_BF11(GraphicObject o) {
        //c$C4D8 LD HL,$FCF4   ; -4, -12
        o.setXYAdj(-12, -4); //FC
        o.resetDead();
        o.setGrNo(o.getTemp10());
        updateOneSprite(o); // update after sparkes change to player
    }

    private void upd_141_B99C(GraphicObject o) { o.setXYAdj(-16, -12); }

    private void upd_142_B99F(GraphicObject o) { o.setXYAdj(-24, 12); }

    private void upd_143_B6A2(GraphicObject o) {
        o.setXYAdj(-16, -8);

        if(o.isTriggered()) {
            o.setGrNo(0xB8); // ; graphic_no = sparkles
            upd_112_to_118_184_BF2B(o);
        }
    }

    private void upd_144_to_149_152_to_157_B6F9(GraphicObject o) {
        o.setXYAdj(-12, -6);

        if(!o.isMovingXY()) return;
        int dx = o.getDX();
        int dy = o.getDY();
        // $B703 CALL $B4AD     ; // ignore audio
        int dxU = o.getDXu();
        int dyU = o.getDYu();
        if(dxU > dyU) {
            if(dx >= 0) o.setBitGrNo(3);
            else o.resetBitGrNo(3);
            o.resetHFlip();
        } else {
            if (dy >= 0) o.resetBitGrNo(3);
            else o.setBitGrNo(3);
            o.setHFlip();
        }

        animate_human_legs_C97F(o);
        o.setWipeAndDraw();
    }

    private void upd_150_151_B73C(GraphicObject o) {
        o.setXYAdj(-12, 7);

        int dx = o.isEast() ? 2 : -2;
        o.setDX(dx);
        o.next().setDX(dx); // ; and for bottom half
        set_guard_wizard_sprite_B76C(o);
        dec_dZ_and_update_XYZ_C700(o);
        if(o.isOobX()) o.toggleEastWest(); // ; toggle direction
        o.next().setX(o.getX()); // ; copy to bottom half
        o.setBothDeadlyFlags();
        o.setWipeAndDraw();
    }

    private void set_guard_wizard_sprite_B76C(GraphicObject o) {
        int dx = o.getDX();
        int dy = o.getDY();
        if(dx ==0 && dy == 0) return;
        int dxU = o.getDXu();
        int dyU = o.getDYu();
        if(dxU > dyU) {
            if(dx >= 0) o.setBitGrNo(0); // ; sprite 31/159
            else o.resetBitGrNo(0); // ; sprite 31/159
            o.resetHFlip();
        } else {
            if(dy >=0) o.resetBitGrNo(0); // ; sprite 31/159
            else o.setBitGrNo(0); // ; sprite 31/159
            o.setHFlip();
        }
    }

    private void upd_160_to_163_B8DA(GraphicObject o) {
        o.setXYAdj(-12, -4);

        if(graphicObjects.firstSpecObj().getGrNo() != 0) {
            upd_111_B95E(o);
            return;
        }

        o.setIgnore3D(); // ; ignore in 3D calcs
        dec_dZ_and_update_XYZ_C700(o); // apply dXYZ now
        o.nextGrNoMod4();

        int z = o.getZ();
        o.setDZ(2); // ; dZ=2 - NOTE: will be applied next frame as C700 has already been called
        if(z >= 0xA0) {
            o.setDZ(1); // dZ=1
            if(!graphicObjects.playerBottom().isWulf()) {
                int grNo = o.getGrNo();
                if((grNo & 3) == 0) {
                    int hl = ret_next_obj_required_C274();
                    o.setGrNo(objects_required_C27D.getU(hl) | 0xA8);
                }
            } else {
                o.setBitGrNo(3);
                o.resetIgnore3D();
            }
        }
        o.setWipeAndDraw();
    }

    private int ret_next_obj_required_C274() {
        int objsInCauldron = variables.get_5BBB();
        return objects_required_C27D.start + objsInCauldron;
    }

    private void upd_164_to_167_B92C(GraphicObject o) {
        o.setXYAdj(-12, -4);

        boolean cauldronRoom = o.isCauldronRoom();
        int dx = 4;
        int dy = 4;
        if(!cauldronRoom && graphicObjects.playerBottom().isNearArch()) {
            dx = 1;
            dy = 1;
        }
        move_towards_plyr_B965(o, dx, dy);
        dec_dZ_and_update_XYZ_C700(o);
        o.nextGrNoMod4();

        if(cauldronRoom & graphicObjects.playerBottom().isSparkles()) { // is player dead (sparkles)
            upd_111_B95E(o); // ; final sparkles in cauldron
            return;
        }
        //set_wipe_and_draw_flags_C692();
    }

    private void move_towards_plyr_B965(GraphicObject o, int dx, int dy) {
        GraphicObject player = graphicObjects.playerBottom();
        if(o.getX() >= player.getX()) dx = -dx; // player W
        o.setDX(dx);

        if(o.getY() >= player.getY()) dy = -dy; // player S
        o.setDY(dy);
    }

    private void upd_168_to_175_B923(GraphicObject o) {
        o.setXYAdj(-12, -4);
        o.setGrNo(0xA0);
    }

    // NOTE: commented as: not used
    private void upd_176_177_B83F(GraphicObject o) {
        upd_12_to_15_C4F2(o);
        // NOTE: rest of the routine is not implemented
    }

    private void upd_178_179_B865(GraphicObject o) {
        o.setXYAdj(-8, -4);

        int bouncingHeight = variables.get_5BBD();
        if(bouncingHeight == 0) variables.set_5BBD(o.getZ() + 0x20); // ; calc bounce height
        o.toggleNextPrevSprite();
        // $B879 CALL $B451     ; // ignore audio
        if(o.isDirUp()) {
            o.setDZ(3);
            dec_dZ_and_update_XYZ_C700(o);
            if(o.getZ() > variables.get_5BBD())
                o.setDirDown();
        } else {
            dec_dZ_and_update_XYZ_C700(o);
            if(o.isOobZ()) o.setDirUp();
            // $B88F CALL $B42E     ; // ignore audio
        }
        o.setBothDeadlyFlags();
        o.setWipeAndDraw();
    }

    private void upd_180_181_B80F(GraphicObject o) {
        o.setXYAdj(-8, -4);

        o.setDZ(1); // ; pre-load dZ=1
        if(o.isNorth()) o.setDY(2); else o.setDY(-2);
        // $B801 CALL $B45D     ; // ignore audio
        dec_dZ_and_update_XYZ_C700(o);
        loc_B82F(o, false);
    }

    private void upd_182_183_B5FF(GraphicObject o) {
        o.setXYAdj(-8, -4);

        int dx = o.getDXu();
        int dy = o.getDYu();
        int dz = o.getDZu();

        variables.set_5BC2(dz); // ; {tmp store
        dec_dZ_and_update_XYZ_C700(o);

        o.setDX(dx); // ; preserve old dX
        o.setDY(dy); // ; preserve old dY
        boolean isWulf = graphicObjects.playerBottom().isWulf();
        int screen = graphicObjects.playerBottom().getScreen();
        boolean screenFlg = (screen & 0x01) == 0;
        int newDz = 4;
        if(!screenFlg) newDz += params.getRnd().getSeed3() & 0x03;
        if(o.isOobZ()) {
            o.setDZ(newDz);
            // $B64C CALL $B42E     ; } // ignore audio
            params.getRnd().updateSeed3();
            if((params.getRnd().getSeed3() & 0x01) == 1) {
                int playerY = graphicObjects.playerBottom().getY();
                int newdY = o.getY() > playerY ? 2 : -2;
                if(isWulf) newdY = -newdY; // ; towards player
                o.setDY(newdY);
                o.setDX(0);
            } else {
                int playerX = graphicObjects.playerBottom().getX();
                int newdX = o.getX() > playerX ? 2 : -2;
                if(isWulf) newdX = -newdX; // ; towards player
                o.setDX(newdX);
                o.setDY(0);
            }
        }
        o.toggleNextPrevSprite();
        o.setBothDeadlyFlags();
        o.setWipeAndDraw();
    }

    private void upd_185_187_BF37(GraphicObject o) {
        int l = o.getTemp10();
        int h = o.getTemp11();
        if(l!=0 || h!=0) {
            int spi = specialObjects.getIndex(h*256+l);
            specialObjects.clearGraphicNo(spi); //  $BF3D LD (HL),$00   ; zap graphic_no in special_objs_tbl
        }
        upd_119_BF3F(o);
    }

    private void build_screen_objects_D1E6() {
        if(variables.get_5BB2() != 0) {
            // update special objects if not the first screen of the game
            update_special_objs_C591();
        }
        clear_scrn_buffer_D567();
        retrieve_screen_D3C6();
        // TODO: implement
        find_special_objs_here_C525();
        adjust_plyr_xyz_for_room_size_D320();

        variables.set_5BAF(0);
        variables.set_5BB0(0);
        variables.set_5BBD(0);
        variables.set_5BBF(0);
        variables.set_5BB7(1);
        // NOTE: this means that in odd rooms spike balls do not drop until an object is picked up.
        // This is not straightforward but these are the traps - a player may be killed when picking up an object
        // and these traps are only in odd rooms
        variables.set_5BC0(graphicObjects.getScreen(graphicObjects.start()) & 1);

        flag_room_visited_D219();
    }

    private void update_special_objs_C591() {
        int iy = 0x5C48;
        for(int i=0; i<2; i++) {
            int grNo = graphicObjects.getGrNo(iy);
            if(grNo != 0) {
                int e = graphicObjects.getTemp10(iy);
                int d = graphicObjects.getTemp11(iy);
                int ptr = d*256 + e; // ptr can be 0 for cauldron bubbles
                if(ptr != 0) {
                    int spi = specialObjects.getIndex(ptr);
                    specialObjects.setGraphicNo(spi, grNo);
                    specialObjects.setCurrentX(spi, graphicObjects.getX(iy));
                    specialObjects.setCurrentY(spi, graphicObjects.getY(iy));
                    specialObjects.setCurrentZ(spi, graphicObjects.getZ(iy));
                    int screen = graphicObjects.getScreen(iy);
                    specialObjects.setScreen(spi, screen);
                }
            }
            iy += 0x20;
        }
    }

    private void retrieve_screen_D3C6() {
        int bc = location_tbl_6251.endExcl()-1; // location table end
        int hl = location_tbl_6251.start; // location table start
        boolean found = false;
        int currLocId = graphicObjects.playerBottom().getScreen();

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
            graphicObjects.reset();
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
        variables.set_5BAD(attr); // current room attributes (color)
        int roomSize = ((attrOrig >> 3) & 0x1F);
        variables.set_5BAB(room_size_tbl_6248.getU(room_size_tbl_6248.start + roomSize*3)); // room size X
        variables.set_5BAC(room_size_tbl_6248.getU(room_size_tbl_6248.start + roomSize*3 + 1)); // room size Y
        variables.set_5BAE(room_size_tbl_6248.getU(room_size_tbl_6248.start + roomSize*3 + 2)); // room size Z
        debugPanel2.append("Retrieved room size: "+roomSize+" X:"+variables.get_5BAB()+" Y:"+variables.get_5BAC()+" Z:"+variables.get_5BAE());
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
                graphicObjects.resetObject(targetAddr);
                // 8 - byte sprite info
                debugPanel2.append("Retrieved sprite: %02x".formatted(bkgObj.getU(bkgAddr)));
                graphicObjects.copyFrom(bkgObj, bkgAddr, targetAddr, 8);
                bkgAddr+=8;
                // 9th byte
                graphicObjects.setScreen(targetAddr, currLocId);
                targetAddr+=0x20;
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
                int templatePart = 0;
                boolean templateDone = false;
                while(!templateDone) {
                    int locByte = location_tbl_6251.getU(hl + i);
                    int x = (locByte & 0b00000111);
                    int y = (locByte & 0b00111000) >> 3;
                    int z = (locByte & 0b11000000) >> 6;
                    graphicObjects.resetObject(targetAddr);
                    graphicObjects.setGrNo(targetAddr, blockDef.getU(blockDef.start + templatePart * 6)); // object ID
                    graphicObjects.setW(targetAddr, blockDef.getU(blockDef.start + templatePart * 6 + 1)); // width
                    graphicObjects.setD(targetAddr, blockDef.getU(blockDef.start + templatePart * 6 + 2)); // depth
                    graphicObjects.setH(targetAddr, blockDef.getU(blockDef.start + templatePart * 6 + 3)); // height
                    graphicObjects.setFlags7(targetAddr, blockDef.getU(blockDef.start + templatePart * 6 + 4)); // flags
                    graphicObjects.setScreen(targetAddr, currLocId); // screen
                    int offsets = blockDef.getU(blockDef.start + templatePart * 6 + 5);
                    int x1 = ((offsets << 3) & 8);
                    int y1 = ((offsets << 2) & 8);
                    graphicObjects.setX(targetAddr, x1 + x * 16 + 0x48); // X
                    graphicObjects.setY(targetAddr, y1 + y * 16 + 0x48); // Y
                    graphicObjects.setZ(targetAddr, ((z * 12 + offsets) & 0xFC) + variables.get_5BAE()); // Y, variable stores room size Z
                    targetAddr += 0x20;
                    templateDone = blockDef.getU(blockDef.start + templatePart * 6 + 6) == 0; // some templates consists of two objects (guards)
                    templatePart++;
                }
            }
            hl+=blockCnt;
        }


        // @label=zero_end_of_graphic_objs_tbl
        // clear rest of graphics objects table
        for(int i = targetAddr; i<graphicObjects.endExcl(); i+=0x20) graphicObjects.resetObject(i);

        debugTable2("Other objects data:", graphicObjects.start(), graphicObjects.getCopy());
    }

    private void find_special_objs_here_C525() {
        int currLocId = graphicObjects.getScreen(graphicObjects.start());
        for(int i=graphicObjects.start() + 2*0x20; i<graphicObjects.start() + 4*0x20; i+=0x20) graphicObjects.resetObject(i);
        for(int spi = 0; spi < specialObjects.size;  spi++) {
            int specialObjGrNo = specialObjects.getGraphicNo(spi);
            int i = graphicObjects.specialObjStartIx;
            boolean saved = false;
            while(i < graphicObjects.specialObjEndIxExcl && !saved) {
                GraphicObject o = graphicObjects.o(i);
                int screenObjGrNo = o.getGrNo();
                if (screenObjGrNo == 0 && specialObjGrNo != 0 && currLocId == specialObjects.getScreen(spi)) {
                    o.resetObject();
                    o.setGrNo(specialObjGrNo);
                    o.setX(specialObjects.getCurrentX(spi));
                    o.setY(specialObjects.getCurrentY(spi));
                    o.setZ(specialObjects.getCurrentZ(spi));
                    o.setW(5);
                    o.setD(5);
                    o.setH(0x0C);
                    o.setFlags7(0x14);
                    o.setScreen(currLocId);
                    o.setTemp10(specialObjects.getAddress(spi) & 0xFF);
                    o.setTemp11((specialObjects.getAddress(spi) & 0xFF00) >> 8);
                    saved = true;
                }
                i++;
            }
        }
    }

    private void adjust_plyr_xyz_for_room_size_D320() {
        debugTable2("Graphic objects table:", 0x5C08, graphicObjects.getCopy());
        int roomSizeX = variables.get_5BAB() - 2;
        int roomSizeY = variables.get_5BAC() - 2;
        GraphicObject player = graphicObjects.playerBottom();
        int x = player.getX();
        int y = player.getY();
        if(x == 0) {
            // @label=enter_arch_e
            adjust_plyr_Z_for_arch_D38C(player,0x37);
            int a = roomSizeX + 0x80 + player.getW();
            player.setX(a);
            copy_spr_1_xy_2_D34D(player);
        } else if(x == 0xFF) {
            // @label=enter_arch_w
            adjust_plyr_Z_for_arch_D38C(player,0xAE);
            int a = 0x80 - roomSizeX - player.getW();
            player.setX(a);
            copy_spr_1_xy_2_D34D(player);
        } else if(y == 0) {
            // @label=enter_arch_n
            adjust_plyr_Z_for_arch_D38C(player,0x51);
            int a = roomSizeY + 0x80 + player.getD();
            player.setY(a);
            copy_spr_1_xy_2_D34D(player);
        } else if(y == 0xFF) {
            // @label=enter_arch_s
            adjust_plyr_Z_for_arch_D38C(player,0xC8);
            int a = 0x80 - roomSizeY - player.getD();
            player.setY(a);
            copy_spr_1_xy_2_D34D(player);
        }
    }

    private void copy_spr_1_xy_2_D34D(GraphicObject player) {
        player.setDraw();
        player.next().setDraw();
        player.next().setX(player.getX());
        player.next().setY(player.getY());
    }

    private void adjust_plyr_Z_for_arch_D38C(GraphicObject player, int c) {
        GraphicObject arch = graphicObjects.firstArch();
        int de = 0x40;
        for(int b=0; b<4; b++) {
            if(arch.getGrNo() < 0x06) { // is arch
                // Arches are recognized by sum of their coordinates (8-bit unsigned, meaning & 0xFF or module 256)
                int xy = (arch.getX()+arch.getY()) & 0xFF;
                if(xy == c) {
                    // @label=adj_plyr_Z
                    int archZ = arch.getZ();
                    player.setZ(archZ);
                    player.next().setZ(archZ + 0x0C);
                }
            }
            arch = arch.next().next(); // each arch is defined as two objects, max 4 arches - always in the same position in graphic object table
        }
    }

    private void flag_room_visited_D219() {
        int screenOrig = graphicObjects.getScreen(graphicObjects.start()); // plyr_spr_1 screen
        int offsetByte = screenOrig / 8;
        int offsetBit = screenOrig % 8;
        scrn_visited_5BE8.setBit(offsetBit, scrn_visited_5BE8.start + offsetByte);
    }

    private boolean calc_pixel_XY_D6C9(GraphicObject o) {
        // first check if object should disappear (including player)
        if(o.getGrNo() == 1) { //; flagged as ???
            o.setGrNo(0);
            return false;
        }

        int x = o.getX() + o.getY() - 0x80 + o.getXAdj();
        o.setPixelX(x & 0xFF);
        int y = (o.getY() - o.getX() + 0x80) / 2 + o.getZ() - 0x68 + o.getYAdj();
        o.setPixelY(y & 0xFF);
        // $D6EC CP $C0         ; bottom line of screen?
        return y < 0xC0; // 192
    }

    private void printVideoMemory() {
        debugTable2("Video (main):", 0x4000, mainMemory.getCopy());
    }

    private void printShadowMemory() {
        debugTable2("Video (shadow):", 0x4000, shadowMemory.getCopy());
    }

    private void debugTable2(String title, int offset, int[] table) {
        debugTable(title, offset, table, 16, debugPanel2);
    }

    private void debugTable(String title, int offset, int[] table, int mod, DebugPanel panel) {
        StringBuilder t= new StringBuilder();
        t.append(title);
        for(int i = 0; i < table.length; i++) {
            if((i % mod) == 0) {
                panel.append(t.toString());
                t = new StringBuilder(String.format("%04x:", i + offset));
            }
            t.append(" ").append(String.format("%02x", table[i]));
        }
        panel.append(t.toString());
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
        IO.println(t.toString());
    }
}

