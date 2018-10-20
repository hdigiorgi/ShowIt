$(() => {

    const imageFormId = "#imageProcessForm"
    const imageListFormId = "#imageListForm"
    const imageProcessUrl = $(imageFormId).attr("action")
    const imageListUrl = $(imageListFormId).attr("action")
    const imagesInputElement = document.querySelector("#images-input")
    const fileInputElement = document.querySelector("#file-input")

    function onComplete(id, name, response) {
        var serverPathToFile = response.filePath
        var fileItem = this.getItemByFileId(id);
        console.log("on complete")
        if (response.success) {
            console.log("on complete success")
            var wrapper = qq(fileItem).getByClass("qq-thumbnail-wrapper")
            wrapper.setAttribute("style", serverPathToFile);
        }
    }
    
    qq.supportedFeatures.imagePreviews = false;
    var uploader = new qq.FineUploader({
        element: document.getElementById("image-uploader"),
        debug: true,
        request: {
            endpoint: imageProcessUrl
        },
        deleteFile: {
            enabled: true,
            endpoint: `${imageProcessUrl}&delete=`
        },
        retry: {
            enableAuto: false,
            showButton: true
        },
        session: {
            endpoint: imageListUrl
        }
    })

    

})