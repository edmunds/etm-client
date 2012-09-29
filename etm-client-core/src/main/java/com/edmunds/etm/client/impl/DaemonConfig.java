/*
 * Copyright 2011 Edmunds.com, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.edmunds.etm.client.impl;

import com.edmunds.etm.common.thrift.ClientConfigDto;
import com.edmunds.etm.common.thrift.DnsConfigDto;
import com.edmunds.etm.common.thrift.HostAddressDto;
import com.edmunds.etm.common.thrift.ServiceConfigDto;
import com.edmunds.etm.common.thrift.ServiceProviderDto;
import com.edmunds.etm.common.thrift.ServiceType;
import com.edmunds.etm.common.thrift.VipConfigDto;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Configuration for the daemon process.
 */
@Component
public class DaemonConfig {

    private String serviceName;
    private ServiceType serviceType;

    private String host;
    private int port;

    private String vipBaseName;
    private int vipPort;
    private boolean vipAutoCreate;
    private boolean vipAutoDelete;

    private String fullDomainName;

    public DaemonConfig() {
        this.serviceType = ServiceType.RAW_VIP;
        this.host = getIpAddress();
        this.port = 80;
        this.vipPort = 80;
        this.vipAutoCreate = true;
        this.vipAutoDelete = true;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType == null ? null : serviceType.name();
    }

    public void setServiceType(String serviceType) {
        this.serviceType = ServiceType.valueOf(serviceType);
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

    public String getVipBaseName() {
        return vipBaseName;
    }

    public void setVipBaseName(String vipBaseName) {
        this.vipBaseName = vipBaseName;
    }

    public int getVipPort() {
        return vipPort;
    }

    public void setVipPort(int vipPort) {
        this.vipPort = vipPort;
    }

    public boolean isVipAutoCreate() {
        return vipAutoCreate;
    }

    public void setVipAutoCreate(boolean vipAutoCreate) {
        this.vipAutoCreate = vipAutoCreate;
    }

    public boolean isVipAutoDelete() {
        return vipAutoDelete;
    }

    public void setVipAutoDelete(boolean vipAutoDelete) {
        this.vipAutoDelete = vipAutoDelete;
    }

    public String getFullDomainName() {
        return fullDomainName;
    }

    public void setFullDomainName(String fullDomainName) {
        this.fullDomainName = fullDomainName;
    }

    public void validate() {
        Validate.isTrue(StringUtils.isNotBlank(serviceName), "daemonConfig.serviceName cannot be blank");

        switch (serviceType) {
            case MAVEN_WEB_APP:
                throw new IllegalStateException("The ETM Daemon does not support MAVEN_WEB_APP");
            case RAW_VIP:
                break;
            case DNS_VIP:
                Validate.isTrue(StringUtils.isNotBlank(fullDomainName), "daemonConfig.fullDomainName cannot be blank");
                break;
            default:
                throw new IllegalStateException("Unknown daemonConfig.serviceType: " + serviceType);
        }
    }

    public ClientConfigDto buildClientConfig() {
        validate();

        final ClientConfigDto clientConfig = new ClientConfigDto();

        clientConfig.setHostAddress(buildHostAddress());
        clientConfig.setContextPath("/");
        // Ignore Maven Module
        // Ignore urlRules
        // Ignore httpMonitor
        clientConfig.addToServiceProviders(buildServiceProvider());

        return clientConfig;
    }

    private HostAddressDto buildHostAddress() {
        final HostAddressDto hostAddress = new HostAddressDto();

        hostAddress.setHost(host);
        hostAddress.setPort(port);

        return hostAddress;
    }

    private ServiceProviderDto buildServiceProvider() {
        final ServiceProviderDto serviceProvider = new ServiceProviderDto();

        serviceProvider.setHostAddress(buildHostAddress());
        serviceProvider.setServiceConfig(buildServiceConfig());

        return serviceProvider;
    }

    private ServiceConfigDto buildServiceConfig() {
        final ServiceConfigDto serviceConfig = new ServiceConfigDto();

        serviceConfig.setServiceName(serviceName);
        serviceConfig.setServiceType(serviceType);
        // Ignore Maven Module
        serviceConfig.setVipConfig(buildVipConfig());
        serviceConfig.setDnsConfig(buildDnsConfig());

        return serviceConfig;
    }

    private VipConfigDto buildVipConfig() {
        final VipConfigDto vipConfig = new VipConfigDto();

        vipConfig.setBaseName(
                serviceType != ServiceType.MAVEN_WEB_APP && StringUtils.isBlank(vipBaseName) ?
                        serviceName :
                        vipBaseName);

        vipConfig.setPort(vipPort);
        vipConfig.setAutoCreate(vipAutoCreate);
        vipConfig.setAutoDelete(vipAutoDelete);

        return vipConfig;
    }

    private DnsConfigDto buildDnsConfig() {
        final DnsConfigDto dnsConfig = new DnsConfigDto();

        dnsConfig.setFullDomainName(fullDomainName);

        return dnsConfig;
    }

    private static String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
