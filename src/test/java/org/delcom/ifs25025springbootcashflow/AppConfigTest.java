package org.delcom.ifs25025springbootcashflow;

import org.delcom.ifs25025springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25025springbootcashflow.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    @Test
    void uploadsAreServedFromTheUploadFolder() throws Exception {
        FileStorage storage = new FileStorage(Files.createTempDirectory("cashflow-config-test").toString());
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        WebMvcConfigurer configurer = new AppConfig().uploadsResourceConfigurer(storage);
        configurer.addResourceHandlers(registry);

        verify(registration).addResourceLocations(storage.getRoot().toUri().toString());
        Mockito.verifyNoMoreInteractions(registration);
    }
}
