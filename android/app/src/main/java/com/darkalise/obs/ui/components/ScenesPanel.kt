package com.darkalise.obs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.darkalise.obs.model.Scene
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsCardBg
import com.darkalise.obs.ui.theme.ObsPurpleDark
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsRedRec
import com.darkalise.obs.ui.theme.ObsTextMuted
import com.darkalise.obs.ui.theme.ObsTextPrimary
import com.darkalise.obs.ui.theme.ObsTextSecondary

@Composable
fun ScenesPanel(
    scenes: List<Scene>,
    activeSceneId: String,
    onSelectScene: (String) -> Unit,
    onCreateScene: (String) -> Unit,
    onRenameScene: (String, String) -> Unit,
    onDuplicateScene: (String) -> Unit,
    onDeleteScene: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTargetScene by remember { mutableStateOf<Scene?>(null) }
    var newSceneNameInput by remember { mutableStateOf("") }

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
                text = "SCENES",
                color = ObsTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = {
                    newSceneNameInput = "Scene ${scenes.size + 1}"
                    showCreateDialog = true
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Scene",
                    tint = ObsPurplePrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(scenes, key = { it.id }) { scene ->
                val isSelected = scene.id == activeSceneId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) ObsPurpleDark.copy(alpha = 0.45f) else Color(0xFF100E1A))
                        .border(
                            1.dp,
                            if (isSelected) ObsPurplePrimary else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { onSelectScene(scene.id) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scene.name,
                            color = if (isSelected) Color.White else ObsTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "${scene.sources.size} sources",
                            color = ObsTextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                renameTargetScene = scene
                                newSceneNameInput = scene.name
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = ObsTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDuplicateScene(scene.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Duplicate",
                                tint = ObsTextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (scenes.size > 1) {
                            IconButton(
                                onClick = { onDeleteScene(scene.id) },
                                modifier = Modifier.size(24.dp)
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

    // Create Scene Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Scene", color = ObsTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newSceneNameInput,
                    onValueChange = { newSceneNameInput = it },
                    label = { Text("Scene Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSceneNameInput.isNotBlank()) {
                        onCreateScene(newSceneNameInput.trim())
                        showCreateDialog = false
                    }
                }) {
                    Text("Create", color = ObsPurplePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = ObsTextSecondary)
                }
            }
        )
    }

    // Rename Scene Dialog
    renameTargetScene?.let { target ->
        AlertDialog(
            onDismissRequest = { renameTargetScene = null },
            title = { Text("Rename Scene", color = ObsTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newSceneNameInput,
                    onValueChange = { newSceneNameInput = it },
                    label = { Text("New Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSceneNameInput.isNotBlank()) {
                        onRenameScene(target.id, newSceneNameInput.trim())
                        renameTargetScene = null
                    }
                }) {
                    Text("Save", color = ObsPurplePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetScene = null }) {
                    Text("Cancel", color = ObsTextSecondary)
                }
            }
        )
    }
}
