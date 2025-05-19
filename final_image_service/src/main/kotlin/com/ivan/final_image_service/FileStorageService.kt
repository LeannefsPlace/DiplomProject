package com.ivan.final_image_service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.UUID

@Service
class FileStorageService {
    private val storageDir = Path.of("uploads").also {
        if (!it.toFile().exists()) it.toFile().mkdirs()
    }

    // Сохраняем файл и возвращаем его ID
    fun storeFile(file: MultipartFile): String {
        val fileId = UUID.randomUUID().toString()
        val targetPath = storageDir.resolve(fileId)
        file.transferTo(targetPath)
        return fileId
    }

    // Получаем файл по ID
    fun getFile(fileId: String): File {
        val file = storageDir.resolve(fileId).toFile()
        if (!file.exists()) throw FileNotFoundException("File not found")
        return file
    }
}