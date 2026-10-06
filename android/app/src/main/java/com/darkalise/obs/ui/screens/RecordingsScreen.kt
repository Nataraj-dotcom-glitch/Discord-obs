package com.darkalise.obs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkalise.obs.model.RecordingItem
import com.darkalise.obs.ui.theme.ObsBlackBg
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsCardBg
import com.darkalise.obs.ui.theme.ObsPanelBg
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsRedRec
import com.darkalise.obs.ui.theme.ObsTextMuted
import com.darkalise.obs.ui.theme.ObsTextPrimary
import com.darkalise.obs.ui.theme.ObsTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(
    recordings: List<RecordingItem>,
    onPlay: (RecordingItem) -> Unit,
    onShare: (RecordingItem) -> Unit,
    onDelete: (RecordingItem) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "RECORDINGS",
                            color = ObsTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Movies/DarkAliseOBS/",
                            color = ObsPurplePrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Studio",
                            tint = ObsTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = ObsPurplePrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsPanelBg)
            )
        },
        containerColor = ObsBlackBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (recordings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsCardBg)
                        .border(1.dp, ObsBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = ObsTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recordings found yet",
                            color = ObsTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Recordings will be saved to Movies/DarkAliseOBS/",
                            color = ObsTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recordings, key = { it.id }) { item ->
                        val dateFormatted = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(Date(item.dateModifiedMillis))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsCardBg)
                                .border(1.dp, ObsBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.fileName,
                                    color = ObsTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text(
                                        text = dateFormatted,
                                        color = ObsTextMuted,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${item.formattedSize}",
                                        color = ObsPurplePrimary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (item.durationMs > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "• ${item.formattedDuration}",
                                            color = ObsTextSecondary,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Play
                                IconButton(onClick = { onPlay(item) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = ObsPurplePrimary
                                    )
                                }
                                // Share
                                IconButton(onClick = { onShare(item) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = ObsTextSecondary
                                    )
                                }
                                // Delete
                                IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ObsRedRec.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
