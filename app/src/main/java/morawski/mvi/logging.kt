package morawski.mvi

import android.util.Log

// Utility functions to facilitate logging and debugging

/**
 * Represents given collection in a simplified manner,
 * only printing the number of elements, its head and tail.
 *
 * Utility method for debugging purposes (printing large collections in their entirety isn't useful)
 */
inline fun <T> Collection<T>.toAbbreviatedString() =
        "$size elements" + when {
            size > 2 -> " (from ${this.first()} to ${this.last()})"
            size > 1 -> " (${this.first()} and ${this.last()})"
            size == 1 -> " (${this.first()})"
            size == 0 -> ""
            else -> error("Negative size of a collection is not handled") // (not impossible with a custom Collection!)
        }

/**
 * Executes a logging function conditionally (only if [BuildConfig.DEBUG] is true)
 */
private inline fun (() -> String).handle(tag: String, loggingFunction: (String, String) -> Unit) {
    if (BuildConfig.DEBUG) {
        loggingFunction(tag, this())
    }
}

/**
 * Produces a readable, prefixed log tag for a given object
 *
 * To be used with [logDebug], [logWarning] and [logError].
 *
 * @receiver the object in which the logging call is executed
 * @param method name of the method in which the logging call is executed (to be specified manually)
 */
fun Any.inMethod(method: String) = "AppTag | ${javaClass.simpleName}.$method"

/**
 * Forwards a debug call to [android.util.Log] if [BuildConfig.DEBUG] is true.
 * Note that the message is evaluated lazily, so there's no performance impact in release mode.
 */
fun logDebug(tag: String, message: () -> String) = message.handle(tag) { tag, message -> Log.d(tag, message()) }

/**
 * Forwards a warning call to [android.util.Log] if [BuildConfig.DEBUG] is true.
 * Note that the message is evaluated lazily, so there's no performance impact in release mode.
 */
fun logWarning(tag: String, message: () -> String) = message.handle(tag) { tag, message -> Log.w(tag, message()) }

/**
 * Forwards an error call to [android.util.Log] if [BuildConfig.DEBUG] is true.
 * Note that the message is evaluated lazily, so there's no performance impact in release mode.
 */
fun logError(tag: String, message: () -> String) = message.handle(tag) { tag, message -> Log.e(tag, message()) }