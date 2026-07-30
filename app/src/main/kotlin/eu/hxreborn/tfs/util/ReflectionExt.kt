package eu.hxreborn.tfs.util

import android.os.Build
import java.lang.reflect.Field
import java.lang.reflect.Method

fun Class<*>.findFieldUpward(name: String): Field? =
    generateSequence(this) { it.superclass }.firstNotNullOfOrNull { cls ->
        runCatching { cls.getDeclaredField(name).apply { isAccessible = true } }.getOrNull()
    }

fun Class<*>.findMethodUpward(
    name: String,
    vararg paramTypes: Class<*>,
): Method? =
    generateSequence(this) { it.superclass }.firstNotNullOfOrNull { cls ->
        runCatching {
            cls
                .getDeclaredMethod(
                    name,
                    *paramTypes,
                ).apply { isAccessible = true }
        }.getOrNull()
    }

// arg counts drift across API levels
fun Class<*>.findMethodUpwardOrWidest(
    name: String,
    vararg paramTypes: Class<*>,
): Method? {
    findMethodUpward(name, *paramTypes)?.let { return it }
    val widest =
        findAllMethodsUpward(name)
            .filter { method ->
                paramTypes.all { param ->
                    method.parameterTypes.any { it.isAssignableFrom(param) }
                }
            }.maxByOrNull { it.parameterCount } ?: return null
    Logger.warn(
        "$name arg count drift expected=${paramTypes.size} " +
            "actual=${widest.parameterCount} sdk=${Build.VERSION.SDK_INT}",
    )
    return widest
}

fun Class<*>.findAllMethodsUpward(name: String): List<Method> =
    generateSequence(this) { it.superclass }
        .flatMap { cls ->
            cls.declaredMethods.filter { it.name == name }.onEach { it.isAccessible = true }
        }.toList()

fun Class<*>.methodAccessible(
    name: String,
    vararg paramTypes: Class<*>,
): Method = getDeclaredMethod(name, *paramTypes).apply { isAccessible = true }

// framework class names split by API level
fun ClassLoader.anyClassFromNames(vararg names: String): Class<*> {
    names.forEach { name ->
        runCatching { loadClass(name) }.getOrNull()?.let { return it }
    }
    error("no class from ${names.toList()} sdk=${Build.VERSION.SDK_INT}")
}

fun Any.readField(name: String): Any? = javaClass.findFieldUpward(name)?.get(this)

internal fun Method.signature(): String =
    buildString {
        append(declaringClass.name)
        append('#')
        append(name)
        append('(')
        append(parameterTypes.joinToString(",") { it.simpleName ?: it.name })
        append(')')
    }
