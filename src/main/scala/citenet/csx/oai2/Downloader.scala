package citenet.csx.oai2
import java.net.URL
import java.io.File
import StringToURL._
import CloseSourceAfter._
import FileUtils._
import scalax.io.JavaConverters._

/**
 * All logic for fetching OAI data. Provides a callback mechanism to receive the contents of each iteration.
 * If the resumption token is manually set, it will also resume where it left off with that token.
 * */
abstract class Downloader extends citenet.oai2.ResumableDownloader {
    val baseUrl = "http://citeseerx.ist.psu.edu/oai2?verb=ListRecords"
    val initUrl = baseUrl + "&metadataPrefix=oai_dc"
    def resumeUrl(token: String) = baseUrl + "&resumptionToken=%s".format(token)
    // Gives a callback mechanism for restoring rather than setting in advance; not implemented here.
    // def restoreResumptionToken: Option[String] = None 
}

/**
 * For use with, for example, the document downloader.
 * */
class SimpleDownloader extends Downloader {
    def restoreResumptionToken: Option[String] = None 
}