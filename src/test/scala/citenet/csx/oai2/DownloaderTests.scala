package citenet.csx.oai2
import org.scalatest.FunSuite

/* Keeps downloading the same file over and over */
class MockDownloader extends BaseDownloader {
    val baseUrl = ""//new java.io.File("").getAbsolutePath()
    val initUrl = baseUrl + "oai-3687964461435121722.tmp-1"
    def resumeUrl(token: String) = "oai-3687964461435121722.tmp-1"
}

class DocumentDownloaderTests extends FunSuite {
    test("can download and parse an OAI2 file") {
        var mock = new MockDownloader()
        mock.maxLoops = 1
        var total = 0
        val dler = new DocumentDownloader(mock)
        // Blocks until it's done
        dler.download(doc => {
            total += 1
            //println("[%s] -- %s".format(doc.source, doc.title))
        })
        assert(total === 500)
    }
    
    test("Can load a list of documents from xml") {
        import scalax.io.JavaConverters._
        import java.io.File
        val content = new File("oai-3687964461435121722.tmp-1").asInput.slurpString(io.Codec.UTF8)
        val list = DocumentDownloader.toDocumentList(content)
        assert(list.size === 500)
    }
}

class DownloaderTests extends FunSuite {

    test("can properly find resumption tokens") {
        expect(Some("bird is the word")) {
            BaseDownloader.findResumptionToken("<resumptionToken>bird is the word</resumptionToken>")
        }
        expect(None) {
            BaseDownloader.findResumptionToken("<resurrectionToken>elvis</resurrectionToken>")
        }
    }
}

class RawFileDownloaderTests extends FunSuite {

    test("can filter files") {
        val files = Array(
            "oai-fsdanfASDF.tmp-1",
            "what am I doing here?",
            "oai-fsda214b3DF.tmp-15",
            "oai-fsdanffdhf.tmp-5")
        val filtered = RawFileDownloader.filterFiles(files)
        val left = files.filter(f => !filtered.contains(f))
        assert(left.size === 1)
        expect("what am I doing here?") {
            left(0)
        }
    }

    test("can find last file") {
        val files = Array(
            "oai-fsdanfASDF.tmp-1",
            "what am I doing here?",
            "oai-fsda214b3DF.tmp-15",
            "oai-fsdanffdhf.tmp-5")
        expect(Some("oai-fsda214b3DF.tmp-15")) {
            RawFileDownloader.findLastFile(files)
        }
    }

    test("can find last token (faked)") {
        val files = Array(
            "oai-fsdanfASDF.tmp-1",
            "what am I doing here?",
            "oai-fsda214b3DF.tmp-15",
            "oai-fsdanffdhf.tmp-5")

        var isFirst = true
        def tokenFinder(fname: String): Option[String] = {
            if (fname.endsWith("15") && isFirst)
                Some("THE TOLKIEN")
            else {
                isFirst = false
                None
            }
        }
        expect((Some("THE TOLKIEN"), 15)) {
            RawFileDownloader.findLastToken(files, tokenFinder)
        }
    }

}