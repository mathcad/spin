package org.spin.cloud.util;

import org.spin.cloud.vo.LinkTraceInfo;

/**
 * 链路跟踪上下文
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/9/14</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LinkTrace {
    private final static ThreadLocal<LinkTraceInfo> TRACE_INFO = new ThreadLocal<>();

    public static LinkTraceInfo getCurrentTraceInfo() {
        return TRACE_INFO.get();
    }

    public static void setCurrentTraceInfo(LinkTraceInfo linktraceInfo) {
        TRACE_INFO.set(linktraceInfo);
    }

    public static LinkTraceInfo removeCurrentTraceInfo() {
        LinkTraceInfo linkTraceInfo = TRACE_INFO.get();
        TRACE_INFO.remove();
        return linkTraceInfo;
    }
}
