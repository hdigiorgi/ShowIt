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

})

class CheckableInputValue {
    constructor(indicatorParent, postUrl, fieldName, onChange, getValue) {
        this.indicatorParent = indicatorParent;
        this.postUrl = postUrl;
        this.fieldName = fieldName;
        this.getValue = getValue
        onChange(() => this.onNewUserInput())
        this.ongoingRequest = false;
    }

    sendRequest() {
        this.ongoingRequest = true
        var request = {}
        request[this.fieldName] = this.getValue()
        $.ajax({
            type: "POST",
            url: this.postUrl,
            data: JSON.stringify(request),
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
          })
          .done(() => this.showSuccess())
          .fail((f) => this.showError(f.responseText))
          .always(() => this.ongoingRequest = false )
    }

    onNewUserInput() {
        this.showLoading()
        clearTimeout(this.checkToSendRequestTimeout)
        this.checkToSendRequestTimeout = setTimeout(() => this.sendRequest(), 2000)
    }

    showLoading() {
        var loadingId = this.indicatorParent.attr("id") + "SuccessIcon"
        if($(`#${loadingId}`).length <= 0) {
            this.indicatorParent.empty()
            this.indicatorParent.append(`<i id=${loadingId} class="fas fa-spinner fa-pulse"></i>`)
        }
    }

    showError(error) {
        this.ongoingRequest = false;
        this.indicatorParent.empty()
        this.indicatorParent.append(`<span class="badge badge-warning">${error}</span>`)
    }

    showSuccess() {
        this.ongoingRequest = false;
        this.indicatorParent.empty()
        var successId = this.indicatorParent.id + "success-icon"
        this.indicatorParent.append(`<i id="${successId}" class="fas fa-check ok"></i>`)
    }
}


$(() => {

    function setupTitleSave() {
        const element = $("#titleInput")
        const url = $("#saveTitleForm").attr("action")
        const indications = $("#titleIndications")
        function getValue() {return element.val()}
        function onChange(f) {return element.on("input", f)}
        new CheckableInputValue(indications, url, "title", onChange, getValue)
    }

    function setupContentSave() {
        const contentEditor = new SimpleMDE({ 
            element: document.getElementById("contentTextArea"),
            spellChecker: false,
            toolbar: ["bold", "italic", "quote", "link", "horizontal-rule","|", 
                      "heading","unordered-list", "ordered-list", "code", "|", 
                      "preview", "side-by-side", "fullscreen"],
            status: false,
            indentWithTabs: false
        });
        const url = $("#saveContentForm").attr("action")
        const indications = $("#contentIndications")
        function getValue() {return contentEditor.value()}
        function onChange(f) {contentEditor.codemirror.on("change", f)}
        new CheckableInputValue(indications, url, "content", onChange, getValue)
    }
   
    
    setupContentSave()
    setupTitleSave()

})