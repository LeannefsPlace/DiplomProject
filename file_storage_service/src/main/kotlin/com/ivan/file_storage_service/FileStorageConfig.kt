package com.ivan.file_storage_service

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.File

@Configuration
class FileStorageConfig {
    @Value("\${app.upload-dir}")
    lateinit var uploadDir: String

    @Value("\${app.images-dir}")
    lateinit var imagesDir: String

    @Value("\${app.files-dir}")
    lateinit var filesDir: String

    @PostConstruct
    fun init() {
        val baseDir = File(uploadDir).apply {
            if (!exists()) mkdirs()
            println("📁 Base dir: ${absolutePath}")
        }

        File(imagesDir).apply {
            mkdirs()
            println("🖼 Images dir: ${absolutePath}")
        }

        File(filesDir).apply {
            mkdirs()
            println("📄 Files dir: ${absolutePath}")
        }

        // Проверка записи
        val testFile = File(imagesDir, "test_write.txt")
        testFile.writeText("test").also {
            println("✅ Write test passed in $imagesDir")
            testFile.delete()
        }
    }
}