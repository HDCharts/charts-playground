package testing

import androidx.compose.runtime.Composer
import androidx.compose.runtime.currentComposer
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.math.ceil

private const val STYLE_PACKAGE = "io.github.hdcharts.charts.style"

/** Reads a dotted property path from a library style object through its backing fields. */
fun readPath(
    root: Any,
    path: String,
): Any? =
    path.split('.').fold(root as Any?) { current, name ->
        current?.let { target ->
            val field =
                generateSequence(target.javaClass) { it.superclass }.firstNotNullOf { type ->
                    type.declaredFields.firstOrNull { it.name == name }
                }
            field.isAccessible = true
            field.get(target)
        }
    }

/** The instance fields of a library style object, by name. */
fun styleFields(style: Any): Map<String, Any?> =
    style.javaClass.declaredFields
        .filterNot { Modifier.isStatic(it.modifiers) || it.isSynthetic }
        .associate { field ->
            field.isAccessible = true
            field.name to field.get(style)
        }

/**
 * Calls `<owner>.<name>()` with every argument left at its default, as generated code does for the
 * members it does not write. Handles both plain and `@Composable` factories.
 */
fun callStyleFactory(
    owner: String,
    name: String,
): Any? {
    val ownerClass = Class.forName("$STYLE_PACKAGE.$owner")
    val instance = ownerClass.getField("INSTANCE").get(null)

    // Functions taking value classes (Color, Dp) have mangled names, e.g. grid-abc123.
    fun Method.isFactory(suffix: String = "") =
        this.name == "$name$suffix" || (this.name.startsWith("$name-") && this.name.endsWith(suffix))
    val composable =
        ownerClass.methods.firstOrNull { method ->
            method.isFactory() && method.parameterTypes.any { it == Composer::class.java }
        }
    if (composable != null) return readComposable { invokeComposable(composable, instance, currentComposer) }
    val withDefaults =
        ownerClass.declaredMethods.firstOrNull { it.isFactory("\$default") }
            ?: ownerClass.methods.firstOrNull { it.isFactory() && it.parameterCount == 0 }
            ?: error("$owner has no factory named '$name'")
    withDefaults.isAccessible = true
    if (withDefaults.parameterCount == 0) return withDefaults.invoke(instance)
    // Static bridge: (receiver, params…, default masks…, marker).
    val types = withDefaults.parameterTypes
    val params = types.size - 2 - maskCount(types.size - 2)
    val args =
        types.mapIndexed { index, type ->
            when {
                index == 0 -> instance
                index <= params -> zeroOf(type)
                index < types.size - 1 -> -1
                else -> null
            }
        }
    return withDefaults.invoke(null, *args.toTypedArray())
}

/** Composable factories take (params…, Composer, changed…, default…) and apply defaults themselves. */
private fun invokeComposable(
    method: Method,
    instance: Any,
    composer: Composer,
): Any? {
    val types = method.parameterTypes
    val composerIndex = types.indexOf(Composer::class.java)
    val changedInts = ceil((composerIndex + 1) / 10.0).toInt()
    val args =
        types.mapIndexed { index, type ->
            when {
                index < composerIndex -> zeroOf(type)
                index == composerIndex -> composer
                index <= composerIndex + changedInts -> 0
                else -> -1
            }
        }
    return method.invoke(instance, *args.toTypedArray())
}

/** Number of default-mask ints for a `$default` bridge with [slots] params plus masks. */
private fun maskCount(slots: Int): Int = generateSequence(1) { it + 1 }.first { masks -> (slots - masks) <= masks * 31 }

private fun zeroOf(type: Class<*>): Any? =
    when (type) {
        java.lang.Boolean.TYPE -> false
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Float.TYPE -> 0f
        java.lang.Double.TYPE -> 0.0
        java.lang.Short.TYPE -> 0.toShort()
        java.lang.Byte.TYPE -> 0.toByte()
        java.lang.Character.TYPE -> 0.toChar()
        else -> null
    }
