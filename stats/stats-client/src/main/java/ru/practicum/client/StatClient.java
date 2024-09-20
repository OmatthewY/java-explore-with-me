package ru.practicum.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stat.EndpointHitDTO;
import ru.practicum.stat.StatsParams;
import ru.practicum.stat.ViewStatsDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StatClient {
    private final RestTemplate restTemplate;
    private final String startUrl;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${client.app.name}")
    private String appName;

    @Autowired
    public StatClient(@Value("${client.url}") String startUrl) {
        this.startUrl = startUrl;
        var factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setConnectionRequestTimeout(10000);
        restTemplate = new RestTemplate(factory);
    }

    public void saveStats(HttpServletRequest request) {
        try {
            String ip = request.getRemoteAddr();
            String uri = request.getRequestURI();
            EndpointHitDTO dto = EndpointHitDTO.builder()
                    .app(appName)
                    .ip(ip)
                    .uri(uri)
                    .timestamp(LocalDateTime.now())
                    .build();

            restTemplate.postForObject(startUrl + "/hit", dto, String.class);

        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    public List<ViewStatsDTO> getStats(StatsParams params) {
        try {
            String uri = UriComponentsBuilder.fromPath("/stats")
                    .queryParam("start", encodeDate(params.getStart()))
                    .queryParam("end", encodeDate(params.getEnd()))
                    .queryParam("uris", params.getUris())
                    .queryParam("unique", params.getUnique()).toUriString();
            ResponseEntity<List<ViewStatsDTO>> response = restTemplate.exchange(startUrl + uri,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<ViewStatsDTO>>() {
                    });
            return response.getBody();
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return Collections.emptyList();
    }

    private String encodeDate(LocalDateTime date) {
        return date.format(DATE_TIME_FORMATTER);
    }
}
