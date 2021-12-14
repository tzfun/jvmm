package org.beifengtz.jvmm.agent;

import java.util.Map;

/**
 * Created in 10:10 2021/12/14
 *
 * @author beifengtz
 */
public class LoggerEvent {
    private String type;
    private String msg;
    private Object[] args;
    private Throwable throwable;
    private String name;

    public String getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public Object[] getArgs() {
        return args;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getName() {
        return name;
    }


    public static LoggerEvent fromMap(Map<String, Object> map) {
        LoggerEvent event = new LoggerEvent();

        event.throwable = (Throwable) map.get("throwable");
        event.type = (String) map.get("type");
        event.msg = (String) map.get("msg");
        event.args = (Object[]) map.get("args");
        event.name = (String) map.get("name");

        return event;
    }
}
