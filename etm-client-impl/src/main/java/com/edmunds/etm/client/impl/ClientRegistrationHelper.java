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
import com.edmunds.etm.common.thrift.HostAddressDto;
import com.edmunds.etm.common.thrift.MavenModuleDto;
import com.edmunds.etm.common.thrift.ServiceConfigDto;
import com.edmunds.etm.common.thrift.ServiceProviderDto;
import com.edmunds.etm.common.thrift.ServiceType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class that is created inside the spring context.
 */
@Component
public class ClientRegistrationHelper {

    private static final String PORT_PROPERTY = "etm.web.port";

    private final ClientManager clientManager;
    private final ListenPortDetectorFactory listenPortDetectorFactory;

    @Autowired
    public ClientRegistrationHelper(
            ClientManager clientManager, ListenPortDetectorFactory listenPortDetectorFactory) {
        this.clientManager = clientManager;
        this.listenPortDetectorFactory = listenPortDetectorFactory;
    }

    public void register(String serverInfo, String ipAddress, String contextPath, ClientConfigDto configDto) {

        final HostAddressDto hostAddress = createHostAddress(serverInfo, ipAddress);
        configDto.setHostAddress(hostAddress);
        configDto.setContextPath(contextPath);

        final MavenModuleDto mavenModule = configDto.getMavenModule();
        final ServiceConfigDto serviceConfigDto = new ServiceConfigDto();

        serviceConfigDto.setServiceName(mavenModule.getGroupId() + ":" + mavenModule.getArtifactId());
        serviceConfigDto.setServiceType(ServiceType.MAVEN_WEB_APP);
        serviceConfigDto.setMavenModule(mavenModule);

        final ServiceProviderDto serviceProviderDto = new ServiceProviderDto();

        serviceProviderDto.setHostAddress(hostAddress);
        serviceProviderDto.setServiceConfig(serviceConfigDto);

        configDto.addToServiceProviders(serviceProviderDto);

        clientManager.register(configDto);
    }

    private HostAddressDto createHostAddress(String serverInfo, String ipAddress) {
        final String listenPortOverride = System.getProperty(PORT_PROPERTY);

        final int listenPort;
        if (StringUtils.isNumeric(listenPortOverride)) {
            listenPort = Integer.parseInt(listenPortOverride);
        } else {
            // Lookup the listen port.
            listenPort = listenPortDetectorFactory.getListenPortDetector(serverInfo).getListenPort();
        }

        return new HostAddressDto(ipAddress, listenPort);
    }
}
