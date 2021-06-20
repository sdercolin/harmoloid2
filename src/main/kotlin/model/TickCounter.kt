package model

import com.sdercolin.harmoloid.core.model.DEFAULT_METER_HIGH
import com.sdercolin.harmoloid.core.model.DEFAULT_METER_LOW
import com.sdercolin.harmoloid.core.model.TICKS_IN_FULL_NOTE

class TickCounter(
    private val tickRate: Double = 1.0,
    private val ticksInFullNote: Long = TICKS_IN_FULL_NOTE.toLong()
) {

    var tick = 0L
        private set

    var measure = 0
        private set

    var numerator = DEFAULT_METER_HIGH
        private set

    var denominator = DEFAULT_METER_LOW
        private set

    private val ticksInMeasure get() = ticksInFullNote * numerator / denominator

    fun goToTick(newTick: Long, newNumerator: Int? = null, newDenominator: Int? = null) {
        val normalizedNewTick = newTick / tickRate
        val tickDiff = normalizedNewTick - tick
        val measureDiff = tickDiff / ticksInMeasure
        measure += measureDiff.toInt()
        tick = normalizedNewTick.toLong()
        numerator = newNumerator ?: numerator
        denominator = newDenominator ?: denominator
    }
}
