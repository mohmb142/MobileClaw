package ai.affiora.mobileclaw.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    private object Keys {
        val SELECTED_PROVIDER = stringPreferencesKey("selected_provider")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val ACTIVE_SKILL_IDS = stringSetPreferencesKey("active_skill_ids")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val PERMISSION_MODE = stringPreferencesKey("permission_mode")
        val ALLOWED_TOOLS = stringSetPreferencesKey("allowed_tools")
        val BEDROCK_MAX_THINKING_ENABLED = booleanPreferencesKey("bedrock_max_thinking_enabled")
        val FAILOVER_ENABLED = booleanPreferencesKey("failover_enabled")
        val FAILOVER_CHAIN = stringSetPreferencesKey("failover_chain")
        val FAILOVER_CHAIN_ORDER = stringPreferencesKey("failover_chain_order")
        val FAILOVER_MAX_ATTEMPTS = stringPreferencesKey("failover_max_attempts")
        val AUTO_SKILL_MODE = stringPreferencesKey("auto_skill_mode")
        fun baseUrlKey(providerId: String) = stringPreferencesKey("base_url_$providerId")
        fun tokenKey(providerId: String) = "token_$providerId"
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(context, "androidclaw_secure_prefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        } catch (e: Exception) {
            android.util.Log.e("UserPreferences", "Keystore corrupted, resetting", e)
            context.deleteSharedPreferences("androidclaw_secure_prefs")
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            EncryptedSharedPreferences.create(context, "androidclaw_secure_prefs", masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
        }
    }

    private val _tokenUpdates = MutableStateFlow(0L)
    val apiKey: Flow<String> = combine(context.dataStore.data.map { it[Keys.SELECTED_PROVIDER] ?: DEFAULT_PROVIDER }, _tokenUpdates) { providerId, _ -> getTokenForProvider(providerId) }
    val selectedProvider: Flow<String> = context.dataStore.data.map { it[Keys.SELECTED_PROVIDER] ?: DEFAULT_PROVIDER }
    val selectedModel: Flow<String> = context.dataStore.data.map { it[Keys.SELECTED_MODEL] ?: DEFAULT_MODEL }
    val activeSkillIds: Flow<Set<String>> = context.dataStore.data.map { it[Keys.ACTIVE_SKILL_IDS] ?: emptySet() }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] ?: false }
    val deviceName: Flow<String> = context.dataStore.data.map { it[Keys.DEVICE_NAME] ?: "" }
    val permissionMode: Flow<String> = context.dataStore.data.map { it[Keys.PERMISSION_MODE] ?: "default" }
    val allowedTools: Flow<Set<String>> = context.dataStore.data.map { it[Keys.ALLOWED_TOOLS] ?: emptySet() }
    val bedrockMaxThinkingEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BEDROCK_MAX_THINKING_ENABLED] ?: false }
    val failoverEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.FAILOVER_ENABLED] ?: true }
    val failoverChain: Flow<List<String>> = context.dataStore.data.map { prefs -> prefs[Keys.FAILOVER_CHAIN_ORDER]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: DEFAULT_FAILOVER_CHAIN }
    val failoverMaxAttempts: Flow<Int> = context.dataStore.data.map { prefs -> (prefs[Keys.FAILOVER_MAX_ATTEMPTS]?.toIntOrNull() ?: DEFAULT_FAILOVER_MAX_ATTEMPTS).coerceIn(1, 4) }
    val autoSkillMode: Flow<String> = context.dataStore.data.map { it[Keys.AUTO_SKILL_MODE] ?: "Off" }

    fun getTokenForProvider(providerId: String): String = encryptedPrefs.getString(Keys.tokenKey(providerId), "") ?: ""
    fun hasTokenForProvider(providerId: String): Boolean = getTokenForProvider(providerId).isNotBlank()
    suspend fun setTokenForProvider(providerId: String, token: String) { withContext(Dispatchers.IO) { encryptedPrefs.edit().putString(Keys.tokenKey(providerId), token).apply() }; _tokenUpdates.value = System.currentTimeMillis() }
    suspend fun setApiKey(apiKey: String) { setTokenForProvider(selectedProvider.first(), apiKey) }
    suspend fun getBaseUrlForProvider(providerId: String): String = context.dataStore.data.map { it[Keys.baseUrlKey(providerId)] ?: "" }.first()
    fun baseUrlFlowForProvider(providerId: String): Flow<String> = context.dataStore.data.map { it[Keys.baseUrlKey(providerId)] ?: "" }
    suspend fun setBaseUrlForProvider(providerId: String, baseUrl: String) { context.dataStore.edit { it[Keys.baseUrlKey(providerId)] = baseUrl } }
    suspend fun setSelectedProvider(provider: String) { context.dataStore.edit { it[Keys.SELECTED_PROVIDER] = provider } }
    suspend fun setSelectedModel(model: String) { context.dataStore.edit { it[Keys.SELECTED_MODEL] = model } }
    suspend fun setActiveSkillIds(skillIds: Set<String>) { context.dataStore.edit { it[Keys.ACTIVE_SKILL_IDS] = skillIds } }
    suspend fun setOnboardingCompleted(completed: Boolean) { context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }; val flagFile = context.filesDir.resolve(".onboarding_completed"); if (completed) flagFile.createNewFile() else flagFile.delete() }
    suspend fun setDeviceName(name: String) { context.dataStore.edit { it[Keys.DEVICE_NAME] = name } }
    suspend fun setPermissionMode(mode: String) { context.dataStore.edit { it[Keys.PERMISSION_MODE] = mode } }
    suspend fun setAllowedTools(tools: Set<String>) { context.dataStore.edit { it[Keys.ALLOWED_TOOLS] = tools } }
    suspend fun setBedrockMaxThinkingEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.BEDROCK_MAX_THINKING_ENABLED] = enabled } }
    suspend fun setFailoverEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.FAILOVER_ENABLED] = enabled } }
    suspend fun setFailoverChain(providerIds: List<String>) { val cleaned = providerIds.map { it.trim() }.filter { it.isNotBlank() }.distinct(); context.dataStore.edit { it[Keys.FAILOVER_CHAIN_ORDER] = cleaned.joinToString(",") } }
    suspend fun setFailoverMaxAttempts(n: Int) { context.dataStore.edit { it[Keys.FAILOVER_MAX_ATTEMPTS] = n.coerceIn(1, 4).toString() } }
    suspend fun setAutoSkillMode(mode: String) { context.dataStore.edit { it[Keys.AUTO_SKILL_MODE] = if (mode in setOf("Off", "AutoOnRemote", "Always")) mode else "Off" } }
    suspend fun addAllowedTool(toolName: String) { context.dataStore.edit { it[Keys.ALLOWED_TOOLS] = (it[Keys.ALLOWED_TOOLS] ?: emptySet()) + toolName } }
    suspend fun removeAllowedTool(toolName: String) { context.dataStore.edit { it[Keys.ALLOWED_TOOLS] = (it[Keys.ALLOWED_TOOLS] ?: emptySet()) - toolName } }
    suspend fun clear() { context.dataStore.edit { it.clear() }; withContext(Dispatchers.IO) { encryptedPrefs.edit().clear().apply() } }

    companion object {
        const val DEFAULT_PROVIDER = "google"
        const val DEFAULT_MODEL = "gemini-2.5-flash"
        const val DEFAULT_FAILOVER_MAX_ATTEMPTS = 4
        val DEFAULT_FAILOVER_CHAIN = listOf("google", "groq", "openrouter", "huggingface")
    }
}
