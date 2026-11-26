package org.kr.kl.ui;

import org.kr.kl.data.DataBlock;
import org.kr.kl.objects.GraphicObject;
import org.kr.kl.objects.SpecialObjects;
import org.kr.kl.objects.SpriteData;
import org.kr.kl.utils.MathUtils;

import java.util.HashMap;
import java.util.HashSet;

public class ObjectsPanel extends GamePanel {
    private VideoMemoryLinear memory;
    private DataBlock font;
    private final int[] collectedText = new int[] {12, 24, 21, 21, 14, 12, 29, 14, 13};
    private final int[] locationsText = new int[] {21, 24, 12, 10, 29, 18, 24, 23, 28};

    public void setFont(DataBlock font) { this.font = font; }

    public void initVideoMemory(SpriteData spriteData) {
        memory = new VideoMemoryLinear(spriteData);
    }

    public void update(DataBlock requiredObjects, int requiredPos, int required, SpecialObjects specialObjects,
                       int loc1GrNo, int loc2GrNo, int currentLocation) {
        memory.clear();
        for(int i=memory.attrStart; i<memory.endExcl; i++) memory.setByteAt(i, Color.getAttribute(Color.WHITE, Color.BLACK, true, false));

        int posY = (23*8) * 0x20;
        for(int i=0; i<collectedText.length; i++) memory.printCharacter(collectedText[i], i + posY + 1, font);

        int requiredPosBCD = MathUtils.byteToBCD(requiredPos);
        int requiredBCD = MathUtils.byteToBCD(required);
        int posL = requiredPosBCD & 0x0F;
        int posH = (requiredPosBCD & 0xF0) >> 4;
        int reqL = requiredBCD & 0x0F;
        int reqH = (requiredBCD & 0xF0) >> 4;
        int posX = collectedText.length + 2;
        if(posH > 0) memory.printCharacter(posH, posX + posY, font);
        memory.printCharacter(posL, posX+1 + posY, font);
        memory.printCharacter(24, posX+3 + posY, font); // O
        memory.printCharacter(15, posX+4 + posY, font); // F
        if(reqH > 0) memory.printCharacter(reqH, posX+6 + posY, font);
        memory.printCharacter(reqL, posX+7 + posY, font);

        int objY = 19*8;
        int objX = 0;
        for(int i=requiredPos; i < required; i++) {
            int grNo = requiredObjects.getU(requiredObjects.start + i) | 0x60;
            printOneObject(grNo, objX, objY, specialObjects.getAttr(grNo));
            if(objX + 2 * 24 > BASE_WIDTH) {
                objX = 0;
                objY -= 24;
            } else objX += 24;
        }

        int locPosY = (14*8) * 0x20;
        for(int i=0; i<locationsText.length; i++) memory.printCharacter(locationsText[i], i + locPosY + 1, font);

        HashMap<Integer, HashSet<Integer>> specObjMap = new HashMap<>();

        for(int i = 0; i < specialObjects.size; i++) addLocation(specObjMap, specialObjects.getGraphicNo(i), specialObjects.getScreen(i));
        // NOTE: the game does not update special objects table when object is dropped - only after leaving the room
        // it is written back to the table. So must also consider current special objects slots
        addLocation(specObjMap, loc1GrNo, currentLocation);
        addLocation(specObjMap, loc2GrNo, currentLocation);
        int locObjX = 0;
        int locObjY = 10*8;
        for(int i=0x60; i<=0x67; i++) {
            printOneObject(i, locObjX, locObjY, specialObjects.getAttr(i));
            HashSet<Integer> l = specObjMap.getOrDefault(i, new HashSet<>());
            int locPos = 0;
            for(Integer e: l) {
                int locL = e & 0x0F;
                int locH = (e & 0xF0) >> 4;
                int baseAddr = 4 + locObjX / 8 + (locObjY + 8) * 0x20 + locPos;
                memory.printCharacter(locH, baseAddr, font);
                memory.printCharacter(locL, baseAddr + 1, font);
                locPos+=3;
            }
            if(i == 0x063) {
                locObjX = 128;
                locObjY = 10*8;
            } else locObjY -= 24;
        }

        setPixelData(memory.toPixels(false));
    }

    private void addLocation(HashMap<Integer, HashSet<Integer>> map, int grNo, int location) {
        if(grNo >= 0x60 && grNo <= 0x67) {
            HashSet<Integer> locations = map.getOrDefault(grNo, new HashSet<>());
            locations.add(location);
            map.put(grNo, locations);
        }
    }

    private void printOneObject(int grNo, int x, int y, int attr) {
        GraphicObject o = new GraphicObject();
        o.setGrNo(grNo);
        o.setPixelX(x);
        o.setPixelY(y);
        memory.printSprite(o);
        int addr = VideoMemory.calcAttribAddr(y, x);
        for(int i=0; i<3; i++)
            for(int j=0; j<3; j++)
                memory.setByteAt(addr + j - i * 0x20, attr);
    }

    public void doClick(int x, int y) {
        IO.println(String.format("(%d,%d)", x, y));
    }
}
