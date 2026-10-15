package org.jellyfin.androidtv.ui.playback

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.text.TextRenderer
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@OptIn(UnstableApi::class)
class SubtitleOffsetTests : FunSpec({
	fun wrapRenderers(subtitleOffset: SubtitleOffset, vararg renderers: Renderer): Array<Renderer> {
		val renderersFactory = mockk<RenderersFactory> {
			every { createRenderers(any(), any(), any(), any(), any()) } returns arrayOf(*renderers)
		}
		return SubtitleOffsetRenderersFactory(renderersFactory, subtitleOffset)
			.createRenderers(mockk(), mockk(), mockk(), mockk(), mockk())
	}

	test("SubtitleOffsetRenderersFactory only wraps subtitle renderers") {
		val textRenderer = mockk<TextRenderer>(relaxed = true)
		val otherRenderer = mockk<Renderer>(relaxed = true)

		val renderers = wrapRenderers(SubtitleOffset(), textRenderer, otherRenderer)

		renderers.size shouldBe 2
		renderers[0] shouldNotBeSameInstanceAs textRenderer
		renderers[1] shouldBeSameInstanceAs otherRenderer
	}

	test("SubtitleOffsetRenderersFactory renders subtitles earlier with a positive offset") {
		val textRenderer = mockk<TextRenderer>(relaxed = true)
		val subtitleOffset = SubtitleOffset().apply { offsetMs = 1_500 }

		wrapRenderers(subtitleOffset, textRenderer)[0].render(10_000_000, 0)

		verify { textRenderer.render(11_500_000, 0) }
	}

	test("SubtitleOffsetRenderersFactory applies offset changes while playing") {
		val textRenderer = mockk<TextRenderer>(relaxed = true)
		val subtitleOffset = SubtitleOffset()
		val renderer = wrapRenderers(subtitleOffset, textRenderer)[0]

		renderer.render(10_000_000, 0)
		subtitleOffset.offsetMs = -2_000
		renderer.render(10_000_000, 0)

		verify { textRenderer.render(10_000_000, 0) }
		verify { textRenderer.render(8_000_000, 0) }
	}
})
