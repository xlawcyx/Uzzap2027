package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

data class ClassicEmoticon(
    val token: String,
    @param:DrawableRes val drawableRes: Int,
    val description: String
)

val CLASSIC_EMOTICONS = listOf(
    ClassicEmoticon(":uzzap_11:", R.drawable.uzzap_emoticon_11, "Big grin"),
    ClassicEmoticon(":uzzap_12:", R.drawable.uzzap_emoticon_12, "Curious"),
    ClassicEmoticon(":uzzap_13:", R.drawable.uzzap_emoticon_13, "Blushing"),
    ClassicEmoticon(":uzzap_14:", R.drawable.uzzap_emoticon_14, "Thinking"),
    ClassicEmoticon(":uzzap_15:", R.drawable.uzzap_emoticon_15, "Surprised"),
    ClassicEmoticon(":uzzap_16:", R.drawable.uzzap_emoticon_16, "Cool"),
    ClassicEmoticon(":uzzap_17:", R.drawable.uzzap_emoticon_17, "Shocked"),
    ClassicEmoticon(":uzzap_18:", R.drawable.uzzap_emoticon_18, "Celebrating"),
    ClassicEmoticon(":uzzap_19:", R.drawable.uzzap_emoticon_19, "Laughing"),
    ClassicEmoticon(":uzzap_20:", R.drawable.uzzap_emoticon_20, "Angry"),
    ClassicEmoticon(":uzzap_47:", R.drawable.uzzap_emoticon_47, "Kiss"),
    ClassicEmoticon(":uzzap_angry:", R.drawable.uzzap_emoticon_angry, "Very angry"),
    ClassicEmoticon(":uzzap_clown:", R.drawable.uzzap_emoticon_clown, "Clown"),
    ClassicEmoticon(":uzzap_cry:", R.drawable.uzzap_emoticon_cry, "Crying"),
    ClassicEmoticon(":uzzap_drink:", R.drawable.uzzap_emoticon_drink, "Drink"),
    ClassicEmoticon(":uzzap_e:", R.drawable.uzzap_emoticon_e, "Worried"),
    ClassicEmoticon(":uzzap_laugh:", R.drawable.uzzap_emoticon_laugh, "Wide smile"),
    ClassicEmoticon(":uzzap_love:", R.drawable.uzzap_emoticon_love, "Love"),
    ClassicEmoticon(":uzzap_party:", R.drawable.uzzap_emoticon_party, "Party"),
    ClassicEmoticon(":uzzap_rose:", R.drawable.uzzap_emoticon_rose, "Rose"),
    ClassicEmoticon(":uzzap_sad:", R.drawable.uzzap_emoticon_sad, "Sad"),
    ClassicEmoticon(":uzzap_sick:", R.drawable.uzzap_emoticon_sick, "Sick"),
    ClassicEmoticon(":uzzap_smile:", R.drawable.uzzap_emoticon_smile, "Smile"),
    ClassicEmoticon(":uzzap_tongue:", R.drawable.uzzap_emoticon_tongue, "Tongue out"),
    ClassicEmoticon(":uzzap_surprise:", R.drawable.uzzap_emoticon_surprise, "Surprised"),
    ClassicEmoticon(":uzzap_wink:", R.drawable.uzzap_emoticon_wink, "Wink"),
    ClassicEmoticon(":uzzap_head_buntis:", R.drawable.uzzap_head_buntis, "Buntis"),
    ClassicEmoticon(":uzzap_head_deckard:", R.drawable.uzzap_head_deckard, "Deckard"),
    ClassicEmoticon(":uzzap_head_jp:", R.drawable.uzzap_head_jp, "JP"),
    ClassicEmoticon(":uzzap_head_kool:", R.drawable.uzzap_head_kool, "Kool"),
    ClassicEmoticon(":uzzap_head_lupz:", R.drawable.uzzap_head_lupz, "Lupz"),
    ClassicEmoticon(":uzzap_head_requiem:", R.drawable.uzzap_head_requiem, "Requiem"),
    ClassicEmoticon(":uzzap_head_root:", R.drawable.uzzap_head_root, "Root"),
    ClassicEmoticon(":uzzap_head_saiyan:", R.drawable.uzzap_head_saiyan, "Saiyan"),
    ClassicEmoticon(":uzzap_head_valkyrie:", R.drawable.uzzap_head_valkyrie, "Valkyrie")
)

private val emoticonsByToken = CLASSIC_EMOTICONS.associateBy(ClassicEmoticon::token)
private val emoticonTokenPattern = Regex(
    CLASSIC_EMOTICONS.joinToString(separator = "|") { Regex.escape(it.token) }
)

fun classicEmoticonForMessage(message: String): ClassicEmoticon? =
    emoticonsByToken[message.trim()]

fun appendClassicEmoticon(draft: String, token: String): String {
    require(token in emoticonsByToken) { "Unknown classic emoticon token" }
    return when {
        draft.isBlank() -> "$token "
        draft.last().isWhitespace() -> "$draft$token "
        else -> "$draft $token "
    }
}

@Composable
fun ClassicEmoticonPicker(
    onEmoticonSelected: (ClassicEmoticon) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CLASSIC_EMOTICONS, key = ClassicEmoticon::token) { emoticon ->
            Surface(
                onClick = { onEmoticonSelected(emoticon) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.testTag("emoticon_${emoticon.token.removeSurrounding(":")}")
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(emoticon.drawableRes),
                        contentDescription = emoticon.description,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(38.dp)
                            .height(31.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClassicEmoticonMessage(
    message: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    lineHeight: TextUnit = 20.sp
) {
    val standaloneEmoticon = classicEmoticonForMessage(message)
    if (standaloneEmoticon != null) {
        Image(
            painter = painterResource(standaloneEmoticon.drawableRes),
            contentDescription = standaloneEmoticon.description,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .width(54.dp)
                .height(44.dp)
        )
        return
    }

    val annotatedMessage = remember(message) {
        buildAnnotatedString {
            var currentIndex = 0
            emoticonTokenPattern.findAll(message).forEach { match ->
                append(message.substring(currentIndex, match.range.first))
                appendInlineContent(match.value, match.value)
                currentIndex = match.range.last + 1
            }
            append(message.substring(currentIndex))
        }
    }
    val inlineContent = CLASSIC_EMOTICONS.associate { emoticon ->
        emoticon.token to InlineTextContent(
            placeholder = Placeholder(
                width = 27.sp,
                height = 22.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Image(
                painter = painterResource(emoticon.drawableRes),
                contentDescription = emoticon.description,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 1.dp)
            )
        }
    }

    Text(
        text = annotatedMessage,
        inlineContent = inlineContent,
        fontSize = fontSize,
        lineHeight = lineHeight,
        color = color,
        modifier = modifier
    )
}
