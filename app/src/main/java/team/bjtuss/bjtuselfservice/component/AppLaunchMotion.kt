package team.bjtuss.bjtuselfservice.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

internal const val APP_LAUNCH_DURATION = 560
internal val AppLaunchEasing = CubicBezierEasing(0.2f, 0f, 0.15f, 1f)

@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalAppTransition = staticCompositionLocalOf<SharedTransitionScope?> { null }
internal val LocalAppVisibility = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.appLaunchBounds(
    route: String,
    visibility: AnimatedVisibilityScope? = LocalAppVisibility.current,
): Modifier {
    val sharedScope = LocalAppTransition.current ?: return this
    if (visibility == null) return this
    return with(sharedScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState("app-launch:$route"),
            animatedVisibilityScope = visibility,
            boundsTransform = { _, _ -> tween(APP_LAUNCH_DURATION, easing = AppLaunchEasing) },
            enter = fadeIn(tween(APP_LAUNCH_DURATION)),
            exit = fadeOut(tween(APP_LAUNCH_DURATION)),
            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopStart
            )
        )
    }
}

@Composable
internal fun AppDestination(
    route: String,
    visibility: AnimatedVisibilityScope,
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize().appLaunchBounds(route, visibility),
            color = MaterialTheme.colorScheme.background,
            content = {}
        )
        Box(
            modifier = Modifier.fillMaxSize().then(with(visibility) {
                Modifier.animateEnterExit(
                    enter = fadeIn(tween(520, delayMillis = 170)) +
                        slideInVertically(tween(560, delayMillis = 90, easing = AppLaunchEasing)) {
                            it / 24
                        },
                    exit = fadeOut(tween(280)) +
                        slideOutVertically(tween(400, easing = AppLaunchEasing)) { it / 32 }
                )
            }),
            content = { content() }
        )
    }
}
