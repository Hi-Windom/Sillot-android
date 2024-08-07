/*
 * Sillot T☳Converbenk Matrix 汐洛彖夲肜矩阵：为智慧新彖务服务
 * Copyright (c) 2024.
 *
 * lastModified: 2024/8/5 17:26
 * updated: 2024/8/5 17:26
 */

package sc.windom.sofill.compose

import android.app.Activity
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import sc.windom.sofill.S
import sc.windom.sofill.U

/**
 * 替代 IconButton ，支持长按、双击
 *
 * 已知问题：
 * - 没有点击动效
 */
@Composable
fun IconButtonPro(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onDoubleTap: ((Offset) -> Unit)? = null,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null
) {
    val combinedModifier = modifier.then(
        Modifier
            .minimumInteractiveComponentSize()
            .clickable(
                onClick = {},
                enabled = false,
                role = Role.Button,
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        onTap?.invoke(offset)
                    },
                    onDoubleTap = { offset ->
                        onDoubleTap?.invoke(offset)
                    },
                    onLongPress = { offset ->
                        onLongPress?.invoke(offset)
                    },
                )
            }
    )
    Icon(imageVector, contentDescription = contentDescription, modifier = combinedModifier)
}

@Composable
fun AudioButtons(uri: Uri) {
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Lcc = LocalContext.current
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = S.C.btn_bgColor_pink.current,
            contentColor = S.C.btn_Color1.current
        ), enabled = false,
        onClick = { /*TODO*/ }) {
        Text(
            text = S.C.btnTextAudio1.current,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = S.C.btn_bgColor_pink.current,
            contentColor = S.C.btn_Color1.current
        ), enabled = true, onClick = {
            uri.let {
                U.openAudioWithThirdPartyApp(Lcc as Activity, it)
            }
        }) {
        Text(
            text = S.C.btnTextOpenByThirdParty.current,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }
}

@Composable
fun VideoButtons(uri: Uri) {
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Lcc = LocalContext.current
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = S.C.btn_bgColor_pink.current,
            contentColor = S.C.btn_Color1.current
        ), enabled = true, onClick = {
            U.handleVideo(Lcc, uri)
        }) {
        Text(
            text = S.C.btnTextVideo1.current,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = S.C.btn_bgColor_pink.current,
            contentColor = S.C.btn_Color1.current
        ), enabled = true, onClick = {
            uri.let {
                U.openVideoWithThirdPartyApp(Lcc as Activity, it)
            }
        }) {
        Text(
            text = S.C.btnTextOpenByThirdParty.current,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }
}

@Composable
fun ApkButtons(btnText: String,  cb: () -> Unit) {
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val btn_Color1 = S.C.btn_Color1.current
    val btn_bgColor1 = S.C.btn_bgColor_pink.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor1,
            contentColor = btn_Color1
        ), enabled = true, onClick = cb) {
        Text(
            text = btnText,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }
}

@Composable
fun MagnetButtons(btnText: String,  cb: () -> Unit) {
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = S.C.btn_bgColor_pink.current,
            contentColor = S.C.btn_Color1.current
        ), enabled = true, onClick = cb) {
        Text(
            text = btnText,
            letterSpacing = btn_lspace,
            fontSize = if (isLandscape) btn_TextFontsizeH else btn_TextFontsizeV
        )
    }
}