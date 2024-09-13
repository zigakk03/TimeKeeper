package com.example.timekeeper.helpers

data class CalendarCell(
    val dayString: String,
    val isToday: Boolean,
    val isDayOff: Boolean,
    val isActive: Boolean
)