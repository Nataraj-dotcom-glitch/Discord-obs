package com.darkalise.obs.model

import java.util.UUID

data class Scene(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sources: List<Source> = emptyList(),
    val isDefault: Boolean = false
) {
    fun duplicate(newName: String = "$name (Copy)"): Scene {
        return copy(
            id = UUID.randomUUID().toString(),
            name = newName,
            sources = sources.map { it.copy(id = UUID.randomUUID().toString()) },
            isDefault = false
        )
    }
}
