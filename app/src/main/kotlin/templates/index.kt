package templates

import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.head
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.onLoad
import kotlinx.html.script
import kotlinx.html.style

fun HTML.index() {
    head {
        meta {
            name = "viewport"
            content = "width=device-width, initial-scale=1.0"
        }
        link {
            rel = "preconnect"
            href = "https://fonts.googleapis.com"
        }
        link {
            rel = "preconnect"
            href = "https://fonts.gstatic.com"
        }
        link {
            media = "print"
            onLoad = "this.onload=null;this.removeAttribute(&#39;media&#39;);"
            href =
                "https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&amp;display=swap"
            rel = "stylesheet"
        }
        script {
            src = "https://unpkg.com/htmx.org@1.9.5"
        }
        script {
            src = "https://unpkg.com/htmx.org/dist/ext/ws.js"
        }
        script {
            src = "https://cdn.tailwindcss.com"
        }
        script {
            src = "https://kit.fontawesome.com/b16bbe82f6.js"
        }
        style {
            +("""
            .app-container, #app {
                margin-top: env(safe-area-inset-top, 0);
                margin-bottom: env(safe-area-inset-bottom, 0);
                height: calc(100vh - env(safe-area-inset-top, 0) - env(safe-area-inset-bottom, 0));
                height: calc(calc(var(--vh, 1vh) * 100) - env(safe-area-inset-top, 0) - env(safe-area-inset-bottom, 0));
                position: relative;
            }

            body {
                height: calc(100vh - env(safe-area-inset-top, 0) - env(safe-area-inset-bottom, 0));
                height: calc(calc(var(--vh, 1vh) * 100) - env(safe-area-inset-top, 0) - env(safe-area-inset-bottom, 0));
            }

            * {
                /* WebKit zooms in when you double tap */
                touch-action: manipulation;
            }
            """.trimIndent())
        }
    }
    body {
        classes = setOf("app-container")
        home()
    }
}