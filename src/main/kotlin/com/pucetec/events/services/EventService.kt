package com.pucetec.events.services

import com.pucetec.events.dto.EventRequest
import com.pucetec.events.dto.EventResponse
import com.pucetec.events.exceptions.BlankFieldException
import com.pucetec.events.exceptions.EventNotFoundException
import com.pucetec.events.exceptions.InvalidCapacityException
import com.pucetec.events.mappers.toEntity
import com.pucetec.events.mappers.toResponse
import com.pucetec.events.repositories.EventRepository
import org.springframework.stereotype.Service

@Service
class EventService(
    private val eventRepository: EventRepository
) {

    fun getAllEvents(): List<EventResponse> =
        eventRepository.findAll().map { it.toResponse() }

    fun getEventById(id: Long): EventResponse =
        eventRepository.findById(id)
            .orElseThrow { EventNotFoundException("Event with id $id was not found") }
            .toResponse()

    fun createEvent(request: EventRequest): EventResponse {
        if (request.name.isBlank()) {
            throw BlankFieldException("Event name cannot be blank")
        }
        if (request.venue.isBlank()) {
            throw BlankFieldException("Event venue cannot be blank")
        }
        if (request.totalTickets < 1) {
            throw InvalidCapacityException("Total tickets must be greater than zero")
        }

        val event = request.toEntity()
        return eventRepository.save(event).toResponse()
    }
}
