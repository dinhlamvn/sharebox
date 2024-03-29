package com.dinhlam.sharebox.view

import android.content.Context
import android.graphics.Rect
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import com.dinhlam.sharebox.R
import com.dinhlam.sharebox.extensions.takeIfGreaterThanZero

class ShareBoxReadMoreTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    companion object {
        private const val DEFAULT_COLLAPSED_LINE = 3
        private const val DEFAULT_COLLAPSED_TEXT = "More"
        private const val DEFAULT_EXPANDED_TEXT = "Less"
    }

    enum class ReadMoreState {
        COLLAPSED, EXPANDED
    }

    private var originalText: CharSequence? = null

    private var readMoreState: ReadMoreState = ReadMoreState.COLLAPSED

    private var collapsedLine: Int = DEFAULT_COLLAPSED_LINE

    private var collapsedText: String = DEFAULT_COLLAPSED_TEXT
    private var expandedText: String = DEFAULT_EXPANDED_TEXT
    private var readMoreTextColor: Int = ContextCompat.getColor(context, R.color.colorPrimary)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ShareBoxReadMoreTextView)
            .use { typedArray ->
                typedArray.getInteger(R.styleable.ShareBoxReadMoreTextView_collapsed_line, -1)
                    .takeIfGreaterThanZero()?.let { numOfLine ->
                        collapsedLine = numOfLine
                    }

                collapsedText =
                    typedArray.getString(R.styleable.ShareBoxReadMoreTextView_collapsed_text)
                        ?: DEFAULT_COLLAPSED_TEXT
                expandedText =
                    typedArray.getString(R.styleable.ShareBoxReadMoreTextView_expanded_text)
                        ?: DEFAULT_EXPANDED_TEXT

                readMoreTextColor = typedArray.getColor(
                    R.styleable.ShareBoxReadMoreTextView_read_more_text_color,
                    ContextCompat.getColor(context, R.color.colorPrimary)
                )
            }
        movementMethod = LinkMovementMethod.getInstance()
        autoLinkMask = Linkify.WEB_URLS
    }

    override fun setOnClickListener(l: OnClickListener?) {
        error("No support method")
    }

    fun setReadMoreText(text: CharSequence?) {
        post {
            val nonNullText = text.takeIf { !it.isNullOrBlank() } ?: return@post super.setText(null)
            val endIndexCollapsedText = calcEndIndex(nonNullText)
            if (endIndexCollapsedText >= nonNullText.length) {
                super.setText(nonNullText)
            } else {
                originalText = nonNullText
                val renderText = if (readMoreState == ReadMoreState.COLLAPSED) {
                    buildSpannedString {
                        append(nonNullText.substring(0, endIndexCollapsedText))
                        append("...")
                        color(readMoreTextColor) {
                            append(collapsedText)
                        }
                    }
                } else {
                    buildSpannedString {
                        append(nonNullText)
                        append(" ")
                        color(readMoreTextColor) {
                            append(expandedText)
                        }
                    }
                }
                super.setText(renderText, BufferType.SPANNABLE)
                super.setOnClickListener {
                    toggleReadMore()
                }
            }
        }
    }

    private fun toggleReadMore() {
        readMoreState = if (readMoreState == ReadMoreState.COLLAPSED) {
            ReadMoreState.EXPANDED
        } else {
            ReadMoreState.COLLAPSED
        }
        setReadMoreText(originalText)
    }

    private fun calcEndIndex(text: CharSequence): Int {
        var endIndex = 0
        val allowTextWidth = getAllowCollapsedTextWidth()

        if (allowTextWidth <= 0) {
            return text.length
        }

        val bounds = Rect()

        do {
            paint.getTextBounds(text.toString(), 0, endIndex, bounds)
            endIndex++
        } while (bounds.width() < allowTextWidth && endIndex < text.length)

        return endIndex
    }

    private fun getAllowCollapsedTextWidth(): Int {
        return (width - marginEnd - marginStart - paddingStart - paddingEnd) * collapsedLine
    }
}