package util

object StringUtil {

    fun stripNonDigits(input: CharSequence): String {
        val sb = StringBuilder(
            input.length /* also inspired by seh's comment */
        )
        for (i in 0 until input.length) {
            val c = input[i]
            if (c.toInt() > 47 && c.toInt() < 58) {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}