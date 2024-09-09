package net.backupcup.stainedlenses.registry

import net.backupcup.stainedlenses.StainedLenses
import net.backupcup.stainedlenses.utils.TimedEvent
import net.backupcup.stainedlenses.utils.TimedEventAction
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.util.Identifier
import java.util.concurrent.CopyOnWriteArrayList

object RegisterTimedEvents {
    private val TIMED_EVENT: Identifier = Identifier(StainedLenses.MOD_ID, "timed_event")
    private var timedEvents = CopyOnWriteArrayList<TimedEvent>()

    /**
     * Creates an instance of a [TimedEvent] that will wait [tickDelay] amount of ticks (where 1 tick = 1/20 second) and then execute the action that was specified in the [actionOnReady]. Setting [isRepeatable] to true will make it repeat the action every [tickDelay] ticks.
     */
    fun createTimedEvent(isRepeatable: Boolean = false, tickDelay: Int = 20, actionOnReady: TimedEventAction): TimedEvent {
        val newEvent = TimedEvent(isRepeatable, tickDelay, actionOnReady)
        timedEvents.add(newEvent)
        return newEvent
    }

    fun destroyTimedEvent(ticker: TimedEvent) {
        timedEvents.remove(ticker)
    }

    fun registerServerTick(){
        ServerTickEvents.START_SERVER_TICK.register(TIMED_EVENT) {
            timedEvents.forEach { it.tickUp() }
        }
    }
}