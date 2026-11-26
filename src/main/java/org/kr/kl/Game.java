package org.kr.kl;

import org.kr.kl.data.DataBlock;
import org.kr.kl.data.InitialData;
import org.kr.kl.objects.*;
import org.kr.kl.ui.*;
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
    private final MapPanel mapPanel;
    private final ObjectsPanel objectsPanel;
    private final SettingsPanel settingsPanel;
    private final DebugPanel debugPanel1;
    private final DebugPanel debugPanel2;
    private final ConcurrentSkipListSet<Integer> keyQueue;

    private final GameParams params;

    private final int SUN_TICK_PER_GAME_TICK = 8;
    private final long CAULDRON_ATTR_DELAY_MS = 120;

    private long lastTick = 0;
    private int sunTick = 0;
    private long tickNo = 0;

    private final VideoMemoryScreen mainMemory;
    private final VideoMemoryLinear shadowMemory;
    // NOTE: variables and other memory locations are treated as ints, not bytes due to lack of unsigned byte type in java
    private final Variables variables = new Variables();
    private int gameCounter = 0;
    private String gameId = "";
    private final Inventory inventory = new Inventory();
    private final DataBlock menu_colours_BDA2 = InitialData.block("menu_colours_BDA2");
    private final Font font_6108 = new Font(InitialData.block("font_6108"), "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ.c%:+-");
    private final Font day_font_BCEC = new Font(InitialData.block("day_font_BCEC"), "0123"); // NOTE: this font looks the same but is shifted by half of byte
    private final GraphicObject sprite_scratchpad_BFDB = new GraphicObject();
    private final SpecialObjects specialObjects = new SpecialObjects();
    private final SpriteData spriteData = new SpriteData();
    private final DataBlock objects_required_C27D =  InitialData.block("objects_required_C27D");
    private final GraphicObject plyr_spr_1_scratchpad_D161 =  new GraphicObject();
    private final GraphicObject plyr_spr_2_scratchpad_D181 =  new GraphicObject();
    private final DataBlock plyr_spr_init_data_D1A1 =  InitialData.block("plyr_spr_init_data_D1A1");
    private final DataBlock sun_moon_yoff_C440 = InitialData.block("sun_moon_yoff_C440");
    private final GraphicObject sun_moon_scratchpad_C44D = new GraphicObjects(0xC44D, 1).o(0);
    // Includes special_objs_here and other_objs_here, 40 32-byte slots
    private final GraphicObjects graphicObjects = new GraphicObjects(0x5C08, 40);
    private final DataBlock objects_to_draw_CE8B = new DataBlock(0x2E); // 46, but there should be not more than 40 objects to draw
    private final Locations locations;
    private final BackgroundObjects backgroundObjects = new BackgroundObjects();
    private final ForegroundObjects foregroundObjects = new ForegroundObjects();
    private final DataBlock panel_data_D27E = InitialData.block("panel_data_D27E");
    private final DataBlock day_txt_BCE7 = InitialData.block("day_txt_BCE7");

    private final KeySequence keySequence;
    private Runnable tickCallback = () -> {};

    public void setTickCallback(Runnable r) { tickCallback = r; }

    private void updateMemory() {
        updateMainMemory();
        updateShadowMemory();
    }

    public Game(GamePanel mainPanel, GamePanel shadowPanel, MapPanel mapPanel, ObjectsPanel objectsPanel,
                SettingsPanel settingsPanel,
                DebugPanel debugPanel1, DebugPanel debugPanel2,
                ConcurrentSkipListSet<Integer> keyQueue, GameParams params, KeySequence keySequence) {
        this.mainPanel = mainPanel;
        this.shadowPanel = shadowPanel;
        this.mapPanel = mapPanel;
        this.objectsPanel = objectsPanel;
        this.settingsPanel = settingsPanel;
        this.debugPanel1 = debugPanel1;
        this.debugPanel2 = debugPanel2;
        this.keyQueue = keyQueue;
        this.params = params;
        this.keySequence = keySequence;

        this.debugPanel1.append("Start");
        this.debugPanel2.append("Start");

        SpriteData spriteData = new SpriteData();
        this.locations = new Locations(spriteData);
        mainMemory = new VideoMemoryScreen(spriteData);
        shadowMemory = new VideoMemoryLinear(spriteData);

        params.freezeSeed();
        for(Location l: locations.locations) {
            retrieve_screen_D3C6(l.id);
            update_sprite_loop_AFC7();
            displayObjects(l.getPreview());
        }
        this.mapPanel.setLocations(locations);
        this.mapPanel.setFont(font_6108);
        this.objectsPanel.setFont(font_6108);
        this.objectsPanel.initVideoMemory(spriteData);
        this.settingsPanel.initVideoMemory(spriteData);
        params.unfreezeSeed();
    }

    public void updateMainMemory() { mainPanel.setPixelData(mainMemory.toPixels(keySequence.isReplaying() ? -1 : tickNo)); }
    public void updateShadowMemory() { shadowPanel.setPixelData(shadowMemory.toPixels(keySequence.isReplaying() ? -1 : tickNo)); }

    @Override
    public void run() {
        start_AF6C();
    }

    private void start_AF6C() {
        debugPanel1.append("start_AF6C");

        gameCounter = 0;
        while(!variables.shouldQuit()) {
            variables.init();
            locations.resetVisited();
            inventory.reset();

            // For testing only
            // 65 14 F2 6F - bottle
            // 66 14 FB 6F - bottle
            // inventory.slots[3].set(0x65, 0x14, 0xF2, 0x6F);
            // inventory.slots[2].set(0x66, 0x14, 0xF8, 0x6F);

            variables.resetGameOver();
            gameId = nowAsString();
            if(!keySequence.isInactive()) new File("saves/"+gameId).mkdir();
            main_AF88();
            gameCounter++;
        }
    }

    private void main_AF88() {
        debugPanel1.append("main_AF88");

        settingsPanel.update();
        variables.setGameStart_5BB2();
        plyr_spr_1_scratchpad_D161.setFlagsC(0);
        variables.setLives_5BBA(params.getInitialLives() + 1); // NOTE: the game starts with lose life
        params.getRnd().updateSeed1();
        clear_scrn_D55F();
        do_menu_selection_BD0C();
        menu_loop_BD23(params.isSkipMenu()); // returns when game starts

        debugPanel2.append("START THE GAME");
        debugPanel2.append("Input method: " + variables.getUserInputMethod_5BA4());

        // CALL $B2CF    ; play tune // ignore audio
        if (gameCounter == 0) shuffle_objects_required_B544();

        int overrideRoomId = -1;
        int overrideWE = 0; // -W +E : -60 / +60 for large room (entrance to entrance)
        int overrideNS = 0; // -S +N
        int overrideZ = 0; // + only : 60 is a little above upper entrance

        init_start_location_D1B1(overrideRoomId, overrideWE, overrideNS, overrideZ);
        init_sun_C46D();
        init_special_objects_C47E();
        player_dies_AFB7();
        updateObjectsPanel();
        game_loop_AFBA();
    }

    private void updateObjectsPanel() {
        objectsPanel.update(objects_required_C27D, variables.getObjectsPutInCauldron_5BBB(), params.getCauldronRequiredObjects(), specialObjects,
                graphicObjects.firstSpecObj().getGrNo(), graphicObjects.firstSpecObj().next().getGrNo(),
                graphicObjects.playerBottom().getScreen());
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

        for(int hl = mainMemory.attrStart; hl < mainMemory.endExcl; hl++) {
            mainMemory.setByteAt(hl, 0x46);
            shadowMemory.setByteAt(hl, 0x46);
        }
        for(int hl = 0; hl < mainMemory.attrStart; hl++) {
            mainMemory.setByteAt(hl, 0);
            shadowMemory.setByteAt(hl, 0);
        }
    }

    private void do_menu_selection_BD0C() {
        debugPanel1.append("do_menu_selection_BD0C");
        variables.setSuppressBorder_5BB8(0);

        for (int hl = 0xBDA2; hl < 0xBDA2 + 8; hl++) { menu_colours_BDA2.set(hl, menu_colours_BDA2.getU(hl) & 0x7F); }
        shadowMemory.clearAndSetBackground();
        display_menu_BEB3();
        flash_menu_BD89();

        display_menu_BEB3();
        // CALL $B2B6    ; ignore audio
    }

    private void display_menu_BEB3() {
        debugPanel1.append("display_menu_BEB3");
        DataBlock menu_xy_BDAA = InitialData.block("menu_xy_BDAA");
        DataBlock menu_text_BDBA = InitialData.block("menu_text_BDBA");
        int hl2 = 0xBDAA; // menu xy
        int de2 = 0xBDBA; // menu text
        for(int de1 = 0xBDA2; de1 < 0xBDA2+8; de1++) {
            int x = menu_xy_BDAA.getU(hl2); // x - menu position
            int y = menu_xy_BDAA.getU(hl2+1); // y
            hl2 += 2;
            de2 = print_text_single_colour_BE31(menu_text_BDBA, x, y, de2, menu_colours_BDA2.getU(de1));
        }
        int a = variables.getSuppressBorder_5BB8(); // suppress border
        if(a == 0) {
            variables.setSuppressBorder_5BB8(1);
            print_border_D296();
            update_screen_D56F(true);
        }
    }

    private int print_text_single_colour_BE31(DataBlock text, int x, int y, int de, int color) {
        // h: y, l: x, de2: text address (first)
        debugPanel1.append(String.format("print_text_single_colour_BE31 ( x: %02x, y: %02x, text_addr: %04x)", x, y, de));
        int bc = VideoMemory.calcVideoAddr(y, x);
        debugPanel1.append(String.format("Video addr: %04x", bc));

        int hl = VideoMemory.calcAttribAddr(y, x);
        de = printText(text, de, bc, hl, color);
        return de;
    }

    private int printText(DataBlock text, int de, int bc, int hl, int attr) {
        boolean textDone = false;
        while(!textDone) {
            shadowMemory.printCharacter(text.getU(de), bc, font_6108);
            mainMemory.setByteAt(hl, attr); // Color.getAttribute(Color.WHITE, Color.GREEN, Color.NONE, Color.NONE));
            shadowMemory.setByteAt(hl, attr);
            textDone = (text.getU(de) & 0x80) > 0;
            hl++;
            de++;
            bc++;
        }
        return de;
    }

    private void print_text_std_font_BE45(DataBlock text, int x, int y, int de) {
        // h: y, l: x, de2: text address (first)
        debugPanel1.append(String.format("print_text_single_colour_BE31 ( x: %02x, y: %02x, text_addr: %04x)", x, y, de));
        int bc = VideoMemory.calcVideoAddr(y, x);
        debugPanel1.append(String.format("Video addr: %04x", bc));

        int attr = text.getU(de); // first byte is the attribute
        int hl = VideoMemory.calcAttribAddr(y, x);

        printText(text, de+1, bc, hl, attr);
    }

    private void flash_menu_BD89() {
        debugPanel1.append("flash_menu_BD89");
        int hl = 0xBDA3; // first menu entry
        int a = variables.getUserInputMethod_5BA4(); // input method
        a = (a >> 1) & 0x3; // joystick / keyboard flag
        // CALL $BEA3
        for(int i = 0; i < 4; i++) {
            if(a == i) menu_colours_BDA2.set(hl+i, menu_colours_BDA2.getU(hl+i) | 0x80 );
            else menu_colours_BDA2.set(hl+i, menu_colours_BDA2.getU(hl+i) & 0x7F );
        }
        if((variables.getUserInputMethod_5BA4() & 0x08)>0) menu_colours_BDA2.set(hl+4, menu_colours_BDA2.getU(hl+4) | 0x80 );
        else menu_colours_BDA2.set(hl+5, menu_colours_BDA2.getU(hl+5) & 0x7F );
    }

    private void print_border_D296() {
        debugPanel1.append("print_border_D296");
        DataBlock border_data_D2CF = InitialData.block("border_data_D2CF");
        int hl = 0xD2CF;
        // CALL $D24C x4
        for(int i=0; i<4; i++) hl = transfer_sprite_and_print_D24C(sprite_scratchpad_BFDB, hl, border_data_D2CF); // corners
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 8, 0, 0x18, hl, border_data_D2CF); // horizontal lines
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 8, 0, 0x18, hl, border_data_D2CF);
        hl = transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 0, 1, 0x80, hl, border_data_D2CF); // vertical lines
        transfer_and_multiple_print_sprite(sprite_scratchpad_BFDB, 0, 1, 0x80, hl, border_data_D2CF);
    }

    private int transfer_sprite_and_print_D24C(GraphicObject o, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        transfer_sprite_D237(o, hl, source);
        shadowMemory.printSprite(o);
        return hl+4;
    }

    // Populate sprite metadata
    private void transfer_sprite_D237(GraphicObject o, int hl, DataBlock source) {
        // hl: sprite index, ix: scratchpad address
        o.setGrNo(source.getU(hl));
        o.setFlags7(source.getU(hl+1));
        o.setPixelX(source.getU(hl+2));
        o.setPixelY(source.getU(hl+3));
    }


    private int transfer_and_multiple_print_sprite(GraphicObject o, int dx, int dy, int times, int hl, DataBlock dataBlock) {
        transfer_sprite_D237(o, hl, dataBlock);
        return multiple_print_sprite_BEE4(o, dx, dy, times, hl);
    }

    private int multiple_print_sprite_BEE4(GraphicObject o, int dx, int dy, int times, int hl) {
        for(int i=0; i<times; i++) {
            shadowMemory.printSprite(o);
            o.setPixelX(o.getPixelX() + dx);
            o.setPixelY(o.getPixelY() + dy);
        }
        return hl+4;
    }

    private void update_screen_D56F(boolean clearBuffer) {
        int bytesX = VideoMemory.WIDTH / 8;
        for(int bufferY = 0; bufferY<VideoMemory.HEIGHT; bufferY++) {
            int screenY = ((bufferY & 0b111) <<3) + ((bufferY & 0b111000) >> 3) + (bufferY & 0b11000000);
            int screenBase = screenY * bytesX;
            int bufferBase = (VideoMemory.HEIGHT - bufferY -1) * bytesX;
            for(int b = 0; b<VideoMemory.WIDTH / 8; b++) {
                mainMemory.setByteAt(screenBase+b, shadowMemory.getByteAt(bufferBase+b));
                if(clearBuffer) shadowMemory.setByteAt(bufferBase+b, 0); // wipe buffer
            }
        }
        // wipe buffer attributes (non-existent in original game)
        if(clearBuffer) {
            for (int attr = shadowMemory.attrStart; attr < shadowMemory.attrStart + VideoMemory.HEIGHT / 8 * VideoMemory.WIDTH / 8; attr++)
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
                int a = variables.getUserInputMethod_5BA4();
                variables.setOldInputMethod_5BA6(a);
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
                variables.setUserInputMethod(a);
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

        plyr_spr_1_scratchpad_D161.copyFromInit(plyr_spr_init_data_D1A1, 0);
        plyr_spr_2_scratchpad_D181.copyFromInit(plyr_spr_init_data_D1A1, 8);
        plyr_spr_1_scratchpad_D161.setTemp10(0x12);
        plyr_spr_2_scratchpad_D181.setTemp10(0x22);

        int randomLoc = locations.getStartLocation(params.getRnd().getSeed1() & 0x3);
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
        variables.resetTransformFlag_5BB1();
        int livesLeft = variables.getLives_5BBA();
        if(livesLeft < 1) {
            variables.setLives_5BBA(-1);
            game_over_BA22();
            return;
        }
        int newLivesLeft = params.isDisableLoseLife() ? livesLeft : livesLeft - 1;
        variables.setLives_5BBA(newLivesLeft);
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

        if(variables.getAllObjectsInCauldron_5BC3() != 0) game_complete_msg_BAAB(); // all objects in cauldron

        gameOverScreen();
        variables.setGameOver();
    }

    private void gameOverScreen() {
        shadowMemory.clearAndSetBackground();
        clear_scrn_D55F();

        variables.setSuppressBorder_5BB8(0);
        DataBlock gameover_colours_BB4C = InitialData.block("gameover_colours_BB4C");
        DataBlock gameover_xy_BB52 = InitialData.block("gameover_xy_BB52");
        DataBlock a_GAME_OVER_BB5E = InitialData.block("a_GAME_OVER_BB5E");
        display_text_list_BEBF(gameover_colours_BB4C, gameover_xy_BB52, a_GAME_OVER_BB5E);
        int days = variables.getDays_5BB9();
        if(days > params.getMaxDays()) days --; // game over due to timeout
        int daysAddr = 15 + 127 * 0x20; // (x,y)=(120, 127)
        int daysBCD = MathUtils.byteToBCD(days);
        if(daysBCD < 0x10) print_BCD_lsd_BCC0(shadowMemory, + 1, daysBCD);
        else print_BCD_number_BCAE(shadowMemory, daysAddr, daysBCD);
        calc_and_display_percent_BC10();

        int objectsInCauldron = variables.getObjectsPutInCauldron_5BBB();
        int objAddr = 23 + 79 * 0x20; // (x,y)=(184, 79)
        int objBCD = MathUtils.byteToBCD(objectsInCauldron);
        if(objBCD < 0x10) print_BCD_lsd_BCC0(shadowMemory, + 1, objBCD);
        else print_BCD_number_BCAE(shadowMemory, objAddr, objBCD);

        int screens = locations.countVisited();
        int allObjInCauldron = variables.getAllObjectsInCauldron_5BC3();

        int a = screens;
        int c = ((a << 1) & 0xFF) | ((a >> 7) & 1);
        c = c & 0xC0;
        a = allObjInCauldron;
        a = a & 1;
        a = a | c;
        a = ((a << 3) & 0xFF) | ((a >> 5) & 7);
        a = a & 0x0E;
        IO.println("rating "+a);
        DataBlock rating_tbl_BBB7 = InitialData.block("rating_tbl_BBB7");
        int rating = a;
        int ratingAddrL = rating_tbl_BBB7.getU(rating_tbl_BBB7.start + rating);
        int ratingAddrH = rating_tbl_BBB7.getU(rating_tbl_BBB7.start + rating + 1);
        int ratingAddr = ratingAddrH * 256 + ratingAddrL;

        IO.println(String.format("rating address %04X",ratingAddr));

        print_text_std_font_BE45(rating_tbl_BBB7, 0x58, 0x27, ratingAddr);

        update_screen_D56F(true);
        waitForKey();
    }

    private void calc_and_display_percent_BC10() {
        int screens = locations.countVisited();
        int objInCauldron = variables.getObjectsPutInCauldron_5BBB();
        int percent = 100 * (screens + 2*objInCauldron) / 156; // 156 = 128 + 2*14, where 14 is maximum number of charms
        //IO.println("percent = " + percent + " BCD " + String.format("%03X", byteToBCD(percent)));
        int percentAddr = 19 + 95 * 0x20; // (x,y)=(152, 95)
        int percentBCD = MathUtils.byteToBCD(percent);
        if(percentBCD < 0x10) {
            print_BCD_lsd_BCC0(shadowMemory, + 1, percentBCD);
        } else {
            print_BCD_number_BCAE(shadowMemory, percentAddr, percentBCD);
            if(percentBCD >= 0x100) {
                print_BCD_lsd_BCC0(shadowMemory, - 1, (percentBCD >> 8) & 0x0F);
            }
        }
    }

    private void game_complete_msg_BAAB() {
        shadowMemory.clearAndSetBackground();
        clear_scrn_D55F();

        variables.setSuppressBorder_5BB8(0);
        DataBlock complete_xy_BAD8 = InitialData.block("complete_xy_BAD8");
        DataBlock complete_colours_BAD2 = InitialData.block("complete_colours_BAD2");
        DataBlock the_potion_casts_BAE4 = InitialData.block("the_potion_casts_BAE4");
        display_text_list_BEBF(complete_colours_BAD2, complete_xy_BAD8, the_potion_casts_BAE4);
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
            int x = xy.getU(xy.start + i*2);
            int y = xy.getU(xy.start + i*2+1);
            de = print_text_single_colour_BE31(text, x, y, de, colors.getU(colors.start + i));
        }
        int a = variables.getSuppressBorder_5BB8(); // suppress border
        if(a == 0) {
            variables.setSuppressBorder_5BB8(1);
            print_border_D296();
        }
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

                debugTable2("Inventory", inventory.data.start, inventory.data.rawCopy());
                debugPanel1.append(String.format("Current room: %02X (hex)", graphicObjects.playerBottom().getScreen()));
                debugPanel1.append(String.format("Objects collected: %d of %d", variables.getObjectsPutInCauldron_5BBB(), params.getCauldronRequiredObjects()));
                int hl = ret_next_obj_required_C274();
                if(hl < objects_required_C27D.endExcl()) {
                    int required = objects_required_C27D.getU(hl) | 0x60;
                    debugPanel1.append(String.format("Next required object: %s", DataBlock.GRAPHIC_NAMES[required]));
                }
                debugPanel1.append(String.format("Rooms visited: %d", locations.countVisited()));
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
                debugPanel1.append(String.format("Mode: %s", keySequence.isReplaying() ? "Replay" : keySequence.isSaving() ? "Save" : "Play"));

                delay();
            }
        }
    }

    private void displayObjects(VideoMemory v) {
        v.clearAndSetBackground(); // For testing
        list_objects_to_draw_CE62();
        calc_display_order_and_render_CEBB(v);
    }

    private boolean onscreen_loop_AFBD() {
        params.getRnd().updateSeed2();
        update_sprite_loop_AFC7();

        if(variables.isExitingScreen()) return false;
        variables.resetGameStart_5BB2(); // ; update special objects table next time
        init_cauldron_bubbles_B8A9();
        //debugTable("Objects to draw: ", 0, objects_to_draw_CE8B.getCopy());

        // set attributes so the buffer contents are visible
        for(int i=0; i < VideoMemory.ATTR_MEM_SIZE; i++) shadowMemory.setByteAt(i + shadowMemory.attrStart, Color.getAttribute(Color.WHITE, Color.BLUE, Color.NONE, Color.NONE));

        displayObjects(shadowMemory);

        // screen attribute (only main screen)
        for(int addr = mainMemory.attrStart; addr<mainMemory.endExcl; addr++) mainMemory.setByteAt(addr, variables.getCurrRoomAttrib_5BAD());

        display_objects_BF4E();

        fill_window_C515(mainMemory, mainMemory.attrStart + 0x20 * 21 + 22, 1, 3, 0);
        fill_window_C515(mainMemory, mainMemory.attrStart + 0x20 * 21 + 29, 1, 3, 0);
        fill_window_C515(mainMemory, mainMemory.attrStart + 0x20 * 20 + 23, 6, 4, 0x42);
        boolean isSun = ((sun_moon_scratchpad_C44D.getGrNo()) & 1) == 0;
        int attr = isSun ? 0x46 : 0x47;
        fill_window_C515(mainMemory, mainMemory.attrStart + 0x20 * 21 + 24, 4, 2, attr);

        display_panel_D255();
        display_sun_moon_frame_C3A4();
        display_day_BCCA();
        print_days_BC66();
        print_lives_gfx_BC7A();
        print_lives_BCA3();
        if(variables.isGameOver()) return true;
        update_screen_D56F(false);

        if(graphicObjects.playerBottom().getGrNo()==0 && graphicObjects.playerTop().getGrNo()==0) {
            player_dies_AFB7();
            variables.setExitingScreen();
            return variables.isGameOver();
        }

        // @label=reset_objs_wipe_flag
        // Mot used - all objects are redrawn
        for(int addr = graphicObjects.start(); addr<graphicObjects.endExcl(); addr+=0x20) graphicObjects.i2o(addr).resetWipe();

        return variables.isGameOver();
    }

    private void init_cauldron_bubbles_B8A9() {
        if(!graphicObjects.playerBottom().isCauldronRoom()) return;

        // NOTE: first special object is cauldron bubbles
        GraphicObject secondSpecObj = graphicObjects.firstSpecObj().next(); // ; special_objs_here[1]
        if(secondSpecObj.getGrNo() != 0) return;
        if(variables.getAllObjectsInCauldron_5BC3() != 0) return;

        secondSpecObj.makeCauldronBubbles();
        secondSpecObj.setPixelXYAdj(-12, -4);
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
        int attr = variables.getCurrRoomAttrib_5BAD(); // room attribute
        attr = (((attr ^ 0xFF) + 2) & 0x07) | 0x40;
        day_txt_BCE7.set(day_txt_BCE7.start, attr);
        int de = day_txt_BCE7.start;
        int x = 0x70;
        int y = 0x0F;
        int videoAddr = VideoMemory.calcVideoAddr(y, x);
        int attrAddr = VideoMemory.calcAttribAddr(y, x);
        de++;

        boolean textDone = false;
        while(!textDone) {
            shadowMemory.printCharacter(day_txt_BCE7.getU(de), videoAddr, day_font_BCEC);
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
            InventorySlot slot = inventory.slots[b+1]; // first slot is not a visible inventory
            if(slot.getGrNo() != 0) {
                int x = (2-b)*24+16;
                int y = 0;
                sprite_scratchpad_BFDB.setPixelX(x);
                sprite_scratchpad_BFDB.setPixelY(y);
                int hl = VideoMemory.calcVideoAddr(y, x);
                fill_window_C515(shadowMemory, hl, 3, 24, 0);
                int spriteIndex = slot.getGrNo();
                sprite_scratchpad_BFDB.setGrNo(spriteIndex);
                shadowMemory.printSprite(sprite_scratchpad_BFDB);
                hl = VideoMemory.calcAttribAddr(y + 0x17, x);
                fill_window_C515(mainMemory, hl, 3, 3, specialObjects.getAttr(spriteIndex));
            }
        }
    }

    private void print_days_BC66() {
        int videoAddress = 0x20 * 7 + 15; // (120, 7)
        print_BCD_number_BCAE(shadowMemory, videoAddress, MathUtils.byteToBCD(variables.getDays_5BB9()));
    }

    private void print_BCD_number_BCAE(VideoMemory v, int bc, int value) {
        // NOTE: we assume that the number is already in BCD
        int digitH = (value >> 4) & 0x0F;
        int digitL = value & 0x0F;
        v.printCharacter(digitH, bc, font_6108);
        v.printCharacter(digitL, bc+1, font_6108);
    }

    private void print_BCD_lsd_BCC0(VideoMemory v, int bc, int value) {
        int digitL = value & 0x0F;
        v.printCharacter(digitL, bc, font_6108);
    }

    private void print_lives_gfx_BC7A() {
        sprite_scratchpad_BFDB.reset();
        sprite_scratchpad_BFDB.setGrNo(0x8C);
        sprite_scratchpad_BFDB.setFlags7(0);
        sprite_scratchpad_BFDB.setPixelX(0x10);
        sprite_scratchpad_BFDB.setPixelY(0x20);
        shadowMemory.printSprite(sprite_scratchpad_BFDB);
        fill_window_C515(mainMemory, mainMemory.attrStart + 0x20 * 18 + 2, 2, 1, 0x47);
        fill_window_C515(mainMemory, mainMemory.attrStart + 0x20 * 19 + 2, 4, 1, 0x47);
    }

    private void print_lives_BCA3() {
        print_BCD_number_BCAE(shadowMemory, 0x20 * 39 + 4, MathUtils.byteToBCD(variables.getLives_5BBA()));
    }

    private void list_objects_to_draw_CE62() {
        objects_to_draw_CE8B.reset();
        int toDrawPos = 0;
        for(int i = 0; i < graphicObjects.size; i++) {
            if(graphicObjects.o(i).getGrNo() != 0/* && (!graphicObjects.isWipe(ix + i*0x20))*/) {
                objects_to_draw_CE8B.set(toDrawPos, i);
                toDrawPos++;
            }
        }
        objects_to_draw_CE8B.set(toDrawPos, 0xFF); //flag end of list
    }

    private void calc_display_order_and_render_CEBB(VideoMemory v) {
        int renderedCnt = 0; // rendered objects cnt
        int de1 = objects_to_draw_CE8B.start;
        int i = de1;
        while(objects_to_draw_CE8B.getU(i) !=0xFF) i++;
        int cntToRender = i - de1;
        RenderStack renderList = new RenderStack();
        while(renderedCnt < cntToRender) {
            boolean rendered1 = (objects_to_draw_CE8B.getU(de1) & 0x80) > 0;
            if(!rendered1) {
                boolean isLast = renderedCnt >= cntToRender-1;
                // if only one object remains, we should render it and exit
                if(isLast) {
                    renderOne(v, de1, renderList);
                    renderedCnt++;
                } else {
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
                                int objectLoc1 = objects_to_draw_CE8B.getU(de1) * 32 + graphicObjects.start();
                                int grNo1 = graphicObjects.i2o(objectLoc1).getGrNo();
                                boolean isSpecial1 = grNo1 >= 0x60 && grNo1 <= 0x66;
                                int objectLoc2 = objects_to_draw_CE8B.getU(de2) * 32 + graphicObjects.start();
                                int grNo2 = graphicObjects.i2o(objectLoc2).getGrNo();
                                boolean isSpecial2 = grNo2 >= 0x60 && grNo2 <= 0x66;
                                if(isSpecial1) graphicObjects.i2o(objectLoc1).setGrNo(0xBB); //; set to twinkle sprite
                                else {
                                    if(isSpecial2) graphicObjects.i2o(objectLoc2).setGrNo(0xBB); //; set to twinkle sprite
                                    // The first object on the list is behind the second
                                    // (e.g. spell is behind cauldron, so it is invisible when inside).
                                    // This makes rendering of coinciding object deterministic.
                                    else is2behind1 = de2 < de1;
                                }
                            }
                            is2behind1 |= is2behind1(relativePosition);
                            if (is2behind1) {
                                if (renderList.contains(objects_to_draw_CE8B.getU(de2))) {
                                    renderOne(v, de2, renderList);
                                    renderedCnt++;
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
                        if (renderList.isEmpty() || renderList.isLast(objects_to_draw_CE8B.getU(de1))) {
                            renderOne(v, de1, renderList);
                            renderedCnt++;
                        }
                        de1++;
                    }
                }
            } else de1++;
            // restart loop if not all objects are rendered
            if(objects_to_draw_CE8B.getU(de1) == 0xFF) de1 = objects_to_draw_CE8B.start;
        }
    }

    private void renderOne(VideoMemory v, int de, RenderStack renderList) {
        GraphicObject o = graphicObjects.o(objects_to_draw_CE8B.getU(de));
        if(calc_pixel_XY_D6C9(o)) v.printSprite(o);
        objects_to_draw_CE8B.set(de, objects_to_draw_CE8B.getU(de) | 0x80);
        renderList.reset();
    }

    // Returns number of case from jump table at $CF69
    private int analyzeRelativePosition(int de1, int de2) {
        if(de1==de2) return 0;

        GraphicObject o1 = graphicObjects.o(objects_to_draw_CE8B.getU(de1));
        GraphicObject o2 = graphicObjects.o(objects_to_draw_CE8B.getU(de2));
        boolean overlap1 = o1.getZ() >= (o2.getZ()+o2.getH());             // $CF05 JR NC,$CF16    ; no overlap (C+=0)
        boolean overlap2 = o2.getZ() >= (o1.getZ()+o1.getH());             // $CF12 JR C,$CF15     ; overlap (C+=1)
        boolean overlap3 = (o1.getY()-o1.getD()) >= (o2.getY()+o2.getD()); // $CF23 SUB L          ; Y1-D1-(Y2+d2)
        boolean overlap4 = (o2.getY()-o2.getD()) >= (o1.getY()+o1.getD()); // $CF33 SUB L          ; Y2-D2-(Y1+D1)
        boolean overlap5 = (o1.getX()-o1.getW()) >= (o2.getX()+o2.getW()); // $CF49 SUB L          ; X1-W1-(X2+W2)
        boolean overlap6 = (o2.getX()-o2.getW()) >= (o1.getX()+o1.getW()); // $CF59 SUB L          ; X2-W2-(X1+W1)

        int c=0;
        if(!overlap1) c += overlap2 ? 2 : 1;
        if(!overlap3) c += overlap4 ? 6 : 3;
        if(!overlap5) c += overlap6 ? 18 : 9;

        return c;
    }

    private boolean is2behind1(int c) { return c==3 || c==4 || c==6 || c==7 || c==12 || c==15 || c==16; }

    private boolean overlaps(int c) { return c == 13; }

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
        if(variables.getAllObjectsInCauldron_5BC3()!=0) return;

        int x = sun_moon_scratchpad_C44D.getPixelX();
        if (x == 0xE1 - 1) toggle_day_night_C3FF();

        int y = ((x + 0x10) >> 2) & 0x0F;
        int hl = 0xC440 + y;
        sun_moon_scratchpad_C44D.setPixelY(sun_moon_yoff_C440.getU(hl));
        if (sunTick == 0) sun_moon_scratchpad_C44D.setPixelX(x + 1);

        fill_window_C515(shadowMemory, 23, 6, 0x1F, 0);
        shadowMemory.printSprite(sun_moon_scratchpad_C44D);

        sprite_scratchpad_BFDB.setGrNo(0x5A);
        sprite_scratchpad_BFDB.setFlags7(0);
        sprite_scratchpad_BFDB.setPixelX(0xB8);
        sprite_scratchpad_BFDB.setPixelY(0);
        shadowMemory.printSprite(sprite_scratchpad_BFDB);
        sprite_scratchpad_BFDB.setPixelX(0xD0);
        sprite_scratchpad_BFDB.setGrNo(0xBA);
        shadowMemory.printSprite(sprite_scratchpad_BFDB);
    }

    // ; Input:HL starting location B  width (bytes) C  height (lines), A - value
    private void fill_window_C515(VideoMemory memory, int hl, int b, int c, int a) {
        for(int y = 0; y<c; y++)
            for(int x = 0; x<b; x++) memory.setByteAt(hl + x + y*0x20, a);
    }

    private void toggle_day_night_C3FF() {
        int currentSprite =  sun_moon_scratchpad_C44D.getGrNo();
        int nextSprite = currentSprite ^ 1; // flip last bit
        sun_moon_scratchpad_C44D.setGrNo(nextSprite);
        sun_moon_scratchpad_C44D.setPixelX(0xB0); // ; pixel X
        variables.setTransformFlag_5BB1(); // transform flag
        if((nextSprite & 1) == 0) {
            int currDays = variables.getDays_5BB9();
            int incDays = params.isDisableIncDays() ? currDays : (currDays + 1); // NOTE: we display only 2 digits
            variables.setDays_5BB9(incDays);
            if(incDays == params.getMaxDays()+1) {
                game_over_BA22();
                return;
            }
            print_days_BC66();
        }
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
            o.setPixelXYAdj(-17, -2);
            int x = o.getX() - 0x0D;
            o.setDX(x); // dX
            int y = o.getY();
            o.setDY(y); // dY=Y
            centreY = 15;
            centreX = 6;
        } else {
            if (o.getGrNo() == 4) o.setPixelXYAdj(1, -3);
            else o.setPixelXYAdj(-7, -3);
            int y = o.getY() + 0x0D;
            o.setDY(y); // dY
            int x = o.getX();
            o.setDX(x); // dX
            centreY = 6;
            centreX = 15;
        }
        o.setDZ(o.getZ());
        chk_plyr_spec_near_arch_C7DB(o, centreY, centreX);

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
        if(o.isHFlip()) o.setPixelXYAdj(-7, -2);
        else o.setPixelXYAdj(-9, -3);
    }

    private void upd_6_7_C4E3(GraphicObject o) {
        o.setPixelXYAdj(-16, -8);
    }

    private void upd_8_C65E(GraphicObject o) {
        o.setPixelXYAdj(-12, -6); //FA

        boolean moving = variables.isPortcullisMoving_5BAF();
        if(moving) return;
        int roomSizeZ = variables.getRoomSizeZ_5BAE();
        int z = o.getZ();
        if(z == roomSizeZ || z <= (roomSizeZ + 0x1F)) {
            if((params.getRnd().getSeed3() & 0x1F) != 0) return; // ; 1in32 chance?

            o.setBitGrNo(0); // ; graphic_no=9
            o.setDZ(1); // ; dZ=1 (up)
        } else {
            int moveCnt = variables.getPortcullisMoveCnt_5BB0();
            if(moveCnt >= 4) {
                if((params.getRnd().getSeed3() & 0x1F) != 0) return;
                moveCnt = 0x80; // ; arbitrarily large count
            }
            variables.setPortcullisMoveCnt_5BB0(moveCnt + 1);
            o.setBitGrNo(0); // ; graphic_no=9
            o.setDZ(-1); // ; dZ=-1 (down)
        }
        variables.setPortcullisMoving_5BAF();
        o.setWipeAndDraw();
    }

    private void upd_9_C6BD(GraphicObject o) {
        o.setPixelXYAdj(-12, -6);

        o.setDeadlyIfObjectHitsPlayer();
        boolean enteringRoom = o.enteringScreen();
        if(enteringRoom) return;
        int dZ =  o.getDZ();
        if(dZ >= 0) {
            o.setDZ(2);
            // $C6EE CALL $B467      ; // ignore audio
            dec_dZ_and_update_XYZ_C700(o);
            int roomSizeZ = variables.getRoomSizeZ_5BAE();
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
        variables.resetPortcullisMoving_5BAF();
        o.resetBitGrNo(0); // graphic_no=8
    }

    private void upd_10_C4E8(GraphicObject o) { o.setPixelXYAdj(-20, -1); }

    private void upd_11_C4ED(GraphicObject o) { o.setPixelXYAdj(-12, -2); }

    private void upd_12_to_15_C4F2(GraphicObject o) { o.setPixelXYAdj(-8, -4); }

    private void upd_128_to_130_C4D3(GraphicObject o) { o.setPixelXYAdj(-8, -2); }

    private void upd_131_to_133_B566(GraphicObject o) {
        o.setPixelXYAdj(-12, -4);

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
            switch(xy) {
                case 0: dY = 4; dX = -4; break;
                case 1: dY = 4; dX = 4; break;
                case 2: dY = -4; dX = -4; break;
                case 3: dY = -4; dX = 4; break;
            }
            o.setDX(dX);
            o.setDY(dY);

            int a = params.getRnd().getSeed3() & 0x03;
            if(a==0) a++;
            a+=0x82;
            o.setGrNo(a);
        } else {
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
            o.setIgnore3D();
            o.setDZ(1);
            move_towards_plyr_B965(o, 4, 4);
        }
        dec_dZ_and_update_XYZ_C700(o);
        o.setWipeAndDraw();
    }

    private void upd_16_to_21_24_to_29_C823(GraphicObject o) {
        o.setPixelXYAdj(-12, -6);
        upd_player_bottom_C82B(o);
    }

    private void upd_player_bottom_C82B(GraphicObject o) {
        if(o.isDead()) {
            if (variables.getAllObjectsInCauldron_5BC3() == 0) {
                o.next().setDead(); // SET 6,(IX+$2D)
                init_death_sparkles_BF21(o);
                return;
            }
        }
        // Note: this behavior comes from increasing SP twice when transformation starts ($C316-$C317)
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

    private void handle_pickup_drop_C00E(GraphicObject player) {
        if(!variables.isPickupPressed_5BB3()) {
            if(!chk_pickup_drop_BFFB() || chk_plyr_OOB_C87A(player) || player.isJumping() || !player.isOobZ()) return;
            boolean cantDrop=false;
            int zOrig = player.getZ();
            player.setZ(zOrig + 12); // ; Z+12
            boolean intersects = do_any_objs_intersect_B4FD(player);
            player.setZ(zOrig); // ; restore original Z
            if(intersects) cantDrop = true;
            variables.setPickupPressed_5BB3();
            int origW = player.getW();
            int origD = player.getD();
            int origH = player.getH();
            player.setW(origW + 4); // ; width+4
            player.setD(origD + 4); // ; depth+4
            player.setH(origH + 4); // ; height+4
            GraphicObject specObjP = graphicObjects.firstSpecObj();
            boolean canPickup = false;
            int b=2;
            while(b>0 && !canPickup) {
                canPickup = specObjP.isSpecialObject() && is_on_or_near_obj_C17A(player, specObjP);
                if(!canPickup) {
                    b--;
                    specObjP = specObjP.next();
                }
            }
            if(canPickup) pickup_object_C141(player, specObjP);
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
                    if(foundPlace) room_to_drop_C0B2(player, slotD, cantDrop);
                };
            }
            player.setW(origW);
            player.setD(origD);
            player.setH(origH);
        } else {
            if(chk_pickup_drop_BFFB()) return;
            variables.resetPickupPressed_5BB3();
        }
    }

    private void room_to_drop_C0B2(GraphicObject player, GraphicObject toDrop, boolean cantDrop) {
        InventorySlot slot = inventory.currentSlot();
        if(slot.getGrNo() == 0) inventory.adjustCarried();
        else {
            if(!cantDrop) {
                int grNo = slot.getGrNo();
                toDrop.setGrNo(grNo);
                if(player.isCauldronRoom()) {
                    int z = player.getZ();
                    if(z >= 0x98) {
                        toDrop.setBitGrNo(3);
                        variables.setObjectDroppingIntoCauldron_5BC4(1);
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
        variables.resetDisableSpikeBallDrop_5BC0();
        InventorySlot slot = inventory.firstSlot();
        slot.setGrNo(obj.getGrNo());
        slot.setFlags(obj.getFlags7());
        int e = obj.getTemp10();
        int d = obj.getTemp11();
        int ptr =  d * 256 + e; // ; ptr graphic object
        int spi = specialObjects.getIndex(ptr);
        specialObjects.clearGraphicNo(spi);
        slot.setPointerL(e);
        slot.setPointerH(d);
        obj.setWipeAndDraw();
        obj.setGrNo(1);

        InventorySlot currentSlot = inventory.currentSlot();
        if(currentSlot.getGrNo() == 0) inventory.adjustCarried();
        else {
            obj.setGrNo(currentSlot.getGrNo());
            drop_object_C106(o, obj);
        }
        updateObjectsPanel();
    }

    private void drop_object_C106(GraphicObject player, GraphicObject obj) {
        InventorySlot currentSlot = inventory.currentSlot();
        obj.setW(5);
        obj.setD(5);
        obj.setH(0x0C);
        obj.setFlags7(currentSlot.getFlags());
        obj.setScreen(player.getScreen());
        obj.setTemp10(currentSlot.getPointerL()); // set ptr to obj_tbl entry
        obj.setTemp11(currentSlot.getPointerH()); // 2-byte pointer to special objects table
        obj.setJustDropped();
        inventory.adjustCarried();
        updateObjectsPanel();
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
        o.setIgnore3D();
        for(int b = 0; b<graphicObjects.size; b++) {
            GraphicObject objToCheck = graphicObjects.o(b);
            if(objToCheck.isNotIgnored() &&
                    o.intersectOnX(objToCheck, 0) &&
                    o.intersectOnY(objToCheck, 0) &&
                    o.intersectOnZ(objToCheck, 0)) {
                o.resetIgnore3D();
                return true;
            }
        }
        o.resetIgnore3D();
        return false;
    }

    private boolean chk_pickup_drop_BFFB() {
        return (variables.getUserInput_5BB5() & 0x10) > 0; // only standard keyboard input
    }

    private boolean chk_plyr_OOB_C87A(GraphicObject p) {
        int roomSizeX = variables.getRoomSizeX_5BAB();
        int roomSizeY = variables.getRoomSizeY_5BAC();
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
        if(variables.getObjectDroppingIntoCauldron_5BC4()!=0) player.setDZ(2);
        boolean forward = variables.isSet_5BB5(2);
        if(player.isJumping() || player.enteringScreen() || forward) calc_plyr_dXY_C9FB(player);

        int dz = player.getDZ();
        if(dz < 0) dz -= 2;
        else {
            boolean jumpingKey = variables.isSet_5BB5(3);
            if (jumpingKey) dz -= 1; else dz -= 2;
        }
        player.setDZ(dz);
        int tmpDz = dz;
        //$C9D6 CALL M,$B451   ; ignore audio

        adj_for_out_of_bounds_CB45(player);
        handle_exit_screen_CA70(player);
        // NOTE: values 0xFF in X and Y are flags indicating which arch user exited the room
        if(!variables.isExitingScreen()) {
            add_dXYZ_C706(player);
            if (player.isOobZ() && tmpDz < 0) player.resetJumping();
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
        int oldRoomSizeX = variables.getRoomSizeX_5BAB(); // L
        int oldRoomSizeY = variables.getRoomSizeY_5BAC(); // H

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

        if(graphicObjects.i2o(objectIx).getDeadlyIfObjectHitsPlayer()) graphicObjects.i2o(playerIx).setDead(); // bit 7->6
        if(graphicObjects.i2o(playerIx).getDeadlyIfPlayerHitsObject()) graphicObjects.i2o(objectIx).setDead(); // bit 5->6
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
        int roomSize = variables.getRoomSizeZ_5BAE();
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

        int roomSizeX = variables.getRoomSizeX_5BAB();

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

        int roomSizeY = variables.getRoomSizeY_5BAC();

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
        if(!variables.isTransformFlag_5BB1() || o.getEnterCounter() > 0 || o.isJumping()) return false;

        variables.setTransformGrNo_5BB1(o.getGrNo()); // preserve graphics no
        o.setTemp10(8); // transform counter

        o.next().setGrNo(1); // update player top
        //set_wipe_and_draw_flags_C692(o.ix + 0x20);
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
        int prevC = variables.getUserInput_5BB5();
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

            variables.setUserInput_5BB5(c);
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
        int keys = variables.getUserInput_5BB5();
        if((keys & 3) == 0) return; // ; left or right?

        if(o.getTurnCounter() > 0 || o.isJumping()) return; // ; already jumping?
        // $C911 CALL $B4C1     ;
        // ignore audio
        o.initTurnCounter();
        boolean right = (keys & 1)>0;
        boolean left = (keys & 2)>0;
        if((right & !o.isHFlip()) || (left & o.isHFlip())) o.toggleBitGrNo(3);
        o.toggleHFlip();
        o.next().setGrNo(o.getGrNo() + 0x10); // ; set sprite for top half
    }

    private void handle_jump_C948(GraphicObject o) {
        boolean jump = DataBlock.isSetS(3, variables.getUserInput_5BB5());
        if(!jump || o.enteringScreen() || o.isJumping()) return;
        if(o.getDZ() < -1) return;
        o.setJumping();
        o.setDZ(8);
    }

    private void handle_forward_C969(GraphicObject o) {
        boolean forward = DataBlock.isSetS(2, variables.getUserInput_5BB5());
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
        o.setDeathSparkles();
        o.setIgnore3D();
        // c$BF31 CALL $B403    ; // ignore audio
        o.setWipeAndDraw();
    }

    private void upd_22_B7A3(GraphicObject o) {
        o.setBothDeadlyFlags();
        o.setPixelXYAdj(-12, -7);
    }

    private void upd_23_B7E7(GraphicObject o) {
        o.setBothDeadlyFlags();
        o.setPixelXYAdj(-16, -8);
    }

    private void upd_30_31_158_159_B9A5(GraphicObject o) {
        o.setPixelXYAdj(-12, 3);

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
        o.setPixelXYAdj(-12, -8);
        upd_player_top_CDE2(o);
    }

    private void upd_player_top_CDE2(GraphicObject o) {
        if(o.isDead() && variables.getAllObjectsInCauldron_5BC3()==0) {
            init_death_sparkles_BF21(o);
            return;
        }

        // Player top is the second object, player bottom is at block start
        o.copyCoordsFrom(o.prev());
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
        o.setPixelXYAdj(-12, -7);
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
        o.setPixelXYAdj(-16, -8);

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
        o.setPixelXYAdj(-16, -8);
        o.setDX(0);
        o.setDY(0);
        // $C4B0 CALL $B3E9    ; // ignore audio

        dec_dZ_and_update_XYZ_C700(o);
        o.setWipeAndDraw();
    }

    private void upd_63_B7A9(GraphicObject o) {
        o.setBothDeadlyFlags();
        o.setPixelXYAdj(-16, -8);

        // This flag is set in every odd room and is cleared after user picks up an object -
        // these are deliberate traps
        boolean disableSpikeBallDrop = variables.isDisableSpikeBallDrop_5BC0();
        if(disableSpikeBallDrop) return;
        if(o.isDropping()) {
            dec_dZ_and_update_XYZ_C700(o);
            if(o.isOobZ()) { //; Z OOB? (hit the floor?)
                o.setRising();
                variables.resetSpikeBallDropping_5BBF();
            }
            // $B7D6 CALL $B451     ; } // ignore audio
        } else {
            boolean isOtherDropping = variables.isSpikeBallDropping_5BBF();
            if(!isOtherDropping) {
                // ; 1-in-16 chance
                if((params.getRnd().getSeed3() & 0xF0) > 0) {
                    o.setDropping(); // ; flag this one as dropping
                    variables.setSpikeBallDropping_5BBF();
                }
            }
        }
        o.setWipeAndDraw();
    }

    private void upd_64_to_79_CDDF(GraphicObject o) {
        o.setPixelXYAdj(-12, -12);
        upd_player_top_CDE2(o);
    }

    private void upd_80_to_83_C5C8(GraphicObject o) {
        o.setPixelXYAdj(-12, -6);

        dec_dZ_and_update_XYZ_C700(o);
        if(o.notMovingXY() || o.isOobXY()) {
            // @label=loc_C5DD
            int rnd3 = (params.getRnd().getSeed3() & 0x03) + 4;
            int dx = graphicObjects.ghostDeltaTbl[rnd3];
            o.setDX(dx);
            int rnd2 = (params.getRnd().getSeed2() & 0x03) + 4;
            int dy = graphicObjects.ghostDeltaTbl[rnd2];;
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

    private void upd_84_C4C3(GraphicObject o) {
        o.setPixelXYAdj(-16, -8);
        dec_dZ_and_update_XYZ_C700(o);
        if(o.isMoving()) {
            o.setDX(0);
            o.setDY(0);
        }
    }

    private void upd_85_C4B6(GraphicObject o) {
        o.setPixelXYAdj(-16, -8);
        dec_dZ_and_update_XYZ_C700(o);
        // $C4C0 JP $C232      ;// ignore audio
    }

    private void upd_86_87_B7ED(GraphicObject o) {
        o.setPixelXYAdj(-8, -4);

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
        o.setPixelXYAdj(-12, -16);
    }

    private void upd_91_B683(GraphicObject o) {
        o.setPixelXYAdj(-16, -8);

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
        o.setPixelXYAdj(-12, -2);
        if(o.isDead()) {
            if (variables.getAllObjectsInCauldron_5BC3() == 0) {
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
            int a = variables.getTransformGrNo_5BB1() ^ 0x20;
            o.setGrNo(a);
            a += 0x10;
            graphicObjects.playerTop().setGrNo(a);
            variables.resetTransformFlag_5BB1();
            o.setPixelXYAdj(-12, -6);
            boolean spriteFlag = o.isBitSetGrNo(5);
            if (spriteFlag) o.setPixelYAdj(o.getPixelYAdj() - 1);
        } else {
            // Continue transformation
            int nextSprite = (params.getRnd().getSeed3() & 3) | 0x5C;
            if(nextSprite != o.getGrNo()) nextSprite = nextSprite ^ 1;
            o.setGrNo(nextSprite);
            o.toggleHFlip();
        }
    }

    private void upd_96_to_102_C28B(GraphicObject o) {
        o.setPixelXYAdj(-12, -4);

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
        o.setPixelXYAdj(-8, -2);

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
            o.setPixelXYAdj(-12, -4);
            int l = o.getTemp10();
            int h = o.getTemp11();
            int ptr = h * 256 + l;
            int spi = specialObjects.getIndex(ptr);
            specialObjects.clearGraphicNo(spi); // ; zap ptr object table entry
            int lives = variables.getLives_5BBA();
            variables.setLives_5BBA(lives+1);
            variables.setDisableSpikeBallDrop_5BC0();
            // $C1E2 CALL $B4A3     ; // ignore audio
            print_lives_BCA3();
            updateObjectsPanel();
        }
        dec_dZ_and_update_XYZ_C700(o);
        if(o.isMoving()) {
            o.setDX(0);
            o.setDY(0);
        }
        o.setWipeAndDraw();
    }

    private void upd_104_to_110_C1F1(GraphicObject o) {
        o.setPixelXYAdj(-12, -4);

        int x = o.getX();
        int dX = x - 0x80;
        if(dX < 0) dX = 1; else if(dX > 0) dX = -1;
        o.setDX(dX);

        int y = o.getY();
        int dY = y - 0x80;
        if(dY < 0) dY = 1; else if(dY > 0) dY = -1;
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
            variables.setObjectsPutInCauldron_5BBB(variables.getObjectsPutInCauldron_5BBB() + 1);
            cycle_colours_with_sound_C2A5();
            boolean gotAllObjects = variables.getObjectsPutInCauldron_5BBB() == params.getCauldronRequiredObjects();
            if(gotAllObjects) prepare_final_animation_C2CC();
        }
        variables.setObjectDroppingIntoCauldron_5BC4(0);
        int l = o.getTemp10();
        int h = o.getTemp11();
        int ptr = h * 256 + l;
        int spi = specialObjects.getIndex(ptr);
        specialObjects.clearGraphicNo(spi);
        updateObjectsPanel();
        upd_111_B95E(o);
    }

    private void prepare_final_animation_C2CC() {
        IO.println("prepare_final_animation_C2CC");

        variables.setAllObjectsInCauldron_5BC3(1);
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
            int hl = mainMemory.attrStart;
            for (int i = 0; i < VideoMemory.ATTR_MEM_SIZE; i++) {
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
        o.setPixelXYAdj(-12, -4);
        o.setGrNo(o.getGrNo()+1);
        // ignore audio
    }

    private void upd_119_BF3F(GraphicObject o) {
        o.setPixelXYAdj(-12, -4);
        upd_111_B95E(o); // Fix bug/feature in the source (writing to ROM)
    }

    private void upd_120_to_126_BEFE(GraphicObject o) {
        o.setPixelXYAdj(-12, -4);
        if(((params.getRnd().getSeed2() ^ 0xFF) & 0x01) == 0) o.setGrNo(o.getGrNo() + 1); // next sprite on odd frames
        o.setWipeAndDraw();
    }

    private void upd_127_BF11(GraphicObject o) {
        o.setPixelXYAdj(-12, -4); //FC
        o.resetDead();
        o.setGrNo(o.getTemp10());
        updateOneSprite(o); // update after sparkes change to player
    }

    private void upd_141_B99C(GraphicObject o) { o.setPixelXYAdj(-16, -12); }

    private void upd_142_B99F(GraphicObject o) { o.setPixelXYAdj(-24, 12); }

    private void upd_143_B6A2(GraphicObject o) {
        o.setPixelXYAdj(-16, -8);

        if(o.isTriggered()) {
            o.setGrNo(0xB8); // ; graphic_no = sparkles
            upd_112_to_118_184_BF2B(o);
        }
    }

    private void upd_144_to_149_152_to_157_B6F9(GraphicObject o) {
        o.setPixelXYAdj(-12, -6);

        if(o.notMovingXY()) return;
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
        o.setPixelXYAdj(-12, 7);

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
        o.setPixelXYAdj(-12, -4);

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
                o.setBitGrNo(2); // make the spell deadly for wulf
                o.resetIgnore3D();
            }
        }
        o.setWipeAndDraw();
    }

    private int ret_next_obj_required_C274() {
        int objsInCauldron = variables.getObjectsPutInCauldron_5BBB();
        return objects_required_C27D.start + objsInCauldron;
    }

    private void upd_164_to_167_B92C(GraphicObject o) {
        o.setPixelXYAdj(-12, -4);

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
        o.setPixelXYAdj(-12, -4);
        o.setGrNo(0xA0);
    }

    // NOTE: commented as: not used
    private void upd_176_177_B83F(GraphicObject o) {
        upd_12_to_15_C4F2(o);
        // NOTE: rest of the routine is not implemented
    }

    private void upd_178_179_B865(GraphicObject o) {
        o.setPixelXYAdj(-8, -4);

        int bouncingHeight = variables.getBallBounceHeight_5BBD();
        if(bouncingHeight == 0) variables.setBallBounceHeight_5BBD(o.getZ() + 0x20); // ; calc bounce height
        o.toggleNextPrevSprite();
        // $B879 CALL $B451     ; // ignore audio
        if(o.isDirUp()) {
            o.setDZ(3);
            dec_dZ_and_update_XYZ_C700(o);
            if(o.getZ() > variables.getBallBounceHeight_5BBD())
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
        o.setPixelXYAdj(-8, -4);

        o.setDZ(1); // ; pre-load dZ=1
        if(o.isNorth()) o.setDY(2); else o.setDY(-2);
        // $B801 CALL $B45D     ; // ignore audio
        dec_dZ_and_update_XYZ_C700(o);
        loc_B82F(o, false);
    }

    private void upd_182_183_B5FF(GraphicObject o) {
        o.setPixelXYAdj(-8, -4);

        int dx = o.getDXu();
        int dy = o.getDYu();

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
        // update special objects if not the first screen of the game
        if(!variables.isGameStart_5BB2()) update_special_objs_C591();
        shadowMemory.clearAndSetBackground();
        retrieve_screen_D3C6(graphicObjects.playerBottom().getScreen());
        find_special_objs_here_C525();
        adjust_plyr_xyz_for_room_size_D320();

        variables.resetPortcullisMoving_5BAF();
        variables.setPortcullisMoveCnt_5BB0(0);
        variables.setBallBounceHeight_5BBD(0);
        variables.resetSpikeBallDropping_5BBF();
        // NOTE: this means that in odd rooms spike balls do not drop until an object is picked up.
        // This is not straightforward but these are the traps - a player may be killed when picking up an object
        // and these traps are only in odd rooms
        variables.setDisableSpikeBallDrop_5BC0(graphicObjects.playerBottom().isOddRoom());

        locations.get(graphicObjects.playerBottom().getScreen()).setVisited();
        mapPanel.drawMap(locations, graphicObjects.playerBottom().getScreen());
    }

    private void update_special_objs_C591() {
        int iy = 0x5C48;
        for(int i=0; i<2; i++) {
            GraphicObject o = graphicObjects.i2o(iy);
            int grNo = o.getGrNo();
            if(grNo != 0) {
                int e = o.getTemp10();
                int d = o.getTemp11();
                int ptr = d*256 + e; // ptr can be 0 for cauldron bubbles
                if(ptr != 0) {
                    int spi = specialObjects.getIndex(ptr);
                    specialObjects.setGraphicNo(spi, grNo);
                    specialObjects.setCurrentX(spi, o.getX());
                    specialObjects.setCurrentY(spi, o.getY());
                    specialObjects.setCurrentZ(spi, o.getZ());
                    int screen = o.getScreen();
                    specialObjects.setScreen(spi, screen);
                }
            }
            iy += 0x20;
        }
    }

    private void retrieve_screen_D3C6(int currLocId) {
        Location loc = locations.get(currLocId);
        int roomId = loc.id;
        debugPanel2.append("Retrieved room: %d / %02x".formatted(roomId,roomId));
        int attr = loc.getAttr();
        variables.setCurrRoomAttrib_5BAD(attr); // current room attributes (color)
        variables.setRoomSizeX_5BAB(loc.getRoomSizeX());
        variables.setRoomSizeY_5BAC(loc.getRoomSizeY());
        variables.setRoomSizeZ_5BAE(loc.getRoomSizeZ());
        debugPanel2.append("Retrieved room size: "+loc.getRoomSize()+" X:"+variables.getRoomSizeX_5BAB()+" Y:"+variables.getRoomSizeY_5BAC()+" Z:"+variables.getRoomSizeZ_5BAE());

        // decode all background objects
        // hl - iterates over room background objects (until end of room data or 0xFF separator)
        int targetPos = graphicObjects.firstBackgroundObject;
        int bkObjPos = 0;
        while(bkObjPos < loc.getBackgroundObjectCnt()) {
            BackgroundObject bkgObj = backgroundObjects.get(loc.getBackgroundObjectId(bkObjPos));
            debugPanel2.append("Retrieved background object: %02x".formatted(loc.getBackgroundObjectId(bkObjPos)));
            for (int spriteNum = 0; spriteNum < bkgObj.size(); spriteNum++) {
                graphicObjects.o(targetPos).initFrom(bkgObj.s(spriteNum), currLocId);
                debugPanel2.append("Retrieved sprite: %02x".formatted(bkgObj.s(spriteNum).grNo()));
                targetPos++;
            }
            bkObjPos++;
        }

        int fgnObjPos = 0;
        while(fgnObjPos < loc.getForegroundObjectCnt()) {
            LocationForegroundObject fo = loc.getForegroundObject(fgnObjPos);
            int blockCnt = fo.cnt();
            int blockType = fo.grNo();
            ForegroundObject fgrObj = foregroundObjects.get(blockType);
            for(int i=0; i<blockCnt; i++) {
                // more than 1 template applies to guards and wizard
                for(int templatePart = 0; templatePart < fgrObj.size(); templatePart ++) {
                    graphicObjects.o(targetPos).initFrom(fgrObj.s(templatePart), currLocId, fo.x(i), fo.y(i), fo.z(i), variables.getRoomSizeZ_5BAE());
                    targetPos ++;
                }
            }
            fgnObjPos++;
        }

        for(int i = targetPos; i<graphicObjects.size; i++) graphicObjects.o(i).reset();

        debugTable2("Other objects data:", graphicObjects.start(), graphicObjects.getCopy());
    }

    private void find_special_objs_here_C525() {
        int currLocId = graphicObjects.playerBottom().getScreen();
        for(int i=graphicObjects.start() + 2*0x20; i<graphicObjects.start() + 4*0x20; i+=0x20) graphicObjects.i2o(i).reset();
        for(int spi = 0; spi < specialObjects.size;  spi++) {
            int specialObjGrNo = specialObjects.getGraphicNo(spi);
            int i = graphicObjects.specialObjStartIx;
            boolean saved = false;
            while(i < graphicObjects.specialObjEndIxExcl && !saved) {
                GraphicObject o = graphicObjects.o(i);
                int screenObjGrNo = o.getGrNo();
                if (screenObjGrNo == 0 && specialObjGrNo != 0 && currLocId == specialObjects.getScreen(spi)) {
                    o.reset();
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
        updateObjectsPanel();
    }

    private void adjust_plyr_xyz_for_room_size_D320() {
        debugTable2("Graphic objects table:", 0x5C08, graphicObjects.getCopy());
        int roomSizeX = variables.getRoomSizeX_5BAB() - 2;
        int roomSizeY = variables.getRoomSizeY_5BAC() - 2;
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

    private boolean calc_pixel_XY_D6C9(GraphicObject o) {
        // first check if object should disappear (including player)
        if(o.getGrNo() == 1) { //; flagged as ???
            o.setGrNo(0);
            return false;
        }

        int x = o.getX() + o.getY() - 0x80 + o.getPixelXAdj();
        o.setPixelX(x & 0xFF);
        int y = (o.getY() - o.getX() + 0x80) / 2 + o.getZ() - 0x68 + o.getPixelYAdj();
        o.setPixelY(y & 0xFF);
        // $D6EC CP $C0         ; bottom line of screen?
        return y < 0xC0; // 192
    }

    private void printVideoMemory() {
        debugTable2("Video (main):", 0, mainMemory.getCopy());
    }

    private void printShadowMemory() {
        debugTable2("Video (shadow):", 0, shadowMemory.getCopy());
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
