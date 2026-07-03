package com.pucetec.events.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ExceptionResponse(
    val message: String,
    val source: String
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BlankFieldException::class, InvalidCapacityException::class)
    fun handleBadRequest(exception: RuntimeException): ResponseEntity<ExceptionResponse> =
        buildResponse(exception, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(
        AttendeeNotFoundException::class,
        EventNotFoundException::class,
        ReservationNotFoundException::class
    )
    fun handleNotFound(exception: RuntimeException): ResponseEntity<ExceptionResponse> =
        buildResponse(exception, HttpStatus.NOT_FOUND)

    @ExceptionHandler(
        SoldOutException::class,
        ReservationLimitExceededException::class,
        ReservationAlreadyCancelledException::class
    )
    fun handleConflict(exception: RuntimeException): ResponseEntity<ExceptionResponse> =
        buildResponse(exception, HttpStatus.CONFLICT)

    private fun buildResponse(
        exception: RuntimeException,
        status: HttpStatus
    ): ResponseEntity<ExceptionResponse> =
        ResponseEntity.status(status).body(
            ExceptionResponse(
                message = exception.message ?: status.reasonPhrase,
                source = exception::class.simpleName ?: "Exception"
            )
        )
}
