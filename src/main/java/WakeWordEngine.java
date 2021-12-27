import ai.picovoice.porcupine.Porcupine;
import com.spotifyVoice.OsCheck;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WakeWordEngine {

    static TargetDataLine micDataLine = null;
    public static void listenForWord(OsCheck.OSType osType) {

        String outputPath = "tempfile";

        // for file output
        File outputFile = null;
        ByteArrayOutputStream outputStream = null;
        long totalBytesCaptured = 0;
        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);

        // get audio capture device
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        //TargetDataLine micDataLine;
        try {
            micDataLine = getDefaultCaptureDevice(dataLineInfo);
            micDataLine.open(format);

        } catch (LineUnavailableException e) {
            System.err.println("Failed to get a valid capture device");
            System.exit(1);
            return;
        }

        Porcupine porcupine = null;
        try {
            //TODO move to main
            String porcupineAccessKey = System.getenv("PicoVoiceAccessKey");
            if (porcupineAccessKey == null){
                System.out.println("set 'PicoVoiceAccessKey' as a system variable with the value from https://console.picovoice.ai/access_key");
                System.exit(-1);
            }

            //TODO use ostype to choose correct model for each OS
            porcupine = new ai.picovoice.porcupine.Porcupine.Builder()
                    .setAccessKey(System.getenv("PicoVoiceAccessKey"))
                    .setKeywordPath("wakeWordModels/friday_en_windows_v2_0_0.ppn")
                    .build();

            if (outputPath != null) {
                outputFile = new File(outputPath);
                outputStream = new ByteArrayOutputStream();
            }

            micDataLine.start();
            System.out.print("Listening for Friday");


            // buffers for processing audio
            int frameLength = porcupine.getFrameLength();
            ByteBuffer captureBuffer = ByteBuffer.allocate(frameLength * 2);
            captureBuffer.order(ByteOrder.LITTLE_ENDIAN);
            short[] porcupineBuffer = new short[frameLength];

            int numBytesRead;
            while (System.in.available() == 0) {

                // read a buffer of audio
                numBytesRead = micDataLine.read(captureBuffer.array(), 0, captureBuffer.capacity());
                totalBytesCaptured += numBytesRead;

                // write to output if we're recording
                if (outputStream != null) {
                    outputStream.write(captureBuffer.array(), 0, numBytesRead);
                }

                // don't pass to porcupine if we don't have a full buffer
                if (numBytesRead != frameLength * 2) {
                    continue;
                }

                // copy into 16-bit buffer
                captureBuffer.asShortBuffer().get(porcupineBuffer);

                // process with porcupine
                int result = porcupine.process(porcupineBuffer);
                if (result >= 0) {
                    System.out.println("\n Got wake word");
                    micDataLine.close();
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        } finally {
            if (outputStream != null && outputFile != null) {

                // need to transfer to input stream to write
                ByteArrayInputStream writeArray = new ByteArrayInputStream(outputStream.toByteArray());
                AudioInputStream writeStream = new AudioInputStream(writeArray, format, totalBytesCaptured / format.getFrameSize());

                try {
                    AudioSystem.write(writeStream, AudioFileFormat.Type.WAVE, outputFile);
                } catch (IOException e) {
                    System.err.printf("Failed to write audio to '%s'.\n", outputFile.getPath());
                    e.printStackTrace();
                }
            }

            if (porcupine != null) {
                porcupine.delete();
            }
        }
    }


    private static TargetDataLine getDefaultCaptureDevice(DataLine.Info dataLineInfo) throws LineUnavailableException {

        if (!AudioSystem.isLineSupported(dataLineInfo)) {
            throw new LineUnavailableException("Default capture device does not support the audio " +
                    "format required by Picovoice (16kHz, 16-bit, linearly-encoded, single-channel PCM).");
        }

        return (TargetDataLine) AudioSystem.getLine(dataLineInfo);
    }
}