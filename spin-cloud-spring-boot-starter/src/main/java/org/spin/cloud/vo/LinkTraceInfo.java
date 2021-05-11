package org.spin.cloud.vo;

import java.io.Serializable;

/**
 * 链路跟踪信息
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/9/14</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LinkTraceInfo implements Serializable {
    private static final long serialVersionUID = -2137151214617260626L;

    private final String traceId;
    private final String parentSpanId;
    private final String spanId;

    private final long entryTime;
    private long exitTime;

    public LinkTraceInfo(String traceId, String parentSpanId, String spanId) {
        entryTime = System.currentTimeMillis();
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public String getSpanId() {
        return spanId;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    public String entryInfo(String requestInfo) {
        return String.format("LinkTrace Entry [%d] - TraceId: %s ParentSpanId: %s SpanId: %s\n    Request Info: %s",
            entryTime,
            traceId,
            parentSpanId,
            spanId,
            requestInfo
        );
    }

    public String exitInfo() {
        return String.format("LinkTrace  Exit [%d] - TraceId: %s ParentSpanId: %s SpanId: %s elapsedTime: %dms",
            exitTime,
            traceId,
            parentSpanId,
            spanId,
            exitTime - entryTime);
    }
}
