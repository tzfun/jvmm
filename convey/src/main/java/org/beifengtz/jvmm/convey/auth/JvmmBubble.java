package org.beifengtz.jvmm.convey.auth;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.beifengtz.jvmm.tools.util.SignatureUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 7:41 下午 2021/5/29
 *
 * @author beifengtz
 */
public class JvmmBubble {

    private static final byte ASCII_START = 33;
    private static final byte ASCII_END = 122;

    private static final List<Byte> asciiList;

    private String key;

    static {
        asciiList = new ArrayList<>(ASCII_END - ASCII_START + 1);
        for (byte i = ASCII_START; i < ASCII_END; ++i) {
            asciiList.add(i);
        }
    }

    public int generateSeed() {
        int seed;
        try {
            seed = RandomUtils.nextInt(1, 2021);
            ArrayList<Byte> pool = new ArrayList<>(asciiList);
            Collections.shuffle(pool);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; ++i) {
                sb.append((char) pool.get(i).byteValue());
            }
            key = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return seed;
    }

    public String getKey() {
        return key;
    }
}
