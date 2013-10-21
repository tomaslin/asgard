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
import com.netflix.asgard.mock.Mocks
import com.netflix.asgard.model.AutoScalingGroupData
import com.netflix.frigga.Names
import com.netflix.frigga.ami.AppVersion
import org.joda.time.DateTime
import spock.lang.Specification

@SuppressWarnings("GroovyAccessibility")
class RelationshipsSpec extends Specification {

    void setup() {
        Mocks.createDynamicMethods()
    }

    private void assertPushSequenceSortResult(List<String> expectedResult, List<String> input) {
        assert expectedResult == input.collect {
            AutoScalingGroupData.from(new AutoScalingGroup().withAutoScalingGroupName(it), null, null, null, [])
        }.sort(Relationships.PUSH_SEQUENCE_COMPARATOR).collect {
            it.autoScalingGroupName
        }
    }

    void 'push sequence comparator'() {

        given:
        Mocks.awsAutoScalingService()

        expect:
        assertPushSequenceSortResult(["discovery-dev",
                "discovery-dev-v997",
                "discovery-dev-v998",
                "discovery-dev-v999",
                "discovery-dev-v000",
                "discovery-dev-v001",
                "discovery-dev-v002",
                "discovery-dev-v003"
        ], [
                "discovery-dev-v997",
                "discovery-dev-v003",
                "discovery-dev-v999",
                "discovery-dev-v001",
                "discovery-dev",
                "discovery-dev-v998",
                "discovery-dev-v002",
                "discovery-dev-v000"
        ])

        assertPushSequenceSortResult(["discovery-dev", "discovery-dev-v000"], ["discovery-dev", "discovery-dev-v000"])
        assertPushSequenceSortResult(["discovery-dev", "discovery-dev-v000"], ["discovery-dev-v000", "discovery-dev"])
        assertPushSequenceSortResult(
                ["discovery-dev", "discovery-dev-v000", "discovery-dev-v001"],
                ["discovery-dev-v001", "discovery-dev", "discovery-dev-v000"])
        assertPushSequenceSortResult([
                "discovery-dev-v001", "discovery-dev-v002"],
                ["discovery-dev-v001", "discovery-dev-v002"])
        assertPushSequenceSortResult(
                ["discovery-dev-v001", "discovery-dev-v002"],
                ["discovery-dev-v002", "discovery-dev-v001"])
        assertPushSequenceSortResult(
                ["discovery-dev-v563", "discovery-dev-v564", "discovery-dev-v565"],
                ["discovery-dev-v563", "discovery-dev-v565", "discovery-dev-v564"])
        assertPushSequenceSortResult(
                ["discovery-dev-v998", "discovery-dev-v999", "discovery-dev-v000"],
                ["discovery-dev-v000", "discovery-dev-v998", "discovery-dev-v999"])
        assertPushSequenceSortResult(
                ["discovery-dev-v998", "discovery-dev-v999", "discovery-dev-v000"],
                ["discovery-dev-v000", "discovery-dev-v999", "discovery-dev-v998"])
        assertPushSequenceSortResult(
                ["discovery-dev-v999", "discovery-dev-v000", "discovery-dev-v001"],
                ["discovery-dev-v000", "discovery-dev-v999", "discovery-dev-v001"])
    }

    void 'build next auto scaling group name'() {
        expect:
        Relationships.buildNextAutoScalingGroupName(previousGroup) == nextGroup

        where:
        previousGroup        || nextGroup
        "discovery-dev"      || "discovery-dev-v000"
        "discovery-dev-v999" || "discovery-dev-v000"
        "discovery-dev-v998" || "discovery-dev-v999"
        "discovery-dev-v997" || "discovery-dev-v998"
        "discovery-dev-v000" || "discovery-dev-v001"
        "discovery-dev-v001" || "discovery-dev-v002"
        "discovery-dev-v002" || "discovery-dev-v003"
        "discovery-dev-v521" || "discovery-dev-v522"
    }

    void 'dissect group name with dot'() {
        given:
        Names names = Relationships.dissectCompoundName("chukwa.collector_1-v889")

        expect:
        with(names) {
            group == "chukwa.collector_1-v889"
            cluster == "chukwa.collector_1"
            app == "chukwa.collector_1"
            stack == null
            detail == null
            push == "v889"
            sequence == 889
        }
    }

    void 'dissect group name that is invalid'() {
        given:
        Names names = Relationships.dissectCompoundName('nccp-moviecontrol%27')

        expect:
        with(names) {
            group == null
            cluster == null
            app == null
            stack == null
            detail == null
            push == null
            sequence == null
        }
    }

    void 'dissect compound name'() {
        given:
        Names names = Relationships.dissectCompoundName(groupName)

        expect:
        with(names) {
            group == expectedGroup
            cluster == expectedCluster
            app == expectedApp
            stack == expectedStack
            detail == expectedDetail
            push == expectedPush
            sequence == expectedSequence
        }

        where:
        groupName                                     | expectedGroup                                 | expectedCluster                               | expectedApp         | expectedStack | expectedDetail        | expectedPush | expectedSequence
        null                                          | null                                          | null                                          | null                | null          | null                  | null         | null
        "actiondrainer"                               | "actiondrainer"                               | "actiondrainer"                               | "actiondrainer"     | null          | null                  | null         | null
        "actiondrainer-v003"                          | "actiondrainer-v003"                          | "actiondrainer"                               | "actiondrainer"     | null          | null                  | "v003"       | 3
        "actiondrainer--v003"                         | "actiondrainer--v003"                         | "actiondrainer-"                              | "actiondrainer"     | null          | null                  | "v003"       | 3
        "actiondrainer---v003"                        | "actiondrainer---v003"                        | "actiondrainer--"                             | "actiondrainer"     | null          | null                  | "v003"       | 3
        "api-test-A"                                  | "api-test-A"                                  | "api-test-A"                                  | "api"               | "test"        | "A"                   | null         | null
        "api-test-A-v406"                             | "api-test-A-v406"                             | "api-test-A"                                  | "api"               | "test"        | "A"                   | "v406"       | 406
        "api-test101"                                 | "api-test101"                                 | "api-test101"                                 | "api"               | "test101"     | null                  | null         | null
        "chukwacollector_1"                           | "chukwacollector_1"                           | "chukwacollector_1"                           | "chukwacollector_1" | null          | null                  | null         | null
        "chukwacollector_1-v889"                      | "chukwacollector_1-v889"                      | "chukwacollector_1"                           | "chukwacollector_1" | null          | null                  | "v889"       | 889
        "api-test-A"                                  | "api-test-A"                                  | "api-test-A"                                  | "api"               | "test"        | "A"                   | null         | null
        "discovery-dev"                               | "discovery-dev"                               | "discovery-dev"                               | "discovery"         | "dev"         | null                  | null         | null
        "discovery-us-east-1d"                        | "discovery-us-east-1d"                        | "discovery-us-east-1d"                        | "discovery"         | "us"          | "east-1d"             | null         | null
        "evcache-us-east-1d-0"                        | "evcache-us-east-1d-0"                        | "evcache-us-east-1d-0"                        | "evcache"           | "us"          | "east-1d-0"           | null         | null
        "evcache-us-east-1d-0-v223"                   | "evcache-us-east-1d-0-v223"                   | "evcache-us-east-1d-0"                        | "evcache"           | "us"          | "east-1d-0"           | "v223"       | 223
        "videometadata-navigator-integration-240-CAN" | "videometadata-navigator-integration-240-CAN" | "videometadata-navigator-integration-240-CAN" | "videometadata"     | "navigator"   | "integration-240-CAN" | null         | null

    }

    void 'dissect group names with label variables'() {
        given:
        Names names = Relationships.dissectCompoundName(compoundName)

        expect:
        with(names) {
            group == expectedGroup
            cluster == expectedCluster
            app == expectedApp
            stack == expectedStack
            detail == expectedDetail
            push == expectedPush
            sequence == expectedSequence
            countries == expectedCountries
            devPhase == expectedDevPhase
            hardware == expectedHardware
            partners == expectedPartners
            revision == expectedRevision
            usedBy == expectedUsedBy
            redBlackSwap == expectedRedBlackSwap
            zone == expectedZone
        }

        where:
        compoundName                                                                                                   | expectedGroup                                                                                                  | expectedCluster                                                                                           | expectedApp     | expectedStack     | expectedDetail | expectedPush | expectedSequence | expectedCountries | expectedDevPhase | expectedHardware | expectedPartners | expectedRevision | expectedUsedBy | expectedRedBlackSwap | expectedZone
        "actiondrainer"                                                                                                | "actiondrainer"                                                                                                | "actiondrainer"                                                                                           | "actiondrainer" | null              | null           | null         | null             | null              | null             | null             | null             | null             | null           | null                 | null
        'cass-nccpintegration-random-junk-c0northamerica-d0prod-h0gamesystems-p0vizio-r027-u0nccp-w0A-z0useast1a-v003' | 'cass-nccpintegration-random-junk-c0northamerica-d0prod-h0gamesystems-p0vizio-r027-u0nccp-w0A-z0useast1a-v003' | 'cass-nccpintegration-random-junk-c0northamerica-d0prod-h0gamesystems-p0vizio-r027-u0nccp-w0A-z0useast1a' | 'cass'          | 'nccpintegration' | 'random-junk'  | 'v003'       | 3                | 'northamerica'    | 'prod'           | 'gamesystems'    | 'vizio'          | '27'             | 'nccp'         | 'A'                  | 'useast1a'
        'cass-nccpintegration-c0northamerica-d0prod'                                                                   | 'cass-nccpintegration-c0northamerica-d0prod'                                                                   | 'cass-nccpintegration-c0northamerica-d0prod'                                                              | 'cass'          | 'nccpintegration' | null           | null         | null             | 'northamerica'    | 'prod'           | null             | null             | null             | null           | null                 | null
        'cass--my-stuff-c0northamerica-d0prod'                                                                         | 'cass--my-stuff-c0northamerica-d0prod'                                                                         | 'cass--my-stuff-c0northamerica-d0prod'                                                                    | 'cass'          | null              | 'my-stuff'     | null         | null             | 'northamerica'    | 'prod'           | null             | null             | null             | null           | null                 | null
        'cass-c0northamerica-d0prod'                                                                                   | 'cass-c0northamerica-d0prod'                                                                                   | 'cass-c0northamerica-d0prod'                                                                              | 'cass'          | null              | null           | null         | null             | 'northamerica'    | 'prod'           | null             | null             | null             | null           | null                 | null
        'cass-c0northamerica-d0prod-v102'                                                                              | 'cass-c0northamerica-d0prod-v102'                                                                              | 'cass-c0northamerica-d0prod'                                                                              | 'cass'          | null              | null           | 'v102'       | 102              | 'northamerica'    | 'prod'           | null             | null             | null             | null           | null                 | null
        'cass-v102'                                                                                                    | 'cass-v102'                                                                                                    | 'cass'                                                                                                    | 'cass'          | null              | null           | 'v102'       | 102              | null              | null             | null             | null             | null             | null           | null                 | null

    }

    void 'dissect app version'() {
        given:
        AppVersion appVersion = Relationships.dissectAppVersion(appVersionString)

        expect:
        with(appVersion) {
            packageName == expectedPackageName
            version == expectedVersion
            commit == expectedCommit
            buildNumber == expectedBuildNumber
            buildJobName == expectedBuildJobName
        }

        where:
        appVersionString                                             | expectedPackageName | expectedVersion | expectedCommit | expectedBuildNumber | expectedBuildJobName
        "helloworld-1.0.0-592112.h154/WE-WAPP-helloworld/154"        | "helloworld"        | "1.0.0"         | "592112"       | "154"               | "WE-WAPP-helloworld"
        "helloworld-server-1.0.0-592112.h154/WE-WAPP-helloworld/154" | "helloworld-server" | "1.0.0"         | "592112"       | "154"               | "WE-WAPP-helloworld"
        "helloworld-1.0.0-592112.h154"                               | "helloworld"        | "1.0.0"         | "592112"       | "154"               | null
        "helloworld-1.0.0-592112"                                    | "helloworld"        | "1.0.0"         | "592112"       | null                | null
    }

    void 'dissect app version invalid values'() {
        expect:
        Relationships.dissectAppVersion(invalidValue) == null

        where:
        invalidValue << [null, "", "blah blah blah"]
    }

    void 'package from app version'() {
        expect:
        Relationships.packageFromAppVersion(appVersion) == expectedPackage

        where:
        appVersion                                            || expectedPackage
        'helloworld-1.0.0-592112.h154/WE-WAPP-helloworld/154' || 'helloworld'
        null                                                  || null
        ''                                                    || null
        'dfjsdfkjsdfkjsd fkjsdf kljsdf ksjdf klsdjf sd'       || null
    }

    void 'app name from group name'() {
        expect:
        Relationships.appNameFromGroupName(groupName) == appName

        where:
        groupName                                     || appName
        "actiondrainer"                               || "actiondrainer"
        "merchweb--loadtest"                          || "merchweb"
        "discovery--us-east-1d"                       || "discovery"
        "merchweb-loadtest"                           || "merchweb"
        "api-test-A"                                  || "api"
        "discovery-dev"                               || "discovery"
        "discovery-us-east-1d"                        || "discovery"
        "evcache-us-east-1d-0"                        || "evcache"
        "evcache-us----east-1d-0"                     || "evcache"
        "videometadata-navigator-integration-240-CAN" || "videometadata"
    }

    void 'app name from launch config name'() {

        expect:
        Relationships.appNameFromLaunchConfigName(launchConfigName) == appName

        where:
        launchConfigName                                           || appName
        "actiondrainer-201010231745"                               || "actiondrainer"
        "merchweb--loadtest-201010231745"                          || "merchweb"
        "discovery--us-east-1d-201010231745"                       || "discovery"
        "merchweb-loadtest-201010231745"                           || "merchweb"
        "api-test-A-201010231745"                                  || "api"
        "discovery-dev-201010231745"                               || "discovery"
        "discovery-us-east-1d-201010231745"                        || "discovery"
        "evcache-us-east-1d-0-201010231745"                        || "evcache"
        "evcache-us----east-1d-0-201010231745"                     || "evcache"
        "videometadata-navigator-integration-240-CAN-201010231745" || "videometadata"
    }

    void 'app name from load balancer name'() {
        expect:
        Relationships.appNameFromLoadBalancerName(loadBalancerName) == appName

        where:
        loadBalancerName                                       || appName
        "actiondrainer-frontend"                               || "actiondrainer"
        "merchweb--loadtest-frontend"                          || "merchweb"
        "discovery--us-east-1d-frontend"                       || "discovery"
        "merchweb-loadtest-frontend"                           || "merchweb"
        "api-test-A-frontend"                                  || "api"
        "discovery-dev-frontend"                               || "discovery"
        "discovery-us-east-1d-frontend"                        || "discovery"
        "evcache-us-east-1d-0-frontend"                        || "evcache"
        "evcache-us----east-1d-0-frontend"                     || "evcache"
        "videometadata-navigator-integration-240-CAN-frontend" || "videometadata"
    }

    void 'stack name from group name'() {
        expect:
        Relationships.stackNameFromGroupName(groupName) == stackName

        where:
        groupName                                     || stackName
        "actiondrainer"                               || ""
        "merchweb--loadtest"                          || ""
        "discovery--us-east-1d"                       || ""
        "merchweb-loadtest"                           || "loadtest"
        "api-test-A"                                  || "test"
        "discovery-dev"                               || "dev"
        "discovery-us-east-1d"                        || "us"
        "evcache-us-east-1d-0"                        || "us"
        "evcache-us----east-1d-0"                     || "us"
        "videometadata-navigator-integration-240-CAN" || "navigator"
    }

    void 'cluster from group name'() {
        expect:
        Relationships.clusterFromGroupName(groupName) == cluster

        where:
        groupName                                     || cluster
        "actiondrainer"                               || "actiondrainer"
        "actiondrainer-v301"                          || "actiondrainer"
        "merchweb--loadtest"                          || "merchweb--loadtest"
        "discovery--us-east-1d-v"                     || "discovery--us-east-1d-v"
        "discovery--us-east-1d-v1"                    || "discovery--us-east-1d-v1"
        "discovery--us-east-1d-v11"                   || "discovery--us-east-1d-v11"
        "discovery--us-east-1d-v111"                  || "discovery--us-east-1d"
        "discovery--us-east-1d-v1111"                 || "discovery--us-east-1d-v1111"
        "merchweb-loadtest"                           || "merchweb-loadtest"
        "api-test-A"                                  || "api-test-A"
        "evcache-us-east-1d-0"                        || "evcache-us-east-1d-0"
        "evcache-us----east-1d-0"                     || "evcache-us----east-1d-0"
        "videometadata-navigator-integration-240-CAN" || "videometadata-navigator-integration-240-CAN"
    }

    void 'avoid reserved format'() {
        expect:
        Relationships.usesReservedFormat(name) == isReserved

        where:
        name                                                                                                           || isReserved
        "abha"                                                                                                         || false
        "abha-v999"                                                                                                    || true
        "abha-v9999999"                                                                                                || false
        "integration-240-usa-iphone-v001"                                                                              || true
        "integration-240-usa-iphone-v22"                                                                               || false
        "integration-v001-usa-iphone"                                                                                  || true
        'cass-nccpintegration-random-junk-c0northamerica-d0prod-h0gamesystems-p0vizio-r027-u0nccp-x0A-z0useast1a-v003' || true
        'c0northamerica'                                                                                               || true
        'junk-c0northamerica'                                                                                          || true
        'c0northamerica'                                                                                               || true
        'random-c0northamerica-junk'                                                                                   || true
        'random-abc0northamerica-junk'                                                                                 || false
    }

    void 'check strict name'() {
        expect:
        Relationships.checkStrictName(name) == isValid

        where:
        name            || isValid
        "abha"          || true
        "account_batch" || false
        "account.batch" || false
        ""              || false
        null            || false
    }

    void 'check app name for load balancer'() {
        expect:
        Relationships.checkAppNameForLoadBalancer(appName) == isValid

        where:
        appName         || isValid
        "abha"          || true
        "account_batch" || false
        "account.batch" || false
        "account#batch" || false
        ""              || false
        null            || false
        "abhav309"      || false
        "abhav309787"   || true
        "v309"          || false
        "v3111111"      || true
    }

    void 'check name'() {
        expect:
        Relationships.checkName(name) == isValid

        where:
        name            || isValid
        "abha"          || true
        "account_batch" || true
        "account.batch" || true
        "account#batch" || false
        ""              || false
        null            || false
    }

    void 'check detail'() {
        expect:
        Relationships.checkDetail(name) == isValid

        where:
        name                                                                   || isValid
        "A"                                                                    || true
        "0"                                                                    || true
        "east-1c-0"                                                            || true
        "230CAN-next-A"                                                        || true
        "integration-240-USA"                                                  || true
        "integration-240-usa-iphone-ipad-ios5-even-numbered-days-not-weekends" || true
        "----"                                                                 || true
        "__._._--_.."                                                          || true
        "230CAN#next-A"                                                        || false
        ""                                                                     || false
        null                                                                   || false
    }

    void 'build auto scaling group name from map'() {
        expect:
        Relationships.buildGroupName(map) == name

        where:
        map                                                        || name
        [appName: "helloworld", stack: "asgardtest", detail: null] || "helloworld-asgardtest"
        [appName: "helloworld", stack: "asgardtest", detail: ""]   || "helloworld-asgardtest"
        [appName: "helloworld", stack: "asgardtest", detail: "2"]  || "helloworld-asgardtest-2"
        [appName: "helloworld", stack: "", detail: ""]             || "helloworld"
        [appName: "helloworld", stack: null, detail: null]         || "helloworld"
        [appName: "discovery", stack: "us", detail: "east-1d"]     || "discovery-us-east-1d"
        [appName: "discovery", stack: "", detail: "us-east-1d"]    || "discovery--us-east-1d"
        [appName: "discovery", stack: null, detail: "us-east-1d"]  || "discovery--us-east-1d"
        [appName: "merchweb", stack: "", detail: "loadtest"]       || "merchweb--loadtest"
        [appName: "merchweb", stack: null, detail: "loadtest"]     || "merchweb--loadtest"
        [appName: "helloworld", stack: "asgardtest", detail: null] || "helloworld-asgardtest"
    }

    void 'build auto scaling group with more attributes'() {
        expect:
        'cass-nccpintegration-random-junk-c0northamerica-d0prod-h0gamesystems-p0vizio-r027-u0nccp-w0A-z0useast1a'
        Relationships.buildGroupName(appName: "cass", stack: "nccpintegration",
                detail: "random-junk", countries: "northamerica", devPhase: "prod",
                hardware: "gamesystems", partners: "vizio", revision: "27", usedBy: "nccp", redBlackSwap: "A",
                zoneVar: "useast1a")

        'cass--random-junk-h0gamesystems-w0A' ==
                Relationships.buildGroupName(appName: "cass", stack: "",
                        detail: "random-junk", countries: null, devPhase: "",
                        hardware: "gamesystems", partners: "", redBlackSwap: "A")

        'cass-h0gamesystems-w0A' ==
                Relationships.buildGroupName(appName: "cass", stack: null, detail: null, devPhase: "",
                        hardware: "gamesystems", partners: "", redBlackSwap: "A")
    }

    void 'build autoscaling group name throws exceptions on in valid input'() {
        when:
        Relationships.buildGroupName(params)

        then:
        thrown expectedException

        where:
        params                                            || expectedException
        [appName: "", stack: "asgardtest", detail: "2"]   || IllegalArgumentException
        [appName: null, stack: "asgardtest", detail: "2"] || NullPointerException
    }

    void 'launch configuration name'() {
        expect:
        Relationships.buildLaunchConfigurationName(name)==~~launchConfigName

        where:
        name                         || launchConfigName
        "helloworld"                 || /helloworld-[0-9]{14}/
        "integration-240-usa-iphone" || /integration-240-usa-iphone-[0-9]{14}/
    }

    void 'build load balancer name'() {
        expect:
        Relationships.buildLoadBalancerName(appName, stack, detail) == loadBalancerName

        where:
        appName      | stack        | detail       || loadBalancerName
        "helloworld" | "asgardtest" | null         || "helloworld-asgardtest"
        "helloworld" | "asgardtest" | ""           || "helloworld-asgardtest"
        "helloworld" | "asgardtest" | "frontend"   || "helloworld-asgardtest-frontend"
        "helloworld" | ""           | ""           || "helloworld"
        "helloworld" | null         | null         || "helloworld"
        "discovery"  | "us"         | "east-1d"    || "discovery-us-east-1d"
        "discovery"  | ""           | "frontend"   || "discovery--frontend"
        "discovery"  | null         | "us-east-1d" || "discovery--us-east-1d"
        "merchweb"   | ""           | "frontend"   || "merchweb--frontend"
        "merchweb"   | null         | "frontend"   || "merchweb--frontend"
    }

    void "build load balancer name throws exception"() {
        when:
        Relationships.buildLoadBalancerName(appName, stack, detail)

        then:
        thrown expectedException

        where:
        appName | stack        | detail     || expectedException
        ""      | "asgardtest" | "frontend" || IllegalArgumentException
        null    | "asgardtest" | "frontend" || NullPointerException
    }

    void 'ami id from description'() {
        expect:
        Relationships.baseAmiIdFromDescription(description) == id

        where:
        description                                                                       || id
        ''                                                                                || null
        null                                                                              || null
        'base_ami_id=ami-50886239,base_ami_name=servicenet-roku-qadd.dc.81210.10.44'      || 'ami-50886239'
        'base_ami_id=ami-1eb75c77,base_ami_name=servicenet-roku-qadd.dc.81210.10.44'      || 'ami-1eb75c77'
        'base_ami_name=servicenet-roku-qadd.dc.81210.10.44,base_ami_id=ami-1eb75c77'      || 'ami-1eb75c77'
        'store=ebs,ancestor_name=ebs-centosbase-x86_64-20101124,ancestor_id=ami-7b4eb912' || 'ami-7b4eb912'
    }

    void 'base ami name from description'() {
        expect:
        Relationships.baseAmiNameFromDescription(description) == name

        where:
        description                                                                       || name
        'base_ami_id=ami-50886239,base_ami_name=servicenet-roku-qadd.dc.81210.10.44'      || 'servicenet-roku-qadd.dc.81210.10.44'
        'base_ami_id=ami-1eb75c77,base_ami_name=servicenet-roku-qadd.dc.81210.10.44'      || 'servicenet-roku-qadd.dc.81210.10.44'
        'base_ami_name=servicenet-roku-qadd.dc.81210.10.44,base_ami_id=ami-1eb75c77'      || 'servicenet-roku-qadd.dc.81210.10.44'
        'store=ebs,ancestor_name=ebs-centosbase-x86_64-20101124,ancestor_id=ami-7b4eb912' || 'ebs-centosbase-x86_64-20101124'
    }

    void 'base ami date from description'() {
        expect:
        Relationships.baseAmiDateFromDescription(description) == baseAmiDate

        where:
        description                                                                       || baseAmiDate
        'base_ami_id=ami-50886239,base_ami_name=servicenet-roku-qadd.dc.81210.10.44'      || null
        'base_ami_id=ami-1eb75c77,base_ami_name=servicenet-roku-qadd.dc.81210.10.44'      || null
        'base_ami_name=servicenet-roku-qadd.dc.81210.10.44,base_ami_id=ami-1eb75c77'      || null
        'store=ebs,ancestor_name=ebs-centosbase-x86_64-20101124,ancestor_id=ami-7b4eb912' || new DateTime(2010, 11, 24, 0, 0, 0, 0)
    }

    void 'build alarm name'() {
        expect:
        Relationships.buildAlarmName('helloworld--scalingtest-v000', '99999') == 'helloworld--scalingtest-v000-99999'
    }

    void 'build policy name'() {
        expect:
        Relationships.buildScalingPolicyName('helloworld--scalingtest-v000', '99999') == 'helloworld--scalingtest-v000-99999'
    }

    void 'labelled environmnet variables'() {
        when:
        Names names = new Names('test')
        names.partners = 'sony'

        then:
        Relationships.labeledEnvironmentVariables(names, 'NETFLIX_') == ['export NETFLIX_PARTNERS=sony']

        when:
        names.devPhase = 'stage'

        then:
        Relationships.labeledEnvironmentVariables(names, 'NETFLIX_') == ['export NETFLIX_DEV_PHASE=stage', 'export NETFLIX_PARTNERS=sony']
    }

    void 'parts'() {
        when:
        Names names = new Names('test')
        names.partners = 'sony'

        then:
        Relationships.parts(names) == ['Partners': 'sony']

        when:
        names.devPhase = 'stage'

        then:
        Relationships.parts(names) == ['Dev Phase': 'stage', 'Partners': 'sony']
    }

}