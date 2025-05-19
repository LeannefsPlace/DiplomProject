package com.ivan.file_storage_service

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    @Value("\${app.images-dir}")
    private lateinit var imagesDir: String

    @Value("\${app.files-dir}")
    private lateinit var filesDir: String

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/photos/**")
            .addResourceLocations("file:$imagesDir/")
            .setCachePeriod(0)

        registry.addResourceHandler("/files/**")
            .addResourceLocations("file:$filesDir/")
            .setCachePeriod(0)
    }
}