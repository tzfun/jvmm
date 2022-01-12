package org.beifengtz.jvmm.web.mvc.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:37 上午 2022/1/12
 *
 * @author beifengtz
 */
public interface BaseFilter extends Filter {

    /**
     * 过滤掉Chrome等浏览器的OPTIONS请求，避免重复filter
     */
    default boolean filterFirst(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return true;
        }
        return false;
    }

    void onCheckFailed(HttpServletResponse httpResponse) throws IOException;

    void onServerErr(HttpServletResponse httpResponse, String msg) throws IOException;

    static void initResponseHeaders(HttpServletResponse httpResponse) {
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Origin,No-Cache,X-Requested-With,If-Modified-Since," +
                "Pragma,Last-Modified,Cache-Control,Expires,Content-Type,X-E4M-With,token");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        httpResponse.setHeader("Access-Control-Max-Age", "0");
        httpResponse.setCharacterEncoding("UTF-8");
    }
}
