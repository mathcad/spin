package org.spin.datasource.tx;

import org.spin.core.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author funkye
 */
public class TransactionContext {

    private static final ThreadLocal<Map<String, String>> CONTEXT_HOLDER = ThreadLocal.withInitial(HashMap::new);

    private static final String XID = "LOCAL_XID";

    /**
     * Gets xid.
     *
     * @return the xid
     */
    public static String getXID() {
        String xid = CONTEXT_HOLDER.get().get(XID);
        if (StringUtils.isNotEmpty(xid)) {
            return xid;
        }
        return null;
    }

    /**
     * Unbind string.
     *
     * @param xid xid
     * @return the string
     */
    public static String unbind(String xid) {
        CONTEXT_HOLDER.get().remove(xid);
        return xid;
    }

    /**
     * bind string.
     *
     * @param xid xid
     * @return the string
     */
    public static String bind(String xid) {
        CONTEXT_HOLDER.get().put(XID, xid);
        return xid;
    }

    /**
     * remove
     */
    public static void remove() {
        CONTEXT_HOLDER.remove();
    }

}
