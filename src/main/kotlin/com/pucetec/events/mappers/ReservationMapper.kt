package com.pucetec.events.mappers

import com.pucetec.events.dto.ReservationResponse
import com.pucetec.events.entities.Reservation

fun Reservation.toResponse() = ReservationResponse(
    id = id,
    attendeeId = attendee.id,
    attendeeName = attendee.name,
    eventId = event.id,
    eventName = event.name,
    status = status.name,
    createdAt = createdAt
)
