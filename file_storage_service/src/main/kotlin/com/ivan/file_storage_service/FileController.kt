package com.ivan.file_storage_service

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.net.URLConnection
import java.nio.file.Files

@RestController
@RequestMapping("/files")
class FileController(
    private val fileStorageService: FileStorageService
) {
    @PostMapping("/images")
    fun uploadImage(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val fileName = fileStorageService.storeImage(file)
        return ResponseEntity.ok(fileName)
    }

    @GetMapping("/images/{fileName:.+}")
    fun getImage(
        @PathVariable fileName: String,
        response: HttpServletResponse
    ) {
        val file = fileStorageService.loadImage(fileName)
        response.contentType = "image/jpeg" // или определять по расширению
        Files.copy(file.toPath(), response.outputStream)
        response.outputStream.flush()
    }

    @PostMapping
    fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        val fileName = fileStorageService.storeFile(file)
        return ResponseEntity.ok(fileName)
    }

    @GetMapping("/{fileName:.+}")
    fun getFile(
        @PathVariable fileName: String,
        response: HttpServletResponse
    ) {
        val file = fileStorageService.loadFile(fileName)
        response.contentType = URLConnection.guessContentTypeFromName(fileName)
        response.setHeader("Content-Disposition", "inline; filename=\"$fileName\"")
        Files.copy(file.toPath(), response.outputStream)
        response.outputStream.flush()
    }

    @DeleteMapping("/{fileName:.+}")
    fun deleteFile(@PathVariable fileName: String): ResponseEntity<Void> {
        fileStorageService.deleteFile(fileName)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/images/{fileName:.+}")
    fun deleteImage(@PathVariable fileName: String): ResponseEntity<Void> {
        fileStorageService.deleteImage(fileName)
        return ResponseEntity.noContent().build()
    }
}

@ControllerAdvice
class FileExceptionHandler {
    @ExceptionHandler(FileNotFoundException::class)
    fun handleFileNotFound(ex: FileNotFoundException): ResponseEntity<Map<String, String>> {
        val response = mapOf("message" to "File not found")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<Map<String, String>> {
        val response = mapOf("message" to "Error processing file: ${ex.message}")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}