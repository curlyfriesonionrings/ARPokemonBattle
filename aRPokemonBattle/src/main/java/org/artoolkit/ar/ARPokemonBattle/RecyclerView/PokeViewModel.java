package org.artoolkit.ar.ARPokemonBattle.RecyclerView;

public class PokeViewModel {
    private boolean mSelected;
    private int mIconResource;
    private String mModelPath;
    private String mSpecies, mLevel;
    private String mMoveListString;

    public PokeViewModel(boolean selected, int icon, String model, String species, String lv,
                         String move) {
        mSelected = selected;
        mIconResource = icon;
        mModelPath = model;
        mSpecies = species; mLevel = lv;
        mMoveListString = move;
    }

    public void setSelected(boolean v) { mSelected = v; }

    public boolean isSelected() { return mSelected; }
    public int getIconId() { return mIconResource; }
    public String getModelPath() { return mModelPath; }
    public String getSpecies() { return mSpecies; }
    public String getLevel() { return mLevel; }
    public String getMoveList() { return mMoveListString; }
}