package team.bjtuss.bjtuselfservice.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElasticScrollTest {
    @Test
    fun displacementIsSymmetricBoundedAndResisted() {
        val limit = 64f
        assertEquals(0f, elasticDisplacement(0f, limit), 0f)
        for (pull in listOf(1f, 20f, 100f, 1000f)) {
            val distance = elasticDisplacement(pull, limit)
            assertTrue(distance > 0f && distance < minOf(pull, limit))
            assertEquals(-distance, elasticDisplacement(-pull, limit), 0.001f)
        }
    }

    @Test
    fun resistanceIncreasesTowardTheLimit() {
        val nearEdge = elasticDisplacement(20f, 64f) - elasticDisplacement(10f, 64f)
        val farFromEdge = elasticDisplacement(220f, 64f) - elasticDisplacement(210f, 64f)
        assertTrue(nearEdge > farFromEdge)
    }

    @Test
    fun returningDragOnlyConsumesTheRemainingPull() {
        assertEquals(-20f, consumedReturningPull(40f, -20f), 0f)
        assertEquals(-40f, consumedReturningPull(40f, -100f), 0f)
        assertEquals(40f, consumedReturningPull(-40f, 100f), 0f)
        assertEquals(0f, consumedReturningPull(0f, 100f), 0f)
        assertEquals(0f, consumedReturningPull(40f, 20f), 0f)
    }
}
