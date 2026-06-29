package oop.search.infrastructure;

import oop.search.application.NewsProvider;
import oop.search.domain.NewsCategory;
import oop.search.domain.NewsResult;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//public class NaverNewsProvider extends AbstractHttpScraper {
public class NaverNewsProvider extends AbstractHttpClient implements NewsProvider {
    //    protected AbstractHttpScraper(String endpoint) {
//        this.endpoint = endpoint;
//    }
    // 생성자 레벨에서 사용할 상수는 static
    private static final String NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json";
    private final String clientId;
    private final String clientSecret;
    private final NewsCategory category;

    // clientId, clientSecret, category
    public NaverNewsProvider() {
        super(NEWS_API_URL);
        this.clientId = System.getenv("NAVER_CLIENT_ID");
        this.clientSecret = System.getenv("NAVER_CLIENT_SECRET");
        String categoryName = System.getenv("NEWS_CATEGORY");
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("NAVER_CLIENT_ID 환경변수가 필요합니다.");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("NAVER_CLIENT_SECRET 환경변수가 필요합니다.");
        }
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalStateException("NEWS_CATEGORY 환경변수가 필요합니다. 예: SIM 또는 DATE");
        }
        this.category = NewsCategory.valueOf(categoryName);
        System.out.println("clientId = " + clientId.substring(0, 3) + "...");
        System.out.println("clientSecret = " + clientSecret.substring(0, 3) + "...");
        System.out.println("category = " + category);
    }

    @Override
    public List<NewsResult> fetchNews(String searchQuery, int limit) {
        String url = endpoint + "?query="
                + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8)
                + "&display=" + limit
                + "&sort=" + category.getQueryValue()
                + "&start=1";
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .build();

        List<NewsResult> results = new ArrayList<>();
        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            String body = response.body();
//            System.out.println("body = " + body);

            // items
            String items = body.split("items")[1]; // 0 <-> 1
            // <- items ->
//            System.out.println("items = " + items);
            String[] itemArr = items.split("},");
            for (String item : itemArr) {
//                System.out.println("item = " + item);
//                String title = item
//                        .split("\"title\":\"")[1] // 0 <-> 1 -> ["title":"]
//                        .split("\",")[0]; // ",
                String title = cutText(item, "\"title\":\"", "\",\n");
                String link = cutText(item, "\"link\":\"", "\",\n");
                String description = cutText(item, "\"description\":\"", "\",\n");
                // pubDate는 문자열 ""가 추가적으로 들어갈 염려가 없기 때문에 바로 "로 구분
                String pubDate = cutText(item, "\"pubDate\":\"", "\"");

                link = link.replace("\\/", "/");
                String imageUrl = fetchImageUrl(link);
                NewsResult result = new NewsResult(title, description, link, pubDate, imageUrl);
                results.add(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//        return List.of();
        return results;
    }

    public String cutText(String original, String prefix, String suffix) {
        return original
                .split(prefix)[1]
                .split(suffix)[0];
    }

    private String fetchImageUrl(String articleUrl) {
        try {
            HttpRequest imageRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(articleUrl))
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> imageResponse = httpClient.send(
                    imageRequest,
                    HttpResponse.BodyHandlers.ofString()
            );
            String html = imageResponse.body();

            Pattern pattern = Pattern.compile(
                    "<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']+)[\"'][^>]*>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1);
            }

            Pattern fallbackPattern = Pattern.compile(
                    "<meta[^>]*name=[\"']twitter:image[\"'][^>]*content=[\"']([^\"']+)[\"'][^>]*>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher fallbackMatcher = fallbackPattern.matcher(html);
            if (fallbackMatcher.find()) {
                return fallbackMatcher.group(1);
            }
        } catch (Exception e) {
            System.out.println("이미지 URL을 가져오지 못했습니다: " + articleUrl);
        }
        return "";
    }

    public static void main(String[] args) {
        NewsProvider provider = new NaverNewsProvider();
        List<NewsResult> results = provider.fetchNews("프리티걸", 10);
//        System.out.println("results = " + results);
        for (NewsResult newsItem : results) {
            System.out.println("newsItem = " + newsItem);
        }
    }
}
