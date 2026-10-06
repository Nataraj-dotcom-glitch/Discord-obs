package com.darkalise.obs.recorder

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.darkalise.obs.model.RecordingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaStoreManager(private val context: Context) {

    companion object {
        const val RELATIVE_DIR = "Movies/DarkAliseOBS/"
        const val MIME_TYPE_MP4 = "video/mp4"
    }

    /**
     * Creates a pending MediaStore video entry in Movies/DarkAliseOBS/
     */
    fun createVideoUri(customPrefix: String = "DarkAliseOBS_rec"): Pair<Uri, String>? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${customPrefix}_$timeStamp.mp4"

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, MIME_TYPE_MP4)
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, RELATIVE_DIR)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = context.contentResolver.insert(collection, values)
        return uri?.let { Pair(it, fileName) }
    }

    /**
     * Publishes the recording once MediaMuxer finishes writing
     */
    fun finishRecording(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, values, null, null)
        }
    }

    /**
     * Queries all recorded videos stored in Movies/DarkAliseOBS/
     */
    suspend fun queryRecordings(): List<RecordingItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<RecordingItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?"
        } else {
            "${MediaStore.Video.Media.DATA} LIKE ?"
        }
        val selectionArgs = arrayOf("%DarkAliseOBS%")
        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol)
                    val date = cursor.getLong(dateCol) * 1000
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val contentUri = ContentUris.withAppendedId(collection, id)

                    list.add(
                        RecordingItem(
                            id = id,
                            uri = contentUri,
                            fileName = name,
                            dateModifiedMillis = date,
                            durationMs = duration,
                            sizeBytes = size
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        list
    }

    /**
     * Delete recording from MediaStore
     */
    suspend fun deleteRecording(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Create Play Intent
     */
    fun createPlayIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, MIME_TYPE_MP4)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Create Share Intent
     */
    fun createShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_MP4
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
