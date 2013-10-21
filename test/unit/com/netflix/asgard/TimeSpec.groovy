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

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import spock.lang.Specification
import spock.lang.Unroll

class TimeSpec extends Specification {

    void 'parse zulu'() {
        given:
        DateTime parsedDateTime = Time.parse('2010-11-08T21:50:33Z')

        expect:
        with(parsedDateTime) {
            year == 2010
            monthOfYear == 11
            dayOfMonth == 8
        }
    }

    void 'parse readable'() {
        given:
        DateTime parsedDateTime = Time.parse('2011-04-20 16:18:20 HAST').withZone(DateTimeZone.forID('US/Hawaii'))

        expect:
        with(parsedDateTime) {
            year == 2011
            monthOfYear == 4
            dayOfMonth == 20
        }
    }

    @Unroll
    void 'parse invalid'() {
        expect:
        Time.parse(time) == null

        where:
        time                  | _
        '2011-04-20 16:18:20' | _
        ''                    | _
        null                  | _
    }

    void 'now utc'() {

        when: '2011-08-21T16:38:05.087-07:00'
        DateTimeZone pacificTime = DateTimeZone.forID('America/Los_Angeles')
        String nowString = new DateTime(pacificTime).toString()

        then:
        nowString.startsWith('201') // Good for 8 years
        nowString.endsWith(':00')
        nowString[10] == 'T'

        when: '2011-08-21T23:38:05.203Z'
        String nowUtcString = Time.nowUtc().toString()

        then:
        nowUtcString.startsWith('201')
        nowUtcString.endsWith('Z')
        nowUtcString[10] == 'T'

        and: 'Hour should be different between time zones'
        nowString[11..12] != nowUtcString[11..12]

        when:
        DateTimeFormatter parser = ISODateTimeFormat.dateTime()
        DateTime nowUtcBackToLocal = parser.parseDateTime(nowUtcString).toDateTime(pacificTime)
        String nowUtcBackToLocalString = nowUtcBackToLocal.toString()

        then: 'Year, month, day, hour should be equal between the local date string and the utc date converted back to local'
        nowUtcBackToLocalString[0..13] == nowString[0..13]
    }
}
