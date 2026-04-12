package com.example.parser.test;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;

public class TestApiMain {

    public static void main(String[] args) throws Exception {

        String url = "https://masters-league.com/wp-admin/admin-ajax.php";

        for (int i = 0; i < 2; i++) {

            String date = LocalDate.now().plusDays(i).toString();

            Connection.Response res = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .header("User-Agent", "Mozilla/5.0")
                    .data("action", "tourslist")
                    .data("date", date)
                    .data("country", "RUS")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute();

            String json = res.body();

            System.out.println("\n===============================");
            System.out.println("📅 DATE: " + date);
            System.out.println("===============================\n");

            System.out.println(json); // 👈 ВОТ ОН — ЧИСТЫЙ API

            System.out.println("\n===============================\n");
        }
    }
}