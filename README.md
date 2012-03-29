# CiteSeer-X Downloader #

Downloads CiteSeer-X data via the OAI2 inteface.

## Using ##

### From the console ###

The easiest way to use it, is to download the simple-build-tool, install it, and then `compile`/`test`/`one-jar` this app. From the project root, run `sbt` from the commandline to inter the interactive shell, and then run the `compile`/`test`/`one-jar`, each, respectively. All tests should pass. When you run the `one-jar` command, it will create a standalone jar; you can use the generated jar with `java -jar [/path/to/the/jar/file].jar` at the command line. If you run it with no arguments, it will output help.

For example, after running `one-jar` in the sbt console, i exit (with `exit`), and then:

	csx-downloader$ java -jar target/scala-2.9.1/csx-downloader_2.9.1-0.1-SNAPSHOT--standalone.jar
	arguments are required.

	csx-downloader [options] output_directory

	(though likely being run via "java -jar path/to/csx-downloader.jar")

	Options:
	    -f, --format=<format>            Set the data output type. (Raw, Json)
	    -l, --maxloops=<max_loops>       Specify a maximum number of times to loop before stopping
	    -t, --maxtries=<max_tries>       Specify a maximum number of times to try for the same resource, before quitting.
	                                     --> defaults to 10
	    -h, --help                       Show this message

The output for -format=raw will be one file per response from the OAI2 server; for json, it will be one file per document. If you don't specify an output directory (though you should), it will output to `./csx-raw` or or `./csx-json`, for raw and json formats, respectively.

### From code ###

This entirely depends on what you want to do, but you probably want to use the `citenet.csx.oai2.DocumentDownloader`, which takes a callback function to execute for every document that it downloads from the service ("document" here being one *paper* on citeseer).

In scala, you could do:

	:::scala
	import citenet.csx.oai2._
	
	object DownloadApp extends App {
        var downloader = new SimpleDownloader()
        downloader.maxDownloadTries = 3
        val documents = new DocumentDownloader(downloader)
        // Blocks until it's done
        documents.download(doc => {
            println("[%s] -- %s".format(doc.source, doc.title))
        })
	}
	
In java:

	:::java
	import citenet.csx.aoi2.*
	
	public class DownloadApp {
		public static main(final String[] args) {
			SimpleDownloader downloader = new SimpleDownloader();
			DocumentDownloader documents = new DocumentDownloader(downloader);
			downloader.setMaxDownloadTries(3);
			// Blocks until it's done
			documents.download(new DocumentDownloader.DownloadCallback() {
				@override
				public void handleDownload(Document doc) {
					System.out.println("[%s] -- %s".format(doc.getSource(), doc.getTitle()));
				}
			});
		}
	}

This creates a command line program that will print out the titles and URLs (of pdfs) of all papers on citeseer, until either it fails to fetch data from the OAI2 service 3 times in a row, or it grabs them all! You can do much more useful things, of course...

