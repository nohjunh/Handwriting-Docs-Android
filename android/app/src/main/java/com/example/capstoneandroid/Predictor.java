package com.example.capstoneandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

public class Predictor {
    private static final String TAG = Predictor.class.getSimpleName();
    public boolean isLoaded = false;
    public int warmupIterNum = 1;
    public int inferIterNum = 1;
    public int cpuThreadNum = 4;
    public String cpuPowerMode = "LITE_POWER_HIGH";
    public String modelPath = "";
    public String modelName = "";
    protected OCRPredictorNative paddlePredictor = null;
    protected float inferenceTime = 0;
    // Only for object detection
    protected Vector<String> wordLabels = new Vector<String>();
    protected int detLongSize = 960;
    protected float scoreThreshold = 0.1f;
    protected ArrayList<Bitmap> inputImages = new ArrayList<>();
    protected volatile String outputResult = "";

    public Predictor() {
    }

    public boolean init(Context appCtx, String modelPath, String labelPath, int useOpencl, int cpuThreadNum, String cpuPowerMode) {
        isLoaded = loadModel(appCtx, modelPath, useOpencl, cpuThreadNum, cpuPowerMode);
        if (!isLoaded) {
            return false;
        }
        isLoaded = loadLabel(appCtx, labelPath);
        return isLoaded;
    }

    public boolean init(Context appCtx, String modelPath, String labelPath, int useOpencl, int cpuThreadNum, String cpuPowerMode,
                        int detLongSize, float scoreThreshold) {
        boolean isLoaded = init(appCtx, modelPath, labelPath, useOpencl, cpuThreadNum, cpuPowerMode);
        if (!isLoaded) {
            return false;
        }
        this.detLongSize = detLongSize;
        this.scoreThreshold = scoreThreshold;
        return true;
    }

    protected boolean loadModel(Context appCtx, String modelPath, int useOpencl, int cpuThreadNum, String cpuPowerMode) {
        // Release model if exists
        releaseModel();
        // Load model
        if (modelPath.isEmpty()) {
            return false;
        }
        String realPath = modelPath;
        if (!modelPath.substring(0, 1).equals("/")) {
            // Read model files from custom path if the first character of mode path is '/'
            // otherwise copy model to cache from assets
            realPath = appCtx.getCacheDir() + "/" + modelPath;
            Utils.copyDirectoryFromAssets(appCtx, modelPath, realPath);
        }
        if (realPath.isEmpty()) {
            return false;
        }
        OCRPredictorNative.Config config = new OCRPredictorNative.Config();
        config.useOpencl = useOpencl;
        config.cpuThreadNum = cpuThreadNum;
        config.cpuPower = cpuPowerMode;
        config.detModelFilename = realPath + File.separator + "ko_PP-OCRv3_det.nb";
        config.recModelFilename = realPath + File.separator + "ko_PP-OCRv3_rec.nb";
        config.clsModelFilename = realPath + File.separator + "cls.nb";
        Log.i("Predictor", "model path" + config.detModelFilename + " ; " + config.recModelFilename + ";" + config.clsModelFilename);
        paddlePredictor = new OCRPredictorNative(config);
        this.cpuThreadNum = cpuThreadNum;
        this.cpuPowerMode = cpuPowerMode;
        this.modelPath = realPath;
        this.modelName = realPath.substring(realPath.lastIndexOf("/") + 1);
        return true;
    }

    public void releaseModel() {
        if (paddlePredictor != null) {
            paddlePredictor.destroy();
            paddlePredictor = null;
        }
        isLoaded = false;
        cpuThreadNum = 1;
        cpuPowerMode = "LITE_POWER_HIGH";
        modelPath = "";
        modelName = "";
    }

    protected boolean loadLabel(Context appCtx, String labelPath) {
        wordLabels.clear();
        wordLabels.add("black");
        // Load word labels from file
        try {
            InputStream assetsInputStream = appCtx.getAssets().open(labelPath);
            int available = assetsInputStream.available();
            byte[] lines = new byte[available];
            assetsInputStream.read(lines);
            assetsInputStream.close();
            String words = new String(lines);
            String[] contents = words.split("\n");
            for (String content : contents) {
                wordLabels.add(content);
            }
            wordLabels.add(" ");
            Log.i(TAG, "Word label size: " + wordLabels.size());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }


    public boolean runModel(int run_det, int run_cls, int run_rec) {
        if (inputImages.size() <= 0 || !isLoaded()) {
            return false;
        }
        // Warm up
        for (Bitmap inputImage: inputImages) {
            for (int i = 0; i < warmupIterNum; i++) {
                paddlePredictor.runImage(inputImage, detLongSize, run_det, run_cls, run_rec);
            }
        }
        warmupIterNum = 0; // do not need warm
        // Run inference
        Date start = new Date();
        ArrayList<ArrayList<OCRResultModel>> results = new ArrayList<>();
        for (Bitmap inputImage: inputImages) {
            ArrayList<OCRResultModel> result = paddlePredictor.runImage(inputImage, detLongSize, run_det, run_cls, run_rec);
            result = postProcess(result);
            results.add(result);
        }
        Date end = new Date();
        inferenceTime = (end.getTime() - start.getTime()) / (float) inferIterNum;
        drawResults(results);
        return true;
    }

    public boolean isLoaded() {
        return paddlePredictor != null && isLoaded;
    }


    private ArrayList<OCRResultModel> postProcess(ArrayList<OCRResultModel> results) {
        Collections.sort(results);
        for (OCRResultModel r: results) {
            StringBuffer word = new StringBuffer();
            for (int index : r.getWordIndex()) {
                if (index >= 0 && index < wordLabels.size()) {
                    word.append(wordLabels.get(index));
                } else {
                    Log.e(TAG, "Word index is not in label list:" + index);
                    word.append("×");
                }
            }
            r.setLabel(word.toString());
            r.setClsLabel(r.getClsIdx() == 1 ? "180" : "0");
        }
        return results;
    }

    private void drawResults(ArrayList<ArrayList<OCRResultModel>> results) {
        StringBuffer outputResultSb = new StringBuffer();
        outputResultSb.delete(0, outputResultSb.length());

        for (ArrayList<OCRResultModel> result: results) {
            for (int i = 0; i < result.size(); i++) {
                OCRResultModel res = result.get(i);
                if (res.getLabel().length() > 0){
                    Log.i("label", res.getLabel());
                    outputResultSb.append(res.getLabel()).append(" ");
                }
            }
        }
        outputResult = outputResultSb.toString();
    }

    public void setInputImages(ArrayList<Bitmap> inputImages) {
        if (inputImages == null || inputImages.size() <= 0) {
            Log.e(TAG, "setInputImages error");
            return;
        }
        this.inputImages.clear();
        for (Bitmap inputImage: inputImages) {
            this.inputImages.add(inputImage.copy(Bitmap.Config.ARGB_8888, true));
        }
    }
}
