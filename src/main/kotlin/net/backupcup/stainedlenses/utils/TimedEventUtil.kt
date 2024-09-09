package net.backupcup.stainedlenses.utils

import net.backupcup.stainedlenses.registry.RegisterTimedEvents

open class TimedEvent(private val isRepeatable: Boolean = false, private val tickDelay: Int = 20, private var actionOnReady: TimedEventAction){
    private var tick = 1
    private var ready = false

    fun tickUp() {
        if (++tick == tickDelay && !ready) {
            ready = true
            ready()
            if (isRepeatable) {
                tick = 1
                ready = false
            }
        } else {
            ready = false
        }
    }

    open fun ready() {
        actionOnReady()
        if (!isRepeatable) RegisterTimedEvents.destroyTimedEvent(this)
    }

    override fun toString(): String { return "| delay: $tickDelay\n| tick: $tick\n| ready: $ready\n| isRepeatable: $isRepeatable" }
}

fun interface TimedEventAction {
    operator fun invoke()
}