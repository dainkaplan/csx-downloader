package citenet.csx.oai2
import org.scalatest.FunSuite

class DocumentTests extends FunSuite {

    val sampleRecord = """
    <record>
      <header>
        <identifier>oai:CiteSeerX.psu:10.1.1.1.2047</identifier>
        <datestamp>2009-04-19</datestamp>
      </header>
      <metadata>
        <oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
          <dc:title>Awesome Paper</dc:title>
          <dc:creator>Mr Awesome</dc:creator>
          <dc:creator>Mr Rad</dc:creator>
          <dc:description>Awesome research, this is.</dc:description>
          <dc:contributor></dc:contributor>
          <dc:publisher/>
          <dc:date>2009-04-19</dc:date>
          <dc:date>1998</dc:date>
          <dc:format>application/pdf</dc:format>
          <dc:type>text</dc:type>
          <dc:identifier>http://citeseerx.ist.psu.edu/citeseerx/viewdoc/summary?doi=10.1.1.1.2047</dc:identifier>
          <dc:source>http://www.getyourpaperhere.com/paper.pdf</dc:source>
          <dc:language>en</dc:language>
          <dc:relation>1</dc:relation>
          <dc:relation>2</dc:relation>
          <dc:relation>3</dc:relation>
          <dc:relation>4</dc:relation>
          <dc:rights></dc:rights>
        </oai_dc:dc>
      </metadata>
    </record>
"""

    test("can parse OAI2 XML into a document") {
        val doc = Document.fromXmlString(sampleRecord)
        expect(List("Mr Awesome", "Mr Rad")) {
            doc.creators
        }
        expect("Awesome Paper") {
            doc.title
        }
        expect("") {
            doc.rights
        }
        expect(List("1", "2", "3", "4")) {
            doc.relations
        }
        expect("application/pdf") {
            doc.format
        }
        expect("http://www.getyourpaperhere.com/paper.pdf") {
            doc.source
        }
    }

    test("Can load a list of documents from OAI2 XML") {
        import scalax.io.JavaConverters._
        import java.io.File
        val content = new File("oai-3687964461435121722.tmp-1").asInput.slurpString(io.Codec.UTF8)
        val list = Document.toDocumentList(content)
        assert(list.size === 500)
    }
}