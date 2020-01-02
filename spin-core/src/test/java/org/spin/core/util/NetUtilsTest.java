package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/1/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class NetUtilsTest {

    @Test
    void testLocalHost() throws SocketException {
//        System.out.println(NetUtils.getLocalhost().getHostAddress());
        NetworkInterface iface;

        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
            iface = ifaces.nextElement();
            if (iface.isLoopback()
                || iface.getDisplayName().toLowerCase().startsWith("microsoft")
                || iface.getDisplayName().toLowerCase().startsWith("wan miniport")
                || iface.getDisplayName().toLowerCase().startsWith("vmware")
                || iface.getDisplayName().toLowerCase().startsWith("virtualbox")
                || iface.getDisplayName().toLowerCase().startsWith("vethernet")
                || iface.getDisplayName().toLowerCase().startsWith("docker")
                || iface.getDisplayName().toLowerCase().startsWith("veth")
                || !iface.getInetAddresses().hasMoreElements()) {
                continue;
            }
            System.out.println(iface.getDisplayName());
            for (Enumeration<InetAddress> inetAddresses = iface.getInetAddresses(); inetAddresses.hasMoreElements(); ) {
                InetAddress inetAddress = inetAddresses.nextElement();
                System.out.println("    " + inetAddress.getHostName() + "----" + inetAddress.getHostAddress());
            }
        }
    }

}
