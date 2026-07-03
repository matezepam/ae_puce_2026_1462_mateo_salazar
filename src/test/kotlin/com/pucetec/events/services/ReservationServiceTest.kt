package com.pucetec.events.services

import com.pucetec.events.entities.Attendee
import com.pucetec.events.entities.Event
import com.pucetec.events.entities.Reservation
import com.pucetec.events.entities.ReservationStatus
import com.pucetec.events.exceptions.AttendeeNotFoundException
import com.pucetec.events.exceptions.EventNotFoundException
import com.pucetec.events.exceptions.ReservationAlreadyCancelledException
import com.pucetec.events.exceptions.ReservationLimitExceededException
import com.pucetec.events.exceptions.ReservationNotFoundException
import com.pucetec.events.exceptions.SoldOutException
import com.pucetec.events.repositories.AttendeeRepository
import com.pucetec.events.repositories.EventRepository
import com.pucetec.events.repositories.ReservationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReservationServiceTest {

    @Mock
    lateinit var reservationRepository: ReservationRepository

    @Mock
    lateinit var attendeeRepository: AttendeeRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @InjectMocks
    lateinit var reservationService: ReservationService

    @Test
    fun `getAllReservations returns mapped reservations`() {
        val attendee = Attendee(id = 1, name = "Ana", email = "ana@example.com")
        val event = Event(id = 2, name = "Concert", venue = "Arena", totalTickets = 10, availableTickets = 8)
        val reservations = listOf(
            Reservation(
                id = 3,
                attendee = attendee,
                event = event,
                status = ReservationStatus.ACTIVE,
                createdAt = LocalDateTime.now()
            )
        )
        Mockito.`when`(reservationRepository.findAll()).thenReturn(reservations)

        val response = reservationService.getAllReservations()

        assertEquals(1, response.size)
        assertEquals(3, response[0].id)
        assertEquals("ACTIVE", response[0].status)
    }

    @Test
    fun `createReservation throws AttendeeNotFoundException when attendee does not exist`() {
        Mockito.`when`(attendeeRepository.findById(1)).thenReturn(Optional.empty())

        assertThrows<AttendeeNotFoundException> {
            reservationService.createReservation(1, 2)
        }
    }

    @Test
    fun `createReservation throws EventNotFoundException when event does not exist`() {
        val attendee = Attendee(id = 1, name = "Ana", email = "ana@example.com")
        Mockito.`when`(attendeeRepository.findById(1)).thenReturn(Optional.of(attendee))
        Mockito.`when`(eventRepository.findById(2)).thenReturn(Optional.empty())

        assertThrows<EventNotFoundException> {
            reservationService.createReservation(1, 2)
        }
    }

    @Test
    fun `createReservation throws SoldOutException when event has no available tickets`() {
        val attendee = Attendee(id = 1, name = "Ana", email = "ana@example.com")
        val event = Event(id = 2, name = "Concert", venue = "Arena", totalTickets = 10, availableTickets = 0)
        Mockito.`when`(attendeeRepository.findById(1)).thenReturn(Optional.of(attendee))
        Mockito.`when`(eventRepository.findById(2)).thenReturn(Optional.of(event))

        assertThrows<SoldOutException> {
            reservationService.createReservation(1, 2)
        }
    }

    @Test
    fun `createReservation throws ReservationLimitExceededException when attendee already has four active reservations`() {
        val attendee = Attendee(id = 1, name = "Ana", email = "ana@example.com")
        val event = Event(id = 2, name = "Concert", venue = "Arena", totalTickets = 10, availableTickets = 3)
        Mockito.`when`(attendeeRepository.findById(1)).thenReturn(Optional.of(attendee))
        Mockito.`when`(eventRepository.findById(2)).thenReturn(Optional.of(event))
        Mockito.`when`(
            reservationRepository.countByAttendeeIdAndStatus(1, ReservationStatus.ACTIVE)
        ).thenReturn(4)

        assertThrows<ReservationLimitExceededException> {
            reservationService.createReservation(1, 2)
        }
    }

    @Test
    fun `createReservation creates active reservation and decrements available tickets`() {
        val attendee = Attendee(id = 1, name = "Ana", email = "ana@example.com")
        val event = Event(id = 2, name = "Concert", venue = "Arena", totalTickets = 10, availableTickets = 3)
        Mockito.`when`(attendeeRepository.findById(1)).thenReturn(Optional.of(attendee))
        Mockito.`when`(eventRepository.findById(2)).thenReturn(Optional.of(event))
        Mockito.`when`(
            reservationRepository.countByAttendeeIdAndStatus(1, ReservationStatus.ACTIVE)
        ).thenReturn(3)
        Mockito.`when`(eventRepository.save(Mockito.any(Event::class.java))).thenAnswer { it.arguments[0] as Event }
        Mockito.`when`(reservationRepository.save(Mockito.any(Reservation::class.java))).thenAnswer {
            val reservation = it.arguments[0] as Reservation
            reservation.id = 5
            reservation
        }

        val response = reservationService.createReservation(1, 2)

        assertEquals(5, response.id)
        assertEquals("ACTIVE", response.status)
        assertEquals(2, event.availableTickets)
        assertNotNull(response.createdAt)
    }

    @Test
    fun `cancelReservation throws ReservationNotFoundException when reservation does not exist`() {
        Mockito.`when`(reservationRepository.findById(5)).thenReturn(Optional.empty())

        assertThrows<ReservationNotFoundException> {
            reservationService.cancelReservation(5)
        }
    }

    @Test
    fun `cancelReservation throws ReservationAlreadyCancelledException when reservation is cancelled`() {
        val reservation = Reservation(id = 5, status = ReservationStatus.CANCELLED)
        Mockito.`when`(reservationRepository.findById(5)).thenReturn(Optional.of(reservation))

        assertThrows<ReservationAlreadyCancelledException> {
            reservationService.cancelReservation(5)
        }
    }

    @Test
    fun `cancelReservation marks reservation as cancelled and increments available tickets`() {
        val attendee = Attendee(id = 1, name = "Ana", email = "ana@example.com")
        val event = Event(id = 2, name = "Concert", venue = "Arena", totalTickets = 10, availableTickets = 2)
        val reservation = Reservation(
            id = 5,
            attendee = attendee,
            event = event,
            status = ReservationStatus.ACTIVE,
            createdAt = LocalDateTime.now()
        )
        Mockito.`when`(reservationRepository.findById(5)).thenReturn(Optional.of(reservation))
        Mockito.`when`(eventRepository.save(Mockito.any(Event::class.java))).thenAnswer { it.arguments[0] as Event }
        Mockito.`when`(reservationRepository.save(Mockito.any(Reservation::class.java))).thenAnswer {
            it.arguments[0] as Reservation
        }

        val response = reservationService.cancelReservation(5)

        assertEquals("CANCELLED", response.status)
        assertEquals(3, event.availableTickets)
    }
}
