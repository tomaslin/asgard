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
class RegionSpec extends Specification {

    void 'withCode'() {
        expect:
        Region.withCode(providedCode) == expectedCode

        where:
        providedCode    || expectedCode
        'us-east-1'     || Region.US_EAST_1
        'us-west-1'     || Region.US_WEST_1
        'us-east'       || null
        'blah'          || null
        ''              || null
        null            || null
        '  us-east-1  ' || null
    }

    void "withCode codes"() {
        expect:
        code == Region.withCode(code).code

        where:
        code << ['us-west-1', 'eu-west-1']
    }

    void 'withPricingJsonCode'() {
        expect:
        Region.withPricingJsonCode(code) == expectedCode

        where:
        code          || expectedCode
        'us-east'     || Region.US_EAST_1
        'us-west'     || Region.US_WEST_1
        'eu-ireland'  || Region.EU_WEST_1
        'apac-tokyo'  || Region.AP_NORTHEAST_1
        'apac-sin'    || Region.AP_SOUTHEAST_1
        'us-east-1'   || null
        'blah'        || null
        ''            || null
        null          || null
        '  us-east  ' || null
    }

    void "withPricingJsonCode codes"() {
        expect:
        jsonCode == Region.withPricingJsonCode(code).code

        where:
        code         || jsonCode
        'us-west'    || 'us-west-1'
        'eu-ireland' || 'eu-west-1'
    }
}