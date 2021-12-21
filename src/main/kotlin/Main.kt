// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.concurrent.fixedRateTimer

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App(onZero: () -> Unit = {}) {
    var countdownInt by remember {
        mutableStateOf(TIMER)
    }
    var timerRunning by remember {
        mutableStateOf(true)
    }
    DisposableEffect(timerRunning) {
        if (!timerRunning) {
            return@DisposableEffect onDispose {}
        }
        val timer = fixedRateTimer(period = 1000, initialDelay = 1000) {
            if (--countdownInt == 0) {
                timerRunning = false
            }
        }
        onDispose {
            timer.cancel()
            timer.purge()
            onZero.invoke()
        }
    }
    MaterialTheme {
        Column {
            val countdownString = countdownInt.toCountdownString()
            AnimatedContent(targetState = countdownString) { targetState ->
                val modifier = Modifier.width(100.dp).padding(vertical = 30.dp)
                when (targetState) {
                    "00:00" -> {
                        val transition = rememberInfiniteTransition()
                        val color by transition.animateColor(
                            Color.Red,
                            Color.Blue,
                            infiniteRepeatable(animation = tween(), repeatMode = RepeatMode.Reverse)
                        )
                        val scale by transition.animateFloat(
                            0.75f,
                            1.25f,
                            infiniteRepeatable(tween(), RepeatMode.Reverse)
                        )
                        Text(targetState, color = color, modifier = modifier.scale(scale), textAlign = TextAlign.Center)
                    }
                    else -> Text(targetState, modifier = modifier, textAlign = TextAlign.Center)
                }

            }
            Row {
                Button(enabled = countdownInt > 0, onClick = {
                    timerRunning = !timerRunning
                }) {
                    AnimatedContent(timerRunning) { targetState ->
                        when (targetState) {
                            true -> Text("Pause")
                            false -> Text("Play")
                        }
                    }
                }
                Button(onClick = {
                    timerRunning = false
                    countdownInt = TIMER
                }) {
                    Text("Stop")
                }
            }
        }
    }
}

private const val TIMER = 5//60 * 25

private fun Int.toCountdownString(): String {
    val seconds = (this % 60)
    val minutes = (this - seconds) / 60
    val secondsString = seconds.paddedTimeString()
    val minutesString = minutes.paddedTimeString()
    return "$minutesString:$secondsString"
}

private fun Int.paddedTimeString(): String = this.toString().run {
    if (this.length == 2) {
        return@run this
    } else {
        return@run this.padStart(2, '0')
    }
}

fun main() = application {
    var isVisible by remember {
        mutableStateOf(true)
    }
    // TODO: Set size in windowState
    val windowState = rememberWindowState()
    Window(state = windowState, onCloseRequest = {
        isVisible = false
    }, visible = isVisible) {
        App {
            isVisible = true
        }
    }
    val painter = rememberVectorPainter(Icons.Default.Add)
    Tray(painter, onAction = {
        isVisible = true
    }) {
        Item("Quit") {
            exitApplication()
        }
    }
}
