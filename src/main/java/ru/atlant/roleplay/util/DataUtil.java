package ru.atlant.roleplay.util;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

@UtilityClass
public class DataUtil {

    public final Gson GSON = new Gson();
    public final Random RANDOM = new Random();

    public Location locationFromString(String location) {
        String[] arr = location.split(";", 6);
        Location loc = new Location(Bukkit.getWorld(arr[0]), Double.parseDouble(arr[1]), Double.parseDouble(arr[2]), Double.parseDouble(arr[3]));
        if (arr.length > 4) {
            loc.setYaw(Float.parseFloat(arr[4]));
            loc.setPitch(Float.parseFloat(arr[5]));
        }
        return loc;
    }

    public <T> ArrayList<T> fromIterableArray(Iterable<T> iterable) {
        if (iterable instanceof ArrayList) {
            return (ArrayList<T>) iterable;
        }
        if (iterable instanceof Collection) {
            return new ArrayList<>((Collection) iterable);
        }
        ArrayList<T> list = new ArrayList<>();
        for (T t : iterable) list.add(t);
        return list;
    }

}
