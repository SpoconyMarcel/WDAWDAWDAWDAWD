package dev.loredev.fakeserver;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FakeServerMod implements ClientModInitializer {
    public static final String MOD_ID = "fakeserver";

    private static final FakeState STATE = new FakeState();
    private static FakeConfig config;

    private static final SuggestionProvider<FabricClientCommandSource> RANK_SUGGESTIONS = (ctx, builder) -> {
        List<String> names = new ArrayList<>();
        for (FakeConfig.Rank r : config.ranks) names.add(r.name);
        return CommandSource.suggestMatching(names, builder);
    };

    public static FakeConfig config() {
        return config;
    }

    public static FakeState state() {
        return STATE;
    }

    @Override
    public void onInitializeClient() {
        config = FakeConfig.load();
        STATE.reset(config);

        ClientTickEvents.END_CLIENT_TICK.register(STATE::tick);
        HudRenderCallback.EVENT.register(FakeHud::render);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> STATE.onJoin());
        ClientSendMessageEvents.CHAT.register(STATE::onPlayerChat);
        ClientSendMessageEvents.ALLOW_COMMAND.register(command -> !FakeCommands.handle(command));
        ClientCommandRegistrationCallback.EVENT.register(FakeServerMod::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("fakeserver")
                .then(ClientCommandManager.literal("on").executes(c -> setEnabled(c, true)))
                .then(ClientCommandManager.literal("off").executes(c -> setEnabled(c, false)))
                .then(ClientCommandManager.literal("reload").executes(FakeServerMod::reload))
                .then(ClientCommandManager.literal("rank")
                        .then(ClientCommandManager.argument("nazwa", StringArgumentType.greedyString())
                                .suggests(RANK_SUGGESTIONS)
                                .executes(FakeServerMod::setRank)))
                .then(ClientCommandManager.literal("nick")
                        .then(ClientCommandManager.argument("nazwa", StringArgumentType.word())
                                .executes(FakeServerMod::setNick)))
                .then(ClientCommandManager.literal("money")
                        .then(ClientCommandManager.literal("set")
                                .then(ClientCommandManager.argument("kwota", LongArgumentType.longArg(0))
                                        .executes(c -> money(c, false))))
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.argument("kwota", LongArgumentType.longArg(0))
                                        .executes(c -> money(c, true)))))
                .then(ClientCommandManager.literal("shards")
                        .then(ClientCommandManager.argument("ilosc", LongArgumentType.longArg(0))
                                .executes(FakeServerMod::shards)))
                .then(ClientCommandManager.literal("toggle")
                        .then(ClientCommandManager.argument("funkcja", StringArgumentType.word())
                                .executes(FakeServerMod::toggle))));
    }

    private static int setEnabled(CommandContext<FabricClientCommandSource> c, boolean value) {
        config.enabled = value;
        config.save();
        c.getSource().sendFeedback(Text.literal("[FakeServer] " + (value ? "włączony" : "wyłączony")));
        return 1;
    }

    private static int reload(CommandContext<FabricClientCommandSource> c) {
        config = FakeConfig.load();
        STATE.reset(config);
        c.getSource().sendFeedback(Text.literal("[FakeServer] config przeładowany"));
        return 1;
    }

    private static int setRank(CommandContext<FabricClientCommandSource> c) {
        String name = StringArgumentType.getString(c, "nazwa").trim();
        if (!config.hasRank(name)) {
            StringBuilder sb = new StringBuilder();
            for (FakeConfig.Rank r : config.ranks) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(r.name);
            }
            c.getSource().sendFeedback(Text.literal("[FakeServer] nie ma takiej rangi. Dostępne: " + sb));
            return 0;
        }
        config.playerRank = config.findRank(name).name;
        config.save();
        c.getSource().sendFeedback(Text.literal("[FakeServer] ranga: " + config.playerRank));
        return 1;
    }

    private static int setNick(CommandContext<FabricClientCommandSource> c) {
        String nick = StringArgumentType.getString(c, "nazwa");
        if (nick.equalsIgnoreCase("reset")) {
            config.playerNick = "";
            config.save();
            c.getSource().sendFeedback(Text.literal("[FakeServer] nick: wróciłeś do swojego prawdziwego"));
            return 1;
        }
        config.playerNick = nick;
        config.save();
        c.getSource().sendFeedback(Text.literal("[FakeServer] nick: " + nick));
        return 1;
    }

    private static int money(CommandContext<FabricClientCommandSource> c, boolean add) {
        long amount = LongArgumentType.getLong(c, "kwota");
        STATE.balance = add ? STATE.balance + amount : amount;
        STATE.persist();
        c.getSource().sendFeedback(Text.literal("[FakeServer] kasa: $" + FakeState.fmt(STATE.balance)));
        return 1;
    }

    private static int shards(CommandContext<FabricClientCommandSource> c) {
        STATE.shards = (int) Math.min(Integer.MAX_VALUE, LongArgumentType.getLong(c, "ilosc"));
        STATE.persist();
        c.getSource().sendFeedback(Text.literal("[FakeServer] shardy: " + STATE.shards));
        return 1;
    }

    private static int toggle(CommandContext<FabricClientCommandSource> c) {
        String what = StringArgumentType.getString(c, "funkcja").toLowerCase();
        boolean value;
        switch (what) {
            case "scoreboard" -> value = config.scoreboard = !config.scoreboard;
            case "tab" -> value = config.fakeTab = !config.fakeTab;
            case "chat" -> value = config.fakeChat = !config.fakeChat;
            case "spam" -> value = config.spam = !config.spam;
            case "joinleave" -> value = config.fakeJoinLeave = !config.fakeJoinLeave;
            case "reply" -> value = config.fakeReplies = !config.fakeReplies;
            case "komendy" -> value = config.fakeCommands = !config.fakeCommands;
            case "give" -> value = config.marketGivesItems = !config.marketGivesItems;
            default -> {
                c.getSource().sendFeedback(Text.literal(
                        "[FakeServer] funkcje: scoreboard, tab, chat, spam, joinleave, reply, komendy, give"));
                return 0;
            }
        }
        config.save();
        c.getSource().sendFeedback(Text.literal("[FakeServer] " + what + ": " + (value ? "włączone" : "wyłączone")));
        return 1;
    }
}
