$(() => {
    FilePond.registerPlugin(
        FilePondPluginFileValidateSize,
        FilePondPluginImageExifOrientation,
        FilePondPluginImagePreview,
        FilePondPluginFileValidateSize 
    );

    const imagesInputElement = document.querySelector("#images-input")
    const fileInputElement = document.querySelector("#file-input")
    const pondImages = FilePond.create(imagesInputElement)
    const pondFile = FilePond.create(fileInputElement)
    const sharedConf = {
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
        server: "./upload/images", 
        imagePreviewMaxFileSize: "1MB",
        imagePreviewHeight: 100,
        maxFileSize: "5MB",
        maxFiles: 3,
        allowMultiple: true
    })

    const fileConf = Object.assign({}, sharedConf, {
        server: "./upload/file",
        allowImagePreview: false,
        allowImageExifOrientation: false
    })

    pondImages.setOptions(imagesConf)
    pondFile.setOptions(fileConf)
})