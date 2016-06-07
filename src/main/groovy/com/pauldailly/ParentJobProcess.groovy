package com.pauldailly

import groovy.util.logging.Slf4j
import org.slf4j.MDC
import org.springframework.stereotype.Component

/**
 * Created by pauldailly on 01/06/2016.
 */
@Slf4j
class ParentJobProcess {

    void executeParentJob(int numChildJobs) {
        MDC.put('CorrelationID', UUID.randomUUID().toString())
        log.info('executing parent process')
        int i
        while (i++ < numChildJobs) {
            new ChildJobProcessor().executeChildJob(UUID.randomUUID().toString())
        }

        try {

            throw new RuntimeException('Something really really bad happened!!!')
        } catch (RuntimeException e) {
            log.warn('I caught a runtime exception and filebeat sent it to logstash', e)
        }

    }
}
