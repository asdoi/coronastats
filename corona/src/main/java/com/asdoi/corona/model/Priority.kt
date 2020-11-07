package com.asdoi.corona.model

enum class Priority {
    WORLD,
    GLOBAL,
    NATIONAL,
    COUNTY,
    LOCAL,
    NONE;

    private fun getPriorityInt(): Int {
        return when (this) {
            NONE -> 0
            WORLD -> 1
            GLOBAL -> 2
            NATIONAL -> 3
            COUNTY -> 4
            LOCAL -> 5
        }
    }

    fun isHigher(priority: Priority) = getPriorityInt() > priority.getPriorityInt()

    fun isLower(priority: Priority) = getPriorityInt() < priority.getPriorityInt()

    fun isEqual(priority: Priority) = priority.getPriorityInt() == getPriorityInt()
}