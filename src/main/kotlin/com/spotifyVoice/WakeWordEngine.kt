package com.spotifyVoice

import ai.picovoice.porcupine.Porcupine
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.*

object WakeWordEngine {
    var micDataLine: TargetDataLine? = null
    fun listenForWord(osType: OsCheck.OSType?) {
        val outputPath = "tempfile"

        // for file output
        var outputFile: File? = null
        var outputStream: ByteArrayOutputStream? = null
        var totalBytesCaptured: Long = 0
        val format = AudioFormat(16000f, 16, 1, true, false)

        // get audio capture device
        val dataLineInfo = DataLine.Info(TargetDataLine::class.java, format)
        //TargetDataLine micDataLine;
        try {
            micDataLine = getDefaultCaptureDevice(dataLineInfo)
            micDataLine!!.open(format)
        } catch (e: LineUnavailableException) {
            System.err.println("Failed to get a valid capture device")
            System.exit(1)
            return
        }
        var porcupine: Porcupine? = null
        try {
            //TODO move to main
            val porcupineAccessKey = System.getenv("PicoVoiceAccessKey")
            if (porcupineAccessKey == null) {
                println("set 'PicoVoiceAccessKey' as a system variable with the value from https://console.picovoice.ai/access_key")
                System.exit(-1)
            }
            var keywordPath: String? = null
            keywordPath = when (osType) {
                OsCheck.OSType.Windows -> "friday_en_windows_v2_0_0.ppn"
                OsCheck.OSType.MacOS -> "friday_en_mac_v2_0_0.ppn"
                OsCheck.OSType.Linux -> "friday_linux.ppn"
                else -> "friday_en_windows_v2_0_0.ppn"
            }
            porcupine = Porcupine.Builder()
                    .setAccessKey(System.getenv("PicoVoiceAccessKey"))
                    .setKeywordPath(keywordPath)
                    .build()
            
            if (outputPath != null) {
                outputFile = File(outputPath)
                outputStream = ByteArrayOutputStream()
            }
            micDataLine!!.start()
            print("Listening for Friday")


            // buffers for processing audio
            val frameLength = porcupine.frameLength
            val captureBuffer = ByteBuffer.allocate(frameLength * 2)
            captureBuffer.order(ByteOrder.LITTLE_ENDIAN)
            val porcupineBuffer = ShortArray(frameLength)
            var numBytesRead: Int
            while (System.`in`.available() == 0) {

                // read a buffer of audio
                numBytesRead = micDataLine!!.read(captureBuffer.array(), 0, captureBuffer.capacity())
                totalBytesCaptured += numBytesRead.toLong()

                // write to output if we're recording
                outputStream?.write(captureBuffer.array(), 0, numBytesRead)

                // don't pass to porcupine if we don't have a full buffer
                if (numBytesRead != frameLength * 2) {
                    continue
                }

                // copy into 16-bit buffer
                captureBuffer.asShortBuffer()[porcupineBuffer]

                // process with porcupine
                val result = porcupine.process(porcupineBuffer)
                if (result >= 0) {
                    println("\n Got wake word")
                    micDataLine!!.close()
                    return
                }
            }
        } catch (e: Exception) {
            System.err.println(e.toString())
        } finally {
            if (outputStream != null && outputFile != null) {

                // need to transfer to input stream to write
                val writeArray = ByteArrayInputStream(outputStream.toByteArray())
                val writeStream = AudioInputStream(writeArray, format, totalBytesCaptured / format.frameSize)
                try {
                    AudioSystem.write(writeStream, AudioFileFormat.Type.WAVE, outputFile)
                } catch (e: IOException) {
                    System.err.printf("Failed to write audio to '%s'.\n", outputFile.path)
                    e.printStackTrace()
                }
            }
            porcupine?.delete()
        }
    }

    @Throws(LineUnavailableException::class)
    private fun getDefaultCaptureDevice(dataLineInfo: DataLine.Info): TargetDataLine {
        if (!AudioSystem.isLineSupported(dataLineInfo)) {
            throw LineUnavailableException("Default capture device does not support the audio " +
                    "format required by Picovoice (16kHz, 16-bit, linearly-encoded, single-channel PCM).")
        }
        return AudioSystem.getLine(dataLineInfo) as TargetDataLine
    }
}