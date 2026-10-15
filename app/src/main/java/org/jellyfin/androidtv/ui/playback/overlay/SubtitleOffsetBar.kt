package org.jellyfin.androidtv.ui.playback.overlay

import android.content.Context
import android.view.ViewGroup
import android.view.Window
import androidx.activity.ComponentDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.base.Icon
import org.jellyfin.androidtv.ui.base.JellyfinTheme
import org.jellyfin.androidtv.ui.base.Text
import org.jellyfin.androidtv.ui.base.button.IconButton
import org.jellyfin.androidtv.ui.base.form.RangeControl
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

private const val STEP_MS = 100L
private const val MAX_OFFSET_MS = 30_000L
private const val TOP_OFFSET_FRACTION = 0.1f
private const val WIDTH_FRACTION = 0.4f

fun formatSubtitleOffset(offsetMs: Long): String {
	val sign = when {
		offsetMs > 0 -> "+"
		offsetMs < 0 -> "-"
		else -> ""
	}
	// Since this is purely numeric data, coerce to en_us to keep the linter happy
	val seconds = DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US))
		.format(abs(offsetMs).milliseconds.toDouble(DurationUnit.SECONDS))
	return "$sign${seconds}s"
}

@Composable
private fun SubtitleOffsetBar(
	initialOffsetMs: Long,
	onOffsetChanged: (offsetMs: Long) -> Unit,
	onClose: () -> Unit,
) {
	var offsetMs by remember { mutableLongStateOf(initialOffsetMs.coerceIn(-MAX_OFFSET_MS, MAX_OFFSET_MS)) }
	val sliderFocusRequester = remember { FocusRequester() }

	BoxWithConstraints(
		contentAlignment = Alignment.TopCenter,
		modifier = Modifier.fillMaxSize(),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.padding(top = maxHeight * TOP_OFFSET_FRACTION)
				.fillMaxWidth(WIDTH_FRACTION)
				.widthIn(min = 288.dp)
				.clip(RoundedCornerShape(6.dp))
				.background(colorResource(R.color.popup_menu_background).copy(alpha = 0.8f))
				.padding(horizontal = 8.dp, vertical = 4.dp),
		) {
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(
					text = formatSubtitleOffset(offsetMs),
					color = colorResource(R.color.button_default_normal_text),
					fontSize = 20.sp,
					textAlign = TextAlign.Center,
				)

				IconButton(
					onClick = onClose,
					modifier = Modifier.align(Alignment.CenterEnd),
				) {
					Icon(
						imageVector = ImageVector.vectorResource(R.drawable.ic_close),
						contentDescription = stringResource(R.string.lbl_close),
						modifier = Modifier.size(18.dp),
					)
				}
			}

			RangeControl(
				min = -MAX_OFFSET_MS.toFloat(),
				max = MAX_OFFSET_MS.toFloat(),
				markers = listOf(0f),
				value = offsetMs.toFloat(),
				stepForward = STEP_MS.toFloat(),
				onValueChange = { value ->
					offsetMs = (value / STEP_MS).roundToLong() * STEP_MS
					onOffsetChanged(offsetMs)
				},
				modifier = Modifier
					.focusRequester(sliderFocusRequester)
					.padding(horizontal = 8.dp, vertical = 12.dp)
					.fillMaxWidth()
					.height(4.dp),
			)
		}
	}

	LaunchedEffect(sliderFocusRequester) { sliderFocusRequester.requestFocus() }
}

/**
 * Show the subtitle offset bar, mirroring the one in jellyfin-web. Changes are applied immediately using [onOffsetChanged].
 */
fun showSubtitleOffsetBar(
	context: Context,
	initialOffsetMs: Long,
	onOffsetChanged: (offsetMs: Long) -> Unit,
	onDismiss: () -> Unit,
): ComponentDialog = ComponentDialog(context).apply {
	val composeView = ComposeView(context).apply {
		setContent {
			JellyfinTheme {
				SubtitleOffsetBar(
					initialOffsetMs = initialOffsetMs,
					onOffsetChanged = onOffsetChanged,
					onClose = ::dismiss,
				)
			}
		}
	}

	requestWindowFeature(Window.FEATURE_NO_TITLE)
	setContentView(composeView)
	setOnDismissListener { onDismiss() }

	window?.apply {
		setBackgroundDrawableResource(android.R.color.transparent)
		setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
		setDimAmount(0f)
	}

	show()
}
