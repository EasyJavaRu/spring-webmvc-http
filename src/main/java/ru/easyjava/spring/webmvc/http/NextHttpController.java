package ru.easyjava.spring.webmvc.http;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class NextHttpController {

    @GetMapping("/day")
    ResponseEntity<String> getDayWithCache() {
        CacheControl cache = CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic();
        return ResponseEntity
                .ok()
                .cacheControl(cache)
                .body(LocalDate.now().toString());
    }

    @GetMapping("/tomorrow")
    ResponseEntity<String> getDayWithEtag() {
        String tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS).toString();
        CacheControl cache = CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic();
        return ResponseEntity
                .ok()
                .cacheControl(cache)
                .eTag(tomorrow)
                .body(tomorrow);
    }

    @GetMapping("/effectivetomorrow")
    ResponseEntity<String> getDayEffectively(WebRequest request) {
        Long lastModified = LocalDate.now().atTime(0,0,0).toInstant(ZoneOffset.UTC).toEpochMilli();
        if (request.checkNotModified(lastModified)) { //Check modification timestamp
            return null;
        }

        String tomorrow = LocalDate.now().plus(1, ChronoUnit.DAYS).toString();
        if (request.checkNotModified(tomorrow)) { //Check etag
            return null;
        }

        CacheControl cache = CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic();
        return ResponseEntity
                .ok()
                .cacheControl(cache)
                .eTag(tomorrow)
                .body(tomorrow);
    }

    @PostMapping("/form")
    String postForm(@RequestParam("key") String key, @RequestParam("value") String value) {
        return String.format("%s = %s", key, value);
    }

    @PostMapping("/file")
    StringBuilder postFile(@RequestPart("metadata") String data, @RequestPart("filedata") MultipartFile file) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append(String.format("File data: %s\n", data));
        result.append("File content:\n");
        result.append(new String(file.getBytes(), "UTF-8"));
        return result;
    }
}
