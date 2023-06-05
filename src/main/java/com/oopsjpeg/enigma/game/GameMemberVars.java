package com.oopsjpeg.enigma.game;

import java.util.HashMap;
import java.util.Map;

public class GameMemberVars
{
    private final Map<GameObject, Map<String, Object>> vars = new HashMap<>();

    private Map<String, Object> getObjectVars(GameObject object)
    {
        if (!vars.containsKey(object))
            vars.put(object, new HashMap<>());
        return vars.get(object);
    }

    public <T> T get(GameObject object, String key, Class<?> type)
    {
        return (T) getObjectVars(object).get(key);
    }

    public <T> void put(GameObject object, String key, T value)
    {
        getObjectVars(object).put(key, value);
    }

    public boolean has(GameObject object, String key)
    {
        return getObjectVars(object).containsKey(key);
    }

}
