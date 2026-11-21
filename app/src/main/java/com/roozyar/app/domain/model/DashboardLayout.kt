package com.roozyar.app.domain.model

enum class DashboardCard(val label: String) {
    PROGRESS("پیشرفت امروز"),
    TODAY_TASKS("وظایف امروز"),
    HABITS("عادت‌های امروز"),
    EVENTS("رویدادهای امروز"),
    FOCUS("تمرکز امروز"),
    GOALS("اهداف فعال"),
    LEVEL("سطح و امتیاز"),
    QUICK_ACTIONS("دسترسی سریع");

    companion object {
        fun fromName(name: String): DashboardCard? = entries.firstOrNull { it.name == name }
    }
}

enum class DashboardMode(val label: String) {
    GENERAL("عمومی"),
    EXAM("کنکور"),
    INTERNSHIP("کارآموزی");

    companion object {
        fun fromName(name: String?): DashboardMode = entries.firstOrNull { it.name == name } ?: GENERAL
    }
}

/** [order] always lists every card exactly once; [hidden] cards keep their place but are not shown. */
data class DashboardLayout(val mode: DashboardMode, val order: List<DashboardCard>, val hidden: Set<DashboardCard>) {
    val visible: List<DashboardCard> get() = order.filter { it !in hidden }

    fun toggled(card: DashboardCard): DashboardLayout = copy(hidden = if (card in hidden) hidden - card else hidden + card)

    /** Moves [card] by [delta] places (negative = up); stays put at either end. */
    fun moved(card: DashboardCard, delta: Int): DashboardLayout {
        val from = order.indexOf(card)
        val to = from + delta
        if (from < 0 || to !in order.indices) return this
        val list = order.toMutableList()
        list.removeAt(from)
        list.add(to, card)
        return copy(order = list)
    }

    companion object {
        /** Ready-made layouts. The exam mode leads with focus time and goals; internship leads with the day's events. */
        fun preset(mode: DashboardMode): DashboardLayout {
            val shown: List<DashboardCard> = when (mode) {
                DashboardMode.GENERAL -> listOf(
                    DashboardCard.PROGRESS, DashboardCard.EVENTS, DashboardCard.TODAY_TASKS,
                    DashboardCard.HABITS, DashboardCard.GOALS, DashboardCard.QUICK_ACTIONS,
                )
                DashboardMode.EXAM -> listOf(
                    DashboardCard.FOCUS, DashboardCard.TODAY_TASKS, DashboardCard.GOALS,
                    DashboardCard.HABITS, DashboardCard.PROGRESS, DashboardCard.LEVEL,
                )
                DashboardMode.INTERNSHIP -> listOf(
                    DashboardCard.EVENTS, DashboardCard.TODAY_TASKS, DashboardCard.PROGRESS,
                    DashboardCard.GOALS, DashboardCard.FOCUS, DashboardCard.QUICK_ACTIONS,
                )
            }
            val rest = DashboardCard.entries.filter { it !in shown }
            return DashboardLayout(mode, shown + rest, rest.toSet())
        }

        /**
         * Reads a stored layout tolerantly: unknown names are dropped, duplicates ignored, and cards missing from
         * [order] are appended hidden. Anything absent falls back to the preset of the stored mode.
         */
        fun parse(mode: String?, order: String?, hidden: String?): DashboardLayout {
            val m = DashboardMode.fromName(mode)
            val base = preset(m)
            if (order == null) return base
            val parsed = order.split(',').mapNotNull { DashboardCard.fromName(it.trim()) }.distinct()
            val missing = DashboardCard.entries.filter { it !in parsed }
            val hiddenSet = hidden?.split(',')?.mapNotNull { DashboardCard.fromName(it.trim()) }?.toSet().orEmpty()
            return DashboardLayout(m, parsed + missing, hiddenSet + missing)
        }
    }

    fun serializedOrder(): String = order.joinToString(",") { it.name }

    fun serializedHidden(): String = hidden.joinToString(",") { it.name }
}
