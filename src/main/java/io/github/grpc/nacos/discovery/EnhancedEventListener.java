package io.github.grpc.nacos.discovery;

import com.alibaba.cloud.nacos.discovery.GatewayLocatorHeartBeatPublisher;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;

/**
 * event listener enhance
 */
public class EnhancedEventListener implements EventListener {

    private final GatewayLocatorHeartBeatPublisher gatewayLocatorHeartBeatPublisher;

    public EnhancedEventListener(GatewayLocatorHeartBeatPublisher gatewayLocatorHeartBeatPublisher) {
        this.gatewayLocatorHeartBeatPublisher = gatewayLocatorHeartBeatPublisher;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof NamingEvent && gatewayLocatorHeartBeatPublisher != null) {
            gatewayLocatorHeartBeatPublisher.publishHeartBeat();
        }
    }
}
