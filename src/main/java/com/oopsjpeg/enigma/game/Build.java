package com.oopsjpeg.enigma.game;

import com.oopsjpeg.enigma.game.obj.Item;

import java.util.ArrayList;

public class Build {
    private final Item item;
    private final int reduction;
    private final ArrayList<Item> postData;

    public Build(Item item, int reduction, ArrayList<Item> postData) {
        this.item = item;
        this.reduction = reduction;
        this.postData = postData;
    }

    public int getCost() {
        return item.getCost() - reduction;
    }

    public Item getItem() {
        return this.item;
    }

    public int getReduction() {
        return this.reduction;
    }

    public ArrayList<Item> getPostData() {
        return this.postData;
    }
}
