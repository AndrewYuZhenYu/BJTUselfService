package team.bjtuss.bjtuselfservice.utils

data class CoursePlaceDisplay(
    val building: String,
    val room: String?,
)

fun compactCoursePlace(place: String): CoursePlaceDisplay {
    val fields = place.split(',', '，', limit = 3)
    val hasCampus = fields.firstOrNull()?.trim() in listOf("海淀西校区", "海淀东校区")
    return when {
        hasCampus && fields.size >= 2 -> CoursePlaceDisplay(
            building = shortBuildingName(fields[1]),
            room = fields.getOrNull(2)
        )
        fields.size == 3 -> CoursePlaceDisplay(
            building = "${fields[0]},${shortBuildingName(fields[1])}",
            room = fields[2]
        )
        fields.size == 2 -> CoursePlaceDisplay(fields[0], fields[1])
        else -> CoursePlaceDisplay(place, null)
    }
}

fun compactCoursePlaceText(place: String): String {
    val display = compactCoursePlace(place)
    return listOfNotNull(display.building, display.room)
        .filter { it.isNotBlank() }
        .joinToString(" ")
}

private fun shortBuildingName(building: String): String = when (building.trim()) {
    "学生活动服务中心" -> "学活"
    "第九教学楼" -> "九教"
    "第七教学楼" -> "七教"
    "第五教学楼" -> "五教"
    "第十七号教学楼" -> "建艺"
    "逸夫教学楼" -> "逸夫楼"
    else -> building
}
