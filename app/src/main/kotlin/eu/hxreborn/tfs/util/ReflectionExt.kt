package eu.hxreborn.tfs.util

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

fun Class<*>.findAllMethodsUpward(name: String): List<Method> =
    generateSequence(this) { it.superclass }
        .flatMap { cls ->
            cls.declaredMethods.filter { it.name == name }.onEach { it.isAccessible = true }
        }.toList()

fun Class<*>.methodAccessible(
    name: String,
    vararg paramTypes: Class<*>,
): Method = getDeclaredMethod(name, *paramTypes).apply { isAccessible = true }

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
