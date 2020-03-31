package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.game.obj.Item;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@RequiredArgsConstructor
public class Build {
    @Getter private final Item item;
    @Getter private final int reduction;
    @Getter private final ArrayList<Item> postData;

    public int getCost() {
        return item.getCost() - reduction;
    }
}
