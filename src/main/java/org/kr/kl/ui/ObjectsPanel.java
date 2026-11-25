package org.kr.kl.ui;

import org.kr.kl.data.DataBlock;
import org.kr.kl.objects.GraphicObject;
import org.kr.kl.objects.SpriteData;
import org.kr.kl.utils.MathUtils;

public class ObjectsPanel extends GamePanel {
    private VideoMemoryLinear memory;
    private DataBlock font;
    private final int[] collectedText = new int[] {12, 24, 21, 21, 14, 12, 29, 14, 13};

    public void setFont(DataBlock font) { this.font = font; }

    public void initVideoMemory(SpriteData spriteData) {
        memory = new VideoMemoryLinear(spriteData);
    }

    public void update(DataBlock requiredObjects, int requiredPos, int required) {
        memory.clear();
        for(int i=memory.attrStart; i<memory.endExcl; i++) memory.setByteAt(i, Color.getAttribute(Color.WHITE, Color.BLACK, true, false));

        int posY = (23*8) * 0x20;
        for(int i=0; i<collectedText.length; i++) memory.printCharacter(collectedText[i], i + posY, font);

        requiredPos = MathUtils.byteToBCD(requiredPos);
        int requiredBCD = MathUtils.byteToBCD(required);
        int posL = requiredPos & 0x0F;
        int posH = (requiredPos & 0xF0) >> 4;
        int reqL = requiredBCD & 0x0F;
        int reqH = (requiredBCD & 0xF0) >> 4;
        int posX = collectedText.length + 1;
        if(posH > 0) memory.printCharacter(posH, posX + posY, font);
        memory.printCharacter(posL, posX+1 + posY, font);
        memory.printCharacter(24, posX+3 + posY, font); // O
        memory.printCharacter(15, posX+4 + posY, font); // F
        if(reqH > 0) memory.printCharacter(reqH, posX+6 + posY, font);
        memory.printCharacter(reqL, posX+7 + posY, font);

        int objY = 19*8;
        int objX = 0;
        for(int i=requiredPos; i < required; i++) {
            GraphicObject o = new GraphicObject();
            o.setGrNo(requiredObjects.getU(requiredObjects.start + i) | 0x60);
            o.setPixelX(objX);
            o.setPixelY(objY);
            memory.printSprite(o);
            if(objX + 2 * o.getSpriteWidthB() * 8 > BASE_WIDTH) {
                objX = 0;
                objY -= 24;
            } else objX += o.getSpriteWidthB()*8;
        }
        setPixelData(memory.toPixels(false));
    }
}
