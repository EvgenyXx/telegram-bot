//package com.example.parser.test;
//
//import com.example.parser.core.dto.ResultDto;
//import com.example.parser.modules.tournament.application.ResultService;
//import com.example.parser.modules.tournament.domain.ParsedResult;
//import org.springframework.boot.WebApplicationType;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.context.ConfigurableApplicationContext;
//
//public class TestApiMain {
//
//    public static void main(String[] args) {
//
//        // Поднимаем Spring БЕЗ веба и БЕЗ бота
//        ConfigurableApplicationContext context =
//                new SpringApplicationBuilder(TestConfig.class)
//                        .web(WebApplicationType.NONE)
//                        .run(args);
//
//        try {
//            ResultService resultService = context.getBean(ResultService.class);
//
//            String url = "https://masters-league.com/tours/liga-s-11466/";
//
//            System.out.println("🚀 START: " + url);
//
//            ParsedResult result = resultService.calculateAll(url);
//
//            // =========================
//            // ВЫВОД
//            // =========================
//            System.out.println("\n===== RESULT =====");
//            System.out.println("TOURNAMENT ID: " + result.getTournamentId());
//            System.out.println("STATUS: " + result.getStatus());
//            System.out.println("NIGHT BONUS: " + result.getNightBonus());
////            System.out.println("HAS REMOVED: " + result.isHasRemovedPlayers());
//
//            System.out.println("\n===== FINAL TABLE =====");
//
//            for (ResultDto r : result.getResults()) {
//                System.out.println(
//                        r.getPlace() + ". " +
//                                r.getPlayer() +
//                                " | total = " + r.getTotal() +
//                                " | bonus = " + r.getBonus()
//                );
//            }
//
//            System.out.println("\n✅ DONE");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            context.close();
//        }
//    }
//}