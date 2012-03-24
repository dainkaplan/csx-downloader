package citenet.csx.oai2
import java.util.Date

object Document {
    import scala.xml.XML
    import scala.xml.NodeSeq

    /* Sample record
    <record>
      <header>
        <identifier>oai:CiteSeerX.psu:10.1.1.1.2047</identifier>
        <datestamp>2009-04-19</datestamp>
      </header>
      <metadata>
        <oai_dc:dc xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
          <dc:title>Tailored Compression of Java Class Files</dc:title>
          <dc:creator>R. Nigel Horspool</dc:creator>
          <dc:creator> Jason Corless</dc:creator>
          <dc:description>Java class files can be transmitted more efficiently over a network if they are compressed. After an...</dc:description>
          <dc:contributor>The Pennsylvania State University CiteSeerX Archives</dc:contributor>
          <dc:publisher/>
          <dc:date>2009-04-19</dc:date>
          <dc:date>2007-11-19</dc:date>
          <dc:date>1998</dc:date>
          <dc:format>application/pdf</dc:format>
          <dc:type>text</dc:type>
          <dc:identifier>http://citeseerx.ist.psu.edu/citeseerx/viewdoc/summary?doi=10.1.1.1.2047</dc:identifier>
          <dc:source>http://www.csr.uvic.ca/~nigelh/Publications/SPE1998.pdf</dc:source>
          <dc:language>en</dc:language>
          <dc:relation>10.1.1.103.9056</dc:relation>
          <dc:relation>10.1.1.33.9488</dc:relation>
          <dc:relation>10.1.1.22.6561</dc:relation>
          <dc:relation>10.1.1.45.4678</dc:relation>
          <dc:rights>Metadata may be used without restrictions as long as the oai identifier remains attached to it.</dc:rights>
        </oai_dc:dc>
      </metadata>
    </record>
*/

    protected def setList(setter: List[String] => Unit)(f: NodeSeq) = {
        setter(f.map(_.text).toList)
    }
    protected def setText(setter: String => Unit)(f: NodeSeq) = {
        setter(f.text)
    }

    /**
     * This is kind of gross, but the concept is that we create a field map,
     * and then iterator over it and use the wrapped setter functions to set
     * each field.
     */
    def fromXmlString(xml: String) = {
        val root = XML.loadString(xml)
        val doc = new Document()
        doc.header.identifier = (root \ "header" \ "identifier").text
        doc.header.datestamp = (root \ "header" \ "datestamp").text
        val fields = Map[String, NodeSeq => Unit](
            "title" -> setText(doc.title_=),
            "creator" -> setList(doc.creators_=),
            "description" -> setText(doc.description_=),
            "contributor" -> setText(doc.contributor_=),
            "publisher" -> setText(doc.publisher_=),
            "dates" -> setList(doc.dates_=),
            "format" -> setText(doc.format_=),
            "identifier" -> setText(doc.id_=),
            "source" -> setText(doc.source_=),
            "language" -> setText(doc.language_=),
            "relation" -> setList(doc.relations_=),
            "rights" -> setText(doc.rights_=))
        for ((key, setter) <- fields) {
            setter(root \ "metadata" \\ key)
        }
        doc
    }
}

class Document {
    class Header {
        var identifier = ""
        var datestamp = ""
    }

    var header: Header = new Header()
    var title = ""
    var creators: List[String] = Nil
    var publisher = ""
    var subject = ""
    var contributor = ""
    var description = ""
    var tags = ""
    var dates: List[String] = Nil
    var format = ""
    var id = ""
    var source = ""
    var language = ""
    var relations: List[String] = Nil
    var rights = ""
}