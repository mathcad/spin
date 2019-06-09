package org.spin.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群与单机选择类
 * @author YIJIUE
 */
public class RedisAloneClusterConfig {

    @Autowired
    private RedisProperties rp;

    @Autowired
    private Environment environment;

    /**
     * 默认使用集群配置 单机与集群可无缝切换
     * @return RedisClusterConfiguration
     */
    @Bean
    public RedisClusterConfiguration redisCluster(){
        String property = environment.getProperty("spring.redis.nodes");
        if (!StringUtils.isEmpty(property)) {
            String[] split = property.split(",");
            if (split.length < 2) {
                String[] ipAndPort = split[0].split(":");
                rp.setHost(ipAndPort[0]);
                rp.setPort(Integer.parseInt(ipAndPort[1]));
            } else {
                List<RedisNode> nodes = new ArrayList<>();
                for (String ipPort : split) {
                    String[] ipPortPair = ipPort.split(":");
                    RedisNode redisNode = new RedisNode(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim()));
                    nodes.add(redisNode);
                }
                RedisClusterConfiguration rc = new RedisClusterConfiguration();
                rc.setClusterNodes(nodes);
                return rc;
            }
        }
        return null;
    }

}
