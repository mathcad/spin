package org.infrastructure.shiro;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.infrastructure.jpa.core.AUser;
import org.infrastructure.redis.ICached;
import org.infrastructure.sys.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * redis Session存储
 *
 * @author xuweinan
 * @version V1.0
 */
public class RedisShiroSessionDao extends AbstractSessionDAO {
    static Logger logger = LoggerFactory.getLogger(RedisShiroSessionDao.class.getName());
    private String sessionprefix="ss-";
    private ICached<Session> cached;

    public RedisShiroSessionDao (){
    }

    Date lastSyncTime=null;

    @Override
    public void update(Session session) throws UnknownSessionException {
        try {
            //未同步或者10s前同步的，执行同步（防止频繁同步）
//			if(lastSyncTime==null || DateUtils.addSeconds(lastSyncTime,10).before(new Date()){
//			}

            if(this.sessListener!=null){
                try{
                    this.sessListener.beforeUpdateSession(session);
                }catch(Exception e){
                    logger.error("",e);
                }
            }

            String sessionId = session.getId().toString();
            long timeout = session.getTimeout()/1000 ;
            cached.put(sessionId, session);
            cached.setExpire(sessionId, timeout);

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

    }

    /**
     *
     * session处理监听器
     *
     * @author zx
     * @create 2016年6月13日 下午2:32:43
     */
    public static interface SessionListener{

        void beforeUpdateSession(Session session);

        void beforeDeleteSession(Session session);
    }

    SessionListener sessListener;

    /**
     * sess监听器
     *
     * @param sessListener
     */
    public void setSessListener(SessionListener sessListener) {
        this.sessListener = sessListener;
    }

    @Override
    public void delete(Session session) {
        try {
            if(this.sessListener!=null){
                try{
                    this.sessListener.beforeDeleteSession(session);
                    AUser user = session.getAttribute(Constants.UserContant.SESSION_KEY) == null ? null : (AUser) session.getAttribute(Constants.UserContant.SESSION_KEY);
                    if(user !=null){

                    }
                }catch(Exception e){
                    logger.error("",e);
                }
            }

            cached.deleteCached(session.getId().toString());
            logger.info("delete session:" + session.getId());
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

    }

    @Override
    public Collection<Session> getActiveSessions() {
        String keys=sessionprefix+"*";
        Set<Session> list = null;
        try {
            list =  cached.getKeys(keys);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return list;
    }

    @Override
    public Serializable doCreate(Session session) {
        Serializable sessionId=session.getId();
        try {
            super.assignSessionId(session,sessionprefix+ super.generateSessionId(session));
            update(session);
            sessionId=session.getId();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return sessionId;

    }

    @Override
    public Session doReadSession(Serializable sessionId) {
        Session session=null;
        try {
            session = (Session)cached.get(sessionId.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return session;
    }

    public ICached<Session> getCached() {
        return cached;
    }

    public void setCached(ICached<Session> cached) {
        this.cached = cached;
    }

    public String getSessionprefix() {
        return sessionprefix;
    }

    public void setSessionprefix(String sessionprefix) {
        this.sessionprefix = sessionprefix;
    }

}