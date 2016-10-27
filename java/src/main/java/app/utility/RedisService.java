package app.utility;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisService extends JedisPool {

    private RedisService(JedisPoolConfig config) {
        super(config, ApplicationProperties.getValue("redis.host"), ApplicationProperties.getValueInt("redis.port"));
    }

    private static JedisPool instance;

    synchronized public static JedisPool getInstance() {
    	if(instance == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(30);
    		instance = new RedisService(config);
    	}
    	return instance;
    }

}