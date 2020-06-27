package ru.atlant.roleplay.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import ru.atlant.roleplay.RolePlay;
import ru.atlant.roleplay.event.EventExecutorModule;
import ru.atlant.roleplay.module.LoadAfter;
import ru.atlant.roleplay.module.Module;
import ru.atlant.roleplay.module.ModuleRegistry;

import java.util.UUID;

@RequiredArgsConstructor
@LoadAfter(clazz = {EventExecutorModule.class})
public class CommandModule implements Module {

    private final RolePlay rolePlay;
    private final ModuleRegistry registry;

    @Getter
    private CommandDispatcher<UUID> dispatcher;

    @Override
    public void onEnable() {
        dispatcher = new CommandDispatcher<>();
        EventExecutorModule events = registry.get(EventExecutorModule.class);
        events.registerListener(PlayerCommandPreprocessEvent.class, event -> {
            val message = event.getMessage().substring(1);
            val index = message.indexOf(' ');
            val label = index == -1 ? message.toLowerCase() : message.substring(0, index).toLowerCase();
            val children = dispatcher.getRoot().getChildren();
            int j = children.size();
            val iterator = children.iterator();
            while (j-- > 0) {
                val child = iterator.next();
                if (child instanceof LiteralCommandNode && ((LiteralCommandNode<UUID>) child).isValidInput(label)) {
                    event.setCancelled(true);
                    val p = event.getPlayer();
                    try {
                        dispatcher.execute(message, p.getUniqueId());
                    } catch (CommandSyntaxException ex) {
                        p.sendMessage(ex.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        p.sendMessage(ex.getMessage());
                    }
                    return;
                }
            }
        }, EventPriority.LOW, true);
    }
}
