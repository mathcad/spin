package org.spin.core.session;

import org.junit.jupiter.api.Test;
import org.spin.core.util.SerializeUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2018/10/27</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class SessionTest {

    @Test
    public void testSerialized() {
        Session session = new SimpleSession();
        session.touch();
        session.setAttribute("aaa", "a");
        byte[] serialize = SerializeUtils.serialize(session);
        session = SerializeUtils.deserialize(serialize);


        System.out.println(session.getAttribute("aaa"));
    }
}
