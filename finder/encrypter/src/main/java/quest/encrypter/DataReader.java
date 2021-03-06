package quest.encrypter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import quest.model.RawWifiData;
import quest.util.Epoch;

public class DataReader {
    private File inputFilesPath;
    private long durationInMs;
    private DataProcessor dataProcessor;
    private int maxRows;
    private Epoch epoch;

    public DataReader(String inputFilesPathStr, int maxRows, int duration, Epoch epoch, DataProcessor processor) {
        inputFilesPath = new File(inputFilesPathStr);
        durationInMs = duration * 60 * 1000;
        dataProcessor = processor;
        this.maxRows = maxRows;
        this.epoch = epoch;
    }

    //current dataset time is in nanosecond, preprocess it to miliseconds
    public String preprocessTimeStr(String timeStrWithNano){
        String[] strSecs = timeStrWithNano.split("\\.",2);
        String nanoSecStr = strSecs[1];

        int nanoSec = Integer.valueOf(nanoSecStr);
        int milliSec = nanoSec/1000;
        return strSecs[0] + "." + String.valueOf(milliSec);
    }

    public void run() throws IOException {
        // Get the list of files in the input directory and sort the files according to
        // the file names
        List<File> inputFiles = Arrays.asList(inputFilesPath.listFiles());
        Collections.sort(inputFiles);

        // debug use
        int rowCounter = 0;
        boolean finishFlag = false;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        // maintain following variables to track timestamp
        Timestamp currentDataTs = null;

        long currentBatchEpoch = 0;
        long currentDataEpoch = 0;

        for (File inputFile : inputFiles) {
            // Skip the directories, e.g. ..,. or other sub dirs
            if (inputFile.isDirectory()) {
                continue;
            } else {
                // Only read CSV data files
                if (!inputFile.getName().split("\\.", 2)[1].equals("csv")) {
                    continue;
                }
                System.out.println("Processing File: " + inputFile.getName());

                try (Reader reader = new FileReader(inputFile);
                        CSVParser csvParser = new CSVParser(reader,
                                CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {

                    for (CSVRecord csvRecord : csvParser) {
                        rowCounter = rowCounter + 1;
                        String timeStr = csvRecord.get("time");

                        //dataset time is in nanosecond format(for the sub-second part), convert to millisecond
                        timeStr = preprocessTimeStr(timeStr);

                        String apIdStr = csvRecord.get("ap_id");
                        String clientIdStr = csvRecord.get("client_id");

                        try {
                            Date date = sdf.parse(timeStr);
                            long timeMs = date.getTime();
                            currentDataTs = new Timestamp(timeMs);
                            currentDataEpoch = epoch.getEpochIdByMs(timeMs);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        //debug
                        //System.out.println("Reading Data Time: " + currentDataTs.toString());
                        
                        RawWifiData rData = new RawWifiData(currentDataTs, apIdStr, clientIdStr);

                        if (currentBatchEpoch == 0){
                            currentBatchEpoch = currentDataEpoch;
                        }

                        if (currentDataEpoch != currentBatchEpoch){
                            dataProcessor.processCurrBatch();
                            currentBatchEpoch = currentDataEpoch;
                        }
  
                        dataProcessor.addToCurrBatch(rData);

                        if(maxRows!=0){
                            if (rowCounter >= maxRows){
                                System.out.println("Read MAX number of lines: " + String.valueOf(maxRows) + ", Finishing");
                                finishFlag = true;
                                break;
                            }
                        }
                    }
                    if (finishFlag){
                        break;
                    }
                }
            }
            System.out.println("Processing File: " + inputFile.getName() + " Done");
            // debug
        }
        //Process the last batch 
        dataProcessor.processCurrBatch();
    }
}
