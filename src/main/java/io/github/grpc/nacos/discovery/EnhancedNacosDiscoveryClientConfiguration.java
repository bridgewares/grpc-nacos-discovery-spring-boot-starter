package io.github.grpc.nacos.discovery;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.GatewayLocatorHeartBeatPublisher;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnBlockingDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EnhancedNacosDiscoveryClientConfiguration
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnBlockingDiscoveryEnabled
@ConditionalOnNacosDiscoveryEnabled
@ConditionalOnGrpcNacosDiscoveryEnabled
@AutoConfigureBefore(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration"})
@AutoConfigureAfter(name = {"com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration"})
public class EnhancedNacosDiscoveryClientConfiguration {

    /**
     * NacosWatch is no longer enabled by default .
     * see https://github.com/alibaba/spring-cloud-alibaba/issues/2868
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.enhanced.watch.enabled", matchIfMissing = true)
    public NacosWatch nacosWatch(NacosServiceManager nacosServiceManager,
                                 NacosDiscoveryProperties nacosDiscoveryProperties,
                                 GatewayLocatorHeartBeatPublisher gatewayLocatorHeartBeatPublisher) {
        return new EnhancedNacosWatch(nacosServiceManager, nacosDiscoveryProperties, gatewayLocatorHeartBeatPublisher);
    }

    /**
     * Spring Cloud Gateway HeartBeat .
     * publish an event every 30 seconds
     * see https://github.com/alibaba/spring-cloud-alibaba/issues/2868
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.gateway.discovery.locator.enabled", matchIfMissing = true)
    public GatewayLocatorHeartBeatPublisher gatewayLocatorHeartBeatPublisher(NacosDiscoveryProperties nacosDiscoveryProperties) {
        return new GatewayLocatorHeartBeatPublisher(nacosDiscoveryProperties);
    }
}
