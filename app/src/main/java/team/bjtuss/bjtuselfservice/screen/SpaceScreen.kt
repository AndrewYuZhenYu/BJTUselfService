package team.bjtuss.bjtuselfservice.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape
import team.bjtuss.bjtuselfservice.R
import team.bjtuss.bjtuselfservice.RouteManager
import team.bjtuss.bjtuselfservice.component.elasticOverscroll
import team.bjtuss.bjtuselfservice.component.LocalAppVisibility
import team.bjtuss.bjtuselfservice.component.appLaunchContainer


@Composable
fun SpaceScreen(navController: NavController, enableLaunchMotion: Boolean) {

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
                launchRoute = if (enableLaunchMotion) space.route else null,
                backgroundColor = MaterialTheme.colorScheme.primary
            ) {
                if (navController.currentDestination?.route == RouteManager.Navigation) {
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
    launchRoute: String? = null,
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
    val hovered by interactionSource.collectIsHoveredAsState()
    var pressExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(180)
            pressExpanded = true
        } else {
            pressExpanded = false
        }
    }
    val lifted = (hovered || pressExpanded) && !opening
    val cardScale = animateFloatAsState(
        targetValue = when {
            opening -> 0.95f
            pressExpanded -> 1.06f
            pressed -> 0.95f
            hovered -> 1.04f
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = if (pressed && !pressExpanded) 140 else 280,
            easing = FastOutSlowInEasing
        ),
        label = "appCardPress"
    )
    val iconLift by animateDpAsState(
        targetValue = if (lifted) 6.dp else 0.dp,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "appCardIconLift"
    )
    val titleLift by animateDpAsState(
        targetValue = if (lifted) 4.dp else 0.dp,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "appCardTitleLift"
    )
    val cardElevation by animateDpAsState(
        targetValue = when {
            hasBackground || isDark || opening -> 0.dp
            lifted -> 12.dp
            pressed -> 0.dp
            else -> 8.dp
        },
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "appCardElevation"
    )
    val cardFill = when {
        isDark && hasBackground -> colors.surfaceContainerHigh.copy(alpha = 0.9f)
        isDark -> colors.surfaceContainerHigh
        hasBackground -> Color.Transparent
        else -> colors.surfaceVariant.copy(alpha = 0.1f).compositeOver(backgroundColor)
    }
    val visibility = LocalAppVisibility.current
    val launchContainer = if (launchRoute != null) {
        Modifier.appLaunchContainer(launchRoute, isDestination = false, visibility = visibility)
    } else Modifier
    val contentAlpha = if (launchRoute != null && visibility != null) {
        visibility.transition.animateFloat(
            transitionSpec = {
                if (targetState == EnterExitState.Visible) tween(180, delayMillis = 150)
                else tween(100)
            },
            label = "appCardContentAlpha"
        ) { state -> if (state == EnterExitState.Visible) 1f else 0f }.value
    } else 1f

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.then(launchContainer)
            .graphicsLayer {
                scaleX = cardScale.value
                scaleY = cardScale.value
            },
        shape = SquircleShape(
            48.dp,
            CornerSmoothing.Medium
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = cardElevation,
            pressedElevation = cardElevation,
            hoveredElevation = cardElevation
        ),
        border = when {
            isDark -> androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f))
            hasBackground -> androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            else -> null
        },
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.Transparent,
        )
    ) {
        Box(Modifier.aspectRatio(1.0f)) {
            Box(Modifier.matchParentSize().background(cardFill))
            Column(
                contentModifier.fillMaxSize().graphicsLayer { alpha = contentAlpha }.padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    modifier = Modifier.graphicsLayer { translationY = -titleLift.toPx() },
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
                            .graphicsLayer { translationY = -iconLift.toPx() }
                    )
                } else if (image != null) {
                    Image(
                        modifier = Modifier.size(70.dp).align(Alignment.End)
                            .graphicsLayer { translationY = -iconLift.toPx() },
                        painter = painterResource(id = image),
                        contentDescription = title,
                        contentScale = if (image == R.drawable.room_occupancy) ContentScale.Crop else ContentScale.Fit
                    )
                }
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


