/*
 * Copyright 2012 Netflix, Inc.
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
package com.netflix.asgard

import com.amazonaws.services.ec2.model.InstanceType
import com.netflix.asgard.mock.Mocks
import com.netflix.asgard.model.InstanceProductType
import com.netflix.asgard.model.InstanceTypeData
import spock.lang.Specification

@SuppressWarnings("GroovyAccessibility")
class InstanceTypeServicePricingSpec extends Specification {

    void 'get instance type data'() {
        when:
        InstanceTypeService instanceTypeService = Mocks.instanceTypeService()
        List<InstanceTypeData> instanceTypes = instanceTypeService.getInstanceTypes(Mocks.userContext())

        then:
        instanceTypes.any { it.name == 'm1.large' }

        // Custom instance type from config is included
        instanceTypes.any { it.name == 'huge.mainframe' }
    }

    void 'get instance type on demand pricing data'() {
        when:
        InstanceTypeService instanceTypeService = Mocks.instanceTypeService()
        Map<Region, RegionalInstancePrices> onDemandPricing = instanceTypeService.retrieveInstanceTypeOnDemandPricing()

        // Virginia
        RegionalInstancePrices usEastOnDemandPricing = onDemandPricing.get(Region.defaultRegion())

        then:
        // Linux
        0.165 == usEastOnDemandPricing.get(InstanceType.C1Medium, InstanceProductType.LINUX_UNIX)
        0.26 == usEastOnDemandPricing.get(InstanceType.M1Large, InstanceProductType.LINUX_UNIX)
        0.52 == usEastOnDemandPricing.get(InstanceType.M1Xlarge, InstanceProductType.LINUX_UNIX)
        0.45 == usEastOnDemandPricing.get(InstanceType.M2Xlarge, InstanceProductType.LINUX_UNIX)
        0.90 == usEastOnDemandPricing.get(InstanceType.M22xlarge, InstanceProductType.LINUX_UNIX)
        1.80 == usEastOnDemandPricing.get(InstanceType.M24xlarge, InstanceProductType.LINUX_UNIX)
        2.10 == usEastOnDemandPricing.get(InstanceType.Cg14xlarge, InstanceProductType.LINUX_UNIX)
        1.30 == usEastOnDemandPricing.get(InstanceType.Cc14xlarge, InstanceProductType.LINUX_UNIX)

        // Windows
        0.285 == usEastOnDemandPricing.get(InstanceType.C1Medium, InstanceProductType.WINDOWS)
        0.46 == usEastOnDemandPricing.get(InstanceType.M1Large, InstanceProductType.WINDOWS)
        0.92 == usEastOnDemandPricing.get(InstanceType.M1Xlarge, InstanceProductType.WINDOWS)
        0.57 == usEastOnDemandPricing.get(InstanceType.M2Xlarge, InstanceProductType.WINDOWS)
        1.14 == usEastOnDemandPricing.get(InstanceType.M22xlarge, InstanceProductType.WINDOWS)
        2.28 == usEastOnDemandPricing.get(InstanceType.M24xlarge, InstanceProductType.WINDOWS)
        2.60 == usEastOnDemandPricing.get(InstanceType.Cg14xlarge, InstanceProductType.WINDOWS)
        1.61 == usEastOnDemandPricing.get(InstanceType.Cc14xlarge, InstanceProductType.WINDOWS)

        when:
        // Europe
        RegionalInstancePrices euWestOnDemandPricing = onDemandPricing.get(Region.EU_WEST_1)

        then:
        0.186 == euWestOnDemandPricing.get(InstanceType.C1Medium, InstanceProductType.LINUX_UNIX)
        0.744 == euWestOnDemandPricing.get(InstanceType.C1Xlarge, InstanceProductType.LINUX_UNIX)
        0.34 == euWestOnDemandPricing.get(InstanceType.M1Large, InstanceProductType.LINUX_UNIX)
        0.68 == euWestOnDemandPricing.get(InstanceType.M1Xlarge, InstanceProductType.LINUX_UNIX)
        1.012 == euWestOnDemandPricing.get(InstanceType.M22xlarge, InstanceProductType.LINUX_UNIX)
        2.024 == euWestOnDemandPricing.get(InstanceType.M24xlarge, InstanceProductType.LINUX_UNIX)
        2.36 == euWestOnDemandPricing.get(InstanceType.Cg14xlarge, InstanceProductType.LINUX_UNIX)
        null == euWestOnDemandPricing.get(InstanceType.Cc14xlarge, InstanceProductType.LINUX_UNIX)
    }

    void 'retrieve instance type reserve pricing data'() {
        when:
        InstanceTypeService instanceTypeService = Mocks.instanceTypeService()
        Map<Region, RegionalInstancePrices> reservedPricing = instanceTypeService.retrieveInstanceTypeReservedPricing()

        // Virginia
        RegionalInstancePrices usEastReservedPricing = reservedPricing.get(Region.defaultRegion())

        then:
        // Linux
        0.06 == usEastReservedPricing.get(InstanceType.C1Medium, InstanceProductType.LINUX_UNIX)
        0.12 == usEastReservedPricing.get(InstanceType.M1Large, InstanceProductType.LINUX_UNIX)
        0.24 == usEastReservedPricing.get(InstanceType.M1Xlarge, InstanceProductType.LINUX_UNIX)
        0.17 == usEastReservedPricing.get(InstanceType.M2Xlarge, InstanceProductType.LINUX_UNIX)
        0.34 == usEastReservedPricing.get(InstanceType.M22xlarge, InstanceProductType.LINUX_UNIX)
        0.68 == usEastReservedPricing.get(InstanceType.M24xlarge, InstanceProductType.LINUX_UNIX)
        0.74 == usEastReservedPricing.get(InstanceType.Cg14xlarge, InstanceProductType.LINUX_UNIX)

        // Windows
        0.125 == usEastReservedPricing.get(InstanceType.C1Medium, InstanceProductType.WINDOWS)
        0.20 == usEastReservedPricing.get(InstanceType.M1Large, InstanceProductType.WINDOWS)
        0.40 == usEastReservedPricing.get(InstanceType.M1Xlarge, InstanceProductType.WINDOWS)
        0.24 == usEastReservedPricing.get(InstanceType.M2Xlarge, InstanceProductType.WINDOWS)
        0.48 == usEastReservedPricing.get(InstanceType.M22xlarge, InstanceProductType.WINDOWS)
        0.96 == usEastReservedPricing.get(InstanceType.M24xlarge, InstanceProductType.WINDOWS)
        1.04 == usEastReservedPricing.get(InstanceType.Cg14xlarge, InstanceProductType.WINDOWS)

        when:
        // Europe
        RegionalInstancePrices euWestReservedPricing = reservedPricing.get(Region.EU_WEST_1)

        then:
        0.08 == euWestReservedPricing.get(InstanceType.C1Medium, InstanceProductType.LINUX_UNIX)
        0.32 == euWestReservedPricing.get(InstanceType.C1Xlarge, InstanceProductType.LINUX_UNIX)
        0.16 == euWestReservedPricing.get(InstanceType.M1Large, InstanceProductType.LINUX_UNIX)
        0.32 == euWestReservedPricing.get(InstanceType.M1Xlarge, InstanceProductType.LINUX_UNIX)
        0.48 == euWestReservedPricing.get(InstanceType.M22xlarge, InstanceProductType.LINUX_UNIX)
        0.96 == euWestReservedPricing.get(InstanceType.M24xlarge, InstanceProductType.LINUX_UNIX)
        null == euWestReservedPricing.get(InstanceType.Cg14xlarge, InstanceProductType.LINUX_UNIX)
    }
}
