package com.puzzle;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class PuzzleController {
    @FXML
    private ImageView puzzle0, puzzle1, puzzle2, puzzle3, puzzle4, puzzle5, puzzle6, puzzle7;
    @FXML
    private ImageView puzzle8, puzzle9, puzzle10, puzzle11, puzzle12, puzzle13, puzzle14, puzzle15;
    @FXML
    private Label label;
    private final PuzzleImageTransfer puzzleImage = PuzzleImageTransfer.getInstance();
    private final BufferedImage[] splittedImage = ButtonController.splitImage(puzzleImage.getPuzzleImage());
    private static ImageView[] puzzle;
    private ImageView draggedImageView;
    private double initialImageX;
    private double initialImageY;
    private double draggedImageX;
    private double draggedImageY;

    @FXML
    private void initialize() {
        puzzle = new ImageView[] { puzzle0, puzzle1, puzzle2, puzzle3, puzzle4, puzzle5, puzzle6, puzzle7,
                puzzle8, puzzle9, puzzle10, puzzle11, puzzle12, puzzle13, puzzle14, puzzle15 };
    }
    @FXML
    private void onMousePressed(MouseEvent event) {
        if (event.getSource() instanceof ImageView) {
            draggedImageView = (ImageView) event.getSource();
            draggedImageView.toFront();
            draggedImageX = draggedImageView.getLayoutX();
            draggedImageY = draggedImageView.getLayoutY();
            initialImageX = event.getSceneX() - draggedImageX;
            initialImageY = event.getSceneY() - draggedImageY;
        }
    }
    @FXML
    private void onMouseDragged(MouseEvent event) {
        if (draggedImageView != null) {
            draggedImageView.setLayoutX(event.getSceneX() - initialImageX);
            draggedImageView.setLayoutY(event.getSceneY() - initialImageY);
        }
    }
    @FXML
    private void onMouseReleased(MouseEvent event) {
        boolean isSwapped = false;
        for (ImageView imageView : puzzle) {
            if (draggedImageView != imageView && isOverlapping(event, imageView)) {
                swapImages(draggedImageView, imageView);
                isSwapped = true;
                if (isPuzzleCorrect()) {
                    label.setText("Puzzle is correct");
                }
            }
        }
        if (!isSwapped) {
            draggedImageView.setLayoutX(draggedImageX);
            draggedImageView.setLayoutY(draggedImageY);
        }
    }
    @FXML
    private void rotateLeft() {
        try {
            draggedImageView.setRotate(draggedImageView.getRotate() + 270);
            if (draggedImageView.getRotate() >= 360) {
                draggedImageView.setRotate(draggedImageView.getRotate() - 360);
            } else {
                draggedImageView.setRotate(draggedImageView.getRotate());
            }
        } catch (NullPointerException exception) {
            System.out.println("Please, select a puzzle");
        }
    }
    @FXML
    private void rotateRight() {
        try {
            draggedImageView.setRotate(draggedImageView.getRotate() + 90);
            if (draggedImageView.getRotate() >= 360) {
                draggedImageView.setRotate(draggedImageView.getRotate() - 360);
            } else {
                draggedImageView.setRotate(draggedImageView.getRotate());
            }
        } catch (NullPointerException exception) {
            System.out.println("Please, select a puzzle");
        }
    }
    @FXML
    private void autoAssemble() {
        double puzzlePieceX = puzzle0.getLayoutX();
        double puzzlePieceY = puzzle0.getLayoutY();
        for (ImageView puzzlePiece : puzzle) {
            puzzlePiece.setRotate(0);
        }
        for (int i = 0; i < puzzle.length - 1; i++) {
            if (isPieceCorrect(puzzle[i], splittedImage[i])) {
                continue;
            }
            for (int j = i + 1; j < puzzle.length; j++) {
                swapImages(puzzle[i], puzzle[j]);
                if (!isPieceCorrect(puzzle[i], splittedImage[i])) {
                    swapImages(puzzle[i], puzzle[j]);
                }
            }
        }
        puzzle0.setLayoutX(puzzlePieceX);
        puzzle0.setLayoutY(puzzlePieceY);
        if (isPuzzleCorrect()) {
            label.setText("Puzzle is correct");
        }
    }
    private void swapImages(ImageView imageView1, ImageView imageView2) {
        Image tempImage = imageView1.getImage();
        double tempLayoutX = imageView1.getLayoutX();
        double tempLayoutY = imageView1.getLayoutY();
        imageView1.setImage(imageView2.getImage());
        imageView2.setImage(tempImage);
        if (draggedImageX == 0 || draggedImageY == 0) {
            imageView1.setLayoutX(tempLayoutX);
            imageView1.setLayoutY(tempLayoutY);
        } else {
            imageView1.setLayoutX(draggedImageX);
            imageView1.setLayoutY(draggedImageY);
            draggedImageX = 0;
            draggedImageY = 0;
        }
        if (imageView1.getRotate() > 0) {
            ImageView tempImageView = new ImageView();
            tempImageView.setRotate(imageView1.getRotate());
            imageView1.setRotate(imageView2.getRotate());
            imageView2.setRotate(tempImageView.getRotate());
        } else if (imageView2.getRotate() > 0) {
            ImageView tempImageView = new ImageView();
            tempImageView.setRotate(imageView2.getRotate());
            imageView2.setRotate(imageView1.getRotate());
            imageView1.setRotate(tempImageView.getRotate());
        }
    }
    private boolean isOverlapping(MouseEvent event, ImageView overlappedImage) {
        double mouseX = event.getSceneX();
        double mouseY = event.getSceneY();

        double overlappedImageX = overlappedImage.getLayoutX();
        double overlappedImageY = overlappedImage.getLayoutY();
        double overlappedImageWidth = overlappedImage.getFitWidth();
        double overlappedImageHeight = overlappedImage.getFitHeight();

        return (mouseX > overlappedImageX
                && mouseX < overlappedImageX + overlappedImageWidth)
                && (mouseY > overlappedImageY
                && mouseY < overlappedImageY + overlappedImageHeight);
    }
    private boolean isPuzzleCorrect() {
        for (int i = 0; i < puzzle.length; i++) {
            BufferedImage img1 = SwingFXUtils.fromFXImage(puzzle[i].getImage(), null);
            int h1 = img1.getHeight();
            int w1 = img1.getWidth();
            long diff = 0;
            for (int j = 0; j < h1; j++) {
                for (int k = 0; k < w1; k++) {
                    //Getting the RGB values of a pixel
                    int pixel1 = img1.getRGB(k, j);
                    Color color1 = new Color(pixel1, true);
                    int r1 = color1.getRed();
                    int g1 = color1.getGreen();
                    int b1 = color1.getBlue();
                    int pixel2 = splittedImage[i].getRGB(k, j);
                    Color color2 = new Color(pixel2, true);
                    int r2 = color2.getRed();
                    int g2 = color2.getGreen();
                    int b2 = color2.getBlue();
                    //sum of differences of RGB values of the two images
                    long data = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                    diff = diff + data;
                }
            }
            double avg = (double) diff /(w1 * h1 * 3);
            double percentage = (avg / 255) * 100;
            if (percentage > 2) {
                return false;
            }
        }
        return true;
    }
    private boolean isPieceCorrect(ImageView imageView, BufferedImage correctPiece) {
        BufferedImage img1 = SwingFXUtils.fromFXImage(imageView.getImage(), null);
        int h1 = img1.getHeight();
        int w1 = img1.getWidth();
        long diff = 0;
        for (int j = 0; j < h1; j++) {
            for (int k = 0; k < w1; k++) {
                //Getting the RGB values of a pixel
                int pixel1 = img1.getRGB(k, j);
                Color color1 = new Color(pixel1, true);
                int r1 = color1.getRed();
                int g1 = color1.getGreen();
                int b1 = color1.getBlue();
                int pixel2 = correctPiece.getRGB(k, j);
                Color color2 = new Color(pixel2, true);
                int r2 = color2.getRed();
                int g2 = color2.getGreen();
                int b2 = color2.getBlue();
                //sum of differences of RGB values of the two images
                long data = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                diff = diff + data;
            }
        }
        double avg = (double) diff /(w1*h1*3);
        double percentage = (avg/255)*100;
        return !(percentage > 2);
    }
}