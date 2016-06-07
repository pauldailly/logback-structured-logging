package com.pauldailly.model

/**
 * Created by pauldailly on 04/06/2016.
 */
class Event {

    String eventId
    String eventCode
    String correlationId
    String messageId
    long timestamp
}

interface MessageEventPayload {

}

class MessageCreatedEvent extends Event {

    String messageBody
    String inboxAddress
    String[] messageRecipients = []
    String messageLocale
    Map<String, String> messageVariables = [:]
}
