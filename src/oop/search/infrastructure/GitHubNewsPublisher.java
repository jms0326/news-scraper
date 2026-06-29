package oop.search.infrastructure;

import oop.search.application.NewsPublisher;
import oop.search.domain.NewsResult;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class GitHubNewsPublisher extends AbstractHttpClient implements NewsPublisher {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/%s/issues";
    // %s : GITHUB_REPOSITORY <- GitHub Actions가 주는 것을 그대로 쓸 예정
    private final String token; // 환경변수로 주어질 것. GitHub Actions가 주는 것을 그대로 쓸 예정

    public GitHubNewsPublisher() {
        super(GITHUB_API_URL
                .formatted(System.getenv("GITHUB_REPOSITORY"))
        );
        this.token = System.getenv("GITHUB_TOKEN");
    }

    @Override
    public void publish(String topic, List<NewsResult> newsResults) {
//        httpClient
        String url = endpoint;

        StringBuilder sb = new StringBuilder();
        sb.append("<strong>%s</strong>(와)과 관련된 최신 뉴스입니다<br><br>".formatted(topic));
        for (NewsResult newsResult : newsResults) {
            sb.append("[%s]".formatted(newsResult.title()));
            sb.append("(%s) <br> `%s` <br>".formatted(
                    newsResult.url(),
                    newsResult.pubDate()));
            sb.append("<blockquote>%s</blockquote>".formatted(newsResult.description()));
            if (newsResult.imageUrl() != null && !newsResult.imageUrl().isBlank()) {
                sb.append("<img src=\"%s\" alt=\"%s\" width=\"320\"><br><br>".formatted(
                        newsResult.imageUrl(),
                        newsResult.title()
                ));
            }
        }
        String body = sb.toString();
        System.out.println("body = " + body);

        String payload = """
                {
                "title": "%s",
                "body": "%s"
                }
                """.formatted(
                // %s -> topic. %s -> 한국기준 현재 시간
                "%s (%s)".formatted(topic, ZonedDateTime.now(ZoneId.of("Asia/Seoul"))),
//                newsResults
                body
        ).trim();

        System.out.println("payload = " + payload);

        HttpRequest request = HttpRequest.newBuilder()
//                .GET()
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .uri(URI.create(url))
//                .header("X-Naver-Client-Id", clientId)
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        try {
            httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
