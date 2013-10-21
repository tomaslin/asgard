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
package com.netflix.asgard.model

import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class AutoScalingGroupHealthCheckTypeSpec extends Specification {

    void 'ensure types are valid'() {
        expect:
        AutoScalingGroupHealthCheckType.ensureValidType(providedType) == expectedType

        where:
        providedType || expectedType
        null         || 'EC2'
        ''           || 'EC2'
        ' '          || 'EC2'
        'EC2'        || 'EC2'
        'ELB'        || 'ELB'
        'ELB '       || 'EC2'
        'elb'        || 'EC2'
        'nonsense'   || 'EC2'
    }

    void 'get types via by'() {
        expect:
        AutoScalingGroupHealthCheckType.by(providedType) == expectedType

        where:
        providedType || expectedType
        'ELB'        || AutoScalingGroupHealthCheckType.ELB
        'EC2'        || AutoScalingGroupHealthCheckType.EC2
        'blah'       || null
        ''           || null
        null         || null
    }
}
