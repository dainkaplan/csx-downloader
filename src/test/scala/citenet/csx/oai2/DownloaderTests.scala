package citenet.csx.oai2
import org.scalatest.FunSuite

/* Keeps downloading the same file over and over */
class MockDownloader extends citenet.oai2.Downloader {
    val baseUrl = "" //new java.io.File("").getAbsolutePath()
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
}

class DownloaderTests extends FunSuite {
    import citenet.oai2._

    test("can properly find resumption tokens") {
        expect(Some("bird is the word")) {
            Downloader.findResumptionToken("    <resumptionToken>bird is the word</resumptionToken>")
        }
        expect(None) {
            Downloader.findResumptionToken("<resurrectionToken>elvis</resurrectionToken>")
        }
    }

    test("can properly find resumption token from real file") {
        import scalax.io.JavaConverters._
        expect(Some("10.1.1.1.2047-1751385-500-oai_dc")) {
            val str = new java.io.File("oai-3687964461435121722.tmp-1").asInput.slurpString()
            Downloader.findResumptionToken(str)
        }
    }
}

class RawFilesTests extends FunSuite {

    // XXX The convoluted nature of this test proves that the code should be restructured...
    test("can resume json files with resumption token") {
        def validateToken(token: String) = {
            expect(Some("10.1.1.1.2047-1751385-500-oai_dc")) { Some(token) }
            "oai-3687964461435121722.tmp-1"
        }

        class JsonFilesMockDownloader(override val outputDir: Option[String]) extends FileDownloader with JsonFiles {
            override val baseUrl = new java.io.File("./").getAbsolutePath()
            override val initUrl = baseUrl + "/oai-3687964461435121722.tmp-1"
            override def resumeUrl(token: String) = baseUrl + "/" + validateToken(token)
            override def saveContent(src: String) = {}
        }

        var mock = new JsonFilesMockDownloader(Some("./"))
        mock.maxLoops = 1
        mock.download(content => {})

        // Check if we properly fetched the resumption token from the second file.
        assert(Some("10.1.1.1.2047-1751385-500-oai_dc") === mock.resumptionToken)
    }

    // XXX The convoluted nature of this test proves that the code should be restructured...
    test("can resume raw files with resumption token") {
        def validateToken(token: String) = {
            expect(Some("10.1.1.1.2047-1751385-500-oai_dc")) { Some(token) }
            "oai-3687964461435121722.tmp-1"
        }

        class RawFilesMockDownloader(override val outputDir: Option[String]) extends FileDownloader with RawFiles {
            override val baseUrl = new java.io.File("./").getAbsolutePath()
            override val initUrl = baseUrl + "/oai-3687964461435121722.tmp-1"
            override def resumeUrl(token: String) = baseUrl + "/" + validateToken(token)
            override def saveContent(src: String) = {
                count += 1
            }
        }

        var mock = new RawFilesMockDownloader(Some("./"))
        mock.maxLoops = 1
        mock.download(content => {})

        // Check if we properly fetched the resumption token from the second file.
        assert(Some("10.1.1.1.2047-1751385-500-oai_dc") === mock.resumptionToken)
    }

    test("can filter raw files") {
        val files = Array(
            "oai-fsdanfASDF.tmp-1",
            "what am I doing here?",
            "oai-fsda214b3DF.tmp-15",
            "oai-fsdanffdhf.tmp-5")
        val filtered = RawFiles.filterFiles(files)
        val left = files.filter(f => !filtered.contains(f))
        assert(left.size === 1)
        expect("what am I doing here?") {
            left(0)
        }
    }

    test("can find last raw file") {
        val files = Array(
            "oai-fsdanfASDF.tmp-1",
            "what am I doing here?",
            "oai-fsda214b3DF.tmp-15",
            "oai-fsdanffdhf.tmp-5")
        expect(Some("oai-fsda214b3DF.tmp-15")) {
            RawFiles.findLastFile(files)
        }
    }

    test("can find last token from raw files") {
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
            RawFiles.findLastToken(files, tokenFinder)
        }
    }
}