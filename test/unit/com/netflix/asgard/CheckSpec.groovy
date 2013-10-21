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

class CheckSpec extends Specification {

    void 'lone or none'() {
        when:
        Collection<String> strings = ['Olive Yew']

        then:
        'Olive Yew' == Check.loneOrNone(strings, String)

        when:
        Collection<Integer> integers = []

        then:
        Check.loneOrNone(integers, Integer) == null

        when:
        Collection<String> moreStrings = ['Brock Lee', 'Sue Flay']
        Check.loneOrNone(moreStrings, String)

        then:
        def ise = thrown IllegalStateException
        ise.message.startsWith('ERROR: Found 2 String items instead of 0 or 1')
    }

    void 'positive'() {
        expect:
        1 == Check.positive(1)
    }

    void 'positive throws exceptions'() {
        when:
        Check.positive(value)

        then:
        thrown expectedException

        where:
        value || expectedException
        0     || IllegalArgumentException
        -1    || IllegalArgumentException
        null  || NullPointerException
    }

    void 'at least'() {
        expect:
        Check.atLeast(min, max) == value

        where:
        min | max || value
        1   | 100 || 100
        99  | 100 || 100
        -3  | -2  || -2
    }

    void 'at least throws an exception'() {
        when:
        Check.atLeast(90, 10)

        then:
        thrown IllegalArgumentException
    }

    void 'at most'() {
        expect:
        Check.atMost(min, max) == value

        where:
        min | max || value
        100 | 1   || 1
        100 | 99  || 99
        -1  | -8  || -8
    }

    void 'at most throws an exception'() {
        when:
        Check.atMost(20, 55)

        then:
        thrown IllegalArgumentException
    }
}
