package org.kr.kl.ui;

import org.kr.kl.GameParams;
import org.kr.kl.objects.SpriteData;

public class SettingsPanel extends GamePanel {
    private VideoMemoryLinear memory;
    private final GameParams params;

    public SettingsPanel(GameParams params) {
        super();
        this.params = params;
    }

    public void initVideoMemory(SpriteData spriteData) {
        memory = new VideoMemoryLinear(spriteData);
    }

    public void update() {
        memory.clear();
        for(int i=memory.attrStart; i<memory.endExcl; i++) memory.setByteAt(i, Color.getAttribute(Color.WHITE, Color.BLACK, true, false));

        IO.println(String.format("params: lives=%d days=%d", params.getInitialLives(), params.getMaxDays()));

        setPixelData(memory.toPixels(false));
    }

    public void doClick(int x, int y) {
        int row = (191-y)/8;
        int col = x/8;

        IO.println(String.format("(%d,%d) row=%d col=%d", x, y, row, col));
    }
}
