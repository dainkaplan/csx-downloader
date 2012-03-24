package citenet.csx.oai2
import org.scalatest.FunSuite

class DownloaderTests extends FunSuite {

    test("can properly find resumption tokens") {
        expect(Some("bird is the word")) {
            Downloader.findResumptionToken(Seq("<resumptionToken>bird is the word</resumptionToken>"))
        }
        expect(None) {
            Downloader.findResumptionToken(Seq("<resurrectionToken>elvis</resurrectionToken>"))
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