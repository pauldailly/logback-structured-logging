package com.pauldailly

import static net.logstash.logback.argument.StructuredArguments.*


import com.pauldailly.model.Event
import com.pauldailly.model.MessageCreatedEvent
import groovy.util.logging.Slf4j
import org.apache.log4j.MDC

import java.time.ZonedDateTime

/**
 * Created by pauldailly on 04/06/2016.
 */
@Slf4j
class ChildJobProcessor {
    void executeChildJob(String messageId) {

        createMessage(messageId)

    }

    void createMessage(String messageId) {
        Event event = new MessageCreatedEvent(
                eventId: UUID.randomUUID().toString(),
                eventCode: 'MESSAGE_CREATED',
                correlationId: MDC.get('CorrelationID'),
                messageId: messageId,
                timestamp: ZonedDateTime.now().toEpochSecond(),
                messageBody: 'Your flight from DUB to LAX has been cancelled',
                inboxAddress: 'inboxAddress',
                messageRecipients: ['recipient1', 'recipient2'],
                messageLocale: 'en',
                messageVariables: [
                        '$leg_startPoint_shortName': 'DUB',
                        '$leg_endPoint_shortName': 'LAX'
                ]
        )
        log.info('New message created {}', event.messageId, fields(event))
    }
}
