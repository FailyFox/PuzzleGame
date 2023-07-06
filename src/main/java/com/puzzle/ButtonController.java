package com.puzzle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ButtonController {
    static final int ROWS = 4;
    static final int COLUMNS = 4;
    static final int PARTS = ROWS * COLUMNS;
    @FXML
    public Label label;
    @FXML
    public ImageView imageView;

    @FXML
    private void splitButtonClick() throws IOException {
        BufferedImage[] images = splitImage(imageView);
        //writing sub-images into image files
        Random random = new Random();
        Set<Integer> usedIndices = new HashSet<>(17, 1);
        for (int i = 0; i < PARTS; i++) {
            int randomIndex;
            do {
                randomIndex = random.nextInt(PARTS);
            } while (usedIndices.contains(randomIndex));
            usedIndices.add(randomIndex);
            File outputFile = new File("src/main/resources/part/img" + randomIndex + ".jpg");
            ImageIO.write(images[i], "jpg", outputFile);
        }
        label.setText("Sub-images have been created");
    }
    @FXML
    private void letsAssembleButtonClick() throws IOException {
        File folder = new File("src/main/resources/part/");
        File[] files = folder.listFiles();
        if (files != null && files.length == PARTS) {
            FXMLLoader fxmlLoader = new FXMLLoader(PuzzleApplication.class.getResource("puzzle.fxml"));
            Stage puzzleWindow = new Stage();
            puzzleWindow.setScene(new Scene(fxmlLoader.load()));
            puzzleWindow.setTitle("Puzzle assembling");
            puzzleWindow.setResizable(false);
            puzzleWindow.show();
            PuzzleApplication.indexWindow.close();
        } else {
            label.setText("There are missing puzzles to assemble");
        }
    }
    public static BufferedImage[] splitImage(ImageView imageView) {
        BufferedImage image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
        int subImageWidth = image.getWidth() / COLUMNS;
        int subImageHeight = image.getHeight() / ROWS;

        BufferedImage[] images = new BufferedImage[PARTS];

        int current_img = 0;

        // iterating over rows and columns for each sub-image
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                // Creating sub image
                images[current_img] = new BufferedImage(subImageWidth, subImageHeight, image.getType());
                Graphics2D img_creator = images[current_img].createGraphics();

                // coordinates of source image
                int src_first_x = subImageWidth * j;
                int src_first_y = subImageHeight * i;

                // coordinates of sub-image
                int dst_corner_x = subImageWidth * j + subImageWidth;
                int dst_corner_y = subImageHeight * i + subImageHeight;

                img_creator.drawImage(image, 0, 0, subImageWidth, subImageHeight, src_first_x, src_first_y,
                        dst_corner_x, dst_corner_y, null);
                current_img++;
            }
        }
        return images;
    }
}