package com.puzzle;

import javafx.scene.image.ImageView;

public class PuzzleImageTransfer {
    private static final PuzzleImageTransfer instance = new PuzzleImageTransfer();
    private ImageView puzzleImage;

    private PuzzleImageTransfer() {}
    public static PuzzleImageTransfer getInstance() {
        return instance;
    }
    public ImageView getPuzzleImage() {
        return puzzleImage;
    }
    public void setPuzzleImage(ImageView puzzleImage) {
        this.puzzleImage = puzzleImage;
    }
}