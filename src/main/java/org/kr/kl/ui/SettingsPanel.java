package org.kr.kl.ui;

import org.kr.kl.GameParams;
import org.kr.kl.objects.Font;
import org.kr.kl.objects.SpriteData;

public class SettingsPanel extends GamePanel {
    private VideoMemoryLinear memory;
    private Font font;
    private final GameParams params;
    private static final String maxLivesText = "MAX. LIVES";
    private static final String livesText = "LIVES";
    private static final String maxDaysText = "MAX. DAYS";
    private static final String toCollectText = "TO COLLECT";
    private static final String loseLiveText = "LOSE LIFE";
    private static final String stopDaysText = "STOP DAYS";
    private static final String disableDeathText = "DISABLE DEATH";
    private static final String skipMenuText = "SKIP MENU";
    private static final String gameSpeedText = "GAME SPEED";

    public SettingsPanel(GameParams params) {
        super();
        this.params = params;
    }

    public void initVideoMemory(SpriteData spriteData) {
        memory = new VideoMemoryLinear(spriteData);
    }

    public void setFont(Font font) { this.font = font; }

    public void update() {
        memory.clear();
        for(int i=memory.attrStart; i<memory.endExcl; i++) memory.setByteAt(i, Color.getAttribute(Color.WHITE, Color.BLACK, true, false));

        int posY = (21*8) * 0x20;
        memory.printText(maxLivesText, posY + 1, font);
        posY -= (2 * 8) * 0x20;
        memory.printText(livesText, posY + 1, font);
        posY -= (2 * 8) * 0x20;
        memory.printText(maxDaysText, posY + 1, font);
        posY -= (2 * 8) * 0x20;
        memory.printText(toCollectText, posY + 1, font);
        posY -= (2 * 8) * 0x20;
        memory.printText(loseLiveText, posY + 1, font);
        posY -= (2 * 8) * 0x20;
        memory.printText(stopDaysText, posY + 1, font);
        posY -= (2 * 8) * 0x20;
        memory.printText(disableDeathText, posY + 1, font);
        posY -= (2 * 8) * 0x20;
        memory.printText(skipMenuText, posY + 1, font);
        posY -= (2 * 8) * 0x20;
        memory.printText(gameSpeedText, posY + 1, font);

        setPixelData(memory.toPixels(false));
    }

    public void doClick(int x, int y) {
        int row = (191-y)/8;
        int col = x/8;

        IO.println(String.format("(%d,%d) row=%d col=%d", x, y, row, col));
    }
}
