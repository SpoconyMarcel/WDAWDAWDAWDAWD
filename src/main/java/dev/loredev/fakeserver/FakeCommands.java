package dev.loredev.fakeserver;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Udawane komendy serwera. Zwraca true, jeśli komenda została obsłużona (i NIE ma iść do prawdziwego serwera). */
public final class FakeCommands {
    private FakeCommands() {}

    public static boolean handle(String raw) {
        FakeConfig cfg = FakeServerMod.config();
        if (cfg == null || !cfg.enabled || !cfg.fakeCommands) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        FakeState st = FakeServerMod.state();

        String[] a = raw.trim().split("\\s+");
        String cmd = a[0].toLowerCase(Locale.ROOT);

        switch (cmd) {
            case "pomoc" -> help(mc, st);
            case "msg", "w", "tell", "m", "pw" -> msg(mc, st, a);
            case "r", "reply" -> reply(mc, st, a);
            case "kasa", "bal", "balance", "money" -> kasa(mc, st, a);
            case "pay" -> pay(mc, st, a);
            case "baltop" -> baltop(mc, st);
            case "rynek", "market", "sklep", "ah" -> st.later(1, () -> mc.setScreen(new MarketScreen()));
            case "spawn" -> teleport(mc, st, "spawn");
            case "home" -> {
                if (st.homeSet) teleport(mc, st, "dom");
                else st.chat(mc, "&cNie masz ustawionego domu! &7Użyj &e/sethome&7.");
            }
            case "sethome" -> {
                st.homeSet = true;
                st.chat(mc, "&aDom został ustawiony.");
            }
            case "tpa" -> {
                if (a.length < 2) st.chat(mc, "&cUżycie: &e/tpa <nick>");
                else st.chat(mc, "&aWysłano prośbę o teleportację do &e" + a[1] + "&a.");
            }
            case "back", "warp", "tpaccept", "tpdeny" ->
                    st.chat(mc, "&7Ta komenda jest niedostępna w sektorze &a" + cfg.sector + "&7.");
            case "daily" -> daily(mc, st);
            case "shardy" -> st.chat(mc, "&7Twoje shardy: &d" + st.shards);
            case "team" -> team(mc, st, a);
            case "stats", "statystyki" -> stats(mc, st);
            case "lista", "online", "gracze" -> lista(mc, st);
            case "ping" -> st.chat(mc, "&7Twój ping: &a" + st.ping + "ms");
            case "discord" -> st.chat(mc, "&9Discord: &f" + cfg.discord);
            case "event" -> st.chat(mc, cfg.eventText);
            case "keyall" -> st.chat(mc, "&7Następny &6keyall &7za &e" + st.keyall + " min&7.");
            case "regulamin" -> st.chat(mc, "&7Regulamin znajdziesz na &f" + cfg.discord + "&7.");
            case "ec", "enderchest", "repair", "fix" -> locked(mc, st, cfg);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void help(MinecraftClient mc, FakeState st) {
        String[] lines = {
                "&6&lKOMENDY &8- &7UPIORNY BANAN",
                "&e/rynek &7- otwiera rynek",
                "&e/kasa &7- stan konta, &e/pay <nick> <kwota> &7- przelew",
                "&e/msg <nick> <wiadomość> &7- wiadomość prywatna (&e/r &7- odpowiedz)",
                "&e/team stworz <nazwa> &7- zakładanie teamu",
                "&e/daily &7- dzienna nagroda, &e/stats &7- statystyki",
                "&e/lista &7- gracze online, &e/baltop &7- najbogatsi",
                "&e/spawn /home /sethome /tpa &7- teleportacje",
                "&e/discord /event /keyall &7- informacje"
        };
        for (String l : lines) st.chat(mc, l);
    }

    private static void msg(MinecraftClient mc, FakeState st, String[] a) {
        if (a.length < 3) {
            st.chat(mc, "&cUżycie: &e/msg <nick> <wiadomość>");
            return;
        }
        FakeState.FakePlayer target = st.findPlayer(a[1]);
        if (target == null) {
            st.chat(mc, "&cGracz &e" + a[1] + " &cnie jest online.");
            return;
        }
        st.sendPrivate(mc, target, String.join(" ", Arrays.copyOfRange(a, 2, a.length)));
    }

    private static void reply(MinecraftClient mc, FakeState st, String[] a) {
        if (st.lastMsg == null) {
            st.chat(mc, "&cNie masz komu odpisać.");
            return;
        }
        if (a.length < 2) {
            st.chat(mc, "&cUżycie: &e/r <wiadomość>");
            return;
        }
        FakeState.FakePlayer target = st.findPlayer(st.lastMsg);
        if (target == null) {
            st.chat(mc, "&cTen gracz wyszedł z serwera.");
            return;
        }
        st.sendPrivate(mc, target, String.join(" ", Arrays.copyOfRange(a, 1, a.length)));
    }

    private static void kasa(MinecraftClient mc, FakeState st, String[] a) {
        if (a.length >= 2) {
            FakeState.FakePlayer p = st.findPlayer(a[1]);
            if (p == null) {
                st.chat(mc, "&cGracz &e" + a[1] + " &cnie jest online.");
            } else {
                long v = 100 + st.rnd.nextInt(500000);
                st.chat(mc, "&7Stan konta gracza &e" + p.name() + "&7: &a$" + FakeState.fmt(v));
            }
            return;
        }
        st.chat(mc, "&7Stan konta: &a$" + FakeState.fmt(st.balance));
    }

    private static void pay(MinecraftClient mc, FakeState st, String[] a) {
        if (a.length < 3) {
            st.chat(mc, "&cUżycie: &e/pay <nick> <kwota>");
            return;
        }
        FakeState.FakePlayer p = st.findPlayer(a[1]);
        if (p == null) {
            st.chat(mc, "&cGracz &e" + a[1] + " &cnie jest online.");
            return;
        }
        long amount;
        try {
            amount = Long.parseLong(a[2]);
        } catch (NumberFormatException e) {
            st.chat(mc, "&cKwota musi być liczbą.");
            return;
        }
        if (amount <= 0) {
            st.chat(mc, "&cKwota musi być większa od zera.");
            return;
        }
        if (amount > st.balance) {
            st.chat(mc, "&cNie masz tyle pieniędzy! &7Stan konta: &a$" + FakeState.fmt(st.balance));
            return;
        }
        st.balance -= amount;
        st.persist();
        st.chat(mc, "&aPrzelano &e$" + FakeState.fmt(amount) + " &ado gracza &e" + p.name() + "&a.");
    }

    private static void baltop(MinecraftClient mc, FakeState st) {
        List<String> names = new ArrayList<>();
        for (FakeState.FakePlayer p : st.active) names.add(p.name());
        Random r = new Random(names.hashCode());
        long v = 9_000_000L + r.nextInt(5_000_000);
        st.chat(mc, "&6&lNAJBOGATSI GRACZE");
        for (int i = 0; i < Math.min(10, names.size()); i++) {
            st.chat(mc, "&e" + (i + 1) + ". &f" + names.get(r.nextInt(names.size())) + " &8- &a$" + FakeState.fmt(v));
            v -= 200_000 + r.nextInt(600_000);
        }
    }

    private static void teleport(MinecraftClient mc, FakeState st, String where) {
        st.chat(mc, "&7Teleportacja za &e5 &7sekund... &cnie ruszaj się!");
        st.later(100, () -> st.chat(mc, "&aPrzeteleportowano (" + where + ")."));
    }

    private static void daily(MinecraftClient mc, FakeState st) {
        if (st.dailyTaken) {
            st.chat(mc, "&cDzienną nagrodę odebrałeś już dzisiaj!");
            return;
        }
        st.dailyTaken = true;
        st.balance += 500;
        st.shards += 5;
        st.persist();
        st.chat(mc, "&aOdebrano dzienną nagrodę: &e$500 &ai &d5 shardów&a!");
    }

    private static void team(MinecraftClient mc, FakeState st, String[] a) {
        if (a.length >= 3 && a[1].equalsIgnoreCase("stworz")) {
            st.team = a[2];
            st.persist();
            st.chat(mc, "&aStworzono team &b" + a[2] + "&a!");
        } else if (a.length >= 2 && (a[1].equalsIgnoreCase("opusc") || a[1].equalsIgnoreCase("usun"))) {
            st.team = "Brak";
            st.persist();
            st.chat(mc, "&7Opuściłeś team.");
        } else {
            st.chat(mc, "&cUżycie: &e/team stworz <nazwa> &7| &e/team opusc");
        }
    }

    private static void stats(MinecraftClient mc, FakeState st) {
        st.chat(mc, "&6&lSTATYSTYKI");
        st.chat(mc, "&7Zabójstwa: &e" + st.kills + " &8| &7Śmierci: &6" + st.deaths + " &8| &7Życia: &c" + st.lives);
        st.chat(mc, "&7Czas gry: &e" + st.playMinutes + "m &8| &7Team: &b" + st.team);
    }

    private static void lista(MinecraftClient mc, FakeState st) {
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (FakeState.FakePlayer p : st.tabList(mc)) {
            if (n++ >= 12) break;
            if (sb.length() > 0) sb.append("&7, ");
            sb.append("&f").append(p.name());
        }
        st.chat(mc, "&7Gracze online (&a" + st.online + "&7): " + sb + "&7...");
    }

    private static void locked(MinecraftClient mc, FakeState st, FakeConfig cfg) {
        int need = cfg.priority(cfg.findRank("BANAN"));
        int have = cfg.priority(cfg.findRank(cfg.playerRank));
        if (have >= need) st.chat(mc, "&aGotowe!");
        else st.chat(mc, "&cTa komenda wymaga rangi &eBANAN &club wyższej. &7Kup ją w &e/sklep&7.");
    }
}
