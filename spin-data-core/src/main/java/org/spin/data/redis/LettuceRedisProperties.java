package org.spin.data.redis;

import java.time.Duration;
import java.util.List;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/10/19</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class LettuceRedisProperties {

    /**
     * enable or disable frame features based on redis
     */
    private Boolean enable = false;

    /**
     * Database index used by the connection factory.
     */
    private int database = 0;

    /**
     * Redis server host.
     */
    private String host = "localhost";

    /**
     * Redis server port.
     */
    private int port = 6379;

    /**
     * Login username of the redis server.
     */
    private String username = "";

    /**
     * Login password of the redis server.
     */
    private String password = "";

    /**
     * Whether to enable SSL support.
     */
    private boolean ssl;

    /**
     * Read timeout.
     */
    private Duration timeout = Duration.ofSeconds(60);

    /**
     * Connection timeout.
     */
    private Duration connectTimeout;

    /**
     * Client name to be set on connections with CLIENT SETNAME.
     */
    private String clientName = "";

    private Cluster cluster;

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Cluster properties.
     */
    public static class Cluster {

        /**
         * Comma-separated list of "host:port" pairs to bootstrap from. This represents an
         * "initial" list of cluster nodes and is required to have at least one entry.
         */
        private List<String> nodes;

        /**
         * Maximum number of redirects to follow when executing commands across the
         * cluster.
         */
        private Integer maxRedirects = 5;

        public List<String> getNodes() {
            return this.nodes;
        }

        public void setNodes(List<String> nodes) {
            this.nodes = nodes;
        }

        public Integer getMaxRedirects() {
            return this.maxRedirects;
        }

        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }
    }
}
