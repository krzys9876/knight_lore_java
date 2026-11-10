package org.kr.kl.objects;

/*; #TABLE(default,centre,:w)
        ; { =h Byte(n) | =h Description }
        ; { +0     | graphic_no. }
        ; { +1     | x (center) }
        ; { +2     | y (center) }
        ; { +3     | z (bottom) }
        ; { +4     | width (X radius) }
        ; { +5     | depth (Y radius) }
        ; { +6     | height }
        ; { +7     | flags }
        ; {        | - 7=vflip sprite }
        ; {        | - 6=hflip sprite }
        ; {        | - 5=wipe }
        ; {        | - 4=draw }
        ; {        | - 3=auto-adjust near arches (player only) }
        ; {        | - 2=moveable }
        ; {        | - 1=ignore in 3D calculations }
        ; {        | - 0=is near arch (player only) }
        ; { +8     | screen }
        ; { +9     | dX }
        ; { +10    | dY }
        ; { +11    | dZ }
        ; { +12    | counter and flags }
        ; {        | - 7-4=counter when entering screen }
        ; {        | - 3=jumping }
        ; {        | - 2=Z out-of-bounds }
        ; {        | - 1=Y out-of-bounds }
        ; {        | - 0=X out-of-bounds }
        ; { +13    | per-object info/flags }
        ; {        | - direction and counters for looking, turning }
        ; {        | - 7=deadly if object hits player }
        ; {        | - 6=dead }
        ; {        | - 5=deadly if player hits object }
        ; {        | - 4=(not used) }
        ; {        | - 3=triggered (dropping, collapsing blocks) }
        ; {        | - 2=up (bouncing ball), dropping (spiked ball) }
        ; {        | - 1=north (NS fire) }
        ; {        | - 0=east (WE fire, EW guard), just dropped (spec objs) }
        ; { +14    | d_x_adj }
        ; { +15    | d_y_adj }
        ; { +16-17 | ptr object table entry or tmp player graphic_no }
        ; { +18    | pixel X adjustment }
        ; { +19    | pixel Y adjustment }
        ; { +20-23 | unused }
        ; { +24    | sprite data width (bytes) }
        ; { +25    | sprite data height (lines) }
        ; { +26    | pixel X }
        ; { +27    | pixel Y }
        ; { +28    | old sprite data width (bytes) }
        ; { +29    | old sprite data height (lines) }
        ; { +30    | old pixel X }
        ; { +31    | old pixel Y }
*/

import org.kr.DataBlock;
import org.kr.InitialData;

public class GraphicObjects {
    public final DataBlock graphic_objs_tbl_5C08;

    public int start() {
        return graphic_objs_tbl_5C08.start;
    }

    public int[] getCopy() {
        return graphic_objs_tbl_5C08.getCopy();
    }

    public final int size;

    public GraphicObjects() {
        graphic_objs_tbl_5C08 = new DataBlock(0x5C08, 0x40 + 0x40 + 0x20 + 0x0460);
        size = graphic_objs_tbl_5C08.size / 32;
    }
}
