$(() => {

    const imageFormId = "#imageProcessForm"
    const imageProcessUrl = $(imageFormId).attr("action")
    const imagesInputElement = document.querySelector("#images-input")
    const fileInputElement = document.querySelector("#file-input")
    
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
        }
    })

})