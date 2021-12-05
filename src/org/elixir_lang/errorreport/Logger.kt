package org.elixir_lang.errorreport

import com.ericsson.otp.erlang.OtpErlangObject
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.diagnostic.AttachmentFactory
import com.intellij.openapi.diagnostic.Logger
import java.lang.StringBuilder

object Logger {
    /*
     * Public Static Methods
     */
    /**
     * Logs error to the `klass`'s [com.intellij.openapi.diagnostic.Logger] instance with the given
     * `userMessage` and the text of `element` as the details and containing file of `element` as an
     * attachment
     *
     * @param klass   Class whose logger to use
     * @param title   Title of error stored in [Throwable].
     * @param element element responsible for the error
     */
    @JvmStatic
    fun error(klass: Class<*>, title: String, element: PsiElement) {
        error(Logger.getInstance(klass), title, element)
    }

    fun error(klass: Class<*>, title: String, term: OtpErlangObject) {
        error(Logger.getInstance(klass), title, term)
    }

    /**
     * Logs error [com.intellij.openapi.diagnostic.Logger] instance with the given `title` and the
     * text of `element` as the details and containing file of `element` as an * attachment
     *
     * @param logger  logger to which to log an error.
     * @param title   Title of error stored in [Throwable].
     * @param element element responsible for the error
     */
    fun error(logger: Logger,
              title: String,
              element: PsiElement) {
        val throwable = Throwable(title)
        val containingFile = element.containingFile
        val message = message(containingFile, element)
        val virtualFile = containingFile.virtualFile
        if (virtualFile != null) {
            val attachment = AttachmentFactory.createAttachment(virtualFile)
            logger.error(message, throwable, attachment)
        } else {
            logger.error(message, throwable)
        }
    }

    /**
     * Logs error [com.intellij.openapi.diagnostic.Logger] instance with the given `title` and the
     * `term`
     *
     * @param logger  logger to which to log an error.
     * @param title   Title of error stored in [Throwable].
     * @param term responsible for the error
     */
    fun error(logger: Logger,
              title: String,
              term: OtpErlangObject) {
        val throwable = Throwable(title)
        val message = message(term)
        logger.error(message, throwable)
    }

    /*
     * Private Static Methods
     */
    private fun className(element: PsiElement): String {
        return """
             
             ### Element Class Name
             
             ```
             ${element.javaClass.name}
             ```
             """.trimIndent()
    }

    private fun excerpt(containingFile: PsiFile, element: PsiElement): String {
        val excerptBuilder = StringBuilder()
        excerptBuilder
                .append('\n')
                .append("### Excerpt\n")
                .append('\n')
        excerptBuilder
                .append("```\n")
                .append(element.text)
                .append('\n')
                .append("```\n")
        val fileViewProvider = containingFile.viewProvider
        val document = fileViewProvider.document
        if (document != null) {
            val virtualFile = containingFile.virtualFile
            if (virtualFile != null) {
                val path = virtualFile.path
                val textRange = element.textRange
                val startingLine = document.getLineNumber(textRange.startOffset)
                val endingLine = document.getLineNumber(textRange.endOffset)
                excerptBuilder
                        .append('\n')
                        .append("From: `").append(path).append(':').append(startingLine).append('`').append('\n')
                        .append("To: `").append(path).append(':').append(endingLine).append('`')
            }
        }
        return excerptBuilder.toString()
    }

    private fun message(containingFile: PsiFile,
                        element: PsiElement): String {
        return """
            ${excerpt(containingFile, element)}
            ${className(element)}
            """.trimIndent()
    }

    private fun message(term: OtpErlangObject): String {
        return """
            
            ### Term
            
            $term
            
            """.trimIndent()
    }
}
