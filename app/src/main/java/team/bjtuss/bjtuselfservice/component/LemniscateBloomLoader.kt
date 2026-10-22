package team.bjtuss.bjtuselfservice.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun LemniscateBloomLoader(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val color = MaterialTheme.colorScheme.primary
    val animation = rememberInfiniteTransition(label = "Lemniscate bloom")
    val progress by animation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "Particle progress"
    )
    val pulse by animation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "Curve pulse"
    )

    Canvas(modifier = modifier.size(size)) {
        val unit = min(this.size.width, this.size.height) / 100f
        val detailScale = 0.52 + ((sin(pulse * 2 * PI + 0.55) + 1) / 2) * 0.48
        val amplitude = 20.0 + 7.0 * detailScale

        fun point(fraction: Double): Offset {
            val angle = fraction * 2 * PI
            val sine = sin(angle)
            val denominator = 1 + sine * sine
            return Offset(
                ((50 + amplitude * cos(angle) / denominator) * unit).toFloat(),
                ((50 + amplitude * sine * cos(angle) / denominator) * unit).toFloat()
            )
        }

        val path = Path()
        for (step in 0..240) {
            val position = point(step / 240.0)
            if (step == 0) path.moveTo(position.x, position.y)
            else path.lineTo(position.x, position.y)
        }
        drawPath(
            path = path,
            color = color.copy(alpha = 0.1f),
            style = Stroke(width = 4.8f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        for (index in 69 downTo 0) {
            val tailOffset = index / 69.0
            val fraction = (progress - tailOffset * 0.4 + 1.0) % 1.0
            val fade = (1 - tailOffset).pow(0.56)
            drawCircle(
                color = color.copy(alpha = (0.04 + fade * 0.96).toFloat()),
                radius = ((0.9 + fade * 2.7) * unit).toFloat(),
                center = point(fraction)
            )
        }
    }
}
