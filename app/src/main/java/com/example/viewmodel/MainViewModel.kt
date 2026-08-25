package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ActionDownloaderApp
import com.example.database.DownloadEntity
import com.example.model.MediaInfo
import com.example.model.VideoFormatOption
import com.example.preferences.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryFilter {
    ALL,
    VIDEOS,
    AUDIO
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ActionDownloaderApp).repository
    val appSettings = AppSettings(application)

    // URL input & Analysis state
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analyzedMedia = MutableStateFlow<MediaInfo?>(null)
    val analyzedMedia: StateFlow<MediaInfo?> = _analyzedMedia.asStateFlow()

    private val _selectedFormat = MutableStateFlow<VideoFormatOption?>(null)
    val selectedFormat: StateFlow<VideoFormatOption?> = _selectedFormat.asStateFlow()

    private val _showQualitySheet = MutableStateFlow(false)
    val showQualitySheet: StateFlow<Boolean> = _showQualitySheet.asStateFlow()

    // Snackbar notifications
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Library Filtering & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow(LibraryFilter.ALL)
    val filterType: StateFlow<LibraryFilter> = _filterType.asStateFlow()

    // Player modal & delete dialog states
    private val _playingMedia = MutableStateFlow<DownloadEntity?>(null)
    val playingMedia: StateFlow<DownloadEntity?> = _playingMedia.asStateFlow()

    private val _itemToDelete = MutableStateFlow<DownloadEntity?>(null)
    val itemToDelete: StateFlow<DownloadEntity?> = _itemToDelete.asStateFlow()

    // App Preferences state
    val isDarkTheme = MutableStateFlow(appSettings.isDarkTheme)
    val isRtl = MutableStateFlow(appSettings.isRtl)
    val isWifiOnly = MutableStateFlow(appSettings.isWifiOnly)
    val isNotificationsEnabled = MutableStateFlow(appSettings.isNotificationsEnabled)
    val defaultQuality = MutableStateFlow(appSettings.defaultQuality)

    // Reactive streams from Database
    val allDownloads: StateFlow<List<DownloadEntity>> = repository.getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDownloads: StateFlow<List<DownloadEntity>> = repository.getActiveDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedDownloads: StateFlow<List<DownloadEntity>> = repository.getCompletedDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCompletedDownloads: StateFlow<List<DownloadEntity>> = combine(
        completedDownloads,
        _searchQuery,
        _filterType
    ) { list, query, filter ->
        list.filter { entity ->
            val matchesQuery = query.isBlank() ||
                    entity.title.contains(query, ignoreCase = true) ||
                    entity.fileName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                LibraryFilter.ALL -> true
                LibraryFilter.VIDEOS -> entity.mimeType.startsWith("video")
                LibraryFilter.AUDIO -> entity.mimeType.startsWith("audio")
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onUrlInputChanged(newUrl: String) {
        _urlInput.value = newUrl
    }

    fun clearUrl() {
        _urlInput.value = ""
        _analyzedMedia.value = null
        _selectedFormat.value = null
        _showQualitySheet.value = false
    }

    fun pasteFromClipboard(text: String) {
        _urlInput.value = text.trim()
        analyzeUrl(text.trim())
    }

    fun analyzeUrl(url: String = _urlInput.value) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            _userMessage.value = "يرجى إدخال رابط فيديو صالح"
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _analyzedMedia.value = null
            _selectedFormat.value = null

            val result = repository.inspectUrl(trimmed)
            _isAnalyzing.value = false

            result.onSuccess { info ->
                _analyzedMedia.value = info
                // Pick default format matching user settings or 1080p
                val prefQuality = defaultQuality.value
                val matchedFormat = info.availableFormats.find {
                    it.id.equals(prefQuality, ignoreCase = true) || it.qualityLabel.contains(prefQuality, ignoreCase = true)
                } ?: info.availableFormats.firstOrNull()

                _selectedFormat.value = matchedFormat
                _showQualitySheet.value = true
            }.onFailure { error ->
                _userMessage.value = "تعذر فحص الرابط: ${error.localizedMessage ?: "تأكد من صحة الرابط"}"
            }
        }
    }

    fun selectFormat(format: VideoFormatOption) {
        _selectedFormat.value = format
    }

    fun dismissQualitySheet() {
        _showQualitySheet.value = false
    }

    fun startDownload() {
        val info = _analyzedMedia.value ?: return
        val format = _selectedFormat.value ?: return

        viewModelScope.launch {
            _showQualitySheet.value = false
            repository.enqueueDownload(info, format)
            _userMessage.value = "تمت إضافة \"${info.title}\" إلى قائمة التنزيلات"
            clearUrl()
        }
    }

    fun pauseDownload(id: Long) {
        repository.pauseDownload(id)
    }

    fun resumeDownload(id: Long) {
        repository.resumeDownload(id)
    }

    fun cancelDownload(id: Long) {
        repository.cancelDownload(id)
        _userMessage.value = "تم إلغاء التنزيل"
    }

    fun requestDelete(entity: DownloadEntity) {
        _itemToDelete.value = entity
    }

    fun dismissDeleteDialog() {
        _itemToDelete.value = null
    }

    fun confirmDelete(deleteFromDisk: Boolean = true) {
        val entity = _itemToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteDownload(entity.id, deleteFromDisk)
            _itemToDelete.value = null
            _userMessage.value = "تم حذف الملف بنجاح"
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(filter: LibraryFilter) {
        _filterType.value = filter
    }

    fun openPlayer(entity: DownloadEntity) {
        _playingMedia.value = entity
    }

    fun closePlayer() {
        _playingMedia.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun toggleDarkTheme(enabled: Boolean) {
        isDarkTheme.value = enabled
        appSettings.isDarkTheme = enabled
    }

    fun toggleRtl(enabled: Boolean) {
        isRtl.value = enabled
        appSettings.isRtl = enabled
    }

    fun toggleWifiOnly(enabled: Boolean) {
        isWifiOnly.value = enabled
        appSettings.isWifiOnly = enabled
    }

    fun toggleNotifications(enabled: Boolean) {
        isNotificationsEnabled.value = enabled
        appSettings.isNotificationsEnabled = enabled
    }

    fun updateDefaultQuality(quality: String) {
        defaultQuality.value = quality
        appSettings.defaultQuality = quality
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _userMessage.value = "تم مسح سجل التنزيلات"
        }
    }
}
