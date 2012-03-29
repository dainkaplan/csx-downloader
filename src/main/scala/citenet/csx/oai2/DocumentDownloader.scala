package citenet.csx.oai2

class DocumentDownloader(dler: citenet.oai2.Downloader) {
    /**
     * After a page of results has been downloaded, the handler will be called
     * once for each document within that set of results.
     */
    def download(handler: Document => Unit) = {
        dler.download((content) => {
            Document.toDocumentList(content).foreach(doc => {
                handler(doc)
            })
        })
    }
    
    // ============================================================
    // ======================== FOR JAVA ==========================
    trait DownloadCallback {
        def handleDownload(doc: Document)
    }

    def download(handler: DownloadCallback) {
        download(handler.handleDownload _)
    }
    // ======================== FOR JAVA ==========================
    // ============================================================
}