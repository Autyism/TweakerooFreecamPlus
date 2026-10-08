plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11"

// src/ is always kept in the 1.21.11 state; never switch the active version.
// The renames below go one way: they are applied to the versions their condition is true for, and
// their reverse pattern "(?!)" matches nothing, so the 1.21.11 source is never touched by them.
// Only whole names that mean one thing in this code are renamed this way; everything else uses //? conditions.
val never = "(?!)"

stonecutter parameters {
    fun oneWay(condition: Boolean, vararg renames: Pair<String, String>) {
        for ((from, to) in renames) {
            replacements.regex(condition) { replace(from, to, never, to) }
        }
    }
}
