package org.kr;

public class Color {
    public static final int BLACK = 0;
    public static final int BLUE = 1;
    public static final int RED = 2;
    public static final int MAGENTA = 3;
    public static final int GREEN = 4;
    public static final int CYAN = 5;
    public static final int YELLOW = 6;
    public static final int WHITE = 7;

    public static final int BRIGHT = 0b01000000;
    public static final int FLASH = 0b10000000;
    public static final int NONE = 0;

    public static final int[] SCREEN_COLOR = new int[] {
            // dark colors
            0x00000000,0x000000AA,0x00AA0000,0x00AA00AA,0x0000AA00,0x0000AAAA,0x00BA8E23,0x00D3D3D3,
            // bright colors
            0x00898989,0x000000FF,0x00FF0000,0x00FF00FF,0x0000FF00,0x0000FFFF,0x00FFFFC5,0x00FFFFFF
    };

    public static int getAttribute(int pen, int paper, boolean bright, boolean flash) {
        return getAttribute(pen, paper, bright ? BRIGHT : 0, flash ? FLASH : 0);
    }

    public static int getAttribute(int pen, int paper, int bright, int flash) {
        return ((pen & 0x07) + ((paper & 0x07) << 3) + bright + flash);
    }

    public static int getScreenColor(int color, boolean bright) {
        return SCREEN_COLOR[(color & 0x07) + (bright ? 8 : 0)];
    }
}
