package org.beifengtz.jvmm.convey.enums;

import io.netty.handler.codec.http.HttpMethod;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:29 2022/9/13
 *
 * @author beifengtz
 */
public enum Method {
    GET(HttpMethod.GET),
    POST(HttpMethod.POST),
    DELETE(HttpMethod.DELETE),
    OPTIONS(HttpMethod.OPTIONS),
    PUT(HttpMethod.PUT);

    private final HttpMethod value;

    Method(HttpMethod value) {
        this.value = value;
    }

    public HttpMethod getValue() {
        return value;
    }
}
