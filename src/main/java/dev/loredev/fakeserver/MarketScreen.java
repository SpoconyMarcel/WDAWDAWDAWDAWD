package dev.loredev.fakeserver;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class MarketScreen extends Screen {
    private static final int SLOT = 20;
    private static final int COLS = 9;

    private final List<FakeConfig.MarketItem> items = new ArrayList<>();
    private final List<ItemStack> stacks = new ArrayList<>();
    private int left;
    private int top;
    private int rows = 1;

    public MarketScreen() {
        super(Text.literal("Rynek"));
    }

    @Override
    protected void init() {
        items.clear();
        stacks.clear();
        for (FakeConfig.MarketItem m : FakeServerMod.config().market) {
            Identifier id = Identifier.tryParse(m.id);
            if (id == null || !Registries.ITEM.containsId(id)) continue;
            items.add(m);
            stacks.add(new ItemStack(Registries.ITEM.get(id), Math.max(1, m.amount)));
        }
        rows = Math.max(1, (items.size() + COLS - 1) / COLS);
        left = (width - COLS * SLOT) / 2;
        top = (height - rows * SLOT) / 2;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private int indexAt(double mx, double my) {
        int cx = (int) Math.floor((mx - left) / SLOT);
        int cy = (int) Math.floor((my - top) / SLOT);
        if (mx < left || my < top || cx < 0 || cx >= COLS || cy < 0 || cy >= rows) return -1;
        int idx = cy * COLS + cx;
        return idx < items.size() ? idx : -1;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int w = COLS * SLOT;
        ctx.fill(left - 8, top - 30, left + w + 8, top + rows * SLOT + 8, 0xF0101010);
        ctx.drawText(textRenderer, "\u00a76\u00a7lRYNEK", left, top - 24, 0xFFFFFFFF, true);
        String bal = "\u00a77Stan konta: \u00a7a$" + FakeState.fmt(FakeServerMod.state().balance);
        ctx.drawText(textRenderer, bal, left + w - textRenderer.getWidth(bal), top - 24, 0xFFFFFFFF, true);
        ctx.drawText(textRenderer, "\u00a77Kliknij przedmiot, aby kupić", left, top - 12, 0xFFFFFFFF, false);

        int hover = -1;
        for (int i = 0; i < items.size(); i++) {
            int x = left + (i % COLS) * SLOT;
            int y = top + (i / COLS) * SLOT;
            boolean h = mouseX >= x && mouseX < x + SLOT && mouseY >= y && mouseY < y + SLOT;
            ctx.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, h ? 0xFF5A5A5A : 0xFF2B2B2B);
            ctx.drawItem(stacks.get(i), x + 2, y + 2);
            ctx.drawStackOverlay(textRenderer, stacks.get(i), x + 2, y + 2);
            if (h) hover = i;
        }

        if (hover >= 0) {
            FakeConfig.MarketItem m = items.get(hover);
            List<Text> tip = new ArrayList<>();
            tip.add(stacks.get(hover).getName());
            tip.add(Text.literal("Ilość: " + m.amount).formatted(Formatting.GRAY));
            tip.add(Text.literal("Cena: $" + FakeState.fmt(m.price)).formatted(Formatting.GREEN));
            tip.add(Text.literal("Kliknij, aby kupić").formatted(Formatting.YELLOW));
            ctx.drawTooltip(textRenderer, tip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int idx = indexAt(mouseX, mouseY);
            if (idx >= 0 && client != null) {
                FakeServerMod.state().buy(client, items.get(idx), stacks.get(idx));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
