package codegen.common

import domain.ColorValue

internal fun colorLiteral(color: ColorValue): String =
    "Color(0x${color.argb.toString(16).uppercase().padStart(length = 8, padChar = '0')})"
