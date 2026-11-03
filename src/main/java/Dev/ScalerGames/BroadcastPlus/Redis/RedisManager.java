package Dev.ScalerGames.BroadcastPlus.Redis;

import Dev.ScalerGames.BroadcastPlus.Files.Engine;
import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Manages the connection to Redis.
 * - Publishes messages via JedisPool (thread-safe).
 * - Subscribes to the channel in a dedicated thread with automatic reconnection.
 */
public class RedisManager {

    private final Main plugin;
    private JedisPool pool;
    private RedisSubscriber subscriber;
    private Thread subscriberThread;
    private volatile boolean running = false;

    private final String host;
    private final int port;
    private final String password;
    private final int timeout;
    private final String channel;

    public RedisManager(Main plugin) {
        this.plugin = plugin;
        this.host     = Engine.getEngineConfig().getString("Redis.host", "localhost");
        this.port     = Engine.getEngineConfig().getInt("Redis.port", 6379);
        this.password = Engine.getEngineConfig().getString("Redis.password", "");
        this.timeout  = Engine.getEngineConfig().getInt("Redis.timeout", 2000);
        this.channel  = Engine.getEngineConfig().getString("Redis.channel", "broadcastplus");
    }

    /** Initializes the pool and starts the subscriber thread. */
    public void connect() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxIdle(5);
        config.setMinIdle(1);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);

        if (password != null && !password.isEmpty()) {
            pool = new JedisPool(config, host, port, timeout, password);
        } else {
            pool = new JedisPool(config, host, port, timeout);
        }

        // Test connection
        try (Jedis jedis = pool.getResource()) {
            jedis.ping();
            Messages.logger("&aRedis conectado com sucesso em " + host + ":" + port);
        } catch (Exception e) {
            Messages.logger("&cFalha ao conectar no Redis: " + e.getMessage());
            return;
        }

        running = true;
        startSubscriberThread();
    }

    /** Publishes a serialized message to the Redis channel. */
    public void publish(RedisMessage message) {
        if (pool == null || pool.isClosed()) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.publish(channel, message.toJson());
            } catch (Exception e) {
                Messages.logger("&cRedis publish error: " + e.getMessage());
            }
        });
    }

    /** Returns true if Redis is enabled and connected. */
    public boolean isEnabled() {
        return pool != null && !pool.isClosed() && running;
    }

    /** Closes all connections cleanly. */
    public void shutdown() {
        running = false;
        if (subscriber != null && subscriber.isSubscribed()) {
            subscriber.unsubscribe();
        }
        if (subscriberThread != null) {
            subscriberThread.interrupt();
        }
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
        Messages.logger("&eRedis desconectado.");
    }

    /** Creates and starts the subscriber thread with automatic reconnection. */
    private void startSubscriberThread() {
        subscriberThread = new Thread(() -> {
            while (running) {
                try (Jedis jedis = pool.getResource()) {
                    subscriber = new RedisSubscriber(plugin);
                    jedis.subscribe(subscriber, channel);
                } catch (Exception e) {
                    if (running) {
                        Messages.logger("&eRedis subscriber desconectado, reconectando em 5s...");
                        try { Thread.sleep(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    }
                }
            }
        }, "BroadcastPlus-Redis-Subscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();
    }
}
