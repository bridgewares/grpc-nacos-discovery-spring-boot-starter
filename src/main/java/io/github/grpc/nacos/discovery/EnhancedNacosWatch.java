package io.github.grpc.nacos.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.GatewayLocatorHeartBeatPublisher;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * nacos enhance
 */
public class EnhancedNacosWatch implements SmartLifecycle, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(EnhancedNacosWatch.class);

    private final Map<String, EventListener> listenerMap = new ConcurrentHashMap<>(16);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final NacosServiceManager nacosServiceManager;

    private final NacosDiscoveryProperties properties;

    private final GatewayLocatorHeartBeatPublisher locatorHeartBeatPublisher;


    public EnhancedNacosWatch(NacosServiceManager nacosServiceManager,
                              NacosDiscoveryProperties properties,
                              GatewayLocatorHeartBeatPublisher locatorHeartBeatPublisher) {
        this.nacosServiceManager = nacosServiceManager;
        this.properties = properties;
        this.locatorHeartBeatPublisher = locatorHeartBeatPublisher;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        this.stop();
        callback.run();
    }

    @Override
    public void start() {
        if (this.running.compareAndSet(false, true)) {

            EventListener eventListener = listenerMap.computeIfAbsent(buildKey(), event -> new EnhancedEventListener(locatorHeartBeatPublisher));

            NamingService namingService = nacosServiceManager.getNamingService();
            try {
                namingService.subscribe(properties.getService(), properties.getGroup(),
                        Arrays.asList(properties.getClusterName()), eventListener);
            } catch (Exception e) {
                log.error("namingService subscribe failed, properties:{}", properties, e);
            }

        }
    }

    private String buildKey() {
        return String.join(":", properties.getService(), properties.getGroup());
    }

    @Override
    public void stop() {
        if (this.running.compareAndSet(true, false)) {

            EventListener eventListener = listenerMap.get(buildKey());
            try {
                NamingService namingService = nacosServiceManager.getNamingService();
                namingService.unsubscribe(properties.getService(), properties.getGroup(),
                        Arrays.asList(properties.getClusterName()), eventListener);
            } catch (Exception e) {
                log.error("namingService unsubscribe failed, properties:{}", properties,
                        e);
            }
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public void destroy() {
        this.stop();
    }
}
