package org.jellyfin.androidtv.ui.playback

import android.os.Handler
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ForwardingRenderer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.text.TextRenderer
import androidx.media3.exoplayer.video.VideoRendererEventListener
import io.github.peerless2012.ass.media.render.AssRenderer
import kotlin.time.Duration.Companion.milliseconds

/**
 * Holds the user adjustable subtitle offset. Matching jellyfin-web, a positive value shows subtitles earlier.
 */
class SubtitleOffset {
	@Volatile
	var offsetMs: Long = 0
}

@OptIn(UnstableApi::class)
private class SubtitleOffsetRenderer(
	renderer: Renderer,
	private val subtitleOffset: SubtitleOffset,
) : ForwardingRenderer(renderer) {
	override fun render(positionUs: Long, elapsedRealtimeUs: Long) {
		super.render(positionUs + subtitleOffset.offsetMs.milliseconds.inWholeMicroseconds, elapsedRealtimeUs)
	}
}

/**
 * [RenderersFactory] wrapping all subtitle renderers (including libass) to apply [subtitleOffset].
 */
@OptIn(UnstableApi::class)
class SubtitleOffsetRenderersFactory(
	private val renderersFactory: RenderersFactory,
	private val subtitleOffset: SubtitleOffset,
) : RenderersFactory {
	override fun createRenderers(
		eventHandler: Handler,
		videoRendererEventListener: VideoRendererEventListener,
		audioRendererEventListener: AudioRendererEventListener,
		textRendererOutput: TextOutput,
		metadataRendererOutput: MetadataOutput,
	): Array<Renderer> = renderersFactory.createRenderers(
		eventHandler,
		videoRendererEventListener,
		audioRendererEventListener,
		textRendererOutput,
		metadataRendererOutput,
	).map { renderer ->
		when (renderer) {
			is TextRenderer, is AssRenderer -> SubtitleOffsetRenderer(renderer, subtitleOffset)
			else -> renderer
		}
	}.toTypedArray()

	override fun createSecondaryRenderer(
		renderer: Renderer,
		eventHandler: Handler,
		videoRendererEventListener: VideoRendererEventListener,
		audioRendererEventListener: AudioRendererEventListener,
		textRendererOutput: TextOutput,
		metadataRendererOutput: MetadataOutput,
	): Renderer? = renderersFactory.createSecondaryRenderer(
		renderer,
		eventHandler,
		videoRendererEventListener,
		audioRendererEventListener,
		textRendererOutput,
		metadataRendererOutput,
	)
}
