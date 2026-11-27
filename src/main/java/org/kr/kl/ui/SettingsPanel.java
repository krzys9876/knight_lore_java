package org.kr.kl.ui;

import org.kr.kl.GameParams;
import org.kr.kl.objects.Font;
import org.kr.kl.objects.SpriteData;

public class SettingsPanel extends GamePanel {
    private VideoMemoryLinear memory;
    private Font font;
    private final GameParams params;
    private static final int maxLivesY = 21;
    private static final int livesY = 19;
    private static final int maxDaysY = 17;
    private static final int toCollectY = 15;
    private static final int loseLifeY = 13;
    private static final int stopDaysY = 11;
    private static final int disableDeathY = 9;
    private static final int skipMenuY = 7;
    private static final int gameSpeedY = 5;
    private static final int backToMenuY = 1;
    private static final int plusOneX = 25;
    private static final int plusTenX = 28;
    private static final int minusOneX = 22;
    private static final int minusTenX = 18;
    private static final int plusMinusOneSize = 2;
    private static final int plusMinusTenSize = 3;
    private static final int yesX = 23;
    private static final int noX = 26;
    private static final int yesNoSize = 1;
    private static final int backX = 7;
    private static final int backSize = 4;

    public SettingsPanel(GameParams params) {
        super();
        this.params = params;
    }

    public void initVideoMemory(SpriteData spriteData) {
        memory = new VideoMemoryLinear(spriteData);
    }

    public void setFont(Font font) { this.font = font; }

    private void printText(String text, int x, int y) {
        memory.printText(text, x, y, font);
    }

    private void printText(String text, int x, int y, int attr) {
        memory.printText(text, x, y, font, attr);
    }

    public void update() {
        memory.clear();
        for(int i=memory.attrStart; i<memory.endExcl; i++) memory.setByteAt(i, Color.getAttribute(Color.WHITE, Color.BLACK, true, false));

        int valueAttr = Color.getAttribute(Color.YELLOW, Color.BLACK, true, false);
        int yesAttr = Color.getAttribute(Color.GREEN, Color.BLACK, true, false);
        int noAttr = Color.getAttribute(Color.RED, Color.BLACK, true, false);
        int backAttr = Color.getAttribute(Color.BLUE, Color.BLACK, true, false);

        printText("MAX. LIVES",1, maxLivesY);
        printText(String.format("%d",params.getInitialLives()), params.getInitialLives() < 10 ? 15 : 14, maxLivesY, valueAttr);
        printText("-10 -1 +1 +10",minusTenX, maxLivesY);

        printText("LIVES", 1, livesY);
        printText(String.format("%d",params.getLives()), params.getLives() < 10 ? 15 : 14, livesY, valueAttr);
        printText("-10 -1 +1 +10",minusTenX, livesY);

        printText("MAX. DAYS", 1, maxDaysY);
        printText(String.format("%d",params.getMaxDays()), params.getMaxDays() < 10 ? 15 : 14, maxDaysY, valueAttr);
        printText("-10 -1 +1 +10",minusTenX, maxDaysY);

        printText("TO COLLECT", 1, toCollectY);
        printText(String.format("%d",params.getCauldronRequiredObjects()), params.getCauldronRequiredObjects() < 10 ? 15 : 14, toCollectY, valueAttr);
        printText("-1 +1",minusOneX, toCollectY);

        printText("LOSE LIFE", 1, loseLifeY);
        printText(params.isDisableLoseLife() ? "NO" : "YES", 14, loseLifeY, params.isDisableLoseLife() ? noAttr : yesAttr);
        printText("Y  N",yesX, loseLifeY);

        printText("STOP DAYS", 1, stopDaysY);
        printText(params.isDisableIncDays() ? "YES" : "NO", 14, stopDaysY, params.isDisableIncDays() ? yesAttr : noAttr);
        printText("Y  N",yesX, stopDaysY);

        printText("NEVER DIE", 1, disableDeathY);
        printText(params.isDisableDeath() ? "YES" : "NO", 14, disableDeathY, params.isDisableDeath() ? yesAttr : noAttr);
        printText("Y  N",yesX, disableDeathY);

        printText("SKIP MENU", 1, skipMenuY);
        printText(params.isSkipMenu() ? "YES" : "NO", 14, skipMenuY, params.isSkipMenu() ? yesAttr : noAttr);
        printText("Y  N",yesX, skipMenuY);

        printText("GAME SPEED", 1, gameSpeedY);
        printText(String.format("%d",params.getDelayMs()), params.getDelayMs() < 10 ? 15 : 14, gameSpeedY, valueAttr);
        printText("-10 -1 +1 +10",minusTenX, gameSpeedY);

        printText("PRESS      TO END THE GAME", 1, backToMenuY);
        printText("HERE", backX, backToMenuY, backAttr);

        setPixelData(memory.toPixels(false));
    }

    public void doClick(int x, int y) {
        int row = (191-y)/8;
        int col = x/8;

        switch (checkPlusMinus(maxLivesY, row, col)) {
            case 1: params.setInitialLives(params.getInitialLives() - 10); break;
            case 2: params.setInitialLives(params.getInitialLives() - 1); break;
            case 3: params.setInitialLives(params.getInitialLives() + 1); break;
            case 4: params.setInitialLives(params.getInitialLives() + 10); break;
            default: break;
        }
        switch (checkPlusMinus(livesY, row, col)) {
            case 1: params.setLives(params.getLives() - 10); break;
            case 2: params.setLives(params.getLives() - 1); break;
            case 3: params.setLives(params.getLives() + 1); break;
            case 4: params.setLives(params.getLives() + 10); break;
            default: break;
        }
        switch (checkPlusMinus(maxDaysY, row, col)) {
            case 1: params.setMaxDays(params.getMaxDays() - 10); break;
            case 2: params.setMaxDays(params.getMaxDays() - 1); break;
            case 3: params.setMaxDays(params.getMaxDays() + 1); break;
            case 4: params.setMaxDays(params.getMaxDays() + 10); break;
            default: break;
        }
        switch (checkPlusMinus(toCollectY, row, col)) {
            case 2: params.setCauldronRequiredObjects(params.getCauldronRequiredObjects() - 1); break;
            case 3: params.setCauldronRequiredObjects(params.getCauldronRequiredObjects() + 1); break;
            default: break;
        }
        switch (checkYesNo(loseLifeY, row, col)) {
            case 1: params.enableLoseLife(); break;
            case 2: params.disableLoseLife(); break;
            default: break;
        }
        switch (checkYesNo(stopDaysY, row, col)) {
            case 1: params.disableIncDays(); break;
            case 2: params.enableIncDays(); break;
            default: break;
        }
        switch (checkYesNo(disableDeathY, row, col)) {
            case 1: params.disableDeath(); break;
            case 2: params.enableDeath(); break;
            default: break;
        }
        switch (checkYesNo(skipMenuY, row, col)) {
            case 1: params.enableSkipMenu(); break;
            case 2: params.disableSkipMenu(); break;
            default: break;
        }
        switch (checkPlusMinus(gameSpeedY, row, col)) {
            case 1: params.setDelayMs(params.getDelayMs() - 10L); break;
            case 2: params.setDelayMs(params.getDelayMs() - 1L); break;
            case 3: params.setDelayMs(params.getDelayMs() + 1L); break;
            case 4: params.setDelayMs(params.getDelayMs() + 10L); break;
            default: break;
        }
        switch (checkBackToMenu(row, col)) {
            case 1: params.setInactive(); break;
            default: break;
        }


        update();
    }

    private int checkPlusMinus(int checkY, int row, int col) {
        if(row!=checkY) return 0;
        if(col>=minusTenX && col<minusTenX+plusMinusTenSize) return 1;
        if(col>=minusOneX && col<minusOneX+plusMinusOneSize) return 2;
        if(col>=plusOneX && col<plusOneX+plusMinusOneSize) return 3;
        if(col>=plusTenX && col<plusTenX+plusMinusTenSize) return 4;
        return 0;
    }

    private int checkYesNo(int checkY, int row, int col) {
        if(row!=checkY) return 0;
        if(col>=yesX && col<yesX+yesNoSize) return 1;
        if(col>=noX && col<noX+yesNoSize) return 2;
        return 0;
    }

    private int checkBackToMenu(int row, int col) {
        if(row!=backToMenuY) return 0;
        if(col>=backX && col<backX+backSize) return 1;
        return 0;
    }
}
