package org.spin.cloud.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.cloud.annotation.UtilClass;
import org.spin.core.util.NetUtils;
import org.spin.core.util.Util;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 网络工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/24</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@UtilClass
public final class NetworkUtils extends Util {
    private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
    private static volatile long updateTime;
    private static final Set<String> networks = new HashSet<>();
    private static final Set<Short> v4NetMasks = new HashSet<>();
    private static final Set<Short> v6NetMasks = new HashSet<>();
    private static final Object lock = new Object();

    private static Set<String> hosts = new HashSet<>();
    private static DiscoveryClient discoveryClient;

    private NetworkUtils() {
    }

    static {
        Util.registerLatch(NetworkUtils.class);
        try {
            List<NetUtils.NetAddress> networkInfo = NetUtils.getNetworkInfo();
            networkInfo.forEach(it -> {
                if (it.isIpV4()) {
                    v4NetMasks.add(it.getNetMask());
                } else if (it.isIpV6()) {
                    v6NetMasks.add(it.getNetMask());
                }
                networks.add(it.getAddress().getHostAddress());
            });
            networks.add("localhost");
            networks.add("127.0.0.1");
            networks.add("0:0:0:0:0:0:0:1");
            hosts.addAll(networks);
        } catch (Exception ignore) {
            // do nothing
        }
    }

    public static void init(DiscoveryClient discoveryClient) {
        NetworkUtils.discoveryClient = discoveryClient;
        updateHosts();
        Util.ready(NetworkUtils.class);
    }

    public static boolean contains(String host) {
        Util.awaitUntilReady(NetworkUtils.class);
        updateHosts();
        return hosts.contains(host.toLowerCase());
    }

    public static boolean inSameVlan(String hostName) {
        Util.awaitUntilReady(NetworkUtils.class);
        updateHosts();
        String host = hostName.toLowerCase();
        if (hosts.contains(host)) {
            return true;
        }

        try {
            byte[] address;
            Set<Short> netMasks;
            if (host.contains(":")) {
                address = NetUtils.textToNumericFormatV6(host);
                netMasks = v6NetMasks;
            } else {
                address = NetUtils.textToNumericFormatV4(host);
                netMasks = v4NetMasks;
            }

            for (Short it : netMasks) {
                byte[] segment = new byte[address.length];
                int bytes = it / 8;
                int bits = it % 8;
                for (int i = 0; i < address.length; i++) {
                    if (i < bytes) {
                        segment[i] = address[i];
                    } else if (i == bytes) {
                        segment[i] = (byte) (address[i] >>> (8 - bits));
                    } else {
                        segment[i] = 0;
                    }
                }
                String hostAddress = InetAddress.getByAddress(segment).getHostAddress();
                if (hosts.contains(hostAddress)) {
                    return true;
                }
            }

        } catch (Exception ignore) {
        }
        return false;
    }

    private static boolean isExpired() {
        return System.currentTimeMillis() - 60_000L > updateTime;
    }

    private static void updateHosts() {
        if (null == discoveryClient) {
            return;
        }
        if (isExpired()) {
            synchronized (lock) {
                if (isExpired()) {
                    logger.info("更新服务实例信息");
                    hosts.clear();
                    hosts.addAll(networks);
                    hosts.addAll(discoveryClient.getServices().stream().flatMap(i -> discoveryClient.getInstances(i).stream()).map(ServiceInstance::getHost).map(String::toLowerCase).collect(Collectors.toSet()));
                    updateTime = System.currentTimeMillis();
                }
            }
        }
    }
}
