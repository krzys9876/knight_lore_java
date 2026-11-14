package org.kr.kl.objects;

import org.kr.DataBlock;

public class Variables {
    private final DataBlock data = new DataBlock(0x5BA0, 0x5BE8 - 0x5BA0 + 1);

    private boolean exitingScreen = false;
    private boolean gameOver = false;
    private boolean shouldQuit = false;

    public void setUserInputMethod(int value) { data.set(0x5BA4, value); }
    public void setSuppressBorder_5BB8(int value) { data.set(0x5BB8, value); }
    public void setOldInputMethod_5BA6(int value) { data.set(0x5BA6, value); }
    public void setOldInputMethod2_5BA7(int value) { data.set(0x5BA7, value); }
    public void setTransformFlag_5BB1(int value) { data.set(0x5BB1, value); }
    public void setAllObjectsInCauldron_5BC3(int value) { data.set(0x5BC3, value); }
    public void setDays_5BB9(int value) { data.set(0x5BB9, value); }
    public void setUserInput_5BB5(int value) { data.set(0x5BB5, value); }
    public void setObjectDroppingIntoCauldron_5BC4(int value) { data.set(0x5BC4, value); }
    public void setObjectsPutInCauldron_5BBB(int value) { data.set(0x5BBB, value); }
    public void setNumScreensVisited_5BC6(int value) { data.set(0x5BC6, value); }
    public void setPickupPressed_5BB3() { data.set(0x5BB3, 1); }
    public void resetPickupPressed_5BB3() { data.set(0x5BB3, 0); }
    public void set_5BB4(int value) { data.set(0x5BB4, value); }
    public void set_5BB2(int value) { data.set(0x5BB2, value); }
    public void set_5BBA(int value) { data.set(0x5BBA, value); }
    public void set_5BB6(int value) { data.set(0x5BB6, value); }
    public void set_5BB7(int value) { data.set(0x5BB7, value); }
    public void set_5BBE(int value) { data.set(0x5BBE, value); }
    public void set_5BB0(int value) { data.set(0x5BB0, value); }
    public void set_5BAF(int value) { data.set(0x5BAF, value); }
    public void set_5BC0(int value) { data.set(0x5BC0, value); }
    public void set_5BBF(int value) { data.set(0x5BBF, value); }
    public void set_5BBD(int value) { data.set(0x5BBD, value); }
    public void set_5BAD(int value) { data.set(0x5BAD, value); }
    public void set_5BAB(int value) { data.set(0x5BAB, value); }
    public void set_5BAC(int value) { data.set(0x5BAC, value); }
    public void set_5BAE(int value) { data.set(0x5BAE, value); }

    public void setBit_5BB2(int bit) { data.setBit(bit, 0x5BB2); }
    public boolean isSet_5BB5(int bit) { return data.isSet(bit, 0x5BB5); }

    public int getUserInputMethod_5BA4() { return data.getU(0x5BA4); }
    public int getSuppressBorder_5BB8() { return data.getU(0x5BB8); }
    public int getOldInputMethod_5BA6() { return data.getU(0x5BA6); }
    public int getOldInputMethod2_5BA7() { return data.getU(0x5BA7); }
    public int getTransformFlag_5BB1() { return data.getU(0x5BB1); }
    public int getAllObjectsInCauldron_5BC3() { return data.getU(0x5BC3); }
    public int getDays_5BB9() { return data.getU(0x5BB9); }
    public int getUserInput_5BB5() { return data.getU(0x5BB5); }
    public int getObjectDroppingIntoCauldron_5BC4() { return data.getU(0x5BC4); }
    public int getObjectsPutInCauldron_5BBB() { return data.getU(0x5BBB); }
    public int getNumScreensVisited_5BC6() { return data.getU(0x5BC6); }
    public boolean isPickupPressed_5BB3() { return data.getU(0x5BB3) == 1; }
    public int get_5BB4() { return data.getU(0x5BB4); }
    public int get_5BB2() { return data.getU(0x5BB2); }
    public int get_5BBA() { return data.getU(0x5BBA); }
    public int get_5BB6() { return data.getU(0x5BB6); }
    public int get_5BB7() { return data.getU(0x5BB7); }
    public int get_5BBE() { return data.getU(0x5BBE); }
    public int get_5BB0() { return data.getU(0x5BB0); }
    public int get_5BAF() { return data.getU(0x5BAF); }
    public int get_5BC0() { return data.getU(0x5BC0); }
    public int get_5BBF() { return data.getU(0x5BBF); }
    public int get_5BBD() { return data.getU(0x5BBD); }
    public int get_5BAD() { return data.getU(0x5BAD); }
    public int get_5BAB() { return data.getU(0x5BAB); }
    public int get_5BAC() { return data.getU(0x5BAC); }
    public int get_5BAE() { return data.getU(0x5BAE); }


    public void init() {
        data.reset();
        setSuppressBorder_5BB8(1);
    }

    public boolean isExitingScreen() { return exitingScreen; }
    public void resetExitingScreen() { this.exitingScreen = false; }
    public void setExitingScreen() { this.exitingScreen = true; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver() { this.gameOver = true; }
    public void resetGameOver() { this.gameOver = false; }

    public boolean shouldQuit() { return shouldQuit; }
    public void setShouldQuit() { this.shouldQuit = true; }
    public void resetShouldQuit() { this.shouldQuit = true; }
}
