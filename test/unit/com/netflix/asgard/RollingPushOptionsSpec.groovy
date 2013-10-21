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

import com.netflix.asgard.push.CommonPushOptions
import com.netflix.asgard.push.RollingPushOptions
import spock.lang.Specification

class RollingPushOptionsSpec extends Specification {

    void 'should wait after push'() {
        expect:
        shouldWait == new RollingPushOptions(common: new CommonPushOptions(afterBootWait: afterBootWait, checkHealth: checkHealth)).shouldWaitAfterBoot()

        where:
        afterBootWait | checkHealth || shouldWait
        30            | false       || true
        1             | false       || true
        30            | true        || false
        0             | false       || false
        0             | true        || false
        -5            | false       || false
        -5            | true        || false
    }
}
