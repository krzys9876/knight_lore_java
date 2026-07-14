package org.kr;

import java.time.LocalDateTime;

public interface ToScreenPixels {
    public int[] toPixels(LocalDateTime time);
    public int[] toPixels(boolean flash);
}
