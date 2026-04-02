package com.example.parser.test;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    public void testApi() throws Exception {

        String url = "https://masters-league.com/wp-admin/admin-ajax.php";

        Connection.Response res = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .header("User-Agent", "Mozilla/5.0")
                .data("action", "tourslist")
                .data("date", "2026-04-02")
                .data("country", "RUS")
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        System.out.println(res.body());
    }
}