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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.fail;

public class GameReplayTest {
    Thread gameThread = null;
    String referencePath = "saves/20261110_195359";

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

        Map<String, Long> hashes = new HashMap<>();
        try(var lines = Files.lines(Path.of(referencePath, "hashes.txt"))) {
            lines.forEach(l  -> {
                var tokens = l.split(" ");
                hashes.put(tokens[0], Long.parseLong(tokens[1]));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        game.setTickCallback(() -> assertTick(game, panel1, hashes));

        gameThread = new Thread(game);
        gameThread.start();
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int[] rgb(BufferedImage img) {
        int[] p = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());   // always ARGB, whatever the image type
        for (int i = 0; i < p.length; i++) p[i] &= 0x00FFFFFF; // 00 - alpha channel, we need zeros here
        return p;
    }

    private void assertTick(Game game, GamePanel panel, Map<String, Long> hashes) {
        long tickNo = game.getTickNo();
        String fileName = String.format("tick_%d.png", tickNo);
        if (!hashes.containsKey(fileName)) fail("Invalid or no hash for: " + fileName);
        long hashRef = hashes.get(fileName);

        long gameHash = panel.screenHash();
        if (hashRef != gameHash) {
            IO.println(fileName);
            String dir = "saves/replay-test";
            new File(dir).mkdir();
            String testFile = "test_tick_" + tickNo + ".png";
            try {
                panel.saveImage(dir, testFile);
            } catch (IOException e) {
                fail(e.getMessage());
            }
            fail("Image for tick " + tickNo + " is incorrect");
        }
    }
}

class TestDebugPanel implements DebugPanel {
    @Override
    public void setText(String t) {}

    @Override
    public void append(String t) {}
}