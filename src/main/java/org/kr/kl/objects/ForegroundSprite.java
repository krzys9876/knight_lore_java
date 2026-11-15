package org.kr.kl.objects;

/*
; Block type data
;
; #TABLE(default,centre,:w)
; { =h Byte(n) | =h Description }
; { +0 | sprite }
; { +1 | width (x) }
; { +2 | depth (y) }
; { +3 | height (z) }
; { +4 | flags (see code) }
; { +5 | offsets (see code) }
 */
public record ForegroundSprite(int grNo, int w, int d, int h, int flags, int offsets) {}
