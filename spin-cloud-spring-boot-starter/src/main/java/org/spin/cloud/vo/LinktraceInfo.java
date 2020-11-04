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
public class LinktraceInfo implements Serializable {
    private static final long serialVersionUID = -2137151214617260626L;

    private String traceId;
    private String parentSpanId;
    private String spanId;

    public LinktraceInfo() {
    }

    public LinktraceInfo(String traceId, String parentSpanId, String spanId) {
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }
}
