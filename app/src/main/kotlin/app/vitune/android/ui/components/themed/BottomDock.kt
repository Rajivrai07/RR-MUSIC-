package app.vitune.android.ui.components.themed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import app.vitune.android.R
import app.vitune.android.ui.screens.settings.SwitchSettingsEntry
import app.vitune.android.utils.center
import app.vitune.android.utils.color
import app.vitune.android.utils.semiBold
import app.vitune.core.ui.LocalAppearance
import app.vitune.core.ui.utils.roundedShape
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * iOS-style floating liquid glass bottom dock.
 *
 * Renders the same [Tab]s as [NavigationRail] (same icons, labels, selected/unselected
 * states and navigation logic) inside a floating translucent pill instead of a side rail.
 */
@Composable
fun BottomDock(
    topIconButtonId: Int,
    onTopIconButtonClick: () -> Unit,
    tabIndex: Int,
    onTabIndexChange: (Int) -> Unit,
    hiddenTabs: ImmutableList<String>,
    setHiddenTabs: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    tabsEditingTitle: String = stringResource(R.string.tabs),
    content: TabsBuilder.() -> Unit
) {
    val (colorPalette, typography) = LocalAppearance.current

    val tabs = TabsBuilder.rememberTabs(content)

    var editing by remember { mutableStateOf(false) }

    if (editing) DefaultDialog(
        onDismiss = { editing = false },
        horizontalPadding = 0.dp
    ) {
        BasicText(
            text = tabsEditingTitle,
            style = typography.s.center.semiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(
                items = tabs,
                key = { it.key }
            ) { tab ->
                SwitchSettingsEntry(
                    title = tab.title(),
                    text = null,
                    isChecked = tab.key !in hiddenTabs,
                    onCheckedChange = {
                        if (!it && hiddenTabs.size == tabs.size - 1) return@SwitchSettingsEntry

                        setHiddenTabs(if (it) hiddenTabs - tab.key else hiddenTabs + tab.key)
                    },
                    isEnabled = tab.canHide && (tab.key in hiddenTabs || hiddenTabs.size < tabs.size - 1)
                )
            }
        }
    }

    val dockShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = dockShape,
                clip = false,
                spotColor = Color.Black.copy(alpha = 0.35f),
                ambientColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(dockShape)
            .background(brush = Brush.verticalGradient(colors = listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.22f))))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.28f),
                shape = dockShape
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp)
        ) {
            tabs.fastForEachIndexed { index, tab ->
                AnimatedVisibility(
                    visible = tabIndex == index || tab.key !in hiddenTabs,
                    modifier = Modifier.weight(1f),
                    label = ""
                ) {
                    val transition = updateTransition(targetState = tabIndex, label = null)

                    val iconAlpha by transition.animateFloat(label = "") {
                        if (it == index) 1f else 0f
                    }

                    val textColor by transition.animateColor(label = "") {
                        if (it == index) colorPalette.text else colorPalette.textDisabled
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(20.dp.roundedShape)
                            .combinedClickable(
                                onClick = { onTabIndexChange(index) },
                                onLongClick = { editing = true }
                            )
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(tab.icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorPalette.text),
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = iconAlpha
                                    translationX = (1f - iconAlpha) * -16.dp.toPx()
                                }
                                .size(22.dp)
                        )

                        Spacer(Modifier.height(2.dp))

                        BasicText(
                            text = tab.title(),
                            style = typography.xs.semiBold.center.color(textColor),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            }

            Image(
                painter = painterResource(topIconButtonId),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorPalette.textSecondary),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onTopIconButtonClick)
                    .padding(10.dp)
                    .size(22.dp)
            )
        }

        // Subtle top-edge highlight for the liquid glass sheen
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 1.dp)
                .width(120.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0f)
                        )
                    )
                )
        )
    }
}
