package com.mapbox.navigation.core.sensors

/**
 * Options for the [SensorEventEmitter]. Use this to decide which sensors are
 * enabled and the frequency.
 *
 * @param enabledSensorTypes set of enabled sensors
 * @param signalsPerSecond signals per second received from sensors
 * @param builder used for updating options
 */
class SensorOptions private constructor(
    val enabledSensorTypes: Set<Int>,
    val signalsPerSecond: Int,
    val builder: Builder
) {
    /**
     * @return the builder that created the [SensorOptions]
     */
    fun toBuilder() = builder

    /**
     * Regenerate whenever a change is made
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorOptions

        if (enabledSensorTypes != other.enabledSensorTypes) return false
        if (signalsPerSecond != other.signalsPerSecond) return false

        return true
    }

    /**
     * Regenerate whenever a change is made
     */
    override fun hashCode(): Int {
        var result = enabledSensorTypes.hashCode()
        result = 31 * result + signalsPerSecond
        return result
    }

    /**
     * Builder of [SensorOptions]
     */
    class Builder {
        private val enabledSensors: MutableSet<Int> = mutableSetOf()
        private var signalsPerSecond: Int = 25

        /**
         * Enable all available sensors
         *
         * @return Builder
         */
        fun enableAllSensors(): Builder {
            this.enabledSensors.addAll(SensorMapper.getSupportedSensorTypes())
            return this
        }

        /**
         * Set a set of sensors that will be handled
         *
         * @param sensorTypes that will be handled
         * @return Builder
         */
        fun enableSensors(sensorTypes: Set<Int>): Builder {
            this.enabledSensors.addAll(sensorTypes)
            return this
        }

        /**
         * Signals per second received from sensors
         *
         * @param signalsPerSecond received from sensors
         * @return Builder
         */
        fun signalsPerSecond(signalsPerSecond: Int): Builder {
            this.signalsPerSecond = signalsPerSecond
            return this
        }

        /**
         * Build a new instance of [SensorOptions]
         *
         * @return SensorOptions
         */
        fun build(): SensorOptions {
            return SensorOptions(
                enabledSensorTypes = enabledSensors,
                signalsPerSecond = signalsPerSecond,
                builder = this
            )
        }
    }
}
