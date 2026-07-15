package org.kr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Static data blocks loaded from the {@code /initial_data.txt} classpath resource.
 * <p>
 * The data lives in a resource rather than inline array literals because a single
 * initializer (notably {@code sprite_graphics_data_728A}, ~15.5k values) exceeds
 * the JVM's 64&nbsp;KB per-method bytecode limit and would fail to compile.
 * <p>
 * Resource format: a line {@code @block <name> <startHex>} opens a block; the lines
 * that follow hold comma/space separated byte values until the next block. Anything
 * from {@code //} to end-of-line is a comment (labels, addresses, etc. are preserved
 * there), and lines starting with {@code #} are file-level comments.
 */
public class InitialData {
    private static final String resourceFile = "/initial_data.txt";
    private static final Map<String, DataBlock> BLOCKS = loadBlocks();

    public static DataBlock block(String name) {
        DataBlock b = BLOCKS.get(name);
        if (b == null) {
            throw new IllegalStateException("missing data block: " + name);
        }
        return b.copy();
    }

    private static Map<String, DataBlock> loadBlocks() {
        Map<String, DataBlock> blocks = new LinkedHashMap<>();
        try (InputStream in = InitialData.class.getResourceAsStream(resourceFile)) {
            if (in == null) throw new IllegalStateException("resource not found on classpath: " + resourceFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String blockName = null;
            int start = 0;
            List<Integer> data = null;
            String line;
            while ((line = reader.readLine()) != null) {
                int comment = line.indexOf("//");
                if (comment >= 0) line = line.substring(0, comment);
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == ';') continue;

                if (line.startsWith("@@block")) {
                    if (blockName != null) blocks.put(blockName, toBlock(start, data));

                    String[] parts = line.split("\\s+");
                    if (parts.length != 3) throw new IllegalStateException("malformed block header: " + line);
                    blockName = parts[1];
                    start = Integer.decode(parts[2]);
                    data = new ArrayList<>();
                    continue;
                }

                if (data == null) throw new IllegalStateException("data before first @block header: " + line);
                for (String token : line.split("[,\\s]+"))
                    if (!token.isEmpty()) data.add(Integer.decode(token));
            }
            if (blockName != null) blocks.put(blockName, toBlock(start, data));
        } catch (IOException e) {
            throw new UncheckedIOException("failed to read " + resourceFile, e);
        }
        return blocks;
    }

    private static DataBlock toBlock(int start, List<Integer> data) {
        int[] values = new int[data.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = data.get(i);
        }
        return new DataBlock(start, values);
    }
}
