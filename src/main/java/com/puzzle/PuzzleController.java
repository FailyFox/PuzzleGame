package com.puzzle;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class PuzzleController {
    @FXML
    private ImageView puzzle0, puzzle1, puzzle2, puzzle3, puzzle4, puzzle5, puzzle6, puzzle7;
    @FXML
    private ImageView puzzle8, puzzle9, puzzle10, puzzle11, puzzle12, puzzle13, puzzle14, puzzle15;
    @FXML
    private Label label;
    private static ImageView[][] puzzle;
    private ImageView draggedImageView;
    private double initialImageX;
    private double initialImageY;
    private double draggedImageX;
    private double draggedImageY;

    @FXML
    private void initialize() {
        puzzle = new ImageView[][] { { puzzle0, puzzle1, puzzle2, puzzle3 },
                                     { puzzle4, puzzle5, puzzle6, puzzle7 },
                                     { puzzle8, puzzle9, puzzle10, puzzle11 },
                                     { puzzle12, puzzle13, puzzle14, puzzle15 } };
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
        for (ImageView[] imageViews : puzzle) {
            for (ImageView imageView : imageViews) {
                if (draggedImageView != imageView && isOverlapping(event, imageView)) {
                    swapImages(draggedImageView, imageView);
                    isSwapped = true;
                    if (isPuzzleCorrect()) {
                        label.setText("Puzzle is correct");
                    } else {
                        label.setText("");
                    }
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
        for (ImageView[] imageView : puzzle) {
            for (ImageView puzzlePiece : imageView) {
                puzzlePiece.setRotate(0);
            }
        }

        for (int i = 0; i < puzzle.length; i++) {
            solveRow(i);
        }
        while (!isVerticallyCompared()) {
            replaceRows();
        }

        if (isPuzzleCorrect()) {
            label.setText("Puzzle is correct");
        } else {
            label.setText("");
        }
    }
    private void solveRow(int row) {
        for (int column = 0; column < puzzle[row].length - 1; column++) {
            ImageView currentPiece = puzzle[row][column];
            ImageView targetPiece;
            int bestMatchRow = -1;
            int bestMatchColumn = -1;
            double bestComplementIndex = Double.MAX_VALUE;
            for (int i = row; i < puzzle.length; i++) {
                for (int j = 0; j < puzzle[i].length; j++) {
                    targetPiece = puzzle[i][j];
                    if (currentPiece.equals(targetPiece)) {
                        continue;
                    }
                    Mat image1 = imageToMat(currentPiece.getImage());
                    Mat image2 = imageToMat(targetPiece.getImage());
                    Rect region1 = new Rect(image1.width() - 10, 0, 10, image1.height());
                    Rect region2 = new Rect(0, 0, 10, image2.height());

                    if (isImagesComplementsHorizontally(image1, image2, region1, region2)) {
                        double complementIndex = imageComplementIndex(image1, image2, region1, region2);
                        if (complementIndex < bestComplementIndex) {
                            bestMatchRow = i;
                            bestMatchColumn = j;
                            bestComplementIndex = complementIndex;
                        }
                    }
                }
            }
            if (bestMatchRow != -1 || bestMatchColumn != -1) {
                targetPiece = puzzle[bestMatchRow][bestMatchColumn];
                swapImages(puzzle[row][column + 1], targetPiece);
            }
        }

        if (isRowCorrect(row)) {
            label.setText("Row correct");
        } else {
            fixRow(row);
        }
    }
    private void fixRow(int row) {
        int bestMatchRow = -1;
        int bestMatchColumn = -1;
        double bestComplementIndex = Double.MAX_VALUE;
        ImageView currentPiece = puzzle[row][0];
        ImageView targetPiece;
        Mat image2 = imageToMat(currentPiece.getImage());
        Rect region2 = new Rect(0, 0, 10, image2.height());
        for (int i = row; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
                targetPiece = puzzle[i][j];

                Mat image1 = imageToMat(targetPiece.getImage());
                Rect region1 = new Rect(image1.width() - 10, 0, 10, image1.height());

                if (isImagesComplementsHorizontally(image1, image2, region1, region2)) {
                    double complementIndex = imageComplementIndex(image1, image2, region1, region2);
                    if (complementIndex < bestComplementIndex) {
                        bestMatchRow = i;
                        bestMatchColumn = j;
                        bestComplementIndex = complementIndex;
                    }
                }
            }
        }
        targetPiece = puzzle[bestMatchRow][bestMatchColumn];
        swapImages(currentPiece, targetPiece);
        solveRow(row);
    }
    private void replaceRows() {
        Mat image1 = imageToMat(puzzle[0][0].getImage());
        Rect region1 = new Rect(0, image1.height() - 10, image1.width(), 10);
        for (int i = 1; i < puzzle[0].length; i++) {
            Mat image2 = imageToMat(puzzle[i][0].getImage());
            Rect region2 = new Rect(0, 0, image1.width(), 10);

            if (isImagesComplementsVertically(image1, image2, region1, region2)) {
                if (i == 1) {
                    for (int column = 0; column < puzzle[0].length; column++) {
                        swapImages(puzzle[i + 1][column], puzzle[i + 2][column]);
                    }
                    return;
                }
                for (int column = 0; column < puzzle[0].length; column++) {
                    swapImages(puzzle[0][column], puzzle[i - 1][column]);
                }
                return;
            }
        }
        for (int column = 0; column < puzzle[0].length; column++) {
            swapImages(puzzle[0][column], puzzle[puzzle[0].length - 1][column]);
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
        return (isHorizontallyCompared() && isVerticallyCompared());
    }
    private boolean isRowCorrect(int row) {
        boolean correct = false;
        for (int j = 0; j < puzzle[row].length-1; j++) {
            Mat image1 = imageToMat(puzzle[row][j].getImage());
            Mat image2 = imageToMat(puzzle[row][j + 1].getImage());

            Rect region1 = new Rect(image1.width() - 10, 0, 10, image1.height());
            Rect region2 = new Rect(0, 0, 10, image2.height());

            if (isImagesComplements(image1, image2, region1, region2)) {
                correct = true;
            } else {
                correct = false;
                break;
            }
        }
        return correct;
    }
    private boolean isHorizontallyCompared() {
        boolean correct = false;
        for (ImageView[] imageViews : puzzle) {
            for (int j = 0; j < imageViews.length - 1; j++) {
                Mat image1 = imageToMat(imageViews[j].getImage());
                Mat image2 = imageToMat(imageViews[j + 1].getImage());

                Rect region1 = new Rect(image1.width() - 10, 0, 10, image1.height());
                Rect region2 = new Rect(0, 0, 10, image2.height());

                if (isImagesComplements(image1, image2, region1, region2)) {
                    correct = true;
                } else {
                    correct = false;
                    break;
                }
            }
            if (!correct) {
                break;
            }
        }
        return correct;
    }
    private boolean isVerticallyCompared() {
        boolean correct = false;
        for (int i = 0; i < puzzle.length - 1; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
                Mat image1 = imageToMat(puzzle[i][j].getImage());
                Mat image2 = imageToMat(puzzle[i + 1][j].getImage());

                Rect region1 = new Rect(0, image1.height() - 10, image1.width(), 10);
                Rect region2 = new Rect(0, 0, image1.width(), 10);

                if (isImagesComplements(image1, image2, region1, region2)) {
                    correct = true;
                } else {
                    correct = false;
                    break;
                }
            }
            if (!correct) {
                break;
            }
        }
        return correct;
    }
    private double imageComplementIndex(Mat image1, Mat image2, Rect region1, Rect region2) {
        Mat subImage1 = image1.submat(region1);
        Mat subImage2 = image2.submat(region2);

        Mat diff = new Mat();
        Core.absdiff(subImage1, subImage2, diff);

        Imgproc.cvtColor(diff, diff, Imgproc.COLOR_BGR2GRAY);

        Imgproc.threshold(diff, diff, 1, 255, Imgproc.THRESH_BINARY);

        Scalar diffSum = Core.sumElems(diff);
        return diffSum.val[0];
    }
    private boolean isImagesComplements(Mat image1, Mat image2, Rect region1, Rect region2) {
        double complementIndex = imageComplementIndex(image1, image2, region1, region2);
        int horizontalThreshold = 629340;
        int verticalThresholdMin = 739755;
        int verticalThresholdMax = 944265;
        return (complementIndex <= horizontalThreshold
                || (complementIndex >= verticalThresholdMin && complementIndex <= verticalThresholdMax))
                && complementIndex != 622710;
    }
    private boolean isImagesComplementsHorizontally(Mat image1, Mat image2, Rect region1, Rect region2) {
        double complementIndex = imageComplementIndex(image1, image2, region1, region2);
        int horizontalThreshold = 629340;
        return complementIndex <= horizontalThreshold && complementIndex != 622710;
    }
    private boolean isImagesComplementsVertically(Mat image1, Mat image2, Rect region1, Rect region2) {
        double complementIndex = imageComplementIndex(image1, image2, region1, region2);
        int verticalThresholdMin = 739755;
        int verticalThresholdMax = 944265;
        return (complementIndex >= verticalThresholdMin && complementIndex <= verticalThresholdMax)
                && complementIndex != 869550;
    }
    private Mat imageToMat(Image image) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        bufferedImage = convertTo3ByteBGRType(bufferedImage);
        Mat mat = new Mat(bufferedImage.getHeight(), bufferedImage.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }
    private BufferedImage convertTo3ByteBGRType(BufferedImage image) {
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        convertedImage.getGraphics().drawImage(image, 0, 0, null);
        return convertedImage;
    }
}