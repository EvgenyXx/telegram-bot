package com.example.parser.test;

import com.example.parser.core.integration.DocumentLoader;
import com.example.parser.core.parser.LeagueDetector;
import com.example.parser.core.stats.*;
import com.example.parser.modules.tournament.parser.MatchParser;
import com.example.parser.modules.tournament.parser.TournamentParser;
import com.example.parser.modules.tournament.service.result.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TestApiMain {

    public static void main(String[] args) {
        try {
            String url = "https://masters-league.com/tours/liga-a-7396/";

            // =========================
            // 🔥 КАЛЬКУЛЯТОРЫ
            // =========================
            LeagueAPointsCalculator leagueA = new LeagueAPointsCalculator();
            LeagueBPointsCalculator leagueB = new LeagueBPointsCalculator();
            LeagueCPointsCalculator leagueC = new LeagueCPointsCalculator();
            SuperLeagueCalculator superLeague = new SuperLeagueCalculator();

            PointsCalculatorFactory factory = new PointsCalculatorFactory(
                    leagueA,
                    leagueB,
                    leagueC,
                    superLeague
            );

            BonusCalculator bonusCalculator = new BonusCalculator();
            PlacementCalculator placementCalculator = new PlacementCalculator();
            NightBonusService nightBonusService = new NightBonusService();

            // =========================
            // 🔥 ПАРСЕРЫ
            // =========================
            TournamentParser tournamentParser = new TournamentParser();
            MatchParser matchParser = new MatchParser();
            LeagueDetector leagueDetector = new LeagueDetector();

            // =========================
            // 🔥 CORE СЛОИ
            // =========================
            TournamentExtractor extractor = new TournamentExtractor(
                    tournamentParser,
                    matchParser,
                    leagueDetector,
                    nightBonusService
            );

            MatchProcessor processor = new MatchProcessor(
                    placementCalculator,
                    factory
            );

            ResultBuilder builder = new ResultBuilder(bonusCalculator); // ✅ фикс

            DocumentLoader loader = new DocumentLoader();

            ResultService resultService = new ResultService(
                    loader,
                    extractor,
                    processor,
                    builder
            );

            // =========================
            // 🚀 TEST BY URL
            // =========================
            System.out.println("===== TEST BY URL =====");

            ParsedResult byUrl = resultService.calculateAll(url);
            printResult(byUrl);

            // =========================
            // 🚀 TEST BY DOCUMENT
            // =========================
            System.out.println("\n===== TEST BY DOCUMENT =====");

            Document doc = Jsoup.connect(url).get();
            ParsedResult byDoc = resultService.calculateAll(doc);

            printResult(byDoc);

            // =========================
            // 🚀 COMPARE
            // =========================
            System.out.println("\n===== COMPARE =====");

            System.out.println("Players URL: " + byUrl.getResults().size());
            System.out.println("Players DOC: " + byDoc.getResults().size());

            if (byUrl.getResults().size() == byDoc.getResults().size()) {
                System.out.println("✅ OK — результаты совпадают");
            } else {
                System.out.println("❌ ERROR — разные результаты");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printResult(ParsedResult result) {
        if (result == null) {
            System.out.println("❌ result is null");
            return;
        }

        System.out.println("TournamentId: " + result.getTournamentId());
        boolean finished = result.getStatus() == TournamentStatus.FINISHED;
        System.out.println("Finished: " + finished);
        System.out.println("Players: " + result.getResults().size());

        result.getResults().forEach(r ->
                System.out.println(r.getPlayer() + " -> " + r.getTotal())
        );
    }
}