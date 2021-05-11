package org.beifengtz.jvmm.core;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * <p>
 * Description: TODO
 * </p>
 * <p>
 * Created in 14:36 2021/5/11
 *
 * @author beifengtz
 */
public class TestServer {

    @Test
    public void testInet() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        System.err.println(addr.getHostAddress());
        System.err.println(addr.getHostName());
    }

    @Test
    public void testMacId() throws SocketException, UnknownHostException {
        NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

        byte[] mac = network.getHardwareAddress();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
        }
        System.err.println(sb);
    }
}
