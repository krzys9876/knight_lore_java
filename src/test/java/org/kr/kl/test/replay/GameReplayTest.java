package org.kr.kl.test.replay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kr.kl.Game;
import org.kr.kl.GameParams;
import org.kr.kl.ui.DebugPanel;
import org.kr.kl.ui.GamePanel;
import org.kr.kl.utils.KeySequence;
import org.kr.kl.utils.KeySequenceMode;
import org.kr.kl.utils.SeedGeneratorSequential;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameReplayTest {
    Thread gameThread = null;
    String referencePath = "saves/20261105_092712";

    @Test
    @DisplayName("Replay complete gameplay")
    public void testGameReplay() {

        GameParams params = new GameParams(new SeedGeneratorSequential(), true, false, false,
                false, 99, 98, 14, 90, 0);
        params.setTickLimit(31934);
        KeySequence keySequence = new KeySequence(KeySequenceMode.REPLAY);
        GamePanel panel1 = new GamePanel();
        GamePanel panel2 = new GamePanel();
        DebugPanel debugPanel1 = new TestDebugPanel();
        DebugPanel debugPanel2 = new TestDebugPanel();
        ConcurrentSkipListSet<Integer> keyQueue = new ConcurrentSkipListSet<>();
        Game game = new Game(panel1, panel2, debugPanel1, debugPanel2, keyQueue, params, keySequence);
        game.setTickCallback(() -> assertTick(game, panel1));

        gameThread = new Thread(game);
        gameThread.start();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //while(gameThread.isAlive()) {}
    }

    private int[] rgb(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int[] p = img.getRGB(0, 0, w, h, null, 0, w);   // always ARGB, whatever the image type
        for (int i = 0; i < p.length; i++) p[i] &= 0xFFFFFF;
        return p;
    }

    private void assertTick(Game game, GamePanel panel) {
        long tickNo = game.getTickNo();
        if(tickNo>31934) {
            IO.println("tickNo: " + tickNo);
        }


        String fileName = referencePath + "/tick_" + tickNo + ".png";
        try {
            BufferedImage imgRef = ImageIO.read(new File(fileName));
            if (imgRef == null) throw new IOException("Invalid or no image file: " + fileName);
            BufferedImage img = panel.getImage();
            int[] rgbRef = rgb(imgRef);
            int[] rgbGame = rgb(img);
            if(!Arrays.equals(rgbRef, rgbGame)) {
                IO.println(fileName);
                for(int i=0; i<rgbGame.length; i++) {
                    if (rgbGame[i] != rgbRef[i]) {
                        IO.println(String.format("%d %d/%d %06X/%06X ERROR", i, i % 256, i / 256, rgbGame[i], rgbRef[i]));
                    }
                }
                String dir = "saves/replay-test";
                new File(dir).mkdir();
                String testFile = dir + "/test_tick_" + tickNo + ".png";
                panel.saveImage(testFile);
                assertTrue(false, "Image for tick "+tickNo+" is incorrect");
            }

        } catch (IOException e) {
            IO.println(fileName);
            throw new RuntimeException(e);
        }
    }
}

class TestDebugPanel implements DebugPanel {
    @Override
    public void setText(String t) {}

    @Override
    public void append(String t) {}
}