package org.delcom.ifs25025springbootcashflow.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Menyediakan favicon untuk browser dan crawler yang meminta path standar. */
@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/icons/icon.png"))
                .build();
    }
}
