package org.jellyfin.androidtv.ui.playback.overlay

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class SubtitleOffsetBarTests : FunSpec({
	test("formatSubtitleOffset with zero offset") {
		formatSubtitleOffset(0) shouldBe "0s"
	}

	test("formatSubtitleOffset with whole seconds") {
		formatSubtitleOffset(2_000) shouldBe "+2s"
		formatSubtitleOffset(-30_000) shouldBe "-30s"
	}

	test("formatSubtitleOffset with fractional seconds") {
		formatSubtitleOffset(1_500) shouldBe "+1.5s"
		formatSubtitleOffset(-100) shouldBe "-0.1s"
	}
})
