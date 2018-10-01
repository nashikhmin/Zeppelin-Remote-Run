package org.intellij.plugin.zeppelin.idea.settings.plugin

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import org.intellij.plugin.zeppelin.models.User

object ZeppelinCredentialsManager {
    private val passwordSafe: PasswordSafe = PasswordSafe.instance
    private const val SERVICE_NAME: String = "Zeppelin"
    private val CredentialAttributes: CredentialAttributes = CredentialAttributes(SERVICE_NAME)
    private const val DEFAULT_USERNAME: String = "user"
    private const val DEFAULT_PASSWORD: String = "password2"

    fun getLogin(): String = passwordSafe.get(CredentialAttributes)?.userName ?: DEFAULT_USERNAME

    fun setCredentials(user: User?) = passwordSafe.set(CredentialAttributes,
            Credentials(user?.name ?: "", user?.password ?: ""))

    fun getPlainPassword(): String = passwordSafe.get(CredentialAttributes)?.getPasswordAsString() ?: DEFAULT_PASSWORD
}