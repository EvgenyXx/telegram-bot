//package com.example.parser;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.springframework.stereotype.Service;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//public class ParserService {
//
//	public void parse() throws Exception {
//
//		String url = "https://masters-league.com/tours/liga-v-7161/";
//		Document doc = Jsoup.connect(url).get();
//
//		String text = doc.text();
//
//
//		String targetPlayer = "Бутвина Никита";
//
//		int total = 0;
//		int place = 0;
//
//		Pattern pattern = Pattern.compile(
//				"(Группа|1/2 финала|За 3-е место|Финал)\\s+" +
//						"(Завершен|Идет)\\s+" +
//						"\\d{2}:\\d{2}\\s+" +
//						"([А-Яа-я]+\\s+[А-Яа-я]+)\\s+" +
//						"(\\d+:\\d+)\\(.*?\\)\\s+" +
//						"([А-Яа-я]+\\s+[А-Яа-я]+)"
//		);
//
//		Matcher matcher = pattern.matcher(text);
//
//		while (matcher.find()) {
//
//			String stage = matcher.group(1);
//			String player1 = matcher.group(3);
//			String score = matcher.group(4);
//			String player2 = matcher.group(5);
//
//			// счет
//			String[] scoreParts = score.split(":");
//			int a = Integer.parseInt(scoreParts[0]);
//			int b = Integer.parseInt(scoreParts[1]);
//
//			// переворот если игрок справа
//			if (player2.equals(targetPlayer)) {
//				String temp = player1;
//				player1 = player2;
//				player2 = temp;
//
//				int tempScore = a;
//				a = b;
//				b = tempScore;
//			}
//
//			// только наш игрок
//			if (!player1.equals(targetPlayer)) continue;
//
//			// ===== ОЧКИ =====
//			int points = 0;
//
//			if (a == 4) points = 1200;
//			else if (a == 3) points = 650;
//			else if (a == 2) points = 500;
//			else if (a == 1) points = 350;
//			else if (a == 0) points = 200;
//
//			total += points;
//
//			// ===== ОПРЕДЕЛЕНИЕ МЕСТА =====
//			if (stage.equals("Финал")) {
//				if (a == 4) place = 1;
//				else place = 2;
//			}
//
//			if (stage.equals("За 3-е место")) {
//				if (a == 4) place = 3;
//				else place = 4;
//			}
//
//			String lastName1 = player1.split(" ")[0];
//			String lastName2 = player2.split(" ")[0];
//
//			System.out.println(stage + " | " +
//					lastName1 + " vs " +
//					lastName2 + " " +
//					a + ":" + b +
//					" | +" + points);
//		}
//
//		// ===== БОНУС =====
//		int bonus = 0;
//
//		if (place == 1) bonus = 1000;
//		else if (place == 2) bonus = 600;
//		else if (place == 3) bonus = 400;
//
//		total += bonus;
//
//		// ===== ВЫВОД =====
//		System.out.println("\n=== РЕЗУЛЬТАТ ===");
//		System.out.println("Место: " + place);
//		System.out.println("Бонус: " + bonus);
//		System.out.println("ИТОГО: " + total);
//	}
//}