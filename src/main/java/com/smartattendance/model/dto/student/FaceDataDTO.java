package com.smartattendance.model.dto.student;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

public class FaceDataDTO {
    private List<Mat> images;
    private List<String> imagePaths;
    private Mat histogram;
    private Mat faceEmbedding;

    public FaceDataDTO() {
        this.images = new ArrayList<>();
        this.imagePaths = new ArrayList<>();
    }

    // Add a face image
    public void addImage(Mat image) {
        if (image != null && !image.empty()) {
            images.add(image);
        }
    }

    // Add a face image path
    public void addImagePath(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            imagePaths.add(imagePath);
        }
    }

    // Add both image and its path
    public void addImageWithPath(Mat image, String imagePath) {
        addImage(image);
        addImagePath(imagePath);
    }

    // Get all face images
    public List<Mat> getImages() {
        return new ArrayList<>(images);
    }

    // Get all image paths
    public List<String> getImagePaths() {
        return new ArrayList<>(imagePaths);
    }

    public Mat getFaceEmbedding() {
        return faceEmbedding;
    }

    public void setFaceEmbedding(Mat faceEmbedding) {
        this.faceEmbedding = faceEmbedding;
    }

    public Mat getHistogram() {
        return histogram;
    }

    public void setHistogram(Mat histogram) {
        this.histogram = histogram;
    }

    // Get number of images
    // public int getImageCount() {
    // return images.size();
    // }

    // public boolean hasSufficientImages(int minImages) {
    // return images.size() >= minImages;
    // }

    // Clear all Images and paths
    // public void clearImages() {
    // images.clear();
    // imagePaths.clear();
    // }

    // Validate if images are non-empty
    // public boolean validateImages() {
    // for (Mat image : images) {
    // if (image == null || image.empty()) {
    // return false;
    // }
    // }
    // return !images.isEmpty();
    // }
}