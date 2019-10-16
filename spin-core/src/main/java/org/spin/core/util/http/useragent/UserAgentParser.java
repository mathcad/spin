package org.spin.core.util.http.useragent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-Agent解析器
 */
public class UserAgentParser {

    /**
     * 解析User-Agent
     *
     * @param userAgentString User-Agent字符串
     * @return {@link UserAgent}
     */
    public static UserAgent parse(String userAgentString) {
        final UserAgent userAgent = new UserAgent();

        final Browser browser = parseBrowser(userAgentString);
        userAgent.setBrowser(parseBrowser(userAgentString));
        userAgent.setVersion(browser.getVersion(userAgentString));

        final Engine engine = parseEngine(userAgentString);
        userAgent.setEngine(engine);
        if (!engine.isUnknown()) {
            userAgent.setEngineVersion(parseEngineVersion(engine, userAgentString));
        }
        userAgent.setOs(parseOS(userAgentString));
        final Platform platform = parsePlatform(userAgentString);
        userAgent.setPlatform(platform);
        userAgent.setMobile(platform.isMobile() || browser.isMobile());


        return userAgent;
    }

    /**
     * 解析浏览器类型
     *
     * @param userAgentString User-Agent字符串
     * @return 浏览器类型
     */
    private static Browser parseBrowser(String userAgentString) {
        for (Browser brower : Browser.browers) {
            if (brower.isMatch(userAgentString)) {
                return brower;
            }
        }
        return Browser.Unknown;
    }

    /**
     * 解析引擎类型
     *
     * @param userAgentString User-Agent字符串
     * @return 引擎类型
     */
    private static Engine parseEngine(String userAgentString) {
        for (Engine engine : Engine.engines) {
            if (engine.isMatch(userAgentString)) {
                return engine;
            }
        }
        return Engine.Unknown;
    }

    /**
     * 解析引擎版本
     *
     * @param engine          引擎
     * @param userAgentString User-Agent字符串
     * @return 引擎版本
     */
    private static String parseEngineVersion(Engine engine, String userAgentString) {
        final String regexp = engine.getName() + "[\\/\\- ]([\\d\\w\\.\\-]+)";
        final Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
        return group1(pattern, userAgentString);
    }

    private static String group1(Pattern pattern, CharSequence content) {
        if (null == content || null == pattern) {
            return null;
        }

        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 解析系统类型
     *
     * @param userAgentString User-Agent字符串
     * @return 系统类型
     */
    private static OS parseOS(String userAgentString) {
        for (OS os : OS.oses) {
            if (os.isMatch(userAgentString)) {
                return os;
            }
        }
        return OS.Unknown;
    }

    /**
     * 解析平台类型
     *
     * @param userAgentString User-Agent字符串
     * @return 平台类型
     */
    private static Platform parsePlatform(String userAgentString) {
        for (Platform platform : Platform.platforms) {
            if (platform.isMatch(userAgentString)) {
                return platform;
            }
        }
        return Platform.Unknown;
    }
}
