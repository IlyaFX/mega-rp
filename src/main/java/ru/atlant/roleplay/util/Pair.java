package ru.atlant.roleplay.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class Pair<K, V> implements Map.Entry<K, V> {

    private K key;
    private V value;

}
