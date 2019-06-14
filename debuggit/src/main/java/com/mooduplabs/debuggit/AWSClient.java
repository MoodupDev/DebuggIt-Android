package com.mooduplabs.debuggit;

import android.content.Context;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

class AWSClient {
    private static final String UPLOADED_IMAGES_FOLDER = "image";
    private static final String UPLOADED_AUDIO_FOLDER = "audio";

    private static String bucketName;
    private static AmazonS3Client s3Client;
    private static TransferManager transferManager;
    private static TransferUtility transferUtility;
    private static Boolean awsConfigured = false;

    protected static void configureAWS(String bucketName, String region) {
        AWSClient.bucketName = bucketName;

        AWSClient.s3Client = new AmazonS3Client(
                new BasicSessionCredentials("", "", ""),
                Region.getRegion(region)
        );

        configureAWS();
    }

    protected static void configureAWS(String bucketName, Region region) {
        AWSClient.bucketName = bucketName;

        AWSClient.s3Client = new AmazonS3Client(
                new BasicSessionCredentials("", "", ""),
                region
        );

        configureAWS();
    }

    private static void configureAWS() {
        transferManager = new TransferManager(s3Client);

        AWSClient.awsConfigured = true;
    }

    protected static Upload uploadImage(InputStream imageStream) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/png");

        PutObjectRequest por = new PutObjectRequest(bucketName, UPLOADED_IMAGES_FOLDER, imageStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        return transferManager.upload(por);
    }

    protected static Upload uploadAudio(InputStream audioStream) {
        ObjectMetadata metadata = new ObjectMetadata();

        PutObjectRequest por = new PutObjectRequest(bucketName, UPLOADED_IMAGES_FOLDER, audioStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        return transferManager.upload(por);
    }

    protected static Boolean isAWSConfigured() {
        return AWSClient.awsConfigured;
    }
}
