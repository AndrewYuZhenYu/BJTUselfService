package team.bjtuss.bjtuselfservice.utils

fun parseCourseWeeks(courseTime: String): List<Int> = try {
    val timeText = courseTime.replace("第", "").replace("周", "")
    buildList {
        timeText.split(",").forEach { part ->
            if (part.contains("-")) {
                val bounds = part.split("-")
                for (week in bounds[0].toInt()..bounds[1].toInt()) add(week)
            } else {
                add(part.toInt())
            }
        }
    }
} catch (_: Exception) {
    emptyList()
}
