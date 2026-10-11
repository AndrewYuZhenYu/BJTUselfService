package team.bjtuss.bjtuselfservice.screen

import androidx.compose.foundation.Image
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape
import team.bjtuss.bjtuselfservice.R
import team.bjtuss.bjtuselfservice.RouteManager
import team.bjtuss.bjtuselfservice.component.elasticOverscroll
import team.bjtuss.bjtuselfservice.component.appLaunchBounds
import androidx.lifecycle.Lifecycle


@Composable
fun SpaceScreen(navController: NavController, enableLaunchMotion: Boolean = true) {

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize().elasticOverscroll(),
        columns = GridCells.Adaptive(140.dp),
        contentPadding = PaddingValues(
            top = 10.dp,
            bottom = 112.dp,
            start = 10.dp,
            end = 10.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(spaces.size, key = { spaces[it].route }) { index ->
            val space = spaces[index]
            SpaceCard(
                title = space.title,
                image = space.image,
                icon = space.icon,
                modifier = if (enableLaunchMotion) Modifier.appLaunchBounds(space.route) else Modifier,
                backgroundColor = MaterialTheme.colorScheme.primary
            ) {
                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.navigate(space.route) { launchSingleTop = true }
                }
            }
        }
    }
}

@Composable
fun SpaceCard(
    title: String,
    image: Int?,
    icon: ImageVector? = null,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    opening: Boolean = false,
    onClick: () -> Unit = {}
) {
    val hasBackground = team.bjtuss.bjtuselfservice.LocalHasBackground.current
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.luminance() < 0.5f
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var touching by remember { mutableStateOf(false) }
    val held = touching || pressed || opening
    val pressScale = animateFloatAsState(
        targetValue = if (held) 0.95f else 1f,
        animationSpec = tween(if (held) 160 else 260, easing = FastOutSlowInEasing),
        label = "appCardPress"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .pointerInput(Unit) {
                // Observe immediately without consuming events needed by the grid or Card.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    touching = true
                    try {
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed || change.isConsumed ||
                                (change.position - down.position).getDistance() > viewConfiguration.touchSlop
                            ) break
                        } while (true)
                    } finally {
                        touching = false
                    }
                }
            }
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
            },
        shape = SquircleShape(
            48.dp,
            CornerSmoothing.Medium
        ),
        elevation = CardDefaults.elevatedCardElevation(
            if (hasBackground || isDark) 0.dp else 8.dp
        ),
        border = when {
            isDark -> androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f))
            hasBackground -> androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            else -> null
        },
        colors = CardDefaults.elevatedCardColors(
            containerColor = when {
                isDark && hasBackground -> colors.surfaceContainerHigh.copy(alpha = 0.9f)
                isDark -> colors.surfaceContainerHigh
                hasBackground -> Color.Transparent
                else -> colors.surfaceVariant.copy(alpha = 0.1f).compositeOver(backgroundColor)
            },
        )
    ) {
        Column(
            contentModifier
                .aspectRatio(1.0f)
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = if (isDark) colors.onSurface else Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isDark) colors.onSurfaceVariant else Color.White,
                    modifier = Modifier.size(70.dp).align(Alignment.End)
                )
            } else if (image != null) {
                Image(
                    modifier = Modifier.size(70.dp).align(Alignment.End),
                    painter = painterResource(id = image),
                    contentDescription = title,
                    contentScale = if (image == R.drawable.room_occupancy) ContentScale.Crop else ContentScale.Fit
                )
            }

        }
    }
}

private val spaces = listOf(
    Space("成绩", R.drawable.grade, RouteManager.Grade),
    Space("课程表", R.drawable.course, RouteManager.CourseSchedule),
    Space("考试安排", R.drawable.exam, RouteManager.ExamSchedule),
    Space("作业", R.drawable.homework, RouteManager.HomeWork),
    Space("课件", R.drawable.courseware, RouteManager.Courseware),
    Space("教室占用", R.drawable.room_occupancy, RouteManager.RoomOccupancy),
    Space("教室人数评估", R.drawable.detect, RouteManager.Building),
    Space("其他功能", R.drawable.other_function, RouteManager.OtherFunction)
)

private data class Space(
    val title: String,
    val image: Int?,
    val route: String,
    val icon: ImageVector? = null,
)


