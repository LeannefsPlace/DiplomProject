package com.ivan.file_storage_service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.*

@Service
class FileStorageService(
    private val fileStorageConfig: FileStorageConfig
) {
    fun getImagesDir() = File(fileStorageConfig.imagesDir).absolutePath
    fun getFilesDir() = File(fileStorageConfig.filesDir).absolutePath

    fun storeImage(file: MultipartFile): String {
        val fileName = generateFileName(file.originalFilename)
        val targetLocation = Path.of(fileStorageConfig.imagesDir, fileName).toFile()
        file.transferTo(targetLocation)
        return fileName
    }

    fun loadImage(fileName: String): File {
        val file = Path.of(fileStorageConfig.imagesDir, fileName).toFile()
        if (!file.exists()) {
            throw FileNotFoundException("Image not found: $fileName")
        }
        return file
    }

    fun storeFile(file: MultipartFile): String {
        val fileName = generateFileName(file.originalFilename)
        val targetLocation = Path.of(fileStorageConfig.filesDir, fileName).toFile()
        file.transferTo(targetLocation)
        return fileName
    }

    fun loadFile(fileName: String): File {
        val file = Path.of(fileStorageConfig.filesDir, fileName).toFile()
        if (!file.exists()) {
            throw FileNotFoundException("File not found: $fileName")
        }
        return file
    }

    fun deleteFile(fileName: String) {
        val file = Path.of(fileStorageConfig.filesDir, fileName).toFile()
        if (file.exists()) {
            file.delete()
        } else {
            throw FileNotFoundException("File not found: $fileName")
        }
    }

    fun deleteImage(fileName: String) {
        val file = Path.of(fileStorageConfig.imagesDir, fileName).toFile()
        if (file.exists()) {
            file.delete()
        } else {
            throw FileNotFoundException("Image not found: $fileName")
        }
    }

    private fun generateFileName(originalFileName: String?): String {
        val extension = originalFileName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() } ?: "bin"
        return "${UUID.randomUUID()}.$extension"
    }
}