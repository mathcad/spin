package org.spin.core.util;

import org.spin.core.ErrorCode;
import org.spin.core.throwable.SpinException;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 网络相关工具类
 */
public abstract class NetUtils {
    public static final String LOCAL_IP_V4 = "127.0.0.1";
    public static final String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";
    public static final String PRIVATE_IPV6 = "FEC0::/48";

    public static final String IPV6_COMPATIBLE_IPV4 = "0:0:0:0";
    public static final String IPV6_MAP_IPV4 = "0:0:0:FFFF";
    public static final Pattern ipv4 = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");

    private static final int INADDR4SZ = 4;
    private static final int INADDR16SZ = 16;
    private static final int INT16SZ = 2;

    // See java.net.URI for more details on how to generate these
    // masks.
    //
    // square brackets
    private static final long L_IPV6_DELIMS = 0x0L; // "[]"
    private static final long H_IPV6_DELIMS = 0x28000000L; // "[]"
    // RFC 3986 gen-delims
    // These gen-delims can appear in authority
    private static final long L_AUTH_DELIMS = 0x400000000000000L; // "@[]:"
    private static final long H_AUTH_DELIMS = 0x28000001L; // "@[]:"
    // colon is allowed in userinfo
    private static final long L_COLON = 0x400000000000000L; // ":"
    private static final long H_COLON = 0x0L; // ":"
    // slash should be encoded in authority
    private static final long L_SLASH = 0x800000000000L; // "/"
    private static final long H_SLASH = 0x0L; // "/"
    // backslash should always be encoded
    // ASCII chars 0-31 + 127 - various controls + CRLF + TAB
    private static final long L_NON_PRINTABLE = 0xffffffffL;
    private static final long H_NON_PRINTABLE = 0x8000000000000000L;
    // All of the above
    private static final long L_EXCLUDE = 0x84008008ffffffffL;
    private static final long H_EXCLUDE = 0x8000000038000001L;

    private static final char[] OTHERS = {
        8263, 8264, 8265, 8448, 8449, 8453, 8454, 10868,
        65109, 65110, 65119, 65131, 65283, 65295, 65306, 65311, 65312
    };

    private NetUtils() {
        throw new SpinException("Cound not create an instance of util class: " + this.getClass().getName());
    }

    /**
     * 根据long值获取ip v4地址
     *
     * @param longIP IP的long表示形式
     * @return IP V4 地址
     */
    public static String longToIpv4(long longIP) {
        // 直接右移24位
        // 将高8位置0，然后右移16位
        return (longIP >>> 24) +
            "." +
            ((longIP & 0x00FFFFFF) >>> 16) +
            "." +
            ((longIP & 0x0000FFFF) >>> 8) +
            "." +
            (longIP & 0x000000FF);
    }

    /**
     * 根据ip地址计算出long型的数据
     *
     * @param strIP IP V4 地址
     * @return long值
     */
    public static long ipv4ToLong(String strIP) {
        if (isIpv4(strIP)) {
            long[] ip = new long[4];
            // 先找到IP地址字符串中.的位置
            int position1 = strIP.indexOf('.');
            int position2 = strIP.indexOf('.', position1 + 1);
            int position3 = strIP.indexOf('.', position2 + 1);
            // 将每个.之间的字符串转换成整型
            ip[0] = Long.parseLong(strIP.substring(0, position1));
            ip[1] = Long.parseLong(strIP.substring(position1 + 1, position2));
            ip[2] = Long.parseLong(strIP.substring(position2 + 1, position3));
            ip[3] = Long.parseLong(strIP.substring(position3 + 1));
            return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
        }
        return 0;
    }

    /**
     * 检测本地端口可用性
     *
     * @param port 被检测的端口
     * @return 是否可用
     */
    public static boolean isUsableLocalPort(int port) {
        if (!isValidPort(port)) {
            // 给定的IP未在指定端口范围中
            return false;
        }
        try {
            new Socket(LOCAL_IP_V4, port).close();
            // socket链接正常，说明这个端口正在使用
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 是否为有效的端口
     *
     * @param port 端口号
     * @return 是否有效
     */
    public static boolean isValidPort(int port) {
        // 有效端口是0～65535
        return port >= 0 && port <= 0xFFFF;
    }

    /**
     * 判定是否为内网IP<br>
     * 私有IP：A类 10.0.0.0-10.255.255.255 B类 172.16.0.0-172.31.255.255 C类 192.168.0.0-192.168.255.255 当然，还有127这个网段是环回地址
     *
     * @param ipAddress IP地址
     * @return 是否为内网IP
     */
    public static boolean isInnerIP(String ipAddress) {
        if (LOCAL_IP_V4.equals(ipAddress)) {
            return true;
        }

        boolean isInnerIp;
        long ipNum = NetUtils.ipv4ToLong(ipAddress);

        long aBegin = ipv4ToLong("10.0.0.0");
        long aEnd = ipv4ToLong("10.255.255.255");

        long a2Begin = ipv4ToLong("100.64.0.0");
        long a2End = ipv4ToLong("100.127.255.255");

        long bBegin = ipv4ToLong("172.16.0.0");
        long bEnd = ipv4ToLong("172.31.255.255");

        long cBegin = ipv4ToLong("192.168.0.0");
        long cEnd = ipv4ToLong("192.168.255.255");

        isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, a2Begin, a2End) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd);
        return isInnerIp;
    }

    /**
     * 获得本机的IP地址列表<br>
     * 返回的IP列表有序，按照系统设备顺序
     *
     * @return IP地址列表 {@link LinkedHashSet}
     */
    public static Set<String> localIpv4s() {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, e);
        }

        if (networkInterfaces == null) {
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION, "Get network interface error!");
        }

        final LinkedHashSet<String> ipSet = new LinkedHashSet<>();

        while (networkInterfaces.hasMoreElements()) {
            final NetworkInterface networkInterface = networkInterfaces.nextElement();
            final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                final InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    ipSet.add(inetAddress.getHostAddress());
                }
            }
        }

        return ipSet;
    }

    /**
     * 相对URL转换为绝对URL
     *
     * @param absoluteBasePath 基准路径，绝对
     * @param relativePath     相对路径
     * @return 绝对URL
     */
    public static String toAbsoluteUrl(String absoluteBasePath, String relativePath) {
        try {
            URL absoluteUrl = new URL(absoluteBasePath);
            return new URL(absoluteUrl, relativePath).toString();
        } catch (Exception e) {
            throw new SpinException(ErrorCode.NETWORK_EXCEPTION,
                StringUtils.plainFormat("To absolute url [{}] base [{}] error!", relativePath, absoluteBasePath));
        }
    }

    /**
     * 隐藏掉IP地址的最后一部分为 * 代替
     *
     * @param ip IP地址
     * @return 隐藏部分后的IP
     */
    public static String hideIpPart(String ip) {
        return ip.substring(0, ip.lastIndexOf('.') + 1) + "*";
    }

    /**
     * 隐藏掉IP地址的最后一部分为 * 代替
     *
     * @param ip IP地址
     * @return 隐藏部分后的IP
     */
    public static String hideIpPart(long ip) {
        return hideIpPart(longToIpv4(ip));
    }

    /**
     * 构建InetSocketAddress<br>
     * 当host中包含端口时（用“：”隔开），使用host中的端口，否则使用默认端口<br>
     * 给定host为空时使用本地host（127.0.0.1）
     *
     * @param host        Host
     * @param defaultPort 默认端口
     * @return InetSocketAddress
     */
    public static InetSocketAddress buildInetSocketAddress(String host, int defaultPort) {
        if (StringUtils.isBlank(host)) {
            host = LOCAL_IP_V4;
        }

        String destHost;
        int port;
        int index = host.indexOf(':');
        if (index != -1) {
            // host:port形式
            destHost = host.substring(0, index);
            port = Integer.parseInt(host.substring(index + 1));
        } else {
            destHost = host;
            port = defaultPort;
        }

        return new InetSocketAddress(destHost, port);
    }

    /**
     * 通过域名得到IP
     *
     * @param hostName HOST
     * @return ip address or hostName if UnknownHostException
     */
    public static String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * 获取本机所有网卡
     *
     * @return 所有网卡，异常返回<code>null</code>
     * @since 3.0.1
     */
    public static List<NetworkInterface> getNetworkInterfaces() {
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new SpinException("获取本机网卡异常", e);
        }

        return StreamUtils.stream(networkInterfaces).collect(Collectors.toList());
    }

    /**
     * 获取本机网卡IP地址，这个地址为所有网卡中非回路地址的第一个<br>
     * 如果获取失败调用 {@link InetAddress#getLocalHost()}方法获取。<br>
     * 此方法不会抛出异常，获取失败将返回<code>null</code><br>
     * <p>
     * 参考：http://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
     *
     * @return 本机网卡IP地址，获取失败返回<code>null</code>
     * @since 3.0.1
     */
    public static InetAddress getLocalhost() {
        InetAddress candidateAddress = null;
        NetworkInterface iface;
        InetAddress inetAddr;
        try {
            for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
                iface = ifaces.nextElement();
                String name = iface.getDisplayName().toLowerCase();
                if (name.contains("vmware") || name.contains("docker") || name.contains("hyper") || name.contains("virtual")) {
                    continue;
                }
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress() && !inetAddr.isLinkLocalAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        } else if (null == candidateAddress) {
                            //非site-local地址做为候选地址返回
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            //ignore
        }

        if (null == candidateAddress) {
            try {
                candidateAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                //ignore
            }
        }

        return candidateAddress;
    }

    public static boolean isIpv4(String strIp) {
        return ipv4.matcher(strIp).matches();
    }

    // ----------------------------------------------------------------------------------------- Private method start

    /**
     * 获取本地网络信息
     *
     * @return 网络信息
     */
    public static List<NetAddress> getNetworkInfo() {
        return getNetworkInterfaces().stream().filter(it -> {
            try {
                return !it.isLoopback();
            } catch (SocketException e) {
                return false;
            }
        }).flatMap(it -> it.getInterfaceAddresses().stream()).map(it -> {
            byte[] address = it.getAddress().getAddress();
            byte[] segment = new byte[address.length];
            int bytes = it.getNetworkPrefixLength() / 8;
            int bits = it.getNetworkPrefixLength() % 8;
            for (int i = 0; i < address.length; i++) {
                if (i < bytes) {
                    segment[i] = address[i];
                } else if (i == bytes) {
                    segment[i] = (byte) (address[i] >>> (8 - bits));
                } else {
                    segment[i] = 0;
                }
            }
            try {
                return new NetAddress(InetAddress.getByAddress(segment), it.getNetworkPrefixLength());
            } catch (UnknownHostException e) {
                throw new SpinException(e);
            }
        }).collect(Collectors.toList());
    }

    /*
     * Converts IPv4 address in its textual presentation form
     * into its numeric binary form.
     *
     * @param src a String representing an IPv4 address in standard format
     * @return a byte array representing the IPv4 numeric address
     */
//    @SuppressWarnings("fallthrough")
    public static byte[] textToNumericFormatV4(String src) {
        byte[] res = new byte[INADDR4SZ];

        long tmpValue = 0;
        int currByte = 0;
        boolean newOctet = true;

        int len = src.length();
        if (len == 0 || len > 15) {
            return null;
        }
        /*
         * When only one part is given, the value is stored directly in
         * the network address without any byte rearrangement.
         *
         * When a two part address is supplied, the last part is
         * interpreted as a 24-bit quantity and placed in the right
         * most three bytes of the network address. This makes the
         * two part address format convenient for specifying Class A
         * network addresses as net.host.
         *
         * When a three part address is specified, the last part is
         * interpreted as a 16-bit quantity and placed in the right
         * most two bytes of the network address. This makes the
         * three part address format convenient for specifying
         * Class B net- work addresses as 128.net.host.
         *
         * When four parts are specified, each is interpreted as a
         * byte of data and assigned, from left to right, to the
         * four bytes of an IPv4 address.
         *
         * We determine and parse the leading parts, if any, as single
         * byte values in one pass directly into the resulting byte[],
         * then the remainder is treated as a 8-to-32-bit entity and
         * translated into the remaining bytes in the array.
         */
        for (int i = 0; i < len; i++) {
            char c = src.charAt(i);
            if (c == '.') {
                if (newOctet || tmpValue < 0 || tmpValue > 0xff || currByte == 3) {
                    return null;
                }
                res[currByte++] = (byte) (tmpValue & 0xff);
                tmpValue = 0;
                newOctet = true;
            } else {
                int digit = Character.digit(c, 10);
                if (digit < 0) {
                    return null;
                }
                tmpValue *= 10;
                tmpValue += digit;
                newOctet = false;
            }
        }
        if (newOctet || tmpValue < 0 || tmpValue >= (1L << ((4 - currByte) * 8))) {
            return null;
        }
        switch (currByte) {
            case 0:
                res[0] = (byte) ((tmpValue >> 24) & 0xff);
            case 1:
                res[1] = (byte) ((tmpValue >> 16) & 0xff);
            case 2:
                res[2] = (byte) ((tmpValue >> 8) & 0xff);
            case 3:
                res[3] = (byte) ((tmpValue) & 0xff);
        }
        return res;
    }

    /*
     * Convert IPv6 presentation level address to network order binary form.
     * credit:
     *  Converted from C code from Solaris 8 (inet_pton)
     *
     * Any component of the string following a per-cent % is ignored.
     *
     * @param src a String representing an IPv6 address in textual format
     * @return a byte array representing the IPv6 numeric address
     */
    public static byte[] textToNumericFormatV6(String src) {
        // Shortest valid string is "::", hence at least 2 chars
        if (src.length() < 2) {
            return null;
        }

        int colonp;
        char ch;
        boolean saw_xdigit;
        int val;
        char[] srcb = src.toCharArray();
        byte[] dst = new byte[INADDR16SZ];

        int srcb_length = srcb.length;
        int pc = src.indexOf('%');
        if (pc == srcb_length - 1) {
            return null;
        }

        if (pc != -1) {
            srcb_length = pc;
        }

        colonp = -1;
        int i = 0, j = 0;
        /* Leading :: requires some special handling. */
        if (srcb[i] == ':')
            if (srcb[++i] != ':')
                return null;
        int curtok = i;
        saw_xdigit = false;
        val = 0;
        while (i < srcb_length) {
            ch = srcb[i++];
            int chval = Character.digit(ch, 16);
            if (chval != -1) {
                val <<= 4;
                val |= chval;
                if (val > 0xffff)
                    return null;
                saw_xdigit = true;
                continue;
            }
            if (ch == ':') {
                curtok = i;
                if (!saw_xdigit) {
                    if (colonp != -1)
                        return null;
                    colonp = j;
                    continue;
                } else if (i == srcb_length) {
                    return null;
                }
                if (j + INT16SZ > INADDR16SZ)
                    return null;
                dst[j++] = (byte) ((val >> 8) & 0xff);
                dst[j++] = (byte) (val & 0xff);
                saw_xdigit = false;
                val = 0;
                continue;
            }
            if (ch == '.' && ((j + INADDR4SZ) <= INADDR16SZ)) {
                String ia4 = src.substring(curtok, srcb_length);
                /* check this IPv4 address has 3 dots, i.e. A.B.C.D */
                int dot_count = 0, index = 0;
                while ((index = ia4.indexOf('.', index)) != -1) {
                    dot_count++;
                    index++;
                }
                if (dot_count != 3) {
                    return null;
                }
                byte[] v4addr = textToNumericFormatV4(ia4);
                if (v4addr == null) {
                    return null;
                }
                for (int k = 0; k < INADDR4SZ; k++) {
                    dst[j++] = v4addr[k];
                }
                saw_xdigit = false;
                break;  /* '\0' was seen by inet_pton4(). */
            }
            return null;
        }
        if (saw_xdigit) {
            if (j + INT16SZ > INADDR16SZ)
                return null;
            dst[j++] = (byte) ((val >> 8) & 0xff);
            dst[j++] = (byte) (val & 0xff);
        }

        if (colonp != -1) {
            int n = j - colonp;

            if (j == INADDR16SZ)
                return null;
            for (i = 1; i <= n; i++) {
                dst[INADDR16SZ - i] = dst[colonp + n - i];
                dst[colonp + n - i] = 0;
            }
            j = INADDR16SZ;
        }
        if (j != INADDR16SZ)
            return null;
        byte[] newdst = convertFromIPv4MappedAddress(dst);
        if (newdst != null) {
            return newdst;
        } else {
            return dst;
        }
    }

    /**
     * @param src a String representing an IPv4 address in textual format
     * @return a boolean indicating whether src is an IPv4 literal address
     */
    public static boolean isIPv4LiteralAddress(String src) {
        return textToNumericFormatV4(src) != null;
    }

    /**
     * @param src a String representing an IPv6 address in textual format
     * @return a boolean indicating whether src is an IPv6 literal address
     */
    public static boolean isIPv6LiteralAddress(String src) {
        return textToNumericFormatV6(src) != null;
    }

    /*
     * Convert IPv4-Mapped address to IPv4 address. Both input and
     * returned value are in network order binary form.
     *
     * @param src a String representing an IPv4-Mapped address in textual format
     * @return a byte array representing the IPv4 numeric address
     */
    public static byte[] convertFromIPv4MappedAddress(byte[] addr) {
        if (isIPv4MappedAddress(addr)) {
            byte[] newAddr = new byte[INADDR4SZ];
            System.arraycopy(addr, 12, newAddr, 0, INADDR4SZ);
            return newAddr;
        }
        return null;
    }

    // Tell whether the given character is found by the given mask pair
    public static boolean match(char c, long lowMask, long highMask) {
        if (c < 64)
            return ((1L << c) & lowMask) != 0;
        if (c < 128)
            return ((1L << (c - 64)) & highMask) != 0;
        return false; // other non ASCII characters are not filtered
    }

    // returns -1 if the string doesn't contain any characters
    // from the mask, the index of the first such character found
    // otherwise.
    public static int scan(String s, long lowMask, long highMask) {
        int i = -1, len;
        if (s == null || (len = s.length()) == 0) return -1;
        boolean match = false;
        while (++i < len && !(match = match(s.charAt(i), lowMask, highMask))) ;
        if (match) return i;
        return -1;
    }

    public static int scan(String s, long lowMask, long highMask, char[] others) {
        int i = -1, len;
        if (s == null || (len = s.length()) == 0) return -1;
        boolean match = false;
        char c, c0 = others[0];
        while (++i < len && !(match = match((c = s.charAt(i)), lowMask, highMask))) {
            if (c >= c0 && (Arrays.binarySearch(others, c) > -1)) {
                match = true;
                break;
            }
        }
        if (match) return i;

        return -1;
    }

    // check authority of hierarchical URL. Appropriate for
    // HTTP-like protocol handlers
    public static String checkAuthority(URL url) {
        String s, u, h;
        if (url == null) return null;
        if ((s = checkUserInfo(u = url.getUserInfo())) != null) {
            return s;
        }
        if ((s = checkHost(h = url.getHost())) != null) {
            return s;
        }
        if (h == null && u == null) {
            return checkAuth(url.getAuthority());
        }
        return null;
    }

    // minimal syntax checks - deeper check may be performed
    // by the appropriate protocol handler
    public static String checkExternalForm(URL url) {
        String s;
        if (url == null) return null;
        int index = scan(s = url.getUserInfo(),
            L_NON_PRINTABLE | L_SLASH,
            H_NON_PRINTABLE | H_SLASH);
        if (index >= 0) {
            return "Illegal character found in authority: "
                + describeChar(s.charAt(index));
        }
        if ((s = checkHostString(url.getHost())) != null) {
            return s;
        }
        return null;
    }

    public static String checkHostString(String host) {
        if (host == null) return null;
        int index = scan(host,
            L_NON_PRINTABLE | L_SLASH,
            H_NON_PRINTABLE | H_SLASH,
            OTHERS);
        if (index >= 0) {
            return "Illegal character found in host: "
                + describeChar(host.charAt(index));
        }
        return null;
    }

    /**
     * Utility routine to check if the InetAddress is an
     * IPv4 mapped IPv6 address.
     *
     * @return a <code>boolean</code> indicating if the InetAddress is
     * an IPv4 mapped IPv6 address; or false if address is IPv4 address.
     */
    private static boolean isIPv4MappedAddress(byte[] addr) {
        if (addr.length < INADDR16SZ) {
            return false;
        }
        return (addr[0] == 0x00) && (addr[1] == 0x00) &&
            (addr[2] == 0x00) && (addr[3] == 0x00) &&
            (addr[4] == 0x00) && (addr[5] == 0x00) &&
            (addr[6] == 0x00) && (addr[7] == 0x00) &&
            (addr[8] == 0x00) && (addr[9] == 0x00) &&
            (addr[10] == (byte) 0xff) &&
            (addr[11] == (byte) 0xff);
    }

    private static String describeChar(char c) {
        if (c < 32 || c == 127) {
            if (c == '\n') return "LF";
            if (c == '\r') return "CR";
            return "control char (code=" + (int) c + ")";
        }
        if (c == '\\') return "'\\'";
        return "'" + c + "'";
    }

    private static String checkUserInfo(String str) {
        // colon is permitted in user info
        int index = scan(str, L_EXCLUDE & ~L_COLON,
            H_EXCLUDE & ~H_COLON);
        if (index >= 0) {
            return "Illegal character found in user-info: "
                + describeChar(str.charAt(index));
        }
        return null;
    }

    private static String checkHost(String str) {
        int index;
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1);
            if (isIPv6LiteralAddress(str)) {
                index = str.indexOf('%');
                if (index >= 0) {
                    index = scan(str = str.substring(index),
                        L_NON_PRINTABLE | L_IPV6_DELIMS,
                        H_NON_PRINTABLE | H_IPV6_DELIMS);
                    if (index >= 0) {
                        return "Illegal character found in IPv6 scoped address: "
                            + describeChar(str.charAt(index));
                    }
                }
                return null;
            }
            return "Unrecognized IPv6 address format";
        } else {
            index = scan(str, L_EXCLUDE, H_EXCLUDE);
            if (index >= 0) {
                return "Illegal character found in host: "
                    + describeChar(str.charAt(index));
            }
        }
        return null;
    }

    private static String checkAuth(String str) {
        int index = scan(str,
            L_EXCLUDE & ~L_AUTH_DELIMS,
            H_EXCLUDE & ~H_AUTH_DELIMS);
        if (index >= 0) {
            return "Illegal character found in authority: "
                + describeChar(str.charAt(index));
        }
        return null;
    }

    /**
     * 指定IP的long是否在指定范围内
     *
     * @param userIp 用户IP
     * @param begin  开始IP
     * @param end    结束IP
     * @return 是否在范围内
     */
    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }
    // ----------------------------------------------------------------------------------------- Private method end

    public static class NetAddress {
        private InetAddress address;
        private short netMask;

        public NetAddress(InetAddress address, short netMask) {
            this.address = address;
            this.netMask = netMask;
        }

        public InetAddress getAddress() {
            return address;
        }

        public void setAddress(InetAddress address) {
            this.address = address;
        }

        public short getNetMask() {
            return netMask;
        }

        public void setNetMask(short netMask) {
            this.netMask = netMask;
        }

        public boolean isIpV4() {
            return address instanceof Inet4Address;
        }

        public boolean isIpV6() {
            return address instanceof Inet6Address;
        }
    }
}
