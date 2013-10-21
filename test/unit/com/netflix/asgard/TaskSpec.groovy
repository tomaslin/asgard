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
class TaskSpec extends Specification {

    void 'generates proper duration string'() {
        given:
        Task task = new Task(startTime: new Date(startTime), updateTime: new Date(updateTime), status: "completed")

        expect:
        task.durationString == expectedDuration

        where:
        startTime     | updateTime    || expectedDuration
        1290061831444 | 1290066308555 || "1h 14m 37s"
        1290066320888 | 1290066324475 || "3s"
        1290066324444 | 1290239124555 || "2d"
        1290329325444 | 1290329325444 || "0s"
        1290329325444 | 1290329325555 || "0s"
        1290239124333 | 1290239315222 || "3m 10s"
        1290239315666 | 1290242925333 || "1h 9s"
        1290239315666 | 1290329325555 || "1d 1h 9s"
    }

    void 'generates proper summary'() {
        given:
        Task task = new Task(name: 'Create ASG helloworld-v001', env: 'prod',
                status: 'completed', userContext: new UserContext(region: Region.US_WEST_2, username: username,
                clientHostName: 'laptop-hsimpson'))

        expect:
        task.summary == summary

        where:
        username   || summary
        'hsimpson' || 'Asgard task completed in prod us-west-2 by hsimpson: Create ASG helloworld-v001'
        null       || 'Asgard task completed in prod us-west-2 by laptop-hsimpson: Create ASG helloworld-v001'

    }
}
