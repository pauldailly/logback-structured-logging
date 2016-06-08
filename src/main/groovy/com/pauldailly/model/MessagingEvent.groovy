package com.pauldailly.model
/**
 * Created by pauldailly on 04/06/2016.
 */
class MessagingEvent<MessageEventPayload>{

    String eventId
    MessageEventCode eventCode
    String correlationId
    String messageId
    String timestamp
    MessageEventPayload payload
}

interface MessageEventPayload {

}

class MessageCreated implements MessageEventPayload {

    String messageBody
    String inboxId
    MessageDeliveryChannel deliveryChannel
    String[] messageRecipients = []
    String messageLocale
    Map<String, String> messageVariables = [:]
    String inboxOwner
}

class MessageDeliveryReport implements MessageEventPayload {
    MessageDeliveryChannel deliveryChannel
    String provider
    String providerMessageId
    String recipientAddress // don't want to log this or store it in DB
    String status
    String statusCode
    String detail
}

enum MessageDeliveryChannel {
    SMS('Twilio'),
    EMAIL('Mailgun'),
    APNS('Apple'),
    GCM('Google')

    String defaultProvider

    MessageDeliveryChannel(String defaultProvider){
        this.defaultProvider = defaultProvider
    }

    static MessageDeliveryChannel randomChannel() {
        Random random = new Random()
        values().getAt(random.nextInt(4))
    }
}

enum MessageEventCode {
    MESSAGE_CREATED, DELIVERY_UPDATE, DELIVERED, DELIVERY_FAILED, DISPATCHED, DISPATCH_FAILED, UNSUBSCRIBED, UNKNOWN
}
