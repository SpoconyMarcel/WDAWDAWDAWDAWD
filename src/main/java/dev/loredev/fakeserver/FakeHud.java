package dev.loredev.fakeserver;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.List;

public final class FakeHud {
    private FakeHud() {}

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        FakeConfig cfg = FakeServerMod.config();
        if (cfg == null || !cfg.enabled || mc.player == null || mc.options.hudHidden) return;
        FakeState st = FakeServerMod.state();

        if (cfg.scoreboard) {
            renderScoreboard(ctx, mc, cfg, st);
        }
        if (cfg.fakeTab && mc.options.playerListKey.isPressed()) {
            renderTab(ctx, mc, cfg, st);
        }
    }

    private static void renderScoreboard(DrawContext ctx, MinecraftClient mc, FakeConfig cfg, FakeState st) {
        TextRenderer tr = mc.textRenderer;
        List<String> lines = st.lines(mc);
        String title = FakeState.color(cfg.title);
        int titleW = tr.getWidth(title);

        int w = titleW;
        for (String l : lines) {
            w = Math.max(w, tr.getWidth(l));
        }
        w += 8;

        int lineH = 10;
        int titleH = 14;
        int h = lines.size() * lineH;
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int bottom = sh / 2 + h / 3 + cfg.scoreboardOffsetY + 6;
        int top = bottom - h - 2;
        int right = sw - 2;
        int left = right - w;

        ctx.fill(left, top - titleH, right, top, 0x70000000);
        ctx.fill(left, top, right, bottom, 0x55000000);
        ctx.drawText(tr, title, left + (w - titleW) / 2, top - titleH + 3, 0xFFFFFFFF, true);
        for (int i = 0; i < lines.size(); i++) {
            ctx.drawText(tr, lines.get(i), left + 4, top + 2 + i * lineH, 0xFFFFFFFF, true);
        }
    }

    private static void drawHead(DrawContext ctx, int x, int y, String name) {
        int h = name.hashCode();
        int r = 90 + (h & 0x7F);
        int g = 70 + ((h >> 7) & 0x7F);
        int b = 60 + ((h >> 14) & 0x7F);
        int skin = 0xFF000000 | (Math.min(r, 255) << 16) | (Math.min(g, 255) << 8) | Math.min(b, 255);
        int hair = 0xFF000000 | ((r / 3) << 16) | ((g / 3) << 8) | (b / 3);
        ctx.fill(x, y, x + 8, y + 8, skin);
        ctx.fill(x, y, x + 8, y + 2, hair);
        ctx.fill(x + 1, y + 3, x + 3, y + 5, 0xFFFFFFFF);
        ctx.fill(x + 5, y + 3, x + 7, y + 5, 0xFFFFFFFF);
        ctx.fill(x + 2, y + 3, x + 3, y + 5, 0xFF303090);
        ctx.fill(x + 5, y + 3, x + 6, y + 5, 0xFF303090);
    }

    private static void drawPing(DrawContext ctx, int x, int y, int ping) {
        int level = ping < 40 ? 5 : (ping < 70 ? 4 : (ping < 100 ? 3 : 2));
        for (int i = 0; i < 5; i++) {
            int hgt = 2 + i;
            int color = i < level ? 0xFF00E000 : 0xFF404040;
            ctx.fill(x + i * 2, y + 8 - hgt, x + i * 2 + 1, y + 8, color);
        }
    }

    private static void renderTab(DrawContext ctx, MinecraftClient mc, FakeConfig cfg, FakeState st) {
        TextRenderer tr = mc.textRenderer;
        int sw = mc.getWindow().getScaledWidth();
        int colW = 150;
        int maxCols = Math.max(1, Math.min(4, (sw - 10) / colW));

        List<FakeState.FakePlayer> list = st.tabList(mc);
        int max = Math.min(list.size(), maxCols * 20);
        int rows = Math.max(1, Math.min(20, max));
        int cols = Math.max(1, (max + rows - 1) / rows);
        int totalW = cols * colW;
        int x0 = (sw - totalW) / 2;
        int y0 = 6;
        int yRows = y0 + 46;

        // nagłówek
        String title = FakeState.color(cfg.title);
        ctx.getMatrices().push();
        ctx.getMatrices().translate(sw / 2f, (float) y0, 0f);
        ctx.getMatrices().scale(2f, 2f, 1f);
        ctx.drawText(tr, title, -tr.getWidth(title) / 2, 0, 0xFFFFFFFF, true);
        ctx.getMatrices().pop();

        String online = "\u00a7a" + st.online() + " \u00a7fONLINE";
        ctx.drawText(tr, online, sw / 2 - tr.getWidth(online) / 2, y0 + 22, 0xFFFFFFFF, true);
        String sector = "\u00a7fSEKTOR: \u00a7a" + cfg.sector;
        ctx.drawText(tr, sector, sw / 2 - tr.getWidth(sector) / 2, y0 + 33, 0xFFFFFFFF, true);

        // wiersze
        for (int i = 0; i < max; i++) {
            FakeState.FakePlayer p = list.get(i);
            int col = i / rows;
            int row = i % rows;
            int x = x0 + col * colW;
            int y = yRows + row * 10;

            ctx.fill(x, y, x + colW - 2, y + 9, 0x70000000);
            drawHead(ctx, x + 1, y + 1, p.name());

            String badge = p.rank().name;
            int bw = tr.getWidth(badge) + 4;
            int bx = x + 11;
            ctx.fill(bx, y, bx + bw, y + 9, 0xFF000000 | p.rank().badgeRgb());
            ctx.drawText(tr, badge, bx + 2, y + 1, 0xFFFFFFFF, true);

            ctx.drawText(tr, p.name(), bx + bw + 3, y + 1, 0xFF000000 | p.rank().nameRgb(), true);
            drawPing(ctx, x + colW - 14, y, p.ping());
        }

        // stopka
        String footer = "\u00a7fSKLEP \u00a77\u2022 \u00a7a\u00a7nPOMOC\u00a7r \u00a77\u2022 \u00a7fDISCORD \u00a77\u2022 \u00a7fEVENT";
        ctx.drawText(tr, footer, sw / 2 - tr.getWidth(footer) / 2, yRows + rows * 10 + 6, 0xFFFFFFFF, true);
    }
}
