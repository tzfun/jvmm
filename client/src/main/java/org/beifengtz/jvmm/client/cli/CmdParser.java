package org.beifengtz.jvmm.client.cli;

import org.beifengtz.jvmm.common.util.StringUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: TODO
 * date: 17:06 2023/4/26
 *
 * @author beifengtz
 */
public class CmdParser {

    private final Map<String, List<String>> kv = new HashMap<>();

    private CmdParser(CmdLine cmdLine, String command) throws ParseException {
        this(cmdLine, command.split(" "));
    }

    private CmdParser(CmdLine cmdLine, String[] args) throws ParseException {
        try {
            String key = null, tmpValue = null;
            for (String s : args) {
                if (s.startsWith(cmdLine.getArgPrefix())) {
                    if (key != null) {
                        push(key, tmpValue);
                    }
                    key = s.substring(cmdLine.getArgPrefix().length());
                } else {
                    if (tmpValue == null) {
                        if (s.startsWith("\"") && !s.endsWith("\"")) {
                            tmpValue = s;
                        } else {
                            push(key, s);
                            key = null;
                        }
                    } else {
                        if (!s.startsWith("\"") && s.endsWith("\"")) {
                            tmpValue += " " + s;
                            push(key, tmpValue.substring(1, tmpValue.length() - 1));
                            key = null;
                            tmpValue = null;
                        } else {
                            tmpValue += " " + s;
                        }
                    }
                }
            }
            if (key != null) {
                push(key, tmpValue);
            }
        } catch (Exception e) {
            throw new ParseException("Parse command failed: " + Arrays.toString(args), 0);
        }
    }

    private void push(String key, String value) {
        List<String> values = kv.computeIfAbsent(key, o -> new ArrayList<>(1));
        if (StringUtil.nonEmpty(value)) {
            values.add(value);
        }
    }

    public static CmdParser parse(CmdLine cmdLine, String command) throws ParseException {
        return new CmdParser(cmdLine, command);
    }

    public static CmdParser parse(CmdLine cmdLine, String[] args) throws ParseException {
        return new CmdParser(cmdLine, args);
    }

    public boolean hasArg(String arg) {
        return kv.containsKey(arg);
    }

    public String getArg(String arg) {
        return getArg(arg, null);
    }

    public String getArg(String arg, String defaultValue) {
        if (kv.containsKey(arg)) {
            List<String> values = kv.get(arg);
            if (!values.isEmpty()) {
                return values.get(0);
            }
        }
        return defaultValue;
    }

    public List<String> getArgList(String arg) {
        return kv.get(arg);
    }

    public int getArgInt(String arg) {
        return Integer.parseInt(getArg(arg));
    }

    public int getArgInt(String arg, int defaultValue) {
        return hasArg(arg) ? Integer.parseInt(getArg(arg)) : defaultValue;
    }

    public long getArgLong(String arg) {
        return Long.parseLong(getArg(arg));
    }

    public long getArgLong(String arg, long defaultValue) {
        return hasArg(arg) ? Long.parseLong(getArg(arg)) : defaultValue;
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
