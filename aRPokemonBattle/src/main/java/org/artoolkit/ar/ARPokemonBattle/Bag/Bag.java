
package org.artoolkit.ar.ARPokemonBattle.Bag;

import java.util.ArrayList;

public class Bag {
    private ArrayList<Item> mInventory;

    public Bag() {
        mInventory = new ArrayList<>();
    }

    public void addToBag(Item item) {
        // Add quantity if item already exists
        if (mInventory.contains(item)) {
            mInventory.get(mInventory.indexOf(item)).addQuantity(item.getQuantity());
        }
        else {
            mInventory.add(item);
        }
    }

    public Item getItemAtIndex(int index) {
        return mInventory.get(index);
    }
    public int getIndexOfItem(Item item) {
        return mInventory.indexOf(item);
    }
    public int getIndexOfItem(int id) {
        for (int i = 0; i < mInventory.size(); i++) {
            if (mInventory.get(i).getItemID() == id) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<Item> getBag() { return mInventory; }
}
