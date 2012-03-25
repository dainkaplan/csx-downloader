package citenet.csx.oai2
import org.fud.optparse._

object Format extends Enumeration {
    type Format = Value
    val Json, Raw, Unknown = Value
}

import Format._

case class Config(
    var format: Format = Unknown,
    var outputDirectory: String = "",
    var isQuiet: Boolean = false,
    var maxLoops: Int = Int.MaxValue,
    var maxTries: Int = 10)

object DownloadApp extends App {
    import Format._

    def toFormatOrNone(f: Format, arg:String) = {
        
        if (f.toString().toLowerCase() == arg.toLowerCase()) {
            Some(f)
        } else
            None
    }

    var config = new Config()
    try {
        var file_args = new OptionParser {
            addArgumentParser[Format] { arg =>
                Format.values.flatMap(f => toFormatOrNone(f, arg)).headOption match {
                    case Some(f) => f
                    case None => throw new InvalidArgumentException("Expected json or raw")
                }
            }
            banner = "csx-downloader [options] output_directory"
            separator("")
            separator("(though likely being run via \"java -jar path/to/csx-downloader.jar\")")
            separator("")
            separator("Options:")
            //bool("-q", "--quiet", 
            //	"Do not write to stdout.") { v => config.isQuiet = v }
            reqd[Format]("-f", "--format=<format>", "Set the data output type. (Raw, Json)") { v => config.format = v }

            optl[Int]("-l", "--maxloops=<max_loops>",
                "Specify a maximum number of times to loop before stopping") { v => config.maxLoops = v.getOrElse(Int.MaxValue) }

            optl[Int]("-t", "--maxtries=<max_tries>",
                "Specify a maximum number of times to try for the same resource, before quitting.",
                "--> defaults to 10") { v => config.maxTries = v.getOrElse(10) }

        }.parse(args)

        // DO STUFF HERE
        if (file_args.isEmpty) {
            println("No output directory specified, using default...")
            file_args = List(null)
        }
        val dler = config.format match {
            case Json => new JsonFilesDownloader(Some(getExistingOutDir(file_args(0), "csx-json")))
            case Raw => new RawFilesDownloader(Some(getExistingOutDir(file_args(0), "csx-raw")))
            case Unknown => {
                println("You must specify a format to output.")
                exit(1)
            }
        }
        val dl = new Download(dler, config)
        dl.start() // Blocks until done.

    } catch { case e: OptionParserException => println(e.getMessage); exit(1) }

    def getExistingOutDir(outdir: String, default: String): String = {
        val f = new java.io.File(if (outdir != null) outdir else default)
        if (!f.exists()) f.mkdirs()
        f.getAbsolutePath()
    }
}

class Download(dler: FileDownloader, config: Config) {
    def start() = {
        println("Will save data to " + dler.outputDir)
        dler.maxDownloadTries = config.maxTries
        dler.maxLoops = config.maxLoops
        dler.download()
    }
}