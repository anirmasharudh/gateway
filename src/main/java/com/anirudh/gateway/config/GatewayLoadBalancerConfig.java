package com.anirudh.gateway.config;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Configuration;

@Configuration
@LoadBalancerClient(name = "payment-service", configuration = PaymentServiceLbConfig.class)
public class GatewayLoadBalancerConfig {
}
