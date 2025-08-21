package com.jose.shareit.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

@Service
public class BarcodeServiceImpl implements BarcodeService {

    @Override
    public byte[] generateBarcodeImage(String barcodeText) {
        try {
            // Use Code128 format
            Code128Writer barcodeWriter = new Code128Writer();
            
            // The BitMatrix represents the black and white squares of the barcode
            BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, 400, 100);

            // Write the BitMatrix to a byte stream as a PNG image
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Could not generate barcode image", e);
        }
    }
}