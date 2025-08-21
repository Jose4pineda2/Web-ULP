package com.jose.shareit.service;

public interface BarcodeService {
    /**
     * Generates a byte array representing a barcode image.
     * @param barcodeText The text to encode into the barcode.
     * @return A byte array containing the PNG image data.
     */
    byte[] generateBarcodeImage(String barcodeText);
}