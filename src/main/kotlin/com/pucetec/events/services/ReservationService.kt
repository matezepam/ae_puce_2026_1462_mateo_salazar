package com.pucetec.events.services

import com.pucetec.events.dto.ReservationResponse
import com.pucetec.events.entities.Reservation
import com.pucetec.events.entities.ReservationStatus
import com.pucetec.events.exceptions.AttendeeNotFoundException
import com.pucetec.events.exceptions.EventNotFoundException
import com.pucetec.events.exceptions.ReservationAlreadyCancelledException
import com.pucetec.events.exceptions.ReservationLimitExceededException
import com.pucetec.events.exceptions.ReservationNotFoundException
import com.pucetec.events.exceptions.SoldOutException
import com.pucetec.events.mappers.toResponse
import com.pucetec.events.repositories.AttendeeRepository
import com.pucetec.events.repositories.EventRepository
import com.pucetec.events.repositories.ReservationRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val attendeeRepository: AttendeeRepository,
    private val eventRepository: EventRepository
) {

    fun getAllReservations(): List<ReservationResponse> =
        reservationRepository.findAll().map { it.toResponse() }

    fun createReservation(attendeeId: Long, eventId: Long): ReservationResponse {
        val attendee = attendeeRepository.findById(attendeeId)
            .orElseThrow { AttendeeNotFoundException("Attendee with id $attendeeId was not found") }
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException("Event with id $eventId was not found") }

        if (event.availableTickets <= 0) {
            throw SoldOutException("Event with id $eventId is sold out")
        }

        val activeReservations = reservationRepository.countByAttendeeIdAndStatus(
            attendeeId,
            ReservationStatus.ACTIVE
        )
        if (activeReservations >= 4) {
            throw ReservationLimitExceededException("Attendee cannot have more than 4 active reservations")
        }

        event.availableTickets -= 1
        eventRepository.save(event)

        val reservation = Reservation(
            attendee = attendee,
            event = event,
            status = ReservationStatus.ACTIVE,
            createdAt = LocalDateTime.now()
        )
        return reservationRepository.save(reservation).toResponse()
    }

    fun cancelReservation(reservationId: Long): ReservationResponse {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { ReservationNotFoundException("Reservation with id $reservationId was not found") }

        if (reservation.status == ReservationStatus.CANCELLED) {
            throw ReservationAlreadyCancelledException("Reservation with id $reservationId is already cancelled")
        }

        reservation.status = ReservationStatus.CANCELLED
        reservation.event.availableTickets += 1
        eventRepository.save(reservation.event)

        return reservationRepository.save(reservation).toResponse()
    }
}
