package oop.search.presentation;

import oop.search.application.NewsPublisher;
import oop.search.domain.NewsResult;

import java.util.List;

public class ConsoleNewsPublisher implements NewsPublisher {
    @Override
    public void publish(String topic, List<NewsResult> newsResults) {
//        System.out.println("뉴스 주제 : " + topic);
        // System.out.println("뉴스 주제 : %s".formatted(topic));
        for (NewsResult newsResult : newsResults) {
//            System.out.println(newsResult);
            //. System.out.println("=".repeat(16));
            String output = """
                    제목 : %s
                    링크 : %s
                    설명 : %s
                    발행일자 : %s
                    이미지 경로 : %s
                    """.formatted(newsResult.title(),
                            newsResult.url(),
                            newsResult.description(),
                            newsResult.pubDate(),
                            newsResult.imageUrl())
                    .trim(); // 앞뒤의 공백이나 줄바꿈을 제거
            System.out.println(output);
        }
    }
}