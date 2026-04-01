package com.xiehe.spine.ui.motion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

fun appRouteContentTransition(
    initialOrder: Int,
    targetOrder: Int,
): ContentTransform {
    val direction = if (targetOrder >= initialOrder) 1 else -1
    return (
        fadeIn(animationSpec = AppMotion.routeFadeInSpec()) +
            slideInHorizontally(animationSpec = AppMotion.routeSlideInSpec()) { full ->
                direction * full / 6
            }
        ).togetherWith(
            fadeOut(animationSpec = AppMotion.routeFadeOutSpec()) +
                slideOutHorizontally(animationSpec = AppMotion.routeSlideOutSpec()) { full ->
                    -direction * full / 7
                },
        )
}

fun <T> AppRouteTransition(
    initialState: T,
    targetState: T,
    orderOf: (T) -> Int,
): ContentTransform = appRouteContentTransition(
    initialOrder = orderOf(initialState),
    targetOrder = orderOf(targetState),
)

fun overlayEntryEnterTransition(): EnterTransition =
    fadeIn(animationSpec = AppMotion.routeFadeInSpec()) +
        slideInHorizontally(animationSpec = AppMotion.routeSlideInSpec()) { full -> full / 5 }

fun overlayEntryExitTransition(): ExitTransition =
    fadeOut(animationSpec = AppMotion.instantSpec())

@Composable
fun AppOverlayEntryHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        modifier = modifier,
        enter = overlayEntryEnterTransition(),
        exit = overlayEntryExitTransition(),
        label = "app_overlay_entry_host",
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun <T> AppRouteContentHost(
    targetState: T,
    orderOf: (T) -> Int,
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            AppRouteTransition(
                initialState = initialState,
                targetState = targetState,
                orderOf = orderOf,
            )
        },
        modifier = modifier,
        label = label,
        content = { state -> content(state) },
    )
}
