package dev.loredev.fakeserver;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class FakeState {
    public record FakePlayer(String name, FakeConfig.Rank rank, int ping, boolean staff) {}

    private record Pending(long due, Runnable action) {}

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    final Random rnd = new Random();
    final List<FakePlayer> active = new ArrayList<>();
    final List<FakePlayer> staff = new ArrayList<>();
    private final List<Pending> pending = new ArrayList<>();
    FakeConfig cfg;

    private long tick;
    private long nextChat;
    private long nextJoinLeave;
    private long nextSlow;
    private long nextTip;

    int online;
    int ping = 40;
    long balance;
    int shards;
    int kills;
    int deaths;
    int lives;
    int keyall;
    int playMinutes;
    String team = "Brak";
    String lastMsg = null;
    boolean homeSet = false;
    boolean dailyTaken = false;

    public static String color(String s) {
        return s.replace('&', '\u00a7');
    }

    public static String fmt(long value) {
        return String.format(Locale.GERMAN, "%,d", value);
    }

    // ---------- start / reset ----------

    public void reset(FakeConfig cfg) {
        this.cfg = cfg;
        pending.clear();
        balance = cfg.balance;
        shards = cfg.shards;
        kills = cfg.kills;
        deaths = cfg.deaths;
        lives = cfg.lives;
        keyall = cfg.keyall;
        playMinutes = cfg.playMinutes;
        team = cfg.team;
        online = cfg.baseOnline;
        rebuildActive();
        schedule();
    }

    public void onJoin() {
        if (cfg == null) return;
        pending.clear();
        rebuildActive();
        schedule();
    }

    private void schedule() {
        nextSlow = tick + 40;
        nextChat = tick + secs(cfg.chatMinSeconds, cfg.chatMaxSeconds);
        nextJoinLeave = tick + secs(cfg.joinLeaveMinSeconds, cfg.joinLeaveMaxSeconds);
        nextTip = tick + 200;
    }

    public void persist() {
        cfg.balance = balance;
        cfg.shards = shards;
        cfg.kills = kills;
        cfg.deaths = deaths;
        cfg.lives = lives;
        cfg.keyall = keyall;
        cfg.playMinutes = playMinutes;
        cfg.team = team;
        cfg.save();
    }

    public int online() {
        return online;
    }

    public String displayNick(MinecraftClient mc) {
        if (cfg != null && cfg.playerNick != null && !cfg.playerNick.isBlank()) {
            return cfg.playerNick;
        }
        return mc.player.getName().getString();
    }

    private long secs(int min, int max) {
        int lo = Math.max(1, min);
        int span = Math.max(1, max - min + 1);
        return (lo + rnd.nextInt(span)) * 20L;
    }

    private String pick(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return list.get(rnd.nextInt(list.size()));
    }

    private FakeConfig.Rank randomRank() {
        int total = 0;
        for (FakeConfig.Rank r : cfg.ranks) total += Math.max(0, r.weight);
        if (total <= 0) return cfg.ranks.get(0);
        int roll = rnd.nextInt(total);
        for (FakeConfig.Rank r : cfg.ranks) {
            roll -= Math.max(0, r.weight);
            if (roll < 0) return r;
        }
        return cfg.ranks.get(0);
    }

    private void rebuildActive() {
        active.clear();
        staff.clear();
        Set<String> taken = new HashSet<>();
        for (FakeConfig.StaffEntry e : cfg.staff) {
            staff.add(new FakePlayer(e.name, cfg.findRank(e.rank), 15 + rnd.nextInt(40), true));
            taken.add(e.name);
        }
        List<String> names = new ArrayList<>();
        for (String n : cfg.nicks) {
            if (!taken.contains(n)) names.add(n);
        }
        while (names.size() < cfg.tabSize) {
            names.add("Gracz" + (1000 + rnd.nextInt(9000)));
        }
        Collections.shuffle(names, rnd);
        for (int i = 0; i < cfg.tabSize && i < names.size(); i++) {
            active.add(new FakePlayer(names.get(i), randomRank(), 20 + rnd.nextInt(80), false));
        }
    }

    // ---------- czat ----------

    public void later(int ticks, Runnable r) {
        pending.add(new Pending(tick + Math.max(1, ticks), r));
    }

    public void chat(MinecraftClient mc, String legacy) {
        if (mc.inGameHud == null) return;
        mc.inGameHud.getChatHud().addMessage(Text.literal(color(legacy)));
    }

    private Text chatLine(FakePlayer p, String msg) {
        MutableText t = Text.literal("");
        int badge = p.rank().badgeRgb();
        int nameColor = p.rank().nameRgb();
        t.append(Text.literal("[" + p.rank().name + "] ").styled(s -> s.withColor(badge)));
        t.append(Text.literal(p.name()).styled(s -> s.withColor(nameColor)));
        t.append(Text.literal(" \u00bb ").formatted(Formatting.DARK_GRAY));
        t.append(Text.literal(msg).formatted(Formatting.GRAY));
        return t;
    }

    private void say(MinecraftClient mc, FakePlayer p, String msg) {
        if (mc.inGameHud == null) return;
        mc.inGameHud.getChatHud().addMessage(chatLine(p, msg));
    }

    private void burst(MinecraftClient mc) {
        if (active.isEmpty()) return;
        String text = pick(cfg.spamMessages);
        if (text == null) return;
        FakePlayer who = active.get(rnd.nextInt(active.size()));
        int count = 3 + rnd.nextInt(4);
        int delay = 1;
        for (int i = 0; i < count; i++) {
            later(delay, () -> say(mc, who, text));
            delay += 4 + rnd.nextInt(8);
        }
    }

    // ---------- tick ----------

    public void tick(MinecraftClient mc) {
        if (cfg == null || !cfg.enabled || mc.player == null) return;
        tick++;

        List<Runnable> due = new ArrayList<>();
        Iterator<Pending> it = pending.iterator();
        while (it.hasNext()) {
            Pending p = it.next();
            if (p.due() <= tick) {
                due.add(p.action());
                it.remove();
            }
        }
        for (Runnable r : due) r.run();

        if (tick % 1200 == 0) {
            minute(mc);
        }
        if (tick >= nextSlow) {
            slowUpdate();
        }
        if (cfg.fakeChat && tick >= nextChat) {
            nextChat = tick + secs(cfg.chatMinSeconds, cfg.chatMaxSeconds);
            if (cfg.spam && rnd.nextInt(100) < cfg.spamChancePercent) {
                burst(mc);
            } else {
                String msg = pick(cfg.chatMessages);
                if (msg != null && !active.isEmpty()) {
                    say(mc, active.get(rnd.nextInt(active.size())), msg);
                }
            }
        }
        if (cfg.fakeJoinLeave && tick >= nextJoinLeave) {
            nextJoinLeave = tick + secs(cfg.joinLeaveMinSeconds, cfg.joinLeaveMaxSeconds);
            joinOrLeave(mc);
        }
        if (cfg.tipText != null && !cfg.tipText.isEmpty() && tick >= nextTip) {
            nextTip = tick + 20L * 90;
            if (mc.inGameHud != null) {
                mc.inGameHud.setOverlayMessage(Text.literal(color(cfg.tipText)), false);
            }
        }
    }

    private void minute(MinecraftClient mc) {
        playMinutes++;
        keyall--;
        if (keyall <= 0) {
            keyall = Math.max(1, cfg.keyallPeriodMinutes);
            chat(mc, "&6&lKEYALL &7Wszyscy gracze otrzymali &eklucz&7!");
        }
        persist();
    }

    private void slowUpdate() {
        nextSlow = tick + 40;
        online += rnd.nextInt(7) - 3 + Integer.signum(cfg.baseOnline - online);
        online = Math.max(1, Math.min(cfg.maxPlayers, online));
        ping = 25 + rnd.nextInt(40);
    }

    private void joinOrLeave(MinecraftClient mc) {
        Set<String> used = new HashSet<>();
        for (FakePlayer p : active) used.add(p.name());
        for (FakePlayer p : staff) used.add(p.name());
        List<String> free = new ArrayList<>();
        for (String n : cfg.nicks) {
            if (!used.contains(n)) free.add(n);
        }
        boolean join = !free.isEmpty()
                && (active.size() < 8 || (active.size() < cfg.tabSize && rnd.nextBoolean()));
        if (join) {
            FakePlayer p = new FakePlayer(free.get(rnd.nextInt(free.size())), randomRank(), 20 + rnd.nextInt(80), false);
            active.add(p);
            chat(mc, cfg.joinMessage.replace("{nick}", p.name()));
        } else if (active.size() > 5) {
            FakePlayer p = active.remove(rnd.nextInt(active.size()));
            chat(mc, cfg.leaveMessage.replace("{nick}", p.name()));
        }
    }

    public void onPlayerChat(String message) {
        if (cfg == null || !cfg.enabled || !cfg.fakeReplies || active.isEmpty()) return;
        if (rnd.nextInt(100) >= cfg.replyChancePercent) return;
        String reply = pick(cfg.replyMessages);
        if (reply == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        FakePlayer who = active.get(rnd.nextInt(active.size()));
        later(30 + rnd.nextInt(60), () -> say(mc, who, reply));
    }

    // ---------- gracze / wiadomości prywatne ----------

    public FakePlayer findPlayer(String name) {
        for (FakePlayer p : active) {
            if (p.name().equalsIgnoreCase(name)) return p;
        }
        for (FakePlayer p : staff) {
            if (p.name().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    public void sendPrivate(MinecraftClient mc, FakePlayer target, String text) {
        lastMsg = target.name();
        chat(mc, "&8[&7Ty &8\u2192 &e" + target.name() + "&8] &f" + text);
        if (cfg.fakeReplies && !target.staff()) {
            String reply = pick(cfg.replyMessages);
            if (reply != null) {
                later(40 + rnd.nextInt(100),
                        () -> chat(mc, "&8[&e" + target.name() + " &8\u2192 &7Ty&8] &f" + reply));
            }
        }
    }

    // ---------- scoreboard / tab ----------

    public List<String> lines(MinecraftClient mc) {
        String nick = displayNick(mc);
        String time = LocalTime.now().format(TIME);
        List<String> out = new ArrayList<>();
        for (String l : cfg.scoreboardLines) {
            out.add(color(l
                    .replace("{nick}", nick)
                    .replace("{rank}", cfg.playerRank)
                    .replace("{balance}", fmt(balance))
                    .replace("{shards}", String.valueOf(shards))
                    .replace("{kills}", String.valueOf(kills))
                    .replace("{deaths}", String.valueOf(deaths))
                    .replace("{lives}", String.valueOf(lives))
                    .replace("{keyall}", String.valueOf(keyall))
                    .replace("{playtime}", String.valueOf(playMinutes))
                    .replace("{team}", team)
                    .replace("{online}", String.valueOf(online))
                    .replace("{max}", String.valueOf(cfg.maxPlayers))
                    .replace("{ping}", String.valueOf(ping))
                    .replace("{time}", time)));
        }
        return out;
    }

    public List<FakePlayer> tabList(MinecraftClient mc) {
        List<FakePlayer> list = new ArrayList<>(active);
        list.addAll(staff);
        list.add(new FakePlayer(displayNick(mc), cfg.findRank(cfg.playerRank), ping, false));
        list.sort((a, b) -> {
            int ra = cfg.priority(a.rank());
            int rb = cfg.priority(b.rank());
            if (ra != rb) return Integer.compare(rb, ra);
            return a.name().compareToIgnoreCase(b.name());
        });
        return list;
    }

    // ---------- rynek ----------

    public void buy(MinecraftClient mc, FakeConfig.MarketItem m, ItemStack stack) {
        if (balance < m.price) {
            chat(mc, "&cNie masz wystarczająco pieniędzy! &7Potrzebujesz &a$" + fmt(m.price) + "&7, masz &a$" + fmt(balance) + "&7.");
            return;
        }
        balance -= m.price;
        persist();
        chat(mc, "&aKupiono &f" + m.amount + "x " + stack.getName().getString() + " &aza &e$" + fmt(m.price) + "&a.");
        if (cfg.marketGivesItems && mc.player != null) {
            mc.player.networkHandler.sendChatCommand("give @s " + m.id + " " + m.amount);
        }
    }
}
