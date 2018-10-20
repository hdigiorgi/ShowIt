$(() => {

    const imageProcessUrl = $("#imageProcessForm").attr("action")
    const imageListUrl = $("#imageListForm").attr("action")
    const attachmentProcessUrl = $("#attachmentProcessForm").attr("action")
    const attachmentListUrl = $("#attachmentListForm").attr("action")
  
    qq.supportedFeatures.imagePreviews = false;
    function createUploader(id, processUrl, listUrl) {
        return new qq.FineUploader({
            element: document.getElementById(id),
            request: {
                endpoint: processUrl
            },
            deleteFile: {
                enabled: true,
                endpoint: `${processUrl}&delete=`
            },
            retry: {
                enableAuto: false,
                showButton: true
            },
            session: {
                endpoint: listUrl
            }
        })
    }

    createUploader("image-uploader", imageProcessUrl, imageListUrl)
    createUploader("attachment-uploader", attachmentProcessUrl, attachmentListUrl)

})