package templates

import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div

fun FlowContent.home() = div {
    classes = setOf("flex items-center justify-center h-full")
    button {
        classes = setOf("bg-neutral-200 broder-2 rounded-md p-5")

        attributes["hx-post"] = "/queue"
        attributes["hx-target"] = "body"

        +"Play Poker"
    }
}
