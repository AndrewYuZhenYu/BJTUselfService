package team.bjtuss.bjtuselfservice.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape

@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalAppTransition = staticCompositionLocalOf<SharedTransitionScope?> { null }
internal val LocalAppVisibility = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.appLaunchContainer(
    route: String,
    isDestination: Boolean,
    visibility: AnimatedVisibilityScope? = LocalAppVisibility.current,
): Modifier {
    val sharedScope = LocalAppTransition.current ?: return this
    if (visibility == null) return this
    val corner by visibility.transition.animateDp(
        transitionSpec = { tween(380, easing = FastOutSlowInEasing) },
        label = "appLaunchCorner"
    ) { state ->
        if ((state == EnterExitState.Visible) == isDestination) 0.dp else 48.dp
    }
    return with(sharedScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState("app-launch:$route"),
            animatedVisibilityScope = visibility,
            boundsTransform = { _, _ -> spring(dampingRatio = 1f, stiffness = 260f) },
            enter = fadeIn(tween(300, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(300, easing = FastOutSlowInEasing)),
        ).clip(SquircleShape(corner, CornerSmoothing.Medium))
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun AppDestination(
    route: String,
    visibility: AnimatedVisibilityScope,
    content: @Composable () -> Unit,
) {
    val contentAlpha by visibility.transition.animateFloat(
        transitionSpec = {
            if (targetState == EnterExitState.Visible) tween(180, delayMillis = 150)
            else tween(100)
        },
        label = "appLaunchContentAlpha"
    ) { state ->
        if (state == EnterExitState.Visible) 1f else 0f
    }
    Box(
        Modifier.fillMaxSize()
            .appLaunchContainer(route, isDestination = true, visibility = visibility)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = contentAlpha }) { content() }
    }
}
