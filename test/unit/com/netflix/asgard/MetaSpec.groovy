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

import com.amazonaws.services.autoscaling.model.AutoScalingGroup
import com.netflix.asgard.model.MetricId
import com.netflix.asgard.model.ScalingPolicyData
import com.netflix.asgard.model.ScalingPolicyData.AdjustmentType
import spock.lang.Specification

class MetaSpec extends Specification {

    void 'toMap'() {
        when:
        MetricId metricId = new MetricId('AWS/EC2', 'RotationsPerMinute')
        Map<String, ?> expected = [
                displayText: 'AWS/EC2 - RotationsPerMinute',
                namespace: 'AWS/EC2',
                metricName: 'RotationsPerMinute'
        ]

        then:
        expected == Meta.toMap(metricId)

        when:
        ScalingPolicyData scalingPolicyData = new ScalingPolicyData(
                adjustmentType: AdjustmentType.PercentChangeInCapacity, cooldown: 60, minAdjustmentStep: 3,
                autoScalingGroupName: 'helloworld-example-v122'
        )
        Map<String, ?> expectedScalingPolicyMap = [
                adjustment: null, adjustmentType: AdjustmentType.PercentChangeInCapacity, alarms: null, arn: null,
                autoScalingGroupName: 'helloworld-example-v122', cooldown: 60, minAdjustmentStep: 3,
                policyName: null
        ]

        then:
        expectedScalingPolicyMap == Meta.toMap(scalingPolicyData)
    }

    void 'pretty'() {
        expect:
        'Auto Scaling Group' == Meta.pretty(AutoScalingGroup)
        "{MaxSize: 5, AvailabilityZones: [], LoadBalancerNames: [], Instances: [], SuspendedProcesses: [], \
EnabledMetrics: [], Tags: [], TerminationPolicies: [], }".stripIndent() == Meta.
                pretty(new AutoScalingGroup().withMaxSize(5))
        'null' == Meta.pretty(null)
    }

    void 'splitCamelCase'() {
        expect:
        Meta.splitCamelCase(input) == output

        where:
        input             || output
        "lowercase"       || 'lowercase'
        "Class"           || 'Class'
        "MyClass"         || 'My Class'
        "HTML"            || 'HTML'
        "PDFLoader"       || 'PDF Loader'
        "AString"         || 'A String'
        "SimpleXMLParser" || 'Simple XML Parser'
        "GL11Version"     || 'GL 11 Version'
        "devPhase"        || 'dev Phase'
        "hardware"        || 'hardware'
        "partners"        || 'partners'
        "revision"        || 'revision'
        "usedBy"          || 'used By'
        "redBlackSwap"    || 'red Black Swap'
    }

    void 'copy'() {
        given:
        AutoScalingGroup original = new AutoScalingGroup().withAutoScalingGroupName('hello')
        AutoScalingGroup copy = Meta.copy(original)

        expect:
        original.autoScalingGroupName == copy.autoScalingGroupName
        !original.is(copy)
    }
}
