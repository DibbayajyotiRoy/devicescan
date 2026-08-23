package com.devicelens.app.domain.model

/**
 * Live progress for a running scan.
 *
 * A network scan legitimately takes tens of seconds — an address sweep and a
 * Bluetooth inquiry cannot be rushed. A spinner with no explanation makes that
 * wait feel broken, so every phase reports what it is doing, how far along it
 * is, and how many devices have turned up so far.
 */
data class ScanProgress(
    val phase: Phase = Phase.IDLE,
    val message: String = "",
    /** 0-100 across the whole scan, not just the current phase. */
    val percent: Int = 0,
    val devicesFound: Int = 0
) {
    enum class Phase(val weight: Int, val label: String) {
        IDLE(0, ""),
        NETWORK(5, "Reading network settings"),
        DISCOVERY(45, "Finding devices"),
        IDENTIFY(25, "Identifying devices"),
        BLUETOOTH(15, "Checking Bluetooth"),
        ANALYSIS(10, "Analysing risks"),
        DONE(100, "Done")
    }

    val isRunning: Boolean get() = phase != Phase.IDLE && phase != Phase.DONE

    companion object {
        val IDLE = ScanProgress()
    }
}
