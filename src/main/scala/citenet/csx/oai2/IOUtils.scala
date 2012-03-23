package citenet.csx.oai2
import java.net.URL

/**
 * Let's us pass strings around as if they were URLs!
 */
object StringToURL {
	implicit def convertString2URL(url: String) = new URL(url)
}

/**
 * Gives us utility methods for working with files.
 */
object FileUtils {
	def writeToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
		val p = new java.io.PrintWriter(f)
		try { op(p) } finally { p.close() }
	}
}

/**
 * Adds extension method to Source, that lets you do:
 * 
 *   source.closeAfter { /* code here */ }
 */
class CloseSourceAfter(c: io.Source) {
	def closeAfter(f: => Unit): Unit = {
		try { f } 
		finally { c.close() }
	}
}

object CloseSourceAfter {
	implicit def sourceToSourceCloseAfter(c: io.Source) = new CloseSourceAfter(c)
}