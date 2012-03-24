package citenet.csx.oai2
import java.net.URL
import java.io.File
import StringToURL._
import CloseSourceAfter._
import FileUtils._
import scalax.io.JavaConverters._

/**
 * Downloads files in Json format, one file per document
 * */
class JsonFilesDownloader(override val outputDir: Option[String]) extends FileDownloader with JsonFiles {}

/**
 * Downloads files in the raw OAI2 format, one file per *request*.
 */
class RawFilesDownloader(override val outputDir: Option[String]) extends FileDownloader with RawFiles {}

/**
 * Mix in for the JsonFilesDownloader.
 * */
trait JsonFiles extends IsFileDownloader {
    def tmpDirectory: java.io.File
    def restoreResumptionToken: Option[String] = {
        val tokenFile = new File(tmpDirectory.getAbsolutePath() + "/resumptionToken.tmp")
        if (!tokenFile.exists()) {
            None
        } else {
            val token = tokenFile.asInput.slurpString()
            if (token.length() > 0) Some(token)
            else None
        }
    }
    def doi(str: String) = {
        str.split(":").last
    }
    def saveContent(content: String) = {
        new File(tmpDirectory.getAbsolutePath() + "/resumptionToken.tmp").delete()
        new File(tmpDirectory.getAbsolutePath() + "/resumptionToken.tmp").asOutput.write(resumptionToken.getOrElse(""))
        Document.toDocumentList(content).foreach(doc => {
            val json = JsonFiles.convert(doc)
            File.createTempFile("doi-" + doi(doc.header.identifier) + "-", ".json", tmpDirectory).asOutput.write(json)
        })
    }
}

object JsonFiles {
    import net.liftweb.json.Serialization.{ read, write }
    import net.liftweb.json._
    implicit val formats = Serialization.formats(NoTypeHints)
    def convert(content: String) = {
        val list = Document.toDocumentList(content)
        pretty(render(Extraction.decompose(list)))
    }
    def convert(content: Document) = {
        pretty(render(Extraction.decompose(content)))
    }
}

/**
 * Mix in for RawFilesDownloader
 * */
trait RawFiles extends IsFileDownloader {
    var count = 0
    def tmpDirectory: java.io.File
    def restoreResumptionToken: Option[String] = {
        val (token, lastCount) = RawFiles.findLastToken(tmpDirectory.list(), citenet.oai2.Downloader.findResumptionToken)
        count = lastCount
        token
    }
    def saveContent(src: String) = {
        count += 1
        val f = File.createTempFile("oai-", ".tmp-%d".format(count), tmpDirectory)
        writeToFile(f) { p => p.print(src) }
    }
}

object RawFiles {
    object RawFileName {
        val pattern = """^.*\.tmp-(\d+)$""".r
        def unapply(filename: String) = {
            val pattern(num) = filename
            Some(num.toInt)
        }
    }

    def filterFiles(files: Array[String]) = {
        files
            .filter(fname => fname.startsWith("oai-"))
            .sortBy(fname => { val RawFileName(count) = fname; count })
            .reverse
    }

    def findLastFile(files: Array[String]) = {
        val sorted = filterFiles(files)
        if (sorted.size > 0) Some(sorted(0)) else None
    }

    def findLastCount(files: Array[String]) = {
        findLastFile(files) match {
            case Some(file) => val RawFileName(count) = file; count
            case None => 0
        }
    }

    def findLastToken(files: Array[String], getToken: String => Option[String]) = {
        val sorted = filterFiles(files)
        var lastToken: Option[String] = None
        val lastCount = findLastCount(sorted)
        sorted.takeWhile(fname => {
            lastToken = getToken(fname)
            lastToken == None
        })
        (lastToken, lastCount)
    }
}


/**
 * Base traite for the mix-ins, this insures they implement everything needed.
 * */
trait IsFileDownloader {
    def restoreResumptionToken: Option[String]
    def saveContent(src: String)
    def tmpDirectory: java.io.File
    var resumptionToken: Option[String]
}

/**
 * all the base-logic for saving the data to files. 
 * */
trait FileDownloader extends Downloader with IsFileDownloader {
    val outputDir: Option[String] = None
    def tmpDirectory: java.io.File = outputDir match {
        case Some(dir) => {
            val f = new File(dir)
            if (f.exists())
                f
            else {
                f.mkdirs()
                println("Note: output directory was created because it did not exist.")
                f
            }
        }
        case None => null
    }

    protected def Noop(src: String) = Unit
    protected def saveAndThen(src: String)(then: (String => Unit)) = {
        saveContent(src)
        then(src)
    }

    override def download(handler: (String => Unit) = Noop) {
        if (resumptionToken == None) {
            resumptionToken = restoreResumptionToken
            if (resumptionToken != None) println("Restoring download with resumption token " + resumptionToken)
        }
        super.download((src) => saveAndThen(src)(handler))
    }
}