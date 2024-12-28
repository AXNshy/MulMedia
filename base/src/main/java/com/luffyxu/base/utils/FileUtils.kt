package com.luffyxu.base.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

object FileUtils {
    fun getString(instream: InputStream?): String {
        val stringBuffer = StringBuilder()
        val line = ByteArray(1024)
        var length = 0
        var bufferedInputStream: BufferedInputStream? = null
        try {
            bufferedInputStream = BufferedInputStream(instream)
            while (bufferedInputStream.read(line).also { length = it } != -1) {
                Log.d("TAG", "read length$length")
                val cur = String(line, Charset.forName("utf-8"))
                Log.d("TAG", "read length$cur")
                stringBuffer.append(cur)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return stringBuffer.toString()
    }

    fun getString1(instream: InputStream?): String {
        val stringBuffer = StringBuilder()
        var line: String? = null
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(instream))
            while (reader.readLine().also { line = it } != null) {
                stringBuffer.append(line)
                stringBuffer.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return stringBuffer.toString()
    }

    @JvmStatic
    fun getStringFromAssets(context: Context, filename: String?): String {
        var data = ""
        try {
            val inputStream = context.assets.open(filename!!)
            data = getString1(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return data
    }

    fun getString(filename: String?): String {
        var data = ""
        try {
            val file = File(filename)
            if (file == null || !file.exists() || file.isDirectory || !file.canRead()) return ""
            val inputStream: InputStream = FileInputStream(File(filename))
            data = getString(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return data
    }

    @JvmStatic
    fun getPath(context: Context, uri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                context,
                uri
            )
        ) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                contentUri = if ("image" == type) {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Files.getContentUri("external")
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = MediaStore.Video.Media.DATA
        val projection = arrayOf(column)
        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    @JvmStatic
    fun getFilePath(context: Context, uri: Uri?): FileDescriptor? {
        if (uri == null) {
            return null
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            var descriptor: ParcelFileDescriptor? = null
            try {
                descriptor = context.contentResolver.openFileDescriptor(uri, "r")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return descriptor!!.fileDescriptor
        }
        return null
    }
}