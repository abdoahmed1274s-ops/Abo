package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.database.DownloadEntity
import com.example.ui.components.DownloadItemCard
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaDarkBg
import com.example.ui.theme.CinemaDarkCard
import com.example.ui.theme.CinemaDarkElevated
import com.example.ui.theme.CinemaDarkOutline
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.CinemaRedContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.LibraryFilter
import com.example.viewmodel.MainViewModel

@Composable
fun DownloadsLibraryScreen(
    viewModel: MainViewModel,
    onShareEntity: (DownloadEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.filteredCompletedDownloads.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val itemToDelete by viewModel.itemToDelete.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "المكتبة والتنزيلات",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = CinemaGold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CinemaGold.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${downloads.size}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = CinemaGold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = {
                Text(
                    text = "بحث في التنزيلات...",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = { viewModel.setSearchQuery("") },
                        modifier = Modifier.testTag("clear_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "مسح البحث",
                            tint = TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CinemaGold,
                unfocusedBorderColor = CinemaDarkOutline,
                focusedContainerColor = CinemaDarkCard,
                unfocusedContainerColor = CinemaDarkCard,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("library_search_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = filterType == LibraryFilter.ALL,
                onClick = { viewModel.setFilterType(LibraryFilter.ALL) },
                label = { Text("الكل") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CinemaGold,
                    selectedLabelColor = CinemaDarkBg,
                    containerColor = CinemaDarkCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = CinemaDarkOutline,
                    selectedBorderColor = CinemaGold,
                    enabled = true,
                    selected = filterType == LibraryFilter.ALL
                )
            )

            FilterChip(
                selected = filterType == LibraryFilter.VIDEOS,
                onClick = { viewModel.setFilterType(LibraryFilter.VIDEOS) },
                label = { Text("فيديوهات") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CinemaGold,
                    selectedLabelColor = CinemaDarkBg,
                    containerColor = CinemaDarkCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = CinemaDarkOutline,
                    selectedBorderColor = CinemaGold,
                    enabled = true,
                    selected = filterType == LibraryFilter.VIDEOS
                )
            )

            FilterChip(
                selected = filterType == LibraryFilter.AUDIO,
                onClick = { viewModel.setFilterType(LibraryFilter.AUDIO) },
                label = { Text("صوتيات") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CinemaGold,
                    selectedLabelColor = CinemaDarkBg,
                    containerColor = CinemaDarkCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = CinemaDarkOutline,
                    selectedBorderColor = CinemaGold,
                    enabled = true,
                    selected = filterType == LibraryFilter.AUDIO
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (downloads.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaDarkCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, CinemaDarkOutline, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CinemaDarkElevated,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = null,
                                    tint = CinemaGold,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) "لم يتم العثور على نتائج للبحث" else "المكتبة فارغة حالياً",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) "جرب البحث بكلمات أخرى" else "الملفات التي تقوم بتنزيلها ستظهر هنا مع إمكانية التشغيل والمشاركة",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(downloads, key = { it.id }) { item ->
                    DownloadItemCard(
                        download = item,
                        onPlayClick = { viewModel.openPlayer(item) },
                        onShareClick = { onShareEntity(item) },
                        onDeleteClick = { viewModel.requestDelete(item) }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (itemToDelete != null) {
        val item = itemToDelete!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            containerColor = CinemaDarkCard,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = CinemaRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "تأكيد حذف الملف",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في حذف \"${item.title}\" نهائياً من جهازك؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete(deleteFromDisk = true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaRed,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_delete_btn")
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissDeleteDialog() },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("cancel_delete_btn")
                ) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }
}
