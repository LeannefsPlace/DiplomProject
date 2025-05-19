package com.ivan.file_storage_service

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController(
    private val fileStorageService: FileStorageService
) {
    @GetMapping("/paths")
    fun getPaths() = mapOf(
        "workingDir" to System.getProperty("user.dir"),
        "imagesDir" to fileStorageService.getImagesDir(),
        "filesDir" to fileStorageService.getFilesDir()
    )
}