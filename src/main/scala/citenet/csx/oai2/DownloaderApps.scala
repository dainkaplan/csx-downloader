package citenet.csx.oai2

object DownloaderApp extends App {
    val dir = if (args.size > 0) args(0) else "csx-raw"
    val f = new java.io.File(dir)
    if (!f.exists()) f.mkdirs()
    val path = f.getAbsolutePath()
    println("Will save raw data to " + path)
    var rawfiles = new RawFilesDownloader(Some(dir))
    rawfiles.maxLoops = 1
    rawfiles.download()
}

object JsonDownloaderApp extends App {
    val dir = if (args.size > 0) args(0) else "csx-json"
    val f = new java.io.File(dir)
    if (!f.exists()) f.mkdirs()
    val path = f.getAbsolutePath()
    println("Will save json data to " + path)
    var rawfiles = new JsonFilesDownloader(Some(dir))
    rawfiles.maxLoops = 1
    rawfiles.download()
}