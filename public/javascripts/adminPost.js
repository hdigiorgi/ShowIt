$(() => {

    const loadingContainer = $("#form-loading");
    const formContainer =  $("#form-container");
    const imageProcessUrl = $("#imageProcessForm").attr("action")
    const imageListUrl = $("#imageListForm").attr("action")
    const attachmentProcessUrl = $("#attachmentProcessForm").attr("action")
    const attachmentListUrl = $("#attachmentListForm").attr("action")
    var uploadsLoaded = 0;

    function onSessionRequestComplete(response, success, request) {
        uploadsLoaded++;
        const showAnimation = {
            properties: {opacity: 1},
            options: {duration: 1000, delay: 500}
        }
        const hideAnimation = {
            properties: {opacity: 0},
            options: {duration: 500}
        }
        if(uploadsLoaded>=2) {
            formContainer.velocity(showAnimation)
            loadingContainer.velocity(hideAnimation)
        }
    }
  
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
            },
            callbacks: {
                onSessionRequestComplete: onSessionRequestComplete
            }
        })
    }

    createUploader("image-uploader", imageProcessUrl, imageListUrl)
    createUploader("attachment-uploader", attachmentProcessUrl, attachmentListUrl)
    var mde = new SimpleMDE({ 
        element: document.getElementById("contentTextArea"),
        spellChecker: false,
        toolbar: ["bold", "italic", "quote", "link", "horizontal-rule","|", 
                  "heading","unordered-list", "ordered-list", "code", "|", 
                  "preview", "side-by-side", "fullscreen"],
        status: false,
        indentWithTabs: false
    });

})