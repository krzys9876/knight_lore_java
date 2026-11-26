package org.kr.kl.ui;

import org.kr.kl.GameParams;
import org.kr.kl.objects.Font;
import org.kr.kl.objects.SpriteData;

public class SettingsPanel extends GamePanel {
    private VideoMemoryLinear memory;
    private Font font;
    private final GameParams params;
    private static final String maxLivesText = "MAX. LIVES";
    private static final int maxLivesY = 21;
    private static final String livesText = "LIVES";
    private static final int livesY = 19;
    private static final String maxDaysText = "MAX. DAYS";
    private static final int maxDaysY = 17;
    private static final String toCollectText = "TO COLLECT";
    private static final int toCollectY = 15;
    private static final String loseLifeText = "LOSE LIFE";
    private static final int loseLifeY = 13;
    private static final String stopDaysText = "STOP DAYS";
    private static final int stopDaysY = 11;
    private static final String disableDeathText = "DISABLE DEATH";
    private static final int disableDeathY = 9;
    private static final String skipMenuText = "SKIP MENU";
    private static final int skipMenuY = 7;
    private static final String gameSpeedText = "GAME SPEED";
    private static final int gameSpeedY = 5;
    private static final String backToMenuText = "PRESS TO GO BACK TO MENU";
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

        printText(maxLivesText,1, maxLivesY);
        printText(livesText, 1, livesY);
        printText(maxDaysText, 1, maxDaysY);
        printText(toCollectText, 1, toCollectY);
        printText(loseLifeText, 1, loseLifeY);
        printText(stopDaysText, 1, stopDaysY);
        printText(disableDeathText, 1, disableDeathY);
        printText(skipMenuText, 1, skipMenuY);
        printText(gameSpeedText, 1, gameSpeedY);
        printText(backToMenuText, 3, backToMenuY);

        setPixelData(memory.toPixels(false));
    }

    public void doClick(int x, int y) {
        int row = (191-y)/8;
        int col = x/8;

        IO.println(String.format("(%d,%d) row=%d col=%d", x, y, row, col));
    }
}
