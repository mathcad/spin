package org.spin.cloud.util;

import org.spin.cloud.vo.LinktraceInfo;

/**
 * 链路跟踪上下文
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/9/14</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class Linktrace {
    private final static ThreadLocal<LinktraceInfo> TRACE_INFO = new ThreadLocal<>();

    public static LinktraceInfo getCurrentTraceInfo() {
        return TRACE_INFO.get();
    }

    public static void setCurrentTraceInfo(LinktraceInfo linktraceInfo) {
        TRACE_INFO.set(linktraceInfo);
    }

    public static LinktraceInfo removeCurrentTraceInfo() {
        LinktraceInfo linktraceInfo = TRACE_INFO.get();
        TRACE_INFO.remove();
        return linktraceInfo;
    }
}
