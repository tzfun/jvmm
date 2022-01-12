package org.beifengtz.jvmm.web.mvc.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 10:40 上午 2022/1/12
 *
 * @author beifengtz
 */
@WebFilter(
        filterName = "Filter0_HeaderFilter",
        urlPatterns = "/*"
)
@Slf4j
public class HeaderFilter implements BaseFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (filterFirst(request, response, chain)) {
            return;
        }

        BaseFilter.initResponseHeaders(httpResponse);

        chain.doFilter(request, response);
    }

    @Override
    public void onCheckFailed(HttpServletResponse httpResponse) throws IOException {

    }

    @Override
    public void onServerErr(HttpServletResponse httpResponse, String msg) throws IOException {

    }
}
