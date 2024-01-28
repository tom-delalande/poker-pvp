package templates

import kotlinx.html.FlowContent
import kotlinx.html.classes
import kotlinx.html.div


fun FlowContent.queue(playerId: Int) = div {
    classes = setOf("flex items-center justify-center h-full")

    attributes["hx-get"] = "/queue/poll/$playerId"
    attributes["hx-trigger"] = "load delay:1s"
    attributes["hx-target"] = "body"

    +"Looking for a game..."
}