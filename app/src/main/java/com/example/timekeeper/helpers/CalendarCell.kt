package com.example.timekeeper.helpers

data class CalendarCell(
    val dayString: String,
    var events: MutableList<EventDataTransfer>,
    val isToday: Boolean,
    val isDayOff: Boolean,
    val isActive: Boolean,
    var isSelected: Boolean
)

data class EventDataTransfer(
    val title: String,
    val color: String?
)