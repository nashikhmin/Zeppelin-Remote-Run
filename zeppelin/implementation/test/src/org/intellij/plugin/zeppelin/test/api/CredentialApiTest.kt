package org.intellij.plugin.zeppelin.test.api

import org.junit.Test
import kotlin.test.assertTrue

class CredentialApiTest: AbstractApiTest() {
    @Test
    fun loginTest() {
        assertTrue(integration.isConnected(), "Connection is failed")
    }
}