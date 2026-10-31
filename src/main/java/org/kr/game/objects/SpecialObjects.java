package org.kr.game.objects;

import org.kr.DataBlock;
import org.kr.InitialData;

public class SpecialObjects {
    private final DataBlock special_objs_tbl_6FF2 = InitialData.block("special_objs_tbl_6FF2");
    public int getU(int address) { return special_objs_tbl_6FF2.getU(address); }
    public void set(int address, int value) { special_objs_tbl_6FF2.set(address, value); }
    public int start() { return special_objs_tbl_6FF2.start; }
    public int endExcl() { return special_objs_tbl_6FF2.endExcl(); }
    public int[] getCopy() { return special_objs_tbl_6FF2.getCopy(); }
}
