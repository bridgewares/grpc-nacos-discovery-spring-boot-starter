package io.github.grpc.nacos.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * grpc nacos enable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnProperty(value = "io.github.grpc.nacos.discovery.immediate.enabled", matchIfMissing = true)
public @interface ConditionalOnGrpcNacosDiscoveryEnabled {
}
