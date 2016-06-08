package com.pauldailly

import static net.logstash.logback.argument.StructuredArguments.v

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import org.apache.log4j.MDC

import com.pauldailly.model.MessageCreated
import com.pauldailly.model.MessageDeliveryChannel
import com.pauldailly.model.MessageDeliveryReport
import com.pauldailly.model.MessageEventCode
import com.pauldailly.model.MessageEventPayload
import com.pauldailly.model.MessagingEvent

import groovy.util.logging.Slf4j
/**
 * Created by pauldailly on 04/06/2016.
 */
@Slf4j
class ChildJobProcessor {
    void executeChildJob(String messageId) {

        MessagingEvent<MessageCreated> messageCreatedEvent = createMessage(messageId)
        log.info("New message created ${messageId}", v("messagingEvent", messageCreatedEvent))
        // attempt dispatch
        MessagingEvent<MessageDeliveryReport> messageDispatchEvent = attemptDispatch(messageCreatedEvent.payload.deliveryChannel, messageCreatedEvent.messageId)
        boolean dispatchSuccess = messageDispatchEvent.payload.statusCode == '200'
        log.info("Message ${messageId} ${dispatchSuccess ? 'was' : 'was not'} dispatched successfully", messageCreatedEvent.messageId, v("messagingEvent", messageDispatchEvent))
        // if successfully dispatched produce random delivery events

        if(!dispatchSuccess) {
            return
        }

        List<MessagingEvent<MessageDeliveryReport>> messageDeliveryReportUpdateEvents = createUpdateEvents(messageId, messageDispatchEvent.payload.deliveryChannel)
        messageDeliveryReportUpdateEvents.each {
            log.info("Message update received for ${messageId}: ${it.payload.detail}", v("messagingEvent", it))
        }

        MessagingEvent<MessageDeliveryReport> finalDeliveryStatusEvent = createFinalDeliveryStatusEvent(messageId, messageDispatchEvent.payload.deliveryChannel)
        log.info("Final delivery status report for ${messageId}: ${finalDeliveryStatusEvent.payload.detail}", v("messagingEvent", finalDeliveryStatusEvent))
    }

    MessagingEvent<MessageDeliveryReport> createFinalDeliveryStatusEvent(String messageId, MessageDeliveryChannel channel) {
        MessageEventCode eventCode = new Random().nextInt(2) % 2 == 0 ? MessageEventCode.DELIVERED : MessageEventCode.DELIVERY_FAILED
        MessageEventPayload payload = new MessageDeliveryReport(
          deliveryChannel: channel,
          provider: channel.defaultProvider,
          providerMessageId: UUID.randomUUID().toString(),
          status: "Delivery ${eventCode == MessageEventCode.DELIVERED ? 'Success' : 'Failure'}",
          detail: "${channel.defaultProvider} ${eventCode == MessageEventCode.DELIVERED ? 'was' : 'was not'} delivered successfully"
        )
        createMessageEventWithCodeAndIdAndPayload(eventCode, messageId, payload)
    }

    List<MessagingEvent<MessageDeliveryReport>> createUpdateEvents(String messageId, MessageDeliveryChannel channel) {
        int numUpdatesToCreate = new Random().nextInt(5)

        (0..numUpdatesToCreate).collect {
            MessageEventPayload payload = new MessageDeliveryReport(
              deliveryChannel: channel,
              provider: channel.defaultProvider,
              providerMessageId: UUID.randomUUID().toString(),
              status: "Message Update Status Code",
              detail: "Update number ${it + 1} from ${channel.defaultProvider}"
            )

            createMessageEventWithCodeAndIdAndPayload(MessageEventCode.DELIVERY_UPDATE, messageId, payload)
        }
    }

    MessagingEvent attemptDispatch(MessageDeliveryChannel channel, String messageId) {
        Random random = new Random()
        random.nextInt(2) % 2 == 0 ? createDispatchSuccessEvent(channel, messageId) : createDispatchFailureEvent(channel, messageId)
    }

    MessagingEvent createDispatchSuccessEvent(MessageDeliveryChannel channel, String messageId) {
        MessageEventPayload payload = new MessageDeliveryReport(
          deliveryChannel: channel,
          provider: channel.defaultProvider,
          providerMessageId: UUID.randomUUID().toString(),
          status: 'Dispatch successful',
          statusCode: '200',
          detail: "Message succesfully dispatched to ${channel.defaultProvider}"
        )

        createMessageEventWithCodeAndIdAndPayload(MessageEventCode.DISPATCHED, messageId, payload)
    }

    MessagingEvent createDispatchFailureEvent(MessageDeliveryChannel channel, String messageId) {
        MessageEventPayload payload = new MessageDeliveryReport(
          deliveryChannel: channel,
          provider: channel.defaultProvider,
          providerMessageId: UUID.randomUUID().toString(),
          status: 'Dispatch failed',
          statusCode: '500',
          detail: "Could not dispatch message to ${channel.defaultProvider} due to server error"
        )

        createMessageEventWithCodeAndIdAndPayload(MessageEventCode.DISPATCH_FAILED, messageId, payload)
    }

    MessagingEvent createMessageEventWithCodeAndIdAndPayload(MessageEventCode eventCode, String messageId, MessageEventPayload payload){
        MessagingEvent event = new MessagingEvent(
          eventId: UUID.randomUUID().toString(),
          eventCode: eventCode,
          correlationId: MDC.get('CorrelationID'),
          messageId: messageId,
          timestamp: ZonedDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE),
          payload: payload
        )
        event
    }

    MessagingEvent createMessage(String messageId) {
        def messageCreatedEvent = new MessageCreated(
          messageBody: 'Your flight from DUB to LAX has been cancelled',
          inboxId: UUID.randomUUID().toString(),
          deliveryChannel: MessageDeliveryChannel.randomChannel(),
          messageRecipients: ['recipient1', 'recipient2'],
          messageLocale: 'en',
          messageVariables: [
            '$leg_startPoint_shortName': 'DUB',
            '$leg_endPoint_shortName'  : 'LAX'
          ],
          inboxOwner: UUID.randomUUID().toString()
        )
        createMessageEventWithCodeAndIdAndPayload(MessageEventCode.MESSAGE_CREATED, messageId, messageCreatedEvent)
    }
}
