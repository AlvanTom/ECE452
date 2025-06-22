package com.example.ece452.data

import java.util.Date

data class Session(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val location: String = "",
    val isIndoor: Boolean = true,
    val gymName: String? = null,
    val createdAt: String = "",
    val routesCount: Int = 0,
    val routes: List<Route> = emptyList()
)

data class Route(
    val id: String = "",
    val routeName: String = "",
    val difficulty: String = "",
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val attempts: List<Attempt> = emptyList()
)

data class Attempt(
    val id: String = "",
    val success: Boolean = false,
    val createdAt: String = ""
)

data class SessionSummary(
    val id: String = "",
    val title: String = "",
    val location: String = "",
    val isIndoor: Boolean = true,
    val gymName: String? = null,
    val createdAt: String = "",
    val routesCount: Int = 0
) 