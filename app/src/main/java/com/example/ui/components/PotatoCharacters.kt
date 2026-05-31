package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.PoutineCategory

@Composable
fun PotatoCharacterAvatar(
    category: PoutineCategory,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF3EFEB)
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(56.dp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2

            // Base Potato Colors
            val potatoColor = Color(0xFFE8C08D) // Default Potato Beige
            val potatoShadowColor = Color(0xFFD8A25A) // Golden Potato Shadow

            // 1. Draw Potato Body (Organic Rounded Oval)
            val potatoWidth = w * 0.65f
            val potatoHeight = h * 0.72f
            val potX = cx - (potatoWidth / 2)
            val potY = cy - (potatoHeight / 2) + 2f

            // Shadow
            drawRoundRect(
                color = potatoShadowColor,
                topLeft = Offset(potX + 3f, potY + 4f),
                size = Size(potatoWidth, potatoHeight),
                cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
            )
            // Body
            drawRoundRect(
                color = potatoColor,
                topLeft = Offset(potX, potY),
                size = Size(potatoWidth, potatoHeight),
                cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
            )

            // 2. Draw Eyes
            val eyeOffsetY = -h * 0.08f
            val eyeSpacing = w * 0.16f
            val eyeRadius = w * 0.045f

            // Default Eyes
            var leftEyeColor = Color(0xFF1A1A1A)
            var rightEyeColor = Color(0xFF1A1A1A)

            // Custom Eyes based on characters
            val eyesY = cy + eyeOffsetY

            // Left Eye
            drawCircle(
                color = leftEyeColor,
                radius = eyeRadius,
                center = Offset(cx - eyeSpacing, eyesY)
            )
            // Right Eye
            drawCircle(
                color = rightEyeColor,
                radius = eyeRadius,
                center = Offset(cx + eyeSpacing, eyesY)
            )

            // Sparkle inside eyes
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.35f,
                center = Offset(cx - eyeSpacing - 1.5f, eyesY - 1.5f)
            )
            drawCircle(
                color = Color.White,
                radius = eyeRadius * 0.35f,
                center = Offset(cx + eyeSpacing - 1.5f, eyesY - 1.5f)
            )

            // Roosy Rosy Cheeks for girls and kids
            if (category == PoutineCategory.MRS_POUTINE || 
                category == PoutineCategory.KIDS || 
                category == PoutineCategory.GRANDMA) {
                drawCircle(
                    color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                    radius = w * 0.08f,
                    center = Offset(cx - eyeSpacing - 6f, eyesY + 10f)
                )
                drawCircle(
                    color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                    radius = w * 0.08f,
                    center = Offset(cx + eyeSpacing + 6f, eyesY + 10f)
                )
            }

            // 3. Draw Mouth (happy curving smile)
            val mouthPath = Path().apply {
                val mouthWidth = w * 0.14f
                val mouthY = cy + 2f
                moveTo(cx - mouthWidth, mouthY)
                quadraticTo(cx, mouthY + h * 0.08f, cx + mouthWidth, mouthY)
            }
            drawPath(
                path = mouthPath,
                color = Color(0xFF1A1A1A),
                style = Stroke(width = 3.5f)
            )

            // 4. Character accessories
            when (category) {
                PoutineCategory.MR_POUTINE -> {
                    // Mr. Poutine: Mustache and black tiny bowtie or top hat
                    val mustachePath = Path().apply {
                        val mY = cy + 3f
                        moveTo(cx, mY)
                        // Left handle
                        quadraticTo(cx - 10f, mY - 6f, cx - 18f, mY - 2f)
                        quadraticTo(cx - 10f, mY + 6f, cx, mY)
                        // Right handle
                        quadraticTo(cx + 10f, mY - 6f, cx + 18f, mY - 2f)
                        quadraticTo(cx + 10f, mY + 6f, cx, mY)
                    }
                    drawPath(path = mustachePath, color = Color(0xFF424242))

                    // Draw a French Chef Hat at top
                    val hatHeight = h * 0.22f
                    val hatWidth = w * 0.38f
                    val hatY = potY - hatHeight + 4f
                    val hatPath = Path().apply {
                        moveTo(cx - hatWidth / 2, hatY + hatHeight)
                        lineTo(cx - hatWidth / 2, hatY + hatHeight / 2)
                        // Bubbles of chef hat
                        cubicTo(
                            cx - hatWidth / 2 - 5f, hatY - 5f,
                            cx - hatWidth / 4, hatY - 10f,
                            cx - hatWidth / 6, hatY
                        )
                        cubicTo(
                            cx, hatY - 12f,
                            cx + hatWidth / 4, hatY - 10f,
                            cx + hatWidth / 2, hatY + hatHeight / 2
                        )
                        lineTo(cx + hatWidth / 2, hatY + hatHeight)
                        close()
                    }
                    drawPath(path = hatPath, color = Color.White)
                    drawPath(path = hatPath, color = Color(0xFFC62828), style = Stroke(width = 2.5f))
                }

                PoutineCategory.MRS_POUTINE -> {
                    // Mrs. Poutine: A cute Canadian red flower hair bow at top right
                    val bowX = cx + w * 0.18f
                    val bowY = potY + 4f
                    val bowPath = Path().apply {
                        moveTo(bowX, bowY)
                        lineTo(bowX - 8f, bowY - 10f)
                        lineTo(bowX - 12f, bowY + 2f)
                        close()
                        moveTo(bowX, bowY)
                        lineTo(bowX + 8f, bowY - 10f)
                        lineTo(bowX + 12f, bowY + 2f)
                        close()
                    }
                    drawPath(path = bowPath, color = Color(0xFFC62828))
                    drawCircle(color = Color(0xFFD8A25A), radius = 3f, center = Offset(bowX, bowY - 3f))
                }

                PoutineCategory.KIDS -> {
                    // Sassy kid cap tilted on the side (Red cap with a Golden brim)
                    val capPath = Path().apply {
                        moveTo(cx - w * 0.22f, potY + 6f)
                        quadraticTo(cx - w * 0.1f, potY - 12f, cx + w * 0.15f, potY + 2f)
                        lineTo(cx + w * 0.28f, potY + 12f)
                        lineTo(cx - w * 0.22f, potY + 12f)
                        close()
                    }
                    drawPath(path = capPath, color = Color(0xFFC62828))
                    // Brim
                    val brimPath = Path().apply {
                        moveTo(cx - w * 0.15f, potY + 10f)
                        lineTo(cx - w * 0.38f, potY + 16f)
                        lineTo(cx - w * 0.1f, potY + 13f)
                    }
                    drawPath(path = brimPath, color = Color(0xFFD8A25A), style = Stroke(width = 4f))
                }

                PoutineCategory.GRANDPA -> {
                    // Grandpa: White mustache, glasses, thinning side hair
                    // Glasses frames
                    val gY = cy + eyeOffsetY
                    val gSpacing = w * 0.16f
                    val gRad = w * 0.08f
                    drawCircle(color = Color(0xFFD8A25A), radius = gRad, center = Offset(cx - gSpacing, gY), style = Stroke(width = 2.5f))
                    drawCircle(color = Color(0xFFD8A25A), radius = gRad, center = Offset(cx + gSpacing, gY), style = Stroke(width = 2.5f))
                    // Bridge
                    drawLine(color = Color(0xFFD8A25A), start = Offset(cx - gSpacing + gRad, gY), end = Offset(cx + gSpacing - gRad, gY), strokeWidth = 2.5f)

                    // Large white grandpa mustache
                    val gMustache = Path().apply {
                        val mY = cy + 4f
                        moveTo(cx, mY)
                        quadraticTo(cx - 12f, mY - 3f, cx - 22f, mY + 2f)
                        quadraticTo(cx - 10f, mY + 8f, cx, mY)
                        quadraticTo(cx + 10f, mY + 8f, cx + 22f, mY + 2f)
                        quadraticTo(cx + 12f, mY - 3f, cx, mY)
                    }
                    drawPath(path = gMustache, color = Color(0xFFEEEEEE))
                }

                PoutineCategory.GRANDMA -> {
                    // Grandma: White hair bun on top, specs, apron
                    val bunX = cx
                    val bunY = potY
                    drawCircle(color = Color(0xFFE0E0E0), radius = w * 0.15f, center = Offset(bunX, bunY))
                    drawCircle(color = Color.White, radius = w * 0.1f, center = Offset(bunX - 2f, bunY - 2f))

                    // Small spectacles
                    val gY = cy + eyeOffsetY
                    val gSpacing = w * 0.14f
                    val gRad = w * 0.065f
                    drawCircle(color = Color(0xFF1A1A1A), radius = gRad, center = Offset(cx - gSpacing, gY), style = Stroke(width = 2.0f))
                    drawCircle(color = Color(0xFF1A1A1A), radius = gRad, center = Offset(cx + gSpacing, gY), style = Stroke(width = 2.0f))
                    drawLine(color = Color(0xFF1A1A1A), start = Offset(cx - gSpacing + gRad, gY), end = Offset(cx + gSpacing - gRad, gY), strokeWidth = 2.0f)

                    // Apron neck bow
                    val apronY = potY + potatoHeight - 2f
                    val apronWidth = w * 0.35f
                    val apronPath = Path().apply {
                        moveTo(cx - apronWidth / 2, apronY)
                        lineTo(cx + apronWidth / 2, apronY)
                        lineTo(cx + apronWidth / 3, h)
                        lineTo(cx - apronWidth / 3, h)
                        close()
                    }
                    drawPath(path = apronPath, color = Color.White)
                    drawPath(path = apronPath, color = Color(0xFFC62828), style = Stroke(width = 1.5f))
                }

                PoutineCategory.COMBOS -> {
                    // Family Combos: Full Family badge! Let's display clean decorative elements.
                    // Canadian maple leaf emblem at the bottom to represent country pride.
                    val leafY = cy + h * 0.18f
                    val mapleLeafPath = Path().apply {
                        moveTo(cx, leafY - 10f)
                        lineTo(cx + 3f, leafY - 4f)
                        lineTo(cx + 10f, leafY - 5f)
                        lineTo(cx + 6f, leafY)
                        lineTo(cx + 9f, leafY + 6f)
                        lineTo(cx + 3f, leafY + 4f)
                        lineTo(cx, leafY + 9f)
                        lineTo(cx - 3f, leafY + 4f)
                        lineTo(cx - 9f, leafY + 6f)
                        lineTo(cx - 6f, leafY)
                        lineTo(cx - 10f, leafY - 5f)
                        lineTo(cx - 3f, leafY - 4f)
                        close()
                    }
                    drawPath(path = mapleLeafPath, color = Color(0xFFC62828))
                }
            }
        }
    }
}
