package dev.loredev.fakeserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FakeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // --- przełączniki ---
    public boolean enabled = true;
    public boolean scoreboard = true;
    public boolean fakeTab = true;
    public boolean fakeChat = true;
    public boolean spam = true;
    public boolean fakeJoinLeave = false;
    public boolean fakeReplies = true;
    public boolean fakeCommands = true;
    /** true = zakup na rynku wykonuje prawdziwe /give (działa w singleplayer z cheatami / jako OP) */
    public boolean marketGivesItems = false;

    // --- wygląd ---
    public String title = "&e&lUPIORNY &6&lBANAN";
    public String sector = "M2";
    public int scoreboardOffsetY = 0;
    public String tipText = "&fDopiero zaczynasz rozgrywkę? Sprawdź listę przydatnych komend! &a/pomoc";
    public String eventText = "&7Brak aktywnego eventu.";
    public String discord = "dc.banansmp.pl";

    // --- liczby ---
    public int baseOnline = 822;
    public int maxPlayers = 4000;
    public int tabSize = 70;
    public int chatMinSeconds = 2;
    public int chatMaxSeconds = 7;
    public int spamChancePercent = 12;
    public int joinLeaveMinSeconds = 6;
    public int joinLeaveMaxSeconds = 20;
    public int replyChancePercent = 70;
    public int keyallPeriodMinutes = 60;

    // --- Twoje statystyki (zapisują się same) ---
    public String playerRank = "VOID";
    /** Wyświetlany nick (pusty = Twój prawdziwy nick). Zmiana: /fakeserver nick <nazwa> */
    public String playerNick = "";
    public long balance = 0;
    public int shards = 0;
    public int kills = 0;
    public int deaths = 0;
    public int lives = 5;
    public int keyall = 5;
    public int playMinutes = 0;
    public String team = "Brak";

    public String joinMessage = "&8[&a+&8] &7{nick}";
    public String leaveMessage = "&8[&c-&8] &7{nick}";

    public List<String> scoreboardLines = new ArrayList<>(List.of(
            "&a$ &fKasa: &a${balance}",
            "&d\u2022 &fShardy: &d{shards}",
            "&e\u2022 &fZabójstwa: &e{kills}",
            "&6\u2022 &fŚmierci: &6{deaths}",
            "&c\u2665 &fŻycia: &c{lives}",
            "&a\u2022 &fKeyall: &a{keyall}m",
            "&e\u2022 &fCzas gry: &e{playtime}m",
            "&b\u2022 &fTeam: &b{team}",
            "",
            "&6www.banansmp.pl"
    ));

    // lista od NAJNIŻSZEJ do NAJWYŻSZEJ rangi
    public List<Rank> ranks = new ArrayList<>(List.of(
            new Rank("VOID", "#AA00FF", "#FFFFFF", 40),
            new Rank("DYNIA", "#FF8800", "#FFFFFF", 50),
            new Rank("BANAN", "#FFCC00", "#FFFF55", 6),
            new Rank("MEDIA", "#FF55FF", "#FF55FF", 3),
            new Rank("M+", "#3399FF", "#55CCFF", 0),
            new Rank("H", "#33CC33", "#55FF55", 0),
            new Rank("H@", "#DD2222", "#FF5555", 0)
    ));

    public List<StaffEntry> staff = new ArrayList<>(List.of(
            new StaffEntry("Zeenciak", "H@"),
            new StaffEntry("ModKarol", "M+"),
            new StaffEntry("ModAgata", "M+"),
            new StaffEntry("HelperTomek", "H"),
            new StaffEntry("HelperOla", "H"),
            new StaffEntry("HelperPawel", "H"),
            new StaffEntry("HelperIga", "H"),
            new StaffEntry("HelperBartek", "H")
    ));

    public List<String> nicks = new ArrayList<>(List.of(
            "Kuba_PL", "xXPiotrekXx", "Szymon123", "MajsterKraft", "AnitaMC", "DiamondKing",
            "Kamil_YT", "ZielonyCreeper", "Ola_Gamer", "PogromcaSmoka", "TomekBuilder", "Wojtek2009",
            "NetherLord", "Basia_MC", "Krzychu_PvP", "LegendaPL", "Mati_Pro", "Natalia_Craft",
            "BananMan", "OgnistyDragon", "Staszek_x", "KarolinaMC", "EnderPL", "Franek_Farm",
            "SkyWalker_PL", "Zuzia_Mine", "BlokMaster", "Hubert_SMP", "RedstoneRobo", "Marta_Builds",
            "GornikPL", "Dawid_Prime", "KretMistrz", "IgaVIP", "Lukasz_MC", "CzarnyWilk",
            "Pawel_Eco", "Julka_Pixel", "Adrian_Trade", "TurboBanan", "Oskar_Mine", "Wiktoria_PL",
            "Bartek_PvP", "SzefowaKopalni", "ZlotyKilof", "MikoBuilds", "Kacper_SMP", "ZombieSlayer",
            "Dynia_Lord", "PanKreeper", "Majka_Craft", "Filip2012", "NocnyLowca", "KotekMC",
            "Radek_Farm", "SuperSzymon", "Ania_Pixel", "Gracjan_PvP", "LodowyKrol", "Blazej_MC",
            "Tosia_Gamer", "KopalniaPL", "Wilczur_PL", "Janek_Redstone", "Maja_Cube", "BlackBanan",
            "TNT_Maniak", "Nikodem_SMP", "Zosia_Craft", "PiksaPL", "Ignacy_x", "DiamentowyKret",
            "Lena_MC", "Stefan_Eco", "CreeperHunter", "Hania_PL", "Olek_Craft", "Rysiek_Farm",
            "PustynnyWilk", "Emil_PvP", "Alicja_Mine", "GrzechuMC", "Borys_Build", "StaryKoparka",
            "Leszek_SMP", "Kinga_Gamer", "FrostPL", "Szymek_Pro", "Daria_Craft"
    ));

    public List<String> chatMessages = new ArrayList<>(List.of(
            "siema wszystkim",
            "ktoś chce wymianę na diamenty?",
            "sprzedam 64 złota za 5k",
            "kto idzie na event?",
            "ale lagi dzisiaj xd",
            "gdzie jest spawn?",
            "potrzebuję pomocy z działką",
            "kupię netherite ingot",
            "gg",
            "kto ma klucze do skrzyń?",
            "dzięki za pomoc!",
            "właśnie wydropiło mi spawner",
            "ktoś pvp na arenie?",
            "hej, jak zrobić home?",
            "xd",
            "ale ten rynek drogi",
            "wbijajcie na minigry",
            "ktoś ma zapasowy kilof?",
            "co tam u was?",
            "idę farmić shardy",
            "ile kosztuje ranga dynia?",
            "kiedy keyall?",
            "kto sprzeda elytre",
            "ktoś mnie zabił na spawnie xd",
            "dajcie pare monet pls"
    ));

    public List<String> spamMessages = new ArrayList<>(List.of(
            "KTO ITY TPAKTO ITY TPAKTO ITY TPAKTO",
            "WBIJAJCIE NA ARENE ARENA ARENA",
            "sprzedam spawner pw",
            "kupie klucze pw",
            "daję rangę za 10k kto pierwszy",
            "ADMIN ODBANUJ MNIE",
            "wbijajcie na live",
            "SPRZEDAM DIAMENTY TANIO PW"
    ));

    public List<String> replyMessages = new ArrayList<>(List.of(
            "hej!", "siema", "tak", "nie wiem xd", "pewnie", "haha", "spoko", "dzięki", "ok", "co?"
    ));

    public List<MarketItem> market = new ArrayList<>(List.of(
            new MarketItem("diamond", 1, 250),
            new MarketItem("iron_ingot", 16, 120),
            new MarketItem("gold_ingot", 16, 200),
            new MarketItem("emerald", 8, 300),
            new MarketItem("netherite_ingot", 1, 5000),
            new MarketItem("ancient_debris", 2, 3500),
            new MarketItem("golden_apple", 1, 400),
            new MarketItem("enchanted_golden_apple", 1, 12000),
            new MarketItem("ender_pearl", 4, 300),
            new MarketItem("obsidian", 16, 150),
            new MarketItem("tnt", 8, 500),
            new MarketItem("elytra", 1, 25000),
            new MarketItem("totem_of_undying", 1, 3000),
            new MarketItem("experience_bottle", 16, 400),
            new MarketItem("cooked_beef", 32, 100),
            new MarketItem("bread", 32, 60),
            new MarketItem("golden_carrot", 16, 120),
            new MarketItem("oak_log", 64, 80),
            new MarketItem("cobblestone", 64, 40),
            new MarketItem("arrow", 64, 90),
            new MarketItem("bow", 1, 150),
            new MarketItem("crossbow", 1, 250),
            new MarketItem("shield", 1, 120),
            new MarketItem("diamond_sword", 1, 700),
            new MarketItem("diamond_pickaxe", 1, 900),
            new MarketItem("diamond_chestplate", 1, 1500),
            new MarketItem("netherite_sword", 1, 8000),
            new MarketItem("blaze_rod", 4, 200),
            new MarketItem("slime_ball", 16, 180),
            new MarketItem("name_tag", 1, 600),
            new MarketItem("saddle", 1, 800),
            new MarketItem("shulker_box", 1, 4000),
            new MarketItem("beacon", 1, 20000),
            new MarketItem("spawner", 1, 150000),
            new MarketItem("dragon_egg", 1, 500000),
            new MarketItem("nether_star", 1, 30000)
    ));

    public static class Rank {
        public String name = "";
        public String badgeColor = "#AAAAAA";
        public String nameColor = "#FFFFFF";
        public int weight = 1;

        public Rank() {}

        public Rank(String name, String badgeColor, String nameColor, int weight) {
            this.name = name;
            this.badgeColor = badgeColor;
            this.nameColor = nameColor;
            this.weight = weight;
        }

        private static int parse(String s, int fallback) {
            try {
                return Integer.parseInt(s.replace("#", ""), 16);
            } catch (Exception e) {
                return fallback;
            }
        }

        public int badgeRgb() {
            return parse(badgeColor, 0xAAAAAA);
        }

        public int nameRgb() {
            return parse(nameColor, 0xFFFFFF);
        }
    }

    public static class StaffEntry {
        public String name = "";
        public String rank = "";

        public StaffEntry() {}

        public StaffEntry(String name, String rank) {
            this.name = name;
            this.rank = rank;
        }
    }

    public static class MarketItem {
        public String id = "stone";
        public int amount = 1;
        public long price = 1;

        public MarketItem() {}

        public MarketItem(String id, int amount, long price) {
            this.id = id;
            this.amount = amount;
            this.price = price;
        }
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("loredev-fakeserver-v2.json");
    }

    public static FakeConfig load() {
        FakeConfig cfg = null;
        Path p = path();
        try {
            if (Files.exists(p)) {
                try (Reader r = Files.newBufferedReader(p)) {
                    cfg = GSON.fromJson(r, FakeConfig.class);
                }
            }
        } catch (Exception e) {
            System.err.println("[FakeServer] Nie udało się wczytać configu: " + e);
        }
        if (cfg == null) {
            cfg = new FakeConfig();
        }
        cfg.normalize();
        cfg.save();
        return cfg;
    }

    public void save() {
        try (Writer w = Files.newBufferedWriter(path())) {
            GSON.toJson(this, w);
        } catch (Exception e) {
            System.err.println("[FakeServer] Nie udało się zapisać configu: " + e);
        }
    }

    private void normalize() {
        if (ranks == null || ranks.isEmpty()) {
            ranks = new ArrayList<>(List.of(new Rank("VOID", "#AA00FF", "#FFFFFF", 1)));
        }
        if (staff == null) staff = new ArrayList<>();
        if (nicks == null) nicks = new ArrayList<>();
        if (chatMessages == null) chatMessages = new ArrayList<>();
        if (spamMessages == null) spamMessages = new ArrayList<>();
        if (replyMessages == null) replyMessages = new ArrayList<>();
        if (scoreboardLines == null) scoreboardLines = new ArrayList<>();
        if (market == null) market = new ArrayList<>();
        if (team == null || team.isEmpty()) team = "Brak";
        if (tabSize < 1) tabSize = 1;
        if (tabSize > 72) tabSize = 72;
    }

    public Rank findRank(String name) {
        for (Rank r : ranks) {
            if (r.name != null && r.name.equalsIgnoreCase(name)) {
                return r;
            }
        }
        return ranks.get(0);
    }

    public boolean hasRank(String name) {
        for (Rank r : ranks) {
            if (r.name != null && r.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public int priority(Rank r) {
        return ranks.indexOf(r);
    }
}
