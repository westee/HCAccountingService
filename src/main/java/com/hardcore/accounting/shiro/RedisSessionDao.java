package com.hardcore.accounting.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class RedisSessionDao extends CachingSessionDAO {
    private static final long TIMEOUT = 30;
    private static final String SESSION_KEY_FORMAT = "xxx_session_%s";
    private final RedisTemplate<String, Object> sessionRedisTemplate;

    public RedisSessionDao(@Qualifier("sessionRedisTemplate") RedisTemplate<String, Object> sessionRedisTemplate) {
        this.sessionRedisTemplate = sessionRedisTemplate;
        setCacheManager(new AbstractCacheManager() {
            @Override
            protected Cache<Serializable, Session> createCache(String name) throws CacheException {
                return new MapCache<Serializable, Session>(name, new ConcurrentHashMap<Serializable, Session>());
            }
        });
    }

    @Override
    protected void doUpdate(Session session) {
        BoundValueOperations<String, Object> sessionValueOperations = sessionRedisTemplate
                .boundValueOps(sessionKeyGenerator(session.getId().toString()));
        sessionValueOperations.set(session);
        sessionValueOperations.expire(TIMEOUT, TimeUnit.MINUTES);
    }

    @Override
    protected void doDelete(Session session) {
        sessionRedisTemplate.delete(sessionKeyGenerator(session.getId().toString()));
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        BoundValueOperations<String, Object> stringObjectBoundValueOperations = sessionRedisTemplate.
                boundValueOps(sessionKeyGenerator(session.getId().toString()));
        stringObjectBoundValueOperations.set(session, TIMEOUT, TimeUnit.MINUTES);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
//        Session session = super.doReadSession(sessionId);
//        if (session == null) {
        String sessionKey = sessionKeyGenerator(sessionId.toString());
        BoundValueOperations<String, Object> sessionValue = sessionRedisTemplate.boundValueOps(sessionKey);
        Session session = (Session) sessionValue.get();
//        }
        return session;
    }

    private String sessionKeyGenerator(String sessionId) {
        return String.format(SESSION_KEY_FORMAT, sessionId);
    }

}
