package citenet.csx.oai2

object DownloaderApp extends App {
    val dir = if (args.size > 0) args(0) else ""
    val f = new java.io.File(dir)
    if (!f.exists()) f.mkdirs()
    val path = f.getAbsolutePath()
    println("Will save raw data to " + path)
    var rawfiles = new RawFileDownloader(Some(dir))
    rawfiles.maxLoops = 1
    rawfiles.download()
}


object DocumentsToJson {
    import net.liftweb.json.Serialization.{ read, write }
    import net.liftweb.json._
    implicit val formats = Serialization.formats(NoTypeHints)
    def convert(content: String) = {
    	val list = DocumentDownloader.toDocumentList(content)
        pretty(render(Extraction.decompose(list)))
    }
    def convert(content: Document) = {
        pretty(render(Extraction.decompose(content)))
    }
}

object JsonDownloaderApp extends App {
    import scalax.io.JavaConverters._
    import DocumentsToJson._
    def doi(str:String) = {
    	str.split(":").last
    }
    val dir = if (args.size > 0) args(0) else "tmp"
    val f = new java.io.File(dir)
    if (!f.exists()) f.mkdirs()
    val path = f.getAbsolutePath()
    println("Will save JSON data to " + path)
    var rawfiles = new RawFileDownloader(None)
    rawfiles.maxLoops = 1
    val json = new DocumentDownloader(rawfiles)
    json.download(doc => {
        val json = DocumentsToJson.convert(doc)
        java.io.File.createTempFile("doi-" + doi(doc.header.identifier) + "-", ".json", new java.io.File(path)).asOutput.write(json)
    })
}