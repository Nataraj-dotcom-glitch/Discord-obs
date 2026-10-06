package com.darkalise.obs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkalise.obs.model.Source
import com.darkalise.obs.model.SourceType
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsCardBg
import com.darkalise.obs.ui.theme.ObsGreenActive
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsRedRec
import com.darkalise.obs.ui.theme.ObsTextMuted
import com.darkalise.obs.ui.theme.ObsTextPrimary
import com.darkalise.obs.ui.theme.ObsTextSecondary

@Composable
fun SourcesPanel(
    sources: List<Source>,
    onAddSource: (SourceType) -> Unit,
    onToggleEnable: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onDeleteSource: (String) -> Unit,
    onReorderSource: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(ObsCardBg, RoundedCornerShape(8.dp))
            .border(1.dp, ObsBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SOURCES",
                color = ObsTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box {
                IconButton(
                    onClick = { showAddMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Source",
                        tint = ObsPurplePrimary
                    )
                }

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false },
                    modifier = Modifier.background(ObsCardBg).border(1.dp, ObsBorder)
                ) {
                    SourceType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName, color = ObsTextPrimary, fontSize = 13.sp) },
                            onClick = {
                                onAddSource(type)
                                showAddMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (sources.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No sources in scene. Click '+' to add.",
                    color = ObsTextMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sources, key = { it.id }) { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF100E1A))
                            .border(1.dp, Color(0xFF231F33), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = source.name,
                                    color = if (source.isEnabled) ObsTextPrimary else ObsTextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "${source.type.displayName} • ${if (source.isLocked) "Locked" else "Unlocked"}",
                                color = ObsTextMuted,
                                fontSize = 10.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Reorder Up
                            IconButton(
                                onClick = { onReorderSource(source.id, false) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropUp,
                                    contentDescription = "Up",
                                    tint = ObsTextSecondary
                                )
                            }
                            // Reorder Down
                            IconButton(
                                onClick = { onReorderSource(source.id, true) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Down",
                                    tint = ObsTextSecondary
                                )
                            }
                            // Toggle Lock
                            IconButton(
                                onClick = { onToggleLock(source.id) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = if (source.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    tint = if (source.isLocked) ObsPurplePrimary else ObsTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            // Toggle Visibility
                            IconButton(
                                onClick = { onToggleEnable(source.id) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = if (source.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibility",
                                    tint = if (source.isEnabled) ObsGreenActive else ObsTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            // Delete
                            IconButton(
                                onClick = { onDeleteSource(source.id) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = ObsRedRec.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
