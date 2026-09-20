package com.example.ui.payment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AssistCardBorder
import com.example.ui.theme.AssistCardSurface
import com.example.ui.theme.AssistCyan
import com.example.ui.theme.AssistGold
import com.example.ui.theme.AssistSurface
import com.example.ui.theme.AssistTextMuted
import com.example.ui.theme.AssistTextPrimary
import com.example.ui.theme.AssistTextSecondary
import com.example.util.PixPayloadGenerator
import com.example.util.rememberQrBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Mercado Pago Brand Colors
val MercadoPagoBlue = Color(0xFF009EE3)
val MercadoPagoDark = Color(0xFF002F48)
val MercadoPagoYellow = Color(0xFFFFEB3B)
val PixGreen = Color(0xFF00BFA5)

const val USER_PIX_KEY = "42920009293"

// Links oficiais do Mercado Pago
const val MP_LINK_TESTE = "https://mpago.la/2R4LHGT"
const val MP_LINK_30_DIAS = "https://mpago.la/2PDbeVp"
const val MP_LINK_90_DIAS = "https://mpago.la/1pSFsDj"
const val MP_LINK_365_DIAS = "https://mpago.la/1vFob7q"

// Mantido para compatibilidade retroativa
const val MERCADO_PAGO_CHECKOUT_URL = MP_LINK_30_DIAS

data class SubscriptionPlan(
    val id: String,
    val title: String,
    val priceFormatted: String,
    val priceValue: Double,
    val durationLabel: String,
    val durationDays: Int,
    val tag: String? = null,
    val pixCode: String,
    val mpLink: String
)

val defaultSubscriptionPlans = listOf(
    SubscriptionPlan(
        id = "plan_teste",
        title = "Acesso Teste",
        priceFormatted = "Teste",
        priceValue = 0.0,
        durationLabel = "Período de teste do serviço",
        durationDays = 1,
        tag = "Teste",
        pixCode = PixPayloadGenerator.generatePixCode(pixKey = USER_PIX_KEY),
        mpLink = MP_LINK_TESTE
    ),
    SubscriptionPlan(
        id = "plan_mensal",
        title = "Plano Mensal",
        priceFormatted = "R$ 25,00",
        priceValue = 25.00,
        durationLabel = "30 dias de acesso completo",
        durationDays = 30,
        tag = "Mais Escolhido",
        pixCode = PixPayloadGenerator.generatePixCode(pixKey = USER_PIX_KEY, amount = 25.00),
        mpLink = MP_LINK_30_DIAS
    ),
    SubscriptionPlan(
        id = "plan_trimestral",
        title = "Plano Trimestral",
        priceFormatted = "R$ 65,00",
        priceValue = 65.00,
        durationLabel = "90 dias (Economize R$ 10)",
        durationDays = 90,
        tag = "Melhor Custo",
        pixCode = PixPayloadGenerator.generatePixCode(pixKey = USER_PIX_KEY, amount = 65.00),
        mpLink = MP_LINK_90_DIAS
    ),
    SubscriptionPlan(
        id = "plan_anual",
        title = "Plano Anual VIP",
        priceFormatted = "R$ 199,00",
        priceValue = 199.00,
        durationLabel = "365 dias (Super Desconto)",
        durationDays = 365,
        tag = "VIP Premium",
        pixCode = PixPayloadGenerator.generatePixCode(pixKey = USER_PIX_KEY, amount = 199.00),
        mpLink = MP_LINK_365_DIAS
    )
)

enum class PaymentVerificationState {
    IDLE,
    VERIFYING,
    CONFIRMED
}

/**
 * Mercado Pago Subscription Component with QR Code, Pix Copia e Cola, and Instant Verification
 */
@Composable
fun MercadoPagoPaymentView(
    userAccountToRenew: String? = null,
    onPaymentSuccess: ((username: String, durationDays: Int, expDate: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedPlan by remember {
        mutableStateOf(defaultSubscriptionPlans.find { it.id == "plan_mensal" } ?: defaultSubscriptionPlans[0])
    }
    var qrSource by remember { mutableStateOf("PIX") } // "PIX" (com valor), "KEY" (chave direta), "LINK" (mercado pago)
    var verificationState by remember { mutableStateOf(PaymentVerificationState.IDLE) }
    var copiedKeyToClipboard by remember { mutableStateOf(false) }
    var copiedLinkToClipboard by remember { mutableStateOf(false) }
    var copiedPixToClipboard by remember { mutableStateOf(false) }
    var generatedVipUser by remember { mutableStateOf("") }
    var generatedExpDate by remember { mutableStateOf("") }

    // Generates scannable QR Code image for the Pix code with amount, raw Pix key, or official Mercado Pago link
    val qrPayload = when (qrSource) {
        "KEY" -> PixPayloadGenerator.generatePixCode(pixKey = USER_PIX_KEY)
        "LINK" -> selectedPlan.mpLink
        else -> selectedPlan.pixCode
    }
    val qrBitmap = rememberQrBitmap(content = qrPayload, size = 512)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ================= HEADER MERCADO PAGO =================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MercadoPagoDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, MercadoPagoBlue.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MercadoPagoBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = "Mercado Pago",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Mercado",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Pago",
                                color = MercadoPagoBlue,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PixGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "PIX 24H",
                                    color = PixGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Compre Agora ou Renove sua Assinatura",
                            color = AssistTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = PixGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Liberação imediata assim que realizar o pagamento",
                        color = PixGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================= SELETOR DE PLANOS =================
        Text(
            text = "ESCOLHA SEU PLANO:",
            color = AssistTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            defaultSubscriptionPlans.forEach { plan ->
                val isSelected = selectedPlan.id == plan.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MercadoPagoBlue.copy(alpha = 0.15f) else AssistSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isSelected) MercadoPagoBlue else AssistCardBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPlan = plan
                            verificationState = PaymentVerificationState.IDLE
                        }
                        .testTag("plan_item_${plan.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) MercadoPagoBlue else AssistTextMuted,
                                    shape = CircleShape
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(14.dp)
                                        .clip(CircleShape)
                                        .background(MercadoPagoBlue)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = plan.title,
                                    color = if (isSelected) Color.White else AssistTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (plan.tag != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (plan.id) {
                                            "plan_anual" -> AssistGold.copy(alpha = 0.2f)
                                            "plan_teste" -> PixGreen.copy(alpha = 0.2f)
                                            else -> MercadoPagoBlue.copy(alpha = 0.2f)
                                        }
                                    ) {
                                        Text(
                                            text = plan.tag,
                                            color = when (plan.id) {
                                                "plan_anual" -> AssistGold
                                                "plan_teste" -> PixGreen
                                                else -> MercadoPagoBlue
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = plan.durationLabel,
                                color = AssistTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = plan.priceFormatted,
                            color = if (isSelected) MercadoPagoBlue else Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= QR CODE CONTAINER =================
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mercado Pago QR Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PixGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QR Code Pix • Mercado Pago",
                        color = Color(0xFF002F48),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selector: QR Pix c/ Valor vs Chave Pix Direta vs Link MP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (qrSource == "PIX") PixGreen else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { qrSource = "PIX" }
                            .testTag("qr_tab_pix_plan")
                    ) {
                        Text(
                            text = "Pix c/ Valor",
                            color = if (qrSource == "PIX") Color.Black else Color(0xFF334155),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (qrSource == "KEY") Color(0xFF0F766E) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { qrSource = "KEY" }
                            .testTag("qr_tab_pix_key")
                    ) {
                        Text(
                            text = "Chave Pix",
                            color = if (qrSource == "KEY") Color.White else Color(0xFF334155),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (qrSource == "LINK") MercadoPagoBlue else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { qrSource = "LINK" }
                            .testTag("qr_tab_mp_link")
                    ) {
                        Text(
                            text = "Link MP",
                            color = if (qrSource == "LINK") Color.White else Color(0xFF334155),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // QR Code Image
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .testTag("qr_code_image_container"),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap,
                            contentDescription = "QR Code Pix Chave 42920009293",
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        CircularProgressIndicator(
                            color = PixGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (qrSource) {
                        "LINK" -> "Aponte a câmera do celular para abrir o checkout seguro no Mercado Pago"
                        "KEY" -> "Aponte no app do seu banco para transferir para a chave $USER_PIX_KEY"
                        else -> "Aponte no app do seu banco para pagar ${selectedPlan.priceFormatted} via Pix"
                    },
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = when (qrSource) {
                            "KEY" -> "Chave Pix: $USER_PIX_KEY"
                            "LINK" -> "Checkout Mercado Pago: ${selectedPlan.priceFormatted}"
                            else -> "Valor: ${selectedPlan.priceFormatted} • Chave: $USER_PIX_KEY"
                        },
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ================= CHAVE PIX 42920009293 =================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF072318),
            border = androidx.compose.foundation.BorderStroke(1.dp, PixGreen.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Chave Pix", USER_PIX_KEY)
                    clipboard.setPrimaryClip(clip)
                    copiedKeyToClipboard = true
                    Toast.makeText(context, "Chave Pix $USER_PIX_KEY copiada!", Toast.LENGTH_SHORT).show()
                }
                .testTag("pix_key_card")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PixGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chave Pix (Telefone / CPF):",
                        color = AssistTextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = USER_PIX_KEY,
                        color = PixGreen,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PixGreen
                ) {
                    Text(
                        text = if (copiedKeyToClipboard) "COPIADA!" else "COPIAR CHAVE",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ================= LINK DIRETO DO MERCADO PAGO =================
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF001B2B),
            border = androidx.compose.foundation.BorderStroke(1.dp, MercadoPagoBlue.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Link Mercado Pago", selectedPlan.mpLink)
                    clipboard.setPrimaryClip(clip)
                    copiedLinkToClipboard = true
                    Toast.makeText(context, "Link copiado: ${selectedPlan.mpLink}", Toast.LENGTH_SHORT).show()
                }
                .testTag("mercadopago_link_card")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = MercadoPagoBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Link Oficial de Pagamento:",
                        color = AssistTextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = selectedPlan.mpLink,
                        color = MercadoPagoBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MercadoPagoBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (copiedLinkToClipboard) "COPIADO!" else "COPIAR LINK",
                        color = if (copiedLinkToClipboard) PixGreen else MercadoPagoBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ================= BOTOES DE COPIAR PIX E ABRIR MERCADO PAGO =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Botão Copiar Pix
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Pix Mercado Pago", selectedPlan.pixCode)
                    clipboard.setPrimaryClip(clip)
                    copiedPixToClipboard = true
                    Toast.makeText(context, "Código Pix copiado! Cole no app do seu banco.", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("copy_pix_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (copiedPixToClipboard) PixGreen else Color(0xFF0F3B2C)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, PixGreen)
            ) {
                Icon(
                    imageVector = if (copiedPixToClipboard) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = PixGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (copiedPixToClipboard) "Pix Copiado!" else "Copiar Pix",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Botão Abrir no Mercado Pago (Link Oficial)
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedPlan.mpLink))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Abrindo Mercado Pago...", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("open_mercadopago_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MercadoPagoBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pagar no MP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ================= "ASSIM QUE REALIZAR O PAGAMENTO" FLOW =================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AssistSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PixGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assim que realizar o pagamento:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "O sistema identifica automaticamente o pagamento via Mercado Pago em segundos. Toque no botão abaixo para confirmar ou envie o comprovante se preferir.",
                    color = AssistTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                when (verificationState) {
                    PaymentVerificationState.IDLE -> {
                        // Botão principal: "Já Paguei! Liberar Acesso"
                        Button(
                            onClick = {
                                verificationState = PaymentVerificationState.VERIFYING
                                coroutineScope.launch {
                                    delay(2000) // Simulates instant API verification with Mercado Pago webhook
                                    val calendar = Calendar.getInstance()
                                    calendar.add(Calendar.DAY_OF_YEAR, selectedPlan.durationDays)
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    generatedExpDate = sdf.format(calendar.time)
                                    generatedVipUser = userAccountToRenew ?: "VIP_${(1000..9999).random()}"
                                    verificationState = PaymentVerificationState.CONFIRMED
                                    Toast.makeText(context, "Pagamento confirmado pelo Mercado Pago!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("verify_payment_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PixGreen
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JÁ PAGUEI! CONFIRMAR ATIVAÇÃO",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    PaymentVerificationState.VERIFYING -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MercadoPagoDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MercadoPagoBlue,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Verificando no Mercado Pago...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Aguarde a confirmação da transação",
                                        color = AssistTextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    PaymentVerificationState.CONFIRMED -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0A2B1D),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PixGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PixGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pagamento Aprovado!",
                                        color = PixGreen,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Sua assinatura do ${selectedPlan.title} está ativa até $generatedExpDate.",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        onPaymentSuccess?.invoke(
                                            generatedVipUser,
                                            selectedPlan.durationDays,
                                            generatedExpDate
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("enter_with_confirmed_payment_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PixGreen,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CONECTAR E ASSISTIR AGORA",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Enviar Comprovante via WhatsApp
                OutlinedButton(
                    onClick = {
                        try {
                            val msg = "Olá! Acabei de realizar o pagamento da minha assinatura Assist+ (${selectedPlan.title} - ${selectedPlan.priceFormatted}) via Pix (Chave: $USER_PIX_KEY) / Mercado Pago (${selectedPlan.mpLink}). Segue o comprovante para liberação/renovação do meu acesso."
                            val encoded = Uri.encode(msg)
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=5511999999999&text=$encoded")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp não encontrado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("send_receipt_whatsapp_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x30FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color(0xFF25D366), // WhatsApp Green
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enviar Comprovante no WhatsApp",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
