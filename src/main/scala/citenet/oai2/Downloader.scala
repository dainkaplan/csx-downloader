package citenet.oai2
import java.net.URL
import java.io.File
import scalax.io.JavaConverters._

/**
 * Base class for OAI2 interaction. By extending this, other OAI2 provides could be accessed.
 * */
abstract class Downloader {
    val baseUrl: String
    val initUrl: String
    def resumeUrl(token: String): String
    var maxLoops: Int = 9999
    var maxDownloadTries = 10
    var resumptionToken: Option[String] = None

    def download(handler: String => Unit): Unit = {
        for (i <- 1 to maxLoops) {
            var data: Option[String] = None
            val url = resumptionToken match {
                case Some(t) => resumeUrl(t)
                case None => initUrl
            }
            (1 to maxDownloadTries).takeWhile(cnt => {
                println("%d [%d]: %s".format(i, cnt, url))
                data = fetch(url)
                data == None // Try again?
            })

            data match {
                case Some(str) => {
                    resumptionToken = Downloader.findResumptionToken(str)
                    handler(str)
                    if (resumptionToken == None) return // we're done
                }
                case None => return // either we errored out too many times, or we're done
            }
        }
    }

    def fetch(url: String): Option[String] = {
        try {
            if (url.startsWith("http"))
                Some(new URL(url).asInput.slurpString(io.Codec.UTF8))
            else
                Some(new File(url).asInput.slurpString(io.Codec.UTF8))
        } catch {
            case _ => None
        }
    }

    // ============================================================
    // ======================== FOR JAVA ==========================
    trait DownloadCallback {
        def handleDownload(src: String)
    }

    def download(handler: DownloadCallback) {
        download(handler.handleDownload _)
    }
    // ======================== FOR JAVA ==========================
    // ============================================================
}

object Downloader {
    /**
     * Searches for a resumption token within a file specified by filename.
     */
    def findResumptionTokenInFile(path: String)(filename: String): Option[String] = {
        val str = new File(path + "/" + filename).asInput.slurpString(io.Codec.UTF8)
        val token = findResumptionToken(str)
        token
    }

    def findResumptionToken(str: String): Option[String] = {
        var token: Option[String] = None
        val regex = """^<resumptionToken>([^<]+)</resumptionToken>.*""".r
        val lines = str.split("\n")
        lines.reverse.takeWhile(line => {
            line.trim() match {
                case regex(t) => token = Some(t); false
                case _ => true // keep searching
            }
        })
        println("Found token:" + token)
        token
    }
}