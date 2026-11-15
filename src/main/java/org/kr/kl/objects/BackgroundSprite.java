package org.kr.kl.objects;

/*
; Background Type Makeup
;
; #TABLE(default,centre,:w)
; { =h Byte(n) | =h Description }
; { +0 | sprite }
; { +1 | x }
; { +2 | y }
; { +3 | z }
; { +4 | width (x) }
; { +5 | depth (y) }
; { +6 | height (z) }
; { +7 | flags (see code) }
 */
public record BackgroundSprite(int grNo, int x, int y, int z, int w, int d, int h, int flags) {}
