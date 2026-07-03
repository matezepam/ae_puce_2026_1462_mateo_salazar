package com.pucetec.events.services

import com.pucetec.events.dto.EventRequest
import com.pucetec.events.entities.Event
import com.pucetec.events.exceptions.BlankFieldException
import com.pucetec.events.exceptions.EventNotFoundException
import com.pucetec.events.exceptions.InvalidCapacityException
import com.pucetec.events.repositories.EventRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class EventServiceTest {

    @Mock
    lateinit var eventRepository: EventRepository

    @InjectMocks
    lateinit var eventService: EventService

    @Test
    fun `getAllEvents returns mapped events`() {
        val events = listOf(
            Event(id = 1, name = "Concert", venue = "Arena", totalTickets = 100, availableTickets = 80)
        )
        Mockito.`when`(eventRepository.findAll()).thenReturn(events)

        val response = eventService.getAllEvents()

        assertEquals(1, response.size)
        assertEquals("Concert", response[0].name)
        assertEquals(80, response[0].availableTickets)
    }

    @Test
    fun `getEventById returns event when it exists`() {
        val event = Event(id = 1, name = "Talk", venue = "Auditorium", totalTickets = 50, availableTickets = 50)
        Mockito.`when`(eventRepository.findById(1)).thenReturn(Optional.of(event))

        val response = eventService.getEventById(1)

        assertEquals(1, response.id)
        assertEquals("Talk", response.name)
    }

    @Test
    fun `getEventById throws EventNotFoundException when event does not exist`() {
        Mockito.`when`(eventRepository.findById(99)).thenReturn(Optional.empty())

        assertThrows<EventNotFoundException> {
            eventService.getEventById(99)
        }
    }

    @Test
    fun `createEvent throws BlankFieldException when name is blank`() {
        val request = EventRequest(name = " ", venue = "Arena", totalTickets = 10)

        assertThrows<BlankFieldException> {
            eventService.createEvent(request)
        }
    }

    @Test
    fun `createEvent throws BlankFieldException when venue is blank`() {
        val request = EventRequest(name = "Festival", venue = "", totalTickets = 10)

        assertThrows<BlankFieldException> {
            eventService.createEvent(request)
        }
    }

    @Test
    fun `createEvent throws InvalidCapacityException when total tickets is less than one`() {
        val request = EventRequest(name = "Festival", venue = "Park", totalTickets = 0)

        assertThrows<InvalidCapacityException> {
            eventService.createEvent(request)
        }
    }

    @Test
    fun `createEvent saves event with available tickets equal to total tickets`() {
        val request = EventRequest(name = "Festival", venue = "Park", totalTickets = 300)
        Mockito.`when`(eventRepository.save(Mockito.any(Event::class.java))).thenAnswer {
            val event = it.arguments[0] as Event
            event.id = 1
            event
        }

        val response = eventService.createEvent(request)

        assertEquals(1, response.id)
        assertEquals(300, response.totalTickets)
        assertEquals(300, response.availableTickets)
    }
}
