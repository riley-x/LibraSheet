package com.example.librasheet.ui.dialogs

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.example.librasheet.ui.theme.LibraSheetTheme

/**
 * Dialog master composable, with arbitrary content followed by a red "cancel" button and a green
 * "ok" button.
 *
 * @param errorMessage A shortcut to print a red message after [content].
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Dialog(
    modifier: Modifier = Modifier,
    title: String = "",
    errorMessage: String = "",
    cancelText: String = "Cancel",
    okText: String = "Ok",
    scrollable: Boolean = false,
    onCancel: () -> Unit = { },
    onDismiss: () -> Unit = onCancel,
    onOk: () -> Unit = { },
    content: @Composable ColumnScope.() -> Unit = { },
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        /** This is needed to allow the dialog to resize if the content changes.
         * This really messes up the previews though.
         * See https://stackoverflow.com/questions/68469681/jetpack-compose-layout-changes-in-dialog-doesnt-update-the-size
         */
        properties = DialogProperties(usePlatformDefaultWidth = false),
        // don't use the title argument, it really messes with the layouts
        text = {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                LocalTextStyle provides MaterialTheme.typography.body1,
            ) {
                Column(
                    modifier = if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
                ) {
                    Text(text = title)
                    content()
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colors.error,
                        )
                    }
                }
            }
        },
        buttons = {
            ConfirmationButtons(
                cancelText = cancelText,
                okText = okText,
                onCancel = onCancel,
                onOk = onOk,
            )
        },
        modifier = modifier,
    )
}



@Preview
@Composable
private fun Preview() {
    LibraSheetTheme {
        Dialog(
            title = "I'm a simple dialog",
        )
    }
}

@Preview
@Composable
private fun PreviewError() {
    LibraSheetTheme {
        Dialog(
            title = "I'm a simple dialog",
            errorMessage = "Error: Oh no!"
        )
    }
}