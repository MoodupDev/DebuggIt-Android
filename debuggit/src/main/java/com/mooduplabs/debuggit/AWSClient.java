package com.mooduplabs.debuggit;

import android.os.AsyncTask;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class AWSClient {
    private static final String UPLOADED_IMAGES_FOLDER = "image/";
    private static final String UPLOADED_IMAGE_PREFIX = "image";
    private static final String UPLOADED_IMAGE_SUFFIX = ".png";
    private static final String UPLOADED_IMAGE_CONTENT_TYPE = "image/png";
    private static final String UPLOADED_AUDIO_FOLDER = "audio/";
    private static final String UPLOADED_AUDIO_PREFIX = "audio";
    private static final String UPLOADED_AUDIO_SUFFIX = ".mpeg";
    private static final String UPLOADED_AUDIO_CONTENT_TYPE = "audio/mpeg";

    private static String bucketName;
    private static AmazonS3Client s3Client;
    private static Boolean awsConfigured = false;

    protected static void configureAWS(String bucketName, String accessKey, String secretKey, String region) {
        AWSClient.bucketName = bucketName;

        AWSClient.s3Client = new AmazonS3Client(
                new BasicAWSCredentials(accessKey, secretKey),
                Region.getRegion(region)
        );

        AWSClient.awsConfigured = true;
    }

    protected static void configureAWS(String bucketName, String accessKey, String secretKey, Region region) {
        AWSClient.bucketName = bucketName;

        AWSClient.s3Client = new AmazonS3Client(
                new BasicAWSCredentials(accessKey, secretKey),
                region
        );

        AWSClient.awsConfigured = true;
    }

    protected static Boolean isAWSConfigured() {
        return AWSClient.awsConfigured;
    }

    protected static void uploadImage(InputStream imageStream, JsonResponseCallback callback) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(UPLOADED_IMAGE_CONTENT_TYPE);

        String key = UPLOADED_IMAGES_FOLDER + generateFileName(UPLOADED_IMAGE_PREFIX, UPLOADED_IMAGE_SUFFIX);

        uploadFile(key, metadata, imageStream, callback);
    }

    protected static void uploadAudio(InputStream audioStream, JsonResponseCallback callback) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(UPLOADED_AUDIO_CONTENT_TYPE);

        String key = UPLOADED_AUDIO_FOLDER + generateFileName(UPLOADED_AUDIO_PREFIX, UPLOADED_AUDIO_SUFFIX);

        uploadFile(key, metadata, audioStream, callback);
    }

    private static String generateFileName(String prefix, String suffix) {
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.getDefault());
        String formattedString = dateFormat.format(date);

        return prefix + " " + formattedString + suffix;
    }

    private static void uploadFile(String key, ObjectMetadata metadata, InputStream stream, JsonResponseCallback callback) {
        PutObjectRequest request = new PutObjectRequest(bucketName, key, stream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        putObjectOnS3(request, callback);
    }

    private static void putObjectOnS3(final PutObjectRequest request, final JsonResponseCallback callback) {
        new AsyncTask<Void, Void, PutObjectResult>() {
            @Override
            protected PutObjectResult doInBackground(Void... params) {
                try {
                    return s3Client.putObject(request);
                } catch (AmazonServiceException ex) {
                    callback.onFailure(ex.getStatusCode(), ex.getMessage());
                } catch (Exception ex) {
                    callback.onException(ex);
                }

                return null;
            }

            @Override
            protected void onPostExecute(PutObjectResult result) {
                if (result == null) {
                    return;
                }

                String url = s3Client.getResourceUrl(bucketName, request.getKey());

                JSONObject resultObject = null;

                try {
                    resultObject = new JSONObject().put("url", url);
                } catch (JSONException ex) {
                    callback.onException(ex);
                }

                callback.onSuccess(resultObject);
            }
        }.execute();
    }
}
