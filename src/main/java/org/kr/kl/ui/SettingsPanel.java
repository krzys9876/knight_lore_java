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

    public SettingsPanel(GameParams params) {
        super();
        this.params = params;
    }

    public void initVideoMemory(SpriteData spriteData) {
        memory = new VideoMemoryLinear(spriteData);
    }

    public void setFont(Font font) { this.font = font; }

    private int charPosToAddr(int x, int y) { return x + y * 8 * 0x20; }

    private void printText(String text, int x, int y) {
        memory.printText(text, charPosToAddr(x, y), font);
    }

    public void update() {
        memory.clear();
        for(int i=memory.attrStart; i<memory.endExcl; i++) memory.setByteAt(i, Color.getAttribute(Color.WHITE, Color.BLACK, true, false));

        printText("MAX. LIVES",1, maxLivesY);
        printText(String.format("%d",params.getInitialLives()), params.getInitialLives() < 10 ? 15 : 14, maxLivesY);
        printText("-10 -1 +1 +10",18, maxLivesY);
        printText("LIVES", 1, livesY);
        //printText(String.format("%d",params.getInitialLives()), params.getInitialLives() < 10 ? 15 : 14, livesY);
        printText("-10 -1 +1 +10",18, livesY);
        printText("MAX. DAYS", 1, maxDaysY);
        printText(String.format("%d",params.getMaxDays()), params.getMaxDays() < 10 ? 15 : 14, maxDaysY);
        printText("-10 -1 +1 +10",18, maxDaysY);
        printText("TO COLLECT", 1, toCollectY);
        printText(String.format("%d",params.getCauldronRequiredObjects()), params.getCauldronRequiredObjects() < 10 ? 15 : 14, toCollectY);
        printText("-1 +1",22, toCollectY);
        printText("LOSE LIFE", 1, loseLifeY);
        printText(params.isDisableLoseLife() ? "NO" : "YES", 14, loseLifeY);
        printText("Y  N",23, loseLifeY);
        printText("STOP DAYS", 1, stopDaysY);
        printText(params.isDisableLoseLife() ? "YES" : "NO", 14, stopDaysY);
        printText("Y  N",23, stopDaysY);
        printText("NEVER DIE", 1, disableDeathY);
        printText(params.isDisableDeath() ? "YES" : "NO", 14, disableDeathY);
        printText("Y  N",23, disableDeathY);
        printText("SKIP MENU", 1, skipMenuY);
        printText(params.isSkipMenu() ? "YES" : "NO", 14, skipMenuY);
        printText("Y  N",23, skipMenuY);
        printText("GAME SPEED", 1, gameSpeedY);
        printText(String.format("%d",params.getDelayMs()), params.getDelayMs() < 10 ? 15 : 14, gameSpeedY);
        printText("-10 -1 +1 +10",18, gameSpeedY);
        printText("PRESS HERE TO END THE GAME", 1, backToMenuY);

        setPixelData(memory.toPixels(false));
    }

    public void doClick(int x, int y) {
        int row = (191-y)/8;
        int col = x/8;

        IO.println(String.format("(%d,%d) row=%d col=%d", x, y, row, col));
    }
}
