package com.ivan.final_image_service

import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files")
class FileController(
    private val fileStorageService: FileStorageService
) {
    // Загрузка файла
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): String {
        return fileStorageService.storeFile(file)
    }

    // Скачивание файла
    @GetMapping("/download/{fileId}")
    fun downloadFile(@PathVariable fileId: String, response: HttpServletResponse) {
        val file = fileStorageService.getFile(fileId)
        response.contentType = "application/octet-stream"
        response.setHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
        file.inputStream().copyTo(response.outputStream)
    }
}