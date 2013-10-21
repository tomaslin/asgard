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

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class StylerSpec extends Specification {

    void 'availability zone to style class'() {
        expect:
        Styler.availabilityZoneToStyleClass(zone) == styledClass

        where:
        zone              || styledClass
        'us-east-1a'      || 'zoneA'
        'us-east-1b'      || 'zoneB'
        'us-east-1c'      || 'zoneC'
        'us-east-1d'      || 'zoneD'
        'us-west-1a'      || 'zoneA'
        'us-west-1b'      || 'zoneB'
        'us-west-1c'      || 'zoneC'
        'eu-west-1a'      || 'zoneA'
        'eu-west-1b'      || 'zoneB'
        'eu-west-1c'      || 'zoneC'
        'ap-southeast-1a' || 'zoneA'
        'ap-southeast-1b' || 'zoneB'
        'ap-southeast-1c' || 'zoneC'
        'ap-northeast-1a' || 'zoneA'
        'ap-northeast-1b' || 'zoneB'
        'ap-northeast-1c' || 'zoneC'
    }

    void 'availability zone returns null'() {
        expect:
        Styler.availabilityZoneToStyleClass(zone) == null

        where:
        zone         || _
        null         || _
        ''           || _
        'hello'      || _
        'us-east-1A' || _
    }

}