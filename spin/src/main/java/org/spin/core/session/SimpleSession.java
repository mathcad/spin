/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.spin.core.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.CollectionUtils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 */
public class SimpleSession implements Session, Serializable {

    private static final long serialVersionUID = -7125642695178165650L;

    private transient static final Logger log = LoggerFactory.getLogger(SimpleSession.class);

    protected static final long MILLIS_PER_SECOND = 1000;
    protected static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    protected static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

    //serialization bitmask fields. DO NOT CHANGE THE ORDER THEY ARE DECLARED!
    static int bitIndexCounter = 0;
    private static final int ID_BIT_MASK = 1 << bitIndexCounter++;
    private static final int START_TIMESTAMP_BIT_MASK = 1 << bitIndexCounter++;
    private static final int STOP_TIMESTAMP_BIT_MASK = 1 << bitIndexCounter++;
    private static final int LAST_ACCESS_TIME_BIT_MASK = 1 << bitIndexCounter++;
    private static final int TIMEOUT_BIT_MASK = 1 << bitIndexCounter++;
    private static final int EXPIRED_BIT_MASK = 1 << bitIndexCounter++;
    private static final int HOST_BIT_MASK = 1 << bitIndexCounter++;
    private static final int ATTRIBUTES_BIT_MASK = 1 << bitIndexCounter++;

    // ==============================================================
    // NOTICE:
    //
    // The following fields are marked as transient to avoid double-serialization.
    // They are in fact serialized (even though 'transient' usually indicates otherwise),
    // but they are serialized explicitly via the writeObject and readObject implementations
    // in this class.
    //
    // If we didn't declare them as transient, the out.defaultWriteObject(); call in writeObject would
    // serialize all non-transient fields as well, effectively doubly serializing the fields (also
    // doubling the serialization size).
    //
    // This finding, with discussion, was covered here:
    //
    // http://mail-archives.apache.org/mod_mbox/shiro-user/201109.mbox/%3C4E81BCBD.8060909@metaphysis.net%3E
    //
    // ==============================================================
    private transient Serializable id;
    private transient LocalDateTime startTimestamp;
    private transient LocalDateTime stopTimestamp;
    private transient LocalDateTime lastAccessTime;
    private transient boolean expired;
    private transient String host;
    private transient Map<Object, Object> attributes;

    public SimpleSession() {
        this.startTimestamp = LocalDateTime.now();
        this.lastAccessTime = this.startTimestamp;
    }

    public SimpleSession(String host) {
        this();
        this.host = host;
    }

    public Serializable getId() {
        return this.id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public LocalDateTime getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(LocalDateTime startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    /**
     * Returns the time the session was stopped, or <tt>null</tt> if the session is still active.
     * <p>
     * A session may become stopped under a number of conditions:
     * </p>
     * <ul>
     * <li>If the user logs out of the system, their current session is terminated (released).</li>
     * <li>If the session expires</li>
     * <li>The application explicitly calls {@link #stop()}</li>
     * <li>If there is an internal system error and the session state can no longer accurately
     * reflect the user's behavior, such in the case of a system crash</li>
     * </ul>
     * <p>
     * Once stopped, a session may no longer be used.  It is locked from all further activity.
     * </p>
     *
     * @return The time the session was stopped, or <tt>null</tt> if the session is still
     * active.
     */
    public LocalDateTime getStopTimestamp() {
        return stopTimestamp;
    }

    public void setStopTimestamp(LocalDateTime stopTimestamp) {
        this.stopTimestamp = stopTimestamp;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public long getTimeout() {
        return 0L;
    }

    @Override
    public void setTimeout(long maxIdleTimeInMillis) {
        // do nothing
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<Object, Object> attributes) {
        this.attributes = attributes;
    }

    public void touch() {
        this.lastAccessTime = LocalDateTime.now();
    }

    public void stop() {
        if (this.stopTimestamp == null) {
            this.stopTimestamp = LocalDateTime.now();
        }
    }

    protected boolean isStopped() {
        return getStopTimestamp() != null;
    }

    protected void expire() {
        stop();
        this.expired = true;
    }

    private Map<Object, Object> getAttributesLazy() {
        Map<Object, Object> attributes = getAttributes();
        if (attributes == null) {
            attributes = new HashMap<>();
            setAttributes(attributes);
        }
        return attributes;
    }

    public Collection<Object> getAttributeKeys() {
        Map<Object, Object> attributes = getAttributes();
        if (attributes == null) {
            return Collections.emptySet();
        }
        return attributes.keySet();
    }

    public Object getAttribute(Object key) {
        Map<Object, Object> attributes = getAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.get(key);
    }

    public void setAttribute(Object key, Object value) {
        if (value == null) {
            removeAttribute(key);
        } else {
            getAttributesLazy().put(key, value);
        }
    }

    public Object removeAttribute(Object key) {
        Map<Object, Object> attributes = getAttributes();
        if (attributes == null) {
            return null;
        } else {
            return attributes.remove(key);
        }
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void validate() {
        // do nothing
    }

    /**
     * Returns {@code true} if the specified argument is an {@code instanceof} {@code SimpleSession} and both
     * {@link #getId() id}s are equal.  If the argument is a {@code SimpleSession} and either 'this' or the argument
     * does not yet have an ID assigned, the value of {@link #onEquals(SimpleSession) onEquals} is returned, which
     * does a necessary attribute-based comparison when IDs are not available.
     * <p>
     * Do your best to ensure {@code SimpleSession} instances receive an ID very early in their lifecycle to
     * avoid the more expensive attributes-based comparison.
     * </P>
     *
     * @param obj the object to compare with this one for equality.
     * @return {@code true} if this object is equivalent to the specified argument, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SimpleSession) {
            SimpleSession other = (SimpleSession) obj;
            Serializable thisId = getId();
            Serializable otherId = other.getId();
            if (thisId != null && otherId != null) {
                return thisId.equals(otherId);
            } else {
                //fall back to an attribute based comparison:
                return onEquals(other);
            }
        }
        return false;
    }

    /**
     * Provides an attribute-based comparison (no ID comparison) - incurred <em>only</em> when 'this' or the
     * session object being compared for equality do not have a session id.
     *
     * @param ss the SimpleSession instance to compare for equality.
     * @return true if all the attributes, except the id, are equal to this object's attributes.
     * @since 1.0
     */
    protected boolean onEquals(SimpleSession ss) {
        return (getStartTimestamp() != null ? getStartTimestamp().equals(ss.getStartTimestamp()) : ss.getStartTimestamp() == null) &&
            (getStopTimestamp() != null ? getStopTimestamp().equals(ss.getStopTimestamp()) : ss.getStopTimestamp() == null) &&
            (getLastAccessTime() != null ? getLastAccessTime().equals(ss.getLastAccessTime()) : ss.getLastAccessTime() == null) &&
            (getTimeout() == ss.getTimeout()) &&
            (getHost() != null ? getHost().equals(ss.getHost()) : ss.getHost() == null) &&
            (getAttributes() != null ? getAttributes().equals(ss.getAttributes()) : ss.getAttributes() == null);
    }

    /**
     * Returns the hashCode.  If the {@link #getId() id} is not {@code null}, its hashcode is returned immediately.
     * If it is {@code null}, an attributes-based hashCode will be calculated and returned.
     * <p>
     * Do your best to ensure {@code SimpleSession} instances receive an ID very early in their lifecycle to
     * avoid the more expensive attributes-based calculation.
     * </p>
     *
     * @return this object's hashCode
     * @since 1.0
     */
    @Override
    public int hashCode() {
        Serializable id = getId();
        if (id != null) {
            return id.hashCode();
        }
        int hashCode = getStartTimestamp() != null ? getStartTimestamp().hashCode() : 0;
        hashCode = 31 * hashCode + (getStopTimestamp() != null ? getStopTimestamp().hashCode() : 0);
        hashCode = 31 * hashCode + (getLastAccessTime() != null ? getLastAccessTime().hashCode() : 0);
        hashCode = 31 * hashCode + Long.valueOf(Math.max(getTimeout(), 0)).hashCode();
        hashCode = 31 * hashCode + (getHost() != null ? getHost().hashCode() : 0);
        hashCode = 31 * hashCode + (getAttributes() != null ? getAttributes().hashCode() : 0);
        return hashCode;
    }

    /**
     * Returns the string representation of this SimpleSession, equal to
     * <code>getClass().getName() + &quot;,id=&quot; + getId()</code>.
     *
     * @return the string representation of this SimpleSession, equal to
     * <code>getClass().getName() + &quot;,id=&quot; + getId()</code>.
     * @since 1.0
     */
    @Override
    public String toString() {
        return getClass().getName() + ",id=" + getId();
    }

    /**
     * Serializes this object to the specified output stream for JDK Serialization.
     *
     * @param out output stream used for Object serialization.
     * @throws IOException if any of this object's fields cannot be written to the stream.
     * @since 1.0
     */
    public void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        short alteredFieldsBitMask = getAlteredFieldsBitMask();
        out.writeShort(alteredFieldsBitMask);
        if (id != null) {
            out.writeObject(id);
        }
        if (startTimestamp != null) {
            out.writeObject(startTimestamp);
        }
        if (stopTimestamp != null) {
            out.writeObject(stopTimestamp);
        }
        if (lastAccessTime != null) {
            out.writeObject(lastAccessTime);
        }
        if (expired) {
            out.writeBoolean(true);
        }
        if (host != null) {
            out.writeUTF(host);
        }
        if (!CollectionUtils.isEmpty(attributes)) {
            out.writeObject(attributes);
        }
    }

    /**
     * Returns a bit mask used during serialization indicating which fields have been serialized. Fields that have been
     * altered (not null and/or not retaining the class defaults) will be serialized and have 1 in their respective
     * index, fields that are null and/or retain class default values have 0.
     *
     * @return a bit mask used during serialization indicating which fields have been serialized.
     * @since 1.0
     */
    private short getAlteredFieldsBitMask() {
        int bitMask = 0;
        bitMask = id != null ? bitMask | ID_BIT_MASK : bitMask;
        bitMask = startTimestamp != null ? bitMask | START_TIMESTAMP_BIT_MASK : bitMask;
        bitMask = stopTimestamp != null ? bitMask | STOP_TIMESTAMP_BIT_MASK : bitMask;
        bitMask = lastAccessTime != null ? bitMask | LAST_ACCESS_TIME_BIT_MASK : bitMask;
        bitMask = expired ? bitMask | EXPIRED_BIT_MASK : bitMask;
        bitMask = host != null ? bitMask | HOST_BIT_MASK : bitMask;
        bitMask = !CollectionUtils.isEmpty(attributes) ? bitMask | ATTRIBUTES_BIT_MASK : bitMask;
        return (short) bitMask;
    }
}
