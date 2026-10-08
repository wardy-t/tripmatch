package com.tomward.tripmatch.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DestinationNotFoundException::class)
    fun handleDestinationNotFound(
        exception: DestinationNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val status = HttpStatus.NOT_FOUND

        return ResponseEntity.status(status).body(
            ApiError(
                status = status.value(),
                error = status.reasonPhrase,
                message = exception.message ?: "Destination was not found",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationFailure(
        exception: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ApiError> {
        val status = HttpStatus.BAD_REQUEST

        val fieldErrors = exception.bindingResult.fieldErrors.associate {
            it.field to (it.defaultMessage ?: "Invalid value")
        }

        return ResponseEntity.status(status).body(
            ApiError(
                status = status.value(),
                error = status.reasonPhrase,
                message = "Request validation failed",
                path = request.requestURI,
                fieldErrors = fieldErrors
            )
        )
    }
}