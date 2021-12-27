package com.spotifyVoice
import java.util.*


class OsCheck {
    // cached result of OS detection
    internal var detectedOS: OSType? = null

    /**
     * detect the operating system from the os.name System property and cache
     * the result
     *
     * @returns - the operating system detected
     */
    val operatingSystemType: OSType?
        get() {
            if (detectedOS == null) {
                val OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
                detectedOS = if (OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0) {
                    OSType.MacOS
                } else if (OS.indexOf("win") >= 0) {
                    OSType.Windows
                } else if (OS.indexOf("nux") >= 0) {
                    OSType.Linux
                } else {
                    OSType.Other
                }
            }
            return detectedOS
        }

    /**
     * types of Operating Systems
     */
    enum class OSType {
        Windows, MacOS, Linux, Other
    }

}