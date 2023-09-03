package org.beifengtz.jvmm.web.mvc.filter;

import lombok.extern.slf4j.Slf4j;
import org.beifengtz.jvmm.common.util.StringUtil;
import org.beifengtz.jvmm.web.common.Constant;
import org.beifengtz.jvmm.web.common.RestfulStatus;
import org.beifengtz.jvmm.web.common.exception.AuthException;
import org.beifengtz.jvmm.web.manage.factory.ResultFactory;
import org.beifengtz.jvmm.web.mvc.service.JvmmService;

import javax.annotation.Resource;
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
 * Created in 10:38 上午 2022/1/12
 *
 * @author beifengtz
 */
@WebFilter(
        filterName = "Filter1_AuthFilter",
        urlPatterns = Constant.API_NON_PUB + "/*"
)
@Slf4j
public class AuthFilter implements BaseFilter {

    @Resource
    private ResultFactory resultFactory;
    @Resource
    private JvmmService jvmmService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (filterFirst(request, response, chain)) {
            return;
        }

        String token = httpRequest.getHeader("token");
        try {
            jvmmService.verifyToken(token);
            log.debug("Auth filter pass.");
            chain.doFilter(request, response);
        } catch (AuthException e) {
            onCheckFailed(httpResponse);
        } catch (Exception e) {
            e.printStackTrace();
            onServerErr(httpResponse, e.getMessage());
        }
    }

    @Override
    public void onCheckFailed(HttpServletResponse httpResponse) throws IOException {
        BaseFilter.initResponseHeaders(httpResponse);
        httpResponse.setContentType("application/json; charset=utf-8");
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.getWriter().write(StringUtil.getGson().toJson(resultFactory.error(RestfulStatus.AUTH_ERR, "Token invalid")));
    }

    @Override
    public void onServerErr(HttpServletResponse httpResponse, String msg) throws IOException {
        BaseFilter.initResponseHeaders(httpResponse);
        httpResponse.setContentType("application/json; charset=utf-8");
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.getWriter().write(StringUtil.getGson().toJson(resultFactory.error(RestfulStatus.SERVER_ERR, msg)));
    }
}
