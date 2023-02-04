package org.beifengtz.jvmm.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.util.concurrent.EventExecutorGroup;
import org.beifengtz.jvmm.convey.handler.HandlerProvider;
import org.beifengtz.jvmm.server.ServerContext;
import org.beifengtz.jvmm.server.entity.conf.SslConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 18:29 2022/9/7
 *
 * @author beifengtz
 */
public class HttpServerHandlerProvider implements HandlerProvider {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandlerProvider.class);

    public static final String HTTP_SERVER_HANDLER_NAME = "httpServerHandler";

    private int idleTime;
    private String name;
    private EventExecutorGroup group;
    private SslContext sslContext;

    private HttpServerHandlerProvider() {
    }

    public HttpServerHandlerProvider(int idleTime, EventExecutorGroup group) {
        this(idleTime, HTTP_SERVER_HANDLER_NAME, group);
    }

    public HttpServerHandlerProvider(int idleTime, String name, EventExecutorGroup group) {
        this.idleTime = idleTime;
        this.name = name;
        this.group = group;
        loadSslContext();
    }

    private void loadSslContext() {
        SslConf sslConf = ServerContext.getConfiguration().getServer().getHttp().getSsl();
        if (sslConf != null && sslConf.isEnable()) {
            File certCaFile = new File(sslConf.getCertCa());
            File certFile = new File(sslConf.getCert());
            File certKeyFile = new File(sslConf.getCertKey());

            ApplicationProtocolConfig alpn = new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                    ApplicationProtocolNames.HTTP_2);

            try {
                SslContextBuilder sslCtxBuilder = SslContextBuilder
                        .forServer(certFile, certKeyFile, sslConf.getKeyPassword())
                        .applicationProtocolConfig(alpn)
                        .sslProvider(sslConf.isOpenssl() ? SslProvider.OPENSSL : SslProvider.JDK)
                        .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                        .trustManager(certCaFile);
                if (certCaFile.exists()) {
                    sslCtxBuilder.trustManager(certCaFile);
                }
                sslContext = sslCtxBuilder.build();
            } catch (SSLException e) {
                logger.error("Load ssl context failed: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public ChannelHandler getHandler() {
        return new HttpServerHandler();
    }

    @Override
    public int getReaderIdle() {
        return idleTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EventExecutorGroup getGroup() {
        return group;
    }

    @Override
    public SslContext getSslContext() {
        return sslContext;
    }
}
