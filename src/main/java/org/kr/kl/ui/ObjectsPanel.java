package org.kr.kl.ui;

import org.kr.kl.data.DataBlock;
import org.kr.kl.utils.MathUtils;

public class ObjectsPanel extends GamePanel {
    private final VideoMemoryLinear memory = new VideoMemoryLinear();
    private DataBlock font;
    private final int[] requiredText = new int[] {27, 14, 26, 30, 18, 27, 14, 13};

    public void setFont(DataBlock font) { this.font = font; }

    public void update(DataBlock requiredObjects, int requiredPos, int required) {
        for(int i=memory.attrStart; i<memory.endExcl; i++) memory.setByteAt(i, Color.getAttribute(Color.WHITE, Color.BLACK, true, false));

        int posY = (23*8) * 0x20;
        for(int i=0; i<requiredText.length; i++) memory.printCharacter(requiredText[i], i + posY, font);

        requiredPos = MathUtils.byteToBCD(requiredPos);
        required = MathUtils.byteToBCD(required);
        int posL = requiredPos & 0x0F;
        int posH = (requiredPos & 0xF0) >> 4;
        int reqL = required & 0x0F;
        int reqH = (required & 0xF0) >> 4;
        int posX = requiredText.length + 1;
        if(posH > 0) memory.printCharacter(posH, posX + posY, font);
        memory.printCharacter(posL, posX+1 + posY, font);
        memory.printCharacter(24, posX+3 + posY, font); // O
        memory.printCharacter(15, posX+4 + posY, font); // F
        if(reqH > 0) memory.printCharacter(reqH, posX+6 + posY, font);
        memory.printCharacter(reqL, posX+7 + posY, font);
        setPixelData(memory.toPixels(false));
    }
}
