$(() => {
    var speed = 200
    var menuContainer = $("#menu-container")
    var defaultHash = "#main-menu"
    var slideLeftOut = {
        properties: {opacity:0.1, translateX:"-100%", translateZ:0},
        options: {duration: speed,  display: 'none'}
    }
    var slideRightIn = {
        properties: {opacity:1, translateX:[0, "100%"], translateZ:0},
        options: {duration: speed,  display: 'block'}
    }

    function show(toShow) {
        if(toShow.is(":hidden")) {
            var children = menuContainer.children()
            for(i=0; i < children.length; i++) {
                var element = $(children[i])
                if(element.is(":visible")){
                    element.velocity(slideLeftOut)
                }
            }
            var show = () => toShow.velocity(slideRightIn)
            setTimeout(show, speed)
        }
    }

    function onHashChange(hash) {
        var element = $(hash)
        if(element.length) {
            show(element)
        } else {
            if(hash != "") {
                console.error(`${hash} doesn't exist`)
            }
            show($(defaultHash))
        }
    }

    $(window).bind('hashchange',() => onHashChange(top.location.hash))
    onHashChange(top.location.hash)
})
