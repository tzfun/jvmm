package org.beifengtz.jvmm.convey.auth;

import org.beifengtz.jvmm.common.util.CommonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
    private static final Set<Byte> ASCII_EXCLUDE = CommonUtil.hashSetOf((byte) 60, (byte) 61, (byte) 62, (byte) 94, (byte) 95, (byte) 96);

    private static final List<Byte> asciiList;

    private String key;

    static {
        asciiList = new ArrayList<>(ASCII_END - ASCII_START - ASCII_EXCLUDE.size() + 1);
        for (byte i = ASCII_START; i < ASCII_END; ++i) {
            if (!ASCII_EXCLUDE.contains(i)) {
                asciiList.add(i);
            }
        }
    }

    public int generateSeed() {
        Random random = new Random();
        int seed = random.ints(1, 1, 2021).findAny().orElse(1998);
        ArrayList<Byte> pool = new ArrayList<>(asciiList);
        Collections.shuffle(pool);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; ++i) {
            sb.append((char) pool.get(i).byteValue());
        }
        key = sb.toString();
        return seed;
    }

    public String getKey() {
        return key;
    }
}
