package tempura.util

/**
 * Allows us to do things like: using(sourceObj) out => { out.println("bam!") }
 */
object CloseableExt {
	type Closeable = { def close(): Unit }
	def using[A, B <: Closeable](closeable: B)(f: B => A): A = try { f(closeable) } finally { closeable.close() }
}
