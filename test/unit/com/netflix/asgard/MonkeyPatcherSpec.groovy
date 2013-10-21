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

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.ec2.model.Image
import com.amazonaws.services.ec2.model.Tag
import com.netflix.asgard.mock.Mocks
import spock.lang.Specification

class MonkeyPatcherSpec extends Specification {

    void setup() {
        Mocks.monkeyPatcherService().afterPropertiesSet()
    }

    void 'add class name to String output for AmazonServiceException'() {
        given:
        AmazonServiceException ase = new AmazonServiceException('Bad things happened')
        ase.errorCode = 'Throttling'
        ase.requestId = '45678'
        ase.statusCode = 400
        ase.serviceName = 'AutoScaling'

        expect:
        'AmazonServiceException: Status Code: 400, AWS Service: AutoScaling, AWS Request ID: 45678, AWS Error Code: Throttling, AWS Error Message: Bad things happened' == ase.toString()
    }

    void 'test asg instance copy'() {
        given:
        com.amazonaws.services.autoscaling.model.Instance asgInstance =
                new com.amazonaws.services.autoscaling.model.Instance(
                        availabilityZone: 'us-east-1a',
                        instanceId: 'i-deadbeef',
                        lifecycleState: 'InService',
                        healthStatus: 'healthy',
                        launchConfigurationName: 'superterrifichappyhour'
                )

        com.amazonaws.services.autoscaling.model.Instance asgInstanceCopy = asgInstance.copy()

        expect:
        !asgInstanceCopy.is(asgInstance)
        with(asgInstanceCopy) {
            availabilityZone == 'us-east-1a'
            instanceId == 'i-deadbeef'
            lifecycleState == 'InService'
            healthStatus == 'healthy'
            launchConfigurationName == 'superterrifichappyhour'
        }
    }

    void 'ec2 instance tag getters'() {
        given:
        com.amazonaws.services.ec2.model.Instance ec2Instance =
                new com.amazonaws.services.ec2.model.Instance(
                        instanceId: 'i-deadbeef',
                ).withTags(new Tag('app', 'helloworld'), new Tag('owner', 'dboreanaz'))

        expect:
        'dboreanaz' == ec2Instance.owner
        'helloworld' == ec2Instance.app
    }

    @SuppressWarnings("GroovyAssignabilityCheck")
    void testImageKeepForever() {
        when:
        Image image = new Image()

        then:
        !image.keepForever

        when:
        image.tags = [new Tag('expiration_time', 'the future')]

        then:
        !image.keepForever

        when:
        image.tags = [new Tag('expiration_time', 'never')]

        then:
        image.keepForever
    }
}
