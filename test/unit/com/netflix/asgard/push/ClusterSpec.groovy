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
package com.netflix.asgard.push

import com.amazonaws.services.autoscaling.model.AutoScalingGroup
import com.amazonaws.services.autoscaling.model.Instance
import com.netflix.asgard.mock.Mocks
import com.netflix.asgard.model.AutoScalingGroupData
import spock.lang.Specification

class ClusterSpec extends Specification {

    void setup() {
        Mocks.createDynamicMethods()
        Mocks.awsAutoScalingService()
    }

    void 'new cluster'() {
        given:
        AutoScalingGroupData groupOrig = makeGroupData('helloworld-example')
        AutoScalingGroupData groupv000 = makeGroupData('helloworld-example-v000')
        AutoScalingGroupData groupv001 = makeGroupData('helloworld-example-v001')
        AutoScalingGroupData groupv002 = makeGroupData('helloworld-example-v002')
        Cluster cluster = new Cluster([groupv000, groupv002, groupOrig, groupv001])

        expect:
        groupOrig.autoScalingGroupName == cluster[0].autoScalingGroupName
        groupv000.autoScalingGroupName == cluster[1].autoScalingGroupName
        groupv001.autoScalingGroupName == cluster[2].autoScalingGroupName
        groupv002.autoScalingGroupName == cluster[3].autoScalingGroupName
    }

    void 'last'() {
        given:
        AutoScalingGroupData groupOrig = makeGroupData('helloworld-example')
        AutoScalingGroupData groupv000 = makeGroupData('helloworld-example-v000')
        AutoScalingGroupData groupv001 = makeGroupData('helloworld-example-v001')
        AutoScalingGroupData groupv002 = makeGroupData('helloworld-example-v002')
        Cluster cluster = new Cluster([groupv000, groupv002, groupOrig, groupv001])

        expect:
        groupv002.autoScalingGroupName == cluster.last().autoScalingGroupName
    }

    void 'get instances'() {
        given:
        Instance deadbeef = new Instance().withInstanceId('i-deadbeef')
        Instance aaaa4444 = new Instance().withInstanceId('i-aaaa4444')
        Instance eeee9999 = new Instance().withInstanceId('i-eeee9999')

        AutoScalingGroupData groupOrig = makeGroupData('helloworld-example', [deadbeef, aaaa4444])
        AutoScalingGroupData groupv000 = makeGroupData('helloworld-example-v000')
        AutoScalingGroupData groupv001 = makeGroupData('helloworld-example-v001', [eeee9999])
        AutoScalingGroupData groupv002 = makeGroupData('helloworld-example-v002')
        Cluster cluster = new Cluster([groupv000, groupv002, groupOrig, groupv001])

        expect:
        [deadbeef, aaaa4444, eeee9999].collect { it.instanceId } == cluster.instances.collect { it.instanceId }
    }

    void 'instance ids'() {
        given:
        Instance ideadbeef = new Instance().withInstanceId('i-deadbeef')
        Instance iaaaa4444 = new Instance().withInstanceId('i-aaaa4444')
        Instance ieeee9999 = new Instance().withInstanceId('i-eeee9999')

        AutoScalingGroupData groupOrig = makeGroupData('helloworld-example', [ideadbeef, iaaaa4444])
        AutoScalingGroupData groupv000 = makeGroupData('helloworld-example-v000')
        AutoScalingGroupData groupv001 = makeGroupData('helloworld-example-v001', [ieeee9999])
        AutoScalingGroupData groupv002 = makeGroupData('helloworld-example-v002')
        Cluster cluster = new Cluster([groupv000, groupv002, groupOrig, groupv001])

        expect:
        ['i-deadbeef', 'i-aaaa4444', 'i-eeee9999'] == cluster.instances.collect { it.instanceId }
    }

    private AutoScalingGroupData makeGroupData(String name, List<Instance> instances = []) {
        AutoScalingGroupData.from(new AutoScalingGroup().withAutoScalingGroupName(name).withInstances(instances), null,
                null, null, [])
    }
}
