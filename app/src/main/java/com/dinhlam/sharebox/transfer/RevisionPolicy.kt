package com.dinhlam.sharebox.transfer

/** Causal history, independent of device clocks. Divergent histories never overwrite. */
object RevisionPolicy {
    enum class Decision { APPLY, SKIP, CONFLICT }

    fun decide(current: List<String>, incoming: List<String>): Decision {
        require(current.isNotEmpty() && incoming.isNotEmpty())
        require(current.distinct().size == current.size && incoming.distinct().size == incoming.size)
        return when {
            current == incoming -> Decision.SKIP
            incoming.size < current.size && current.take(incoming.size) == incoming -> Decision.SKIP
            incoming.size > current.size && incoming.take(current.size) == current -> Decision.APPLY
            else -> Decision.CONFLICT
        }
    }
}
