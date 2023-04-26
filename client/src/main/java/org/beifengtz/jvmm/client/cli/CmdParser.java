package org.beifengtz.jvmm.client.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * description: TODO
 * date: 17:06 2023/4/26
 *
 * @author beifengtz
 */
public class CmdParser {

    private final Map<String, String> kv = new HashMap<>();

    private CmdParser(CmdLine cmdLine, String command) {
        String[] split = command.split(" ");
        String key = null, tmpValue = null;
        for (String s : split) {
            if (s.startsWith(cmdLine.getArgPrefix())) {
                if (key != null) {
                    if (tmpValue == null) {
                        kv.put(key, "");
                    } else {
                        kv.put(key, tmpValue);
                    }
                }
                key = s.substring(cmdLine.getArgPrefix().length());
            } else {
                if (tmpValue == null) {
                    if (s.startsWith("\"") && !s.endsWith("\"")) {
                        tmpValue = s;
                    } else {
                        kv.put(key, s);
                        key = null;
                    }
                } else {
                    if (!s.startsWith("\"") && s.endsWith("\"")) {
                        tmpValue += " " + s;
                        kv.put(key, tmpValue.substring(1, tmpValue.length() - 1));
                        key = null;
                        tmpValue = null;
                    } else {
                        tmpValue += " " + s;
                    }
                }
            }
        }
        if (key != null) {
            kv.put(key, tmpValue);
        }
    }

    public static CmdParser create(CmdLine cmdLine, String command) {
        return new CmdParser(cmdLine, command);
    }

    public boolean hasArg(String arg) {
        return kv.containsKey(arg);
    }

    public String getArg(String arg) {
        return kv.get(arg);
    }

    public int getArgInt(String arg) {
        return Integer.parseInt(getArg(arg));
    }

    public long getArgLong(String arg) {
        return Long.parseLong(getArg(arg));
    }

    public boolean getArgBoolean(String arg) {
        String val = getArg(arg);
        if (val == null) {
            return false;
        } else {
            return !val.matches("false|0|no");
        }
    }
}
