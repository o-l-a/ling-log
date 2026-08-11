package com.example.myinputlog.data

class UnauthenticatedAccessException : IllegalStateException(
    "Attempted to access the database while no user was logged in."
)