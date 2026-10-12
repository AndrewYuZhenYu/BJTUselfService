package team.bjtuss.bjtuselfservice.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
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
internal fun Modifier.appLaunchBackground(
    route: String,
    isDestination: Boolean,
    visibility: AnimatedVisibilityScope? = LocalAppVisibility.current,
): Modifier {
    val sharedScope = LocalAppTransition.current ?: return this
    if (visibility == null) return this
    val corner by visibility.transition.animateDp(label = "appLaunchCorner") { state ->
        if ((state == EnterExitState.Visible) == isDestination) 0.dp else 48.dp
    }
    return with(sharedScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState("app-launch:$route"),
            animatedVisibilityScope = visibility,
            boundsTransform = { _, _ -> spring(dampingRatio = 0.9f, stiffness = 500f) },
            enter = fadeIn(spring(stiffness = 1600f)),
            exit = fadeOut(spring(stiffness = 1600f)),
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
    val backgroundAlpha by visibility.transition.animateFloat(label = "appLaunchBackgroundAlpha") { state ->
        if (state == EnterExitState.Visible) 0f else 1f
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            Modifier.fillMaxSize()
                .appLaunchBackground(route, isDestination = true, visibility = visibility)
                .graphicsLayer { alpha = backgroundAlpha }
                .background(MaterialTheme.colorScheme.background)
        )
        Box(Modifier.fillMaxSize()) { content() }
    }
}
