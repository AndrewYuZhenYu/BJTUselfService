package team.bjtuss.bjtuselfservice.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CoursePlaceFormatterTest {
    @Test
    fun removesCampusAndKeepsRoomDirection() {
        assertEquals("九教 东102", compactCoursePlaceText("海淀西校区,第九教学楼,东102"))
        assertEquals("学活 YF209", compactCoursePlaceText("海淀东校区,学生活动服务中心,YF209"))
    }

    @Test
    fun keepsUnlistedBuildingsAndRooms() {
        assertEquals("思源西楼 SX107", compactCoursePlaceText("海淀西校区,思源西楼,SX107"))
        assertEquals("逸夫楼 YF209", compactCoursePlaceText("海淀西校区,逸夫教学楼,YF209"))
    }
}
