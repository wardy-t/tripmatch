package com.tomward.tripmatch.exception

class DestinationNotFoundException(id: Long) :
    RuntimeException("Destination with id $id was not found")