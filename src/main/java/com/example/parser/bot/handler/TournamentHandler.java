package com.example.parser.bot.handler;

import com.example.parser.domain.dto.ResultDto;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.notification.formatter.TournamentMessageFormatter;
import com.example.parser.player.PlayerService;
import com.example.parser.stats.NightBonusService;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.TournamentResultService;
import com.example.parser.tournament.TournamentWatcherService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TournamentHandler {

    private final ResultService resultService;
    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;
    private final NightBonusService nightBonusService;
    private final TournamentMessageFormatter formatter;
    private final TournamentWatcherService watcherService;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        System.out.println("START API TEST");

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



        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);

        if (player == null) {
            messageService.send(bot, chatId, "❌ Ты не зарегистрирован");
            return;
        }

        if (player.isBlocked()) {
            messageService.send(bot, chatId, "🚫 Ты заблокирован");
            return;
        }

        ResultService.ParsedResult parsed = resultService.calculateAll(text);

        Long tournamentId = parsed.getTournamentId();
        List<ResultDto> results = parsed.getResults();



        String date = results.isEmpty() ? null : results.get(0).getDate();

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 Результаты турнира:\n\n");

        // дата
        if (date != null) {
            sb.append(formatDate(date)).append("\n\n");
        }

        // бонус
        double bonus = parsed.getNightBonus();

        // 1. текст результатов
        sb.append(formatter.formatResults(results, bonus));

        // 2. логика + сохранение
        boolean found = tournamentResultService.processResults(
                results,
                player,
                tournamentId,
                bonus,
                parsed.isFinished()
        );

        if (found) {
            messageService.send(bot, chatId,
                    "📅 Ты есть в турнире!\n" +
                            "Проверь дату и время");
        }

        if (!parsed.isFinished()) {
            watcherService.watch(text, telegramId, chatId, bot);

            sb.append("\n⏳ Турнир ещё не завершён")
                    .append("\n👁 Мы добавили его в отслеживание")
                    .append("\n\n💡 Как только турнир закончится — пришлём финальные результаты и сохраним их\n");
        }

        // отправка
        messageService.send(bot, chatId, sb.toString());
        messageService.sendMenu(bot, chatId, telegramId);
    }

    private String formatDate(String rawDate) {
        try {
            LocalDate date;

            if (rawDate.contains("-")) {
                date = LocalDate.parse(rawDate);
            } else {
                DateTimeFormatter input = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                date = LocalDate.parse(rawDate, input);
            }

            DateTimeFormatter output =
                    DateTimeFormatter.ofPattern("d MMMM yyyy 'года'", new Locale("ru"));

            return "📅 " + date.format(output);

        } catch (Exception e) {
            return rawDate;
        }
    }
}