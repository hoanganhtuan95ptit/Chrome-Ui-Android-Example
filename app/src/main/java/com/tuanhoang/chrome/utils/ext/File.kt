package com.tuanhoang.chrome.utils.ext

import com.one.coreapp.App
import com.one.coreapp.utils.FileUtils
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileOutputStream

fun saveCache(fileName: String, _byteArray: ByteArray?) {

    val byteArray = _byteArray ?: return

    val file = FileUtils.createFile(App.shared, true, "caches", fileName)!!

    val fileOutputStream = FileOutputStream(file)
    fileOutputStream.write(byteArray)
    fileOutputStream.close()
}


fun getCache(fileName: String): ByteArray? {

    val file = FileUtils.createFile(App.shared, true, "caches", fileName) ?: return null


    val fileInputStream = FileInputStream(file)

    val bufferedInputStream = BufferedInputStream(fileInputStream)

    val bytes = ByteArray(file.length().toInt())
    bufferedInputStream.read(bytes, 0, bytes.size)

    bufferedInputStream.close()
    fileInputStream.close()

    return bytes
}