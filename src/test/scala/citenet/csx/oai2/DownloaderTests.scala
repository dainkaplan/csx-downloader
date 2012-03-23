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