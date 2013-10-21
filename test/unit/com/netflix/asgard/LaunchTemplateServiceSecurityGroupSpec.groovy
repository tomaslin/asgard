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

import com.netflix.asgard.mock.Mocks
import spock.lang.Specification

class LaunchTemplateServiceSecurityGroupSpec extends Specification {

    void setup() {
        Mocks.createDynamicMethods()
    }

    void 'include default security groups'() {
        given:
        LaunchTemplateService launchTemplateService = Mocks.launchTemplateService()
        List<String> original = ["account_batch", "abcache"]
        Collection<String> result = launchTemplateService.includeDefaultSecurityGroups(original)

        expect:
        !original.contains("nf-infrastructure")
        !original.contains("nf-datacenter")
        result.contains("nf-infrastructure")
        result.contains("nf-datacenter")
        result.contains("account_batch")
        result.contains("abcache")
        result.size() == 4
    }

    void 'include default security groups without duplication'() {
        given:
        LaunchTemplateService launchTemplateService = Mocks.launchTemplateService()
        List<String> original = ["account_batch", "nf-infrastructure"]
        Collection<String> result = launchTemplateService.includeDefaultSecurityGroups(original)

        expect:
        original.contains("nf-infrastructure")
        !original.contains("nf-datacenter")
        result.contains("nf-infrastructure")
        result.contains("nf-datacenter")
        result.contains("account_batch")
        result.size() == 3
    }
}
