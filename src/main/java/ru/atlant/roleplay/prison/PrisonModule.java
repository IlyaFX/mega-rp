package ru.atlant.roleplay.prison;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.command.CommandHelper;
import ru.atlant.roleplay.command.CommandModule;
import ru.atlant.roleplay.command.requirement.RequirementPermission;
import ru.atlant.roleplay.config.ConfigModule;
import ru.atlant.roleplay.invs.ClickableItem;
import ru.atlant.roleplay.invs.Item;
import ru.atlant.roleplay.invs.SmartInventory;
import ru.atlant.roleplay.invs.content.Pagination;
import ru.atlant.roleplay.invs.content.SlotIterator;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;
import ru.atlant.roleplay.user.UsersModule;
import ru.atlant.roleplay.util.*;

import java.util.Comparator;
import java.util.Objects;

@RequiredArgsConstructor
@LoadAfter(clazz = {UsersModule.class, ConfigModule.class, CommandModule.class})
public class PrisonModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    private SmartInventory starredUsers;

    private Location prisonLocation;
    private Location unarrestLocation;

    private double maxPrisonDistance;

    private String wantedPermission;

    @Override
    public void onEnable() {
        UsersModule users = registry.get(UsersModule.class);
        ConfigModule config = registry.get(ConfigModule.class);
        config
                .subscribe("prison-location", loc -> prisonLocation = loc, DataUtil::locationFromString, new Location(Bukkit.getWorlds().get(0), 0, 120, 0), true)
                .subscribe("unarrest-location", loc -> unarrestLocation = loc, DataUtil::locationFromString, new Location(Bukkit.getWorlds().get(0), 100, 120, 0), true)
                .subscribe("max-prison-distance", dis -> maxPrisonDistance = dis, Double::parseDouble, 30.0, true)
                .subscribe("wanted-permission", perm -> wantedPermission = perm, "roleplay.wanted", true);
        users.subscribe((old, data) -> {
            Player player = Bukkit.getPlayer(data.getUser());
            if (old == null)
                return;
            if (old.getStars() != data.getStars()) {
                player.sendTitle(FormatUtil.starsFormat(data.getStars()), Colors.cWhite + "Новый уровень розыска!", 5, 50, 5);
            }
            if (old.getPrison() > 0 && data.getPrison() <= 0) {
                unarrest(player);
            }
            if (old.getPrison() <= 0 && data.getPrison() > 0) {
                arrest(player);
            }
        }, true);
        Bukkit.getScheduler().runTaskTimer(rolePlay, () -> {
            Bukkit.getOnlinePlayers().stream().map(player -> users.dataUnsafe(player.getUniqueId())).filter(Objects::nonNull).filter(UsersModule.UserData::inPrison).forEach(data -> {
                users.replaceViolation(data.getUser(), data.getStars(), data.getPrison() - 1000);
            });
        }, 20, 20);
        Bukkit.getScheduler().runTaskTimer(rolePlay, () -> {
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player ->
                            users.data(player.getUniqueId())
                                    .map(UsersModule.UserData::inPrison)
                                    .orElse(false) && WorldUtil.distance(player, prisonLocation) > maxPrisonDistance)
                    .forEach(this::arrestTeleport);
        }, 40, 40);

        this.starredUsers = SmartInventory.builder().provider((player, contents) -> {
            ClickableItem[] items = Bukkit
                    .getOnlinePlayers()
                    .stream()
                    .map(pl -> new Pair<>(pl, WorldUtil.distance(pl, player)))
                    .sorted(Comparator.comparingDouble(Pair::getValue))
                    .map(pair -> new Pair<>(users.dataUnsafe(pair.getKey().getUniqueId()), pair.getValue()))
                    .filter(pair -> pair.getKey().getStars() > 0)
                    .map(pair -> {
                        Player pl = Bukkit.getPlayer(pair.getKey().getUser());
                        int dis = pair.getValue().intValue();
                        return ClickableItem.empty(Item.builder().type(Material.SKULL_ITEM).amount(1).meta(SkullMeta.class, meta -> meta.setOwningPlayer(pl))
                                .displayName(Colors.cYellow + pl.getName()).loreLines(
                                        "      ",
                                        FormatUtil.starsFormat(pair.getKey().getStars()),
                                        Colors.cWhite + "Расстояние: " + Colors.cAqua + dis + " " + FormatUtil.pluralFormRu(dis, "блок", "блока", "блоков"),
                                        "     ",
                                        Colors.cMagenta + "Скорее бегите ловить",
                                        Colors.cMagenta + "этого преступника!",
                                        Colors.cMagenta + "/arrest " + pl.getName()
                                ).build());
                    })
                    .toArray(ClickableItem[]::new);
            Pagination pagination = contents.pagination();
            pagination.setItems(items);
            pagination.setItemsPerPage(7 * 4);
            SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1);
            iterator.blacklist(1, 8).blacklist(2, 0).blacklist(2, 8).blacklist(3, 0).blacklist(3, 8).blacklist(4, 0).blacklist(4, 8);
            pagination.addToIterator(iterator);
            contents.fillBorders(ClickableItem.empty(Item.builder().type(Material.STAINED_GLASS_PANE).amount(1).color(Color.GRAY).displayName("  ").build()));
            contents.set(5, 1, ClickableItem.of(Item.builder().type(Material.ARROW).amount(1).displayName(Colors.cYellow + "Предыдущая страница").build(), event -> starredUsers.open(player, pagination.previous().getPage())));
            contents.set(5, 7, ClickableItem.of(Item.builder().type(Material.ARROW).amount(1).displayName(Colors.cYellow + "Следующая страница").build(), event -> starredUsers.open(player, pagination.next().getPage())));
        }).title("Их разыскивает полиция").build();

        registry.get(CommandModule.class).getDispatcher().register(
                CommandHelper.literal("wanted")
                        .executes(CommandHelper.wrap(ctx -> {
                            RequirementPermission.permission(ctx.getSource(), wantedPermission);
                            starredUsers.open(Bukkit.getPlayer(ctx.getSource()));
                        }))
        );
    }

    public void arrest(Player player) {
        arrestTeleport(player);
    }

    public void arrestTeleport(Player player) {
        player.teleport(prisonLocation);
    }

    public void unarrest(Player player) {
        player.sendTitle(Colors.cYellow + "Свобода!", Colors.cWhite + "Наконец свежий воздух...", 5, 60, 5);
        player.sendMessage(FormatUtil.fine("Вы наконец вышли на свободу! Надеемся, что вы больше никогда не попадёте в эту тюрьму."));
        unarrestTeleport(player);
    }

    public void unarrestTeleport(Player player) {
        player.teleport(unarrestLocation);
    }
}
