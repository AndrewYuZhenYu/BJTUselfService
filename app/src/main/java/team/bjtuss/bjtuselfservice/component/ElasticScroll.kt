package team.bjtuss.bjtuselfservice.component

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

internal fun elasticDisplacement(pull: Float, limit: Float): Float =
    if (limit <= 0f) 0f else sign(pull) * limit * (1f - 1f / (1f + abs(pull) * 0.5f / limit))

internal fun consumedReturningPull(pull: Float, delta: Float): Float =
    if (pull * delta < 0f) sign(delta) * minOf(abs(pull), abs(delta)) else 0f

fun Modifier.elasticOverscroll(
    orientation: Orientation = Orientation.Vertical,
    allowStart: Boolean = true,
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val limit = with(LocalDensity.current) { 64.dp.toPx() }
    var pull by remember(orientation, allowStart) { mutableFloatStateOf(0f) }
    var settling by remember { androidx.compose.runtime.mutableStateOf<Job?>(null) }
    val connection = remember(orientation, allowStart, limit, scope) {
        object : NestedScrollConnection {
            fun axis(offset: Offset) = if (orientation == Orientation.Vertical) offset.y else offset.x
            fun velocityAxis(velocity: Velocity) = if (orientation == Orientation.Vertical) velocity.y else velocity.x
            fun offset(value: Float) = if (orientation == Orientation.Vertical) Offset(0f, value) else Offset(value, 0f)
            fun velocity(value: Float) = if (orientation == Orientation.Vertical) Velocity(0f, value) else Velocity(value, 0f)

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                settling?.cancel()
                val consumed = consumedReturningPull(pull, axis(available))
                pull += consumed
                return offset(consumed)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val delta = axis(available)
                if (source != NestedScrollSource.UserInput || delta == 0f ||
                    (!allowStart && delta > 0f)
                ) return Offset.Zero
                // Only unused edge movement is absorbed; ordinary scrolling stays native.
                pull = (pull + delta).coerceIn(-limit * 8f, limit * 8f)
                return offset(delta)
            }

            override suspend fun onPreFling(available: Velocity): Velocity =
                if (pull != 0f) velocity(velocityAxis(available)) else Velocity.Zero

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val remaining = velocityAxis(available)
                if (pull == 0f && (allowStart || remaining < 0f)) {
                    pull = (remaining * 0.012f).coerceIn(-limit * 2f, limit * 2f)
                }
                if (pull == 0f) return Velocity.Zero
                settling?.cancel()
                settling = scope.launch {
                    animate(pull, 0f, animationSpec = spring(dampingRatio = 0.85f, stiffness = 250f)) { value, _ ->
                        pull = value
                    }
                    pull = 0f
                }
                return velocity(remaining)
            }
        }
    }
    clipToBounds()
        .nestedScroll(connection)
        .graphicsLayer {
            val displacement = elasticDisplacement(pull, limit)
            if (orientation == Orientation.Vertical) translationY = displacement
            else translationX = displacement
        }
}
