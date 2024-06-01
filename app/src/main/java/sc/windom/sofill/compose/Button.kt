package sc.windom.sofill.compose

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import sc.windom.sofill.S
import sc.windom.sofill.U


@Composable
fun AudioButtons() {
    val inspectionMode = LocalInspectionMode.current // 获取当前是否处于预览模式// 获取窗口尺寸
    val Button_Width = S.C.Button_Width.current
    val btn_lspace = S.C.btn_lspace.current
    val btn_PaddingTopH = S.C.btn_PaddingTopH.current
    val btn_PaddingTopV = S.C.btn_PaddingTopV.current
    val btn_TextFontsizeH = S.C.btn_TextFontsizeH.current
    val btn_TextFontsizeV = S.C.btn_TextFontsizeV.current
    val btn_Color1 = S.C.btn_Color1.current
    val btn_bgColor1 = S.C.btn_bgColor_pink.current
    val btnText1 = S.C.btnTextAudio1.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor1,
            contentColor = btn_Color1
        ), enabled = false,
        onClick = { /*TODO*/ }) {
        Text(
            text = btnText1,
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
    val btn_Color1 = S.C.btn_Color1.current
    val btn_bgColor1 = S.C.btn_bgColor_pink.current
    val btnText1 = S.C.btnTextVideo1.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE // 是否横屏（宽高比）
    Button(modifier= Modifier
        .width(Button_Width.dp)
        .padding(top = if (isLandscape) btn_PaddingTopH else btn_PaddingTopV),
        colors = ButtonDefaults.buttonColors(
            containerColor = btn_bgColor1,
            contentColor = btn_Color1
        ), enabled = true, onClick = {
            U.handleVideo(Lcc, uri)
        }) {
        Text(
            text = btnText1,
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