package org.jellyfin.androidtv.ui.playback.overlay.action

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.ComponentDialog
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.androidtv.ui.playback.canAdjustSubtitleOffset
import org.jellyfin.androidtv.ui.playback.overlay.CustomPlaybackTransportControlGlue
import org.jellyfin.androidtv.ui.playback.overlay.VideoPlayerAdapter
import org.jellyfin.androidtv.ui.playback.overlay.showSubtitleOffsetBar
import org.jellyfin.androidtv.ui.playback.setSubtitleIndex
import org.jellyfin.sdk.model.api.MediaStreamType
import timber.log.Timber

class ClosedCaptionsAction(
	context: Context,
	customPlaybackTransportControlGlue: CustomPlaybackTransportControlGlue,
) : CustomAction(context, customPlaybackTransportControlGlue) {
	private var popup: PopupMenu? = null
	private var offsetBar: ComponentDialog? = null

	init {
		initializeWithIcon(R.drawable.ic_select_subtitle)
	}

	override fun handleClickAction(
		playbackController: PlaybackController,
		videoPlayerAdapter: VideoPlayerAdapter,
		context: Context,
		view: View,
	) {
		if (playbackController.currentStreamInfo == null) {
			Timber.w("StreamInfo null trying to obtain subtitles")
			Toast.makeText(context, "Unable to obtain subtitle info", Toast.LENGTH_LONG).show()
			return
		}

		videoPlayerAdapter.leanbackOverlayFragment.setFading(false)
		removePopup()
		popup = PopupMenu(context, view, Gravity.END).apply {
			with(menu) {
				var order = 0
				add(0, -1, order++, context.getString(R.string.lbl_none)).apply {
					isChecked = playbackController.subtitleStreamIndex == -1
				}

				for (sub in playbackController.currentMediaSource.mediaStreams.orEmpty()) {
					if (sub.type != MediaStreamType.SUBTITLE) continue

					add(0, sub.index, order++, sub.displayTitle).apply {
						isChecked = sub.index == playbackController.subtitleStreamIndex
					}
				}

				setGroupCheckable(0, true, false)

				if (playbackController.canAdjustSubtitleOffset) {
					add(1, SUBTITLE_OFFSET_ITEM_ID, order++, context.getString(R.string.lbl_subtitle_delay))
				}
			}
			setOnDismissListener {
				videoPlayerAdapter.leanbackOverlayFragment.setFading(true)
				popup = null
			}
			setOnMenuItemClickListener { item ->
				if (item.itemId == SUBTITLE_OFFSET_ITEM_ID) showOffsetBar(playbackController, videoPlayerAdapter, context)
				else playbackController.setSubtitleIndex(item.itemId)
				true
			}
		}
		popup?.show()
	}

	private fun showOffsetBar(
		playbackController: PlaybackController,
		videoPlayerAdapter: VideoPlayerAdapter,
		context: Context,
	) {
		videoPlayerAdapter.leanbackOverlayFragment.hideOverlay()
		offsetBar?.dismiss()
		offsetBar = showSubtitleOffsetBar(
			context = context,
			initialOffsetMs = playbackController.subtitleOffsetMs,
			onOffsetChanged = { offsetMs -> playbackController.subtitleOffsetMs = offsetMs },
			onDismiss = { offsetBar = null },
		)
	}

	fun removePopup() {
		popup?.dismiss()
		offsetBar?.dismiss()
	}

	private companion object {
		const val SUBTITLE_OFFSET_ITEM_ID = -2
	}
}
