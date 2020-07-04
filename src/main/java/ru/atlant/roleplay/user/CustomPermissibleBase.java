package ru.atlant.roleplay.user;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftHumanEntity;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;

import java.util.UUID;

public class CustomPermissibleBase extends PermissibleBase {

    private UUID uuid;
    private UsersModule users;

    public CustomPermissibleBase(ServerOperator operator, UsersModule module) {
        super(operator);
        if (operator instanceof CraftHumanEntity) {
            this.uuid = ((CraftHumanEntity) operator).getUniqueId();
            this.users = module;
        }
    }

    @Override
    public boolean hasPermission(String inName) {
        boolean val = super.hasPermission(inName);
        if (uuid != null && !val) {
            val = users.hasPermission(uuid, inName);
        }
        return val;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        boolean val = super.hasPermission(perm);
        if (uuid != null && !val) {
            val = users.hasPermission(uuid, perm.getName());
        }
        return val;
    }

    @Override
    public boolean isPermissionSet(String name) {
        boolean val = super.isPermissionSet(name);
        if (uuid != null && !val) {
            val = users.hasPermission(uuid, name);
        }
        return val;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        boolean val = super.isPermissionSet(perm);
        if (uuid != null && !val) {
            val = users.hasPermission(uuid, perm.getName());
        }
        return val;
    }
}
