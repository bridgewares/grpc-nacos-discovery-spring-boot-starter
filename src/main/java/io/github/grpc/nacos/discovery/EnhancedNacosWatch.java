package io.github.grpc.nacos.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.GatewayLocatorHeartBeatPublisher;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * nacos enhance
 */
public class EnhancedNacosWatch extends NacosWatch {

    private final static Logger logger = Logger.getLogger(EnhancedNacosWatch.class.getName());

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final NacosServiceManager nacosServiceManager;

    private final NacosDiscoveryProperties properties;

    private final GatewayLocatorHeartBeatPublisher locatorHeartBeatPublisher;

    private volatile Field cachedListenerMapField;

    public EnhancedNacosWatch(NacosServiceManager nacosServiceManager, NacosDiscoveryProperties properties, GatewayLocatorHeartBeatPublisher locatorHeartBeatPublisher) {

        super(nacosServiceManager, properties);

        this.nacosServiceManager = nacosServiceManager;
        this.properties = properties;
        this.locatorHeartBeatPublisher = locatorHeartBeatPublisher;
    }

    @Override
    public void start() {
        super.start();

        if (this.running.compareAndSet(false, true)) {
            try {
                Field listenerMapField = getListenerMapField();
                Map<String, EventListener> listenerMap = getListenerMap(listenerMapField);
                if (listenerMap == null) {
                    return;
                }
                String key = buildKey();
                EventListener originalListener = listenerMap.get(key);
                if (originalListener == null || originalListener instanceof EnhancedEventListener) {
                    return;
                }
                EnhancedEventListener enhancedEventListener = new EnhancedEventListener(originalListener, locatorHeartBeatPublisher);
                boolean replaced = listenerMap.replace(key, originalListener, enhancedEventListener);
                if (!replaced) {
                    logger.warning("Listener was modified concurrently, enhancement skipped");
                    return;
                }
                replaceListenerMap(listenerMapField, listenerMap);

                NamingService namingService = nacosServiceManager.getNamingService();

                namingService.subscribe(properties.getService(), properties.getGroup(),
                        Collections.singletonList(properties.getClusterName()), enhancedEventListener);
                namingService.unsubscribe(properties.getService(), properties.getGroup(),
                        Collections.singletonList(properties.getClusterName()), originalListener);
                logger.info("Successfully enhanced NacosWatch listener for key: " + key);

            } catch (Exception e) {
                logger.log(Level.SEVERE, "enhanced nacosWatch Failed", e);
            }
        }
    }

    private String buildKey() {
        return String.join(":", properties.getService(), properties.getGroup());
    }

    private Field getListenerMapField() throws NoSuchFieldException {
        if (cachedListenerMapField == null) {
            synchronized (this) {
                if (cachedListenerMapField == null) {
                    Field field = NacosWatch.class.getDeclaredField("listenerMap");
                    field.setAccessible(true);
                    cachedListenerMapField = field;
                }
            }
        }
        return cachedListenerMapField;
    }

    @SuppressWarnings("unchecked")
    private Map<String, EventListener> getListenerMap(Field field) throws IllegalArgumentException, IllegalAccessException {
        Object obj = field.get(this);
        return (obj instanceof ConcurrentHashMap) ? (ConcurrentHashMap<String, EventListener>) obj : null;
    }

    private void replaceListenerMap(Field field, Map<String, EventListener> listenerMap) throws IllegalArgumentException, IllegalAccessException {
        field.set(this, listenerMap);
    }
}
