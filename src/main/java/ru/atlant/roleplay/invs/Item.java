package ru.atlant.roleplay.invs;

import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftMetaItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.atlant.roleplay.util.Color;
import ru.atlant.roleplay.util.DataUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@UtilityClass
public class Item {

    public Builder builder() {
        return new Builder();
    }

    public Builder fromStack(ItemStack stack) {
        val builder = new Builder();
        builder.type = stack.getType();
        builder.amount = stack.getAmount();
        builder.damage = stack.getDurability();
        builder.override = stack.getItemMeta();
        return builder;
    }

    @NoArgsConstructor
    public class Builder {

        public Builder(Builder original) {
            type = original.type;
            amount = original.amount;
            damage = original.damage;
            consumerList.addAll(original.consumerList);
        }

        private Material type = Material.AIR;
        private int amount = 1;
        private short damage;
        private final List<Consumer<ItemMeta>> consumerList = new ArrayList<>(2);
        private ItemMeta override;

        public Builder type(Material type) {
            this.type = type;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder damage(short damage) {
            this.damage = damage;
            return this;
        }

        public Builder color(Color color) {
            return damage((short) color.getWoolData());
        }

        public <T extends ItemMeta> Builder meta(Class<T> clazz, Consumer<T> consumer) {
            consumerList.add((Consumer<ItemMeta>) consumer);
            return this;
        }

        public Builder displayName(String name) {
            return meta(ItemMeta.class, itemMeta -> itemMeta.setDisplayName("§r" + name));
        }

        public Builder lore(String... lore) {
            return meta(ItemMeta.class, itemMeta -> ((CraftMetaItem) itemMeta).lore = Arrays.asList(lore));
        }

        public Builder lore(Iterable<String> lore) {
            return meta(ItemMeta.class, itemMeta ->
                    ((CraftMetaItem) itemMeta).lore = DataUtil.fromIterableArray(lore)
            );
        }

        public Builder loreLines(String... lines) {
            return meta(ItemMeta.class, itemMeta -> {
                List<String> lore = itemMeta.getLore();
                if (lore == null) lore = new ArrayList<>(Arrays.asList(lines));
                else java.util.Collections.addAll(lore, lines);
                ((CraftMetaItem) itemMeta).lore = lore;
            });
        }

        public Builder flags(ItemFlag... flags) {
            return meta(ItemMeta.class, itemMeta -> itemMeta.addItemFlags(flags));
        }

        public Builder enchantment(Enchantment enchantment, int level) {
            return meta(ItemMeta.class, itemMeta -> itemMeta.addEnchant(enchantment, level, true));
        }

        public Builder unbreakable(boolean unbreakable) {
            return meta(ItemMeta.class, itemMeta -> itemMeta.spigot().setUnbreakable(unbreakable));
        }

        public ItemStack build() {
            val stack = new ItemStack(type, amount, damage);
            val override = this.override;
            ItemMeta itemMeta = stack.getItemMeta();
            if (itemMeta == null) {
                itemMeta = Bukkit.getItemFactory().getItemMeta(stack.getType());
            }
            val meta = override != null ? override : itemMeta;
            val consumerList = this.consumerList;
            for (int i = 0, size = consumerList.size(); i < size; i++) {
                consumerList.get(i).accept(meta);
            }
            if (override != null) {
                stack.setItemMeta(override);
            }
            return stack;
        }
    }

}
