package ru.easyjava.spring.webmvc.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HttpController {

    @GetMapping("/strings")
    List<String> listStrings() {
        return Arrays.asList("Lorem", "ipsum", "dolor", "sit", "amet");
    }

    @GetMapping("/private")
    String getTruth(@RequestHeader(value = "Authorization", defaultValue = "") String authorization) {
        if (!"password".equals(authorization)) {
            throw new NotAuthorizedException();
        }
        return "The banana is big, but the skin is bigger.";
    }

    @PostMapping(value = "/note", consumes = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.CREATED)
    String createJsonNote(@RequestBody String data) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, String>> typeRef
                = new TypeReference<HashMap<String, String>>() {};
        Map<String, String> note = mapper.readValue(data, typeRef);
        return String.format("title=%s\ncontent=%s\n", note.get("title"), note.get("content"));
    }

    @PostMapping(value = "/note", consumes = "text/xml")
    @ResponseStatus(HttpStatus.CREATED)
    String createXMLNote(@RequestBody String data) throws IOException {
        XStream mapper = new XStream();
        mapper.registerConverter(new MapConverter());
        mapper.alias("note", Map.class);

        Map<String, String> note = (Map<String, String>) mapper.fromXML(data);
        return String.format("title=%s\ncontent=%s\n", note.get("title"), note.get("content"));
    }

    @GetMapping(value = "/note", produces = "application/json;charset=UTF-8")
    String getJsonNote() {
        return "{\"title\":\"Stored note\", \"content\": \"A note on the server\"}";
    }

    @GetMapping(value = "/note", produces = "application/html")
    String getHtmlNote() {
        return "<html><head><title>Stored note</title></head><body><h1>Stored note</h1><p>A note on the server</p></body></html>";
    }

    @GetMapping(value = "/cookie")
    String readCookie(@CookieValue(value = "data", defaultValue = "") String data) {
        return String.format("Cookie value is '%s'", data);
    }

    @GetMapping(value = "/darkside")
    void setCookie(HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie("data", "Come_to_the_dark_side");
        cookie.setPath("/");
        cookie.setMaxAge(86400);

        response.addCookie(cookie);
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("We have cookies");
        response.getWriter().flush();
        response.getWriter().close();
    }
}
