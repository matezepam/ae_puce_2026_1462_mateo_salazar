package com.pucetec.events.mappers

import com.pucetec.events.dto.EventRequest
import com.pucetec.events.dto.EventResponse
import com.pucetec.events.entities.Event

fun EventRequest.toEntity() = Event(
    name = name,
    venue = venue,
    totalTickets = totalTickets,
    availableTickets = totalTickets
)

fun Event.toResponse() = EventResponse(
    id = id,
    name = name,
    venue = venue,
    totalTickets = totalTickets,
    availableTickets = availableTickets
)
