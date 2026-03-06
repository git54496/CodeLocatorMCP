package com.bytedance.tools.codelocator.adapter

import kotlin.test.Test
import kotlin.test.assertTrue

class BuildInfoTests {

    @Test
    fun `build info version is not blank`() {
        assertTrue(BuildInfo.version.isNotBlank())
    }
}
