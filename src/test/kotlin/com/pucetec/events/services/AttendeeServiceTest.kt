package com.pucetec.events.services

import com.pucetec.events.dto.AttendeeRequest
import com.pucetec.events.entities.Attendee
import com.pucetec.events.exceptions.BlankFieldException
import com.pucetec.events.repositories.AttendeeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AttendeeServiceTest {

    @Mock
    lateinit var attendeeRepository: AttendeeRepository

    @InjectMocks
    lateinit var attendeeService: AttendeeService

    @Test
    fun `createAttendee throws BlankFieldException when name is blank`() {
        val request = AttendeeRequest(name = "", email = "ana@example.com")

        assertThrows<BlankFieldException> {
            attendeeService.createAttendee(request)
        }
    }

    @Test
    fun `createAttendee throws BlankFieldException when email is blank`() {
        val request = AttendeeRequest(name = "Ana", email = " ")

        assertThrows<BlankFieldException> {
            attendeeService.createAttendee(request)
        }
    }

    @Test
    fun `createAttendee saves valid attendee`() {
        val request = AttendeeRequest(name = "Ana", email = "ana@example.com")
        Mockito.`when`(attendeeRepository.save(Mockito.any(Attendee::class.java))).thenAnswer {
            val attendee = it.arguments[0] as Attendee
            attendee.id = 1
            attendee
        }

        val response = attendeeService.createAttendee(request)

        assertEquals(1, response.id)
        assertEquals("Ana", response.name)
        assertEquals("ana@example.com", response.email)
    }
}
