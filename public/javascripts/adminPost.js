$(() => {
    FilePond.registerPlugin(
        FilePondPluginFileValidateSize,
        FilePondPluginFilePoster 
    );

    const imageFormId = "#imageProcessForm"
    const imageProcessUrl = $(imageFormId).attr("action")
    const imagesInputElement = document.querySelector("#images-input")
    const fileInputElement = document.querySelector("#file-input")
    function getFilePondServer(url) {
        return {
            process: url,
            revert: url,
            load: `${url}&load=`,
            restore: `${url}&load=`,
            fetch: null
        }
    }

    function getValuesFromForm(id) {
        const children = $(id).children()
        var r = []
        for(i=0; i < children.length; i++) {
            r.push(children[i].value)
        }
        return r
    }

    function getFilePondFiles(server, id) {
        return getValuesFromForm(id).map((source) => {
            return {
                source: source,
                options: {
                    type: 'limbo',
                    file: {
                        name: source,
                        type: 'image/jpeg'
                    },
                    metadata: {
                        poster: `${server.load}${source}`
                    }
                },
                
            }
        })
    }

    const sharedConf = {
        allowRevert: true,
        labelMaxFileSizeExceeded: "File is too large",
        labelMaxFileSize: "Maximum file size is {filesize}",
        labelTotalFileSizeExceeded: "Maximum total size exceeded",
        labelMaxTotalFileSize: "Maximum total file size is {filesize}",
        labelIdle:'Drag & Drop your files or <span class="filepond--label-action"> Browse </span>',
        labelFileWaitingForSize: 'Waiting for size',
        labelFileSizeNotAvailable: 'Size not available',
        labelFileLoading: 'Loading',
        labelFileLoadError: 'Error during load',
        labelFileProcessing: 'Uploading',
        labelFileProcessingComplete: 'Upload complete',
        labelFileProcessingAborted: 'Upload cancelled',
        labelFileProcessingError: 'Error during upload',
        labelTapToCancel: 'tap to cancel',
        labelTapToRetry: 'tap to retry',
        labelTapToUndo: 'tap to undo',
        labelButtonRemoveItem: 'Remove',
        labelButtonAbortItemLoad: 'Abort',
        labelButtonRetryItemLoad: 'Retry',
        labelButtonAbortItemProcessing: 'Cancel',
        labelButtonUndoItemProcessing: 'Undo',
        labelButtonRetryItemProcessing: 'Retry',
        labelButtonProcessItem: 'Upload'
    }

    const imagesConf = Object.assign({}, sharedConf, {
        server: getFilePondServer(imageProcessUrl), 
        imagePreviewMaxFileSize: "1MB",
        imagePreviewHeight: 100,
        maxFileSize: "30MB",
        allowMultiple: true,
        files: getFilePondFiles(getFilePondServer(imageProcessUrl),imageFormId)
    })

    const fileConf = Object.assign({}, sharedConf, {
        server: "/admin/post/edit/ID_COMES_HERE/file",
        allowImagePreview: false,
        allowImageExifOrientation: false,
    })

    FilePond.create(imagesInputElement, imagesConf)
    FilePond.create(fileInputElement, fileConf)



})