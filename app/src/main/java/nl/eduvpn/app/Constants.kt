/*
 *  This file is part of eduVPN.
 *
 *     eduVPN is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     eduVPN is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with eduVPN.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.eduvpn.app

import android.net.Uri
import java.util.*

/**
 * Contains application-wide constant values.
 * Created by Daniel Zolnai on 2016-09-14.
 */
object Constants {
    @JvmField
    val DEBUG = BuildConfig.BUILD_TYPE.equals("debug", ignoreCase = true)

    @JvmField
    val HELP_URI = Uri.parse("https://www.eduvpn.org/faq.html")

    @JvmField
    val LOCALE = Locale.getDefault()
    val ENGLISH_LOCALE = Locale.ENGLISH

    const val SERVER_LIST_VALID_FOR_MS: Long = 3600000 // 1 hour

    const val CERT_EXPIRY_NOTIFICATION_CHANNEL_ID = "cert_expiry"
    const val CERT_EXPIRY_NOTIFICATION_ID = 1

    const val VPN_CONNECTION_NOTIFICATION_CHANNEL_ID = "vpn_connection"
    const val VPN_CONNECTION_NOTIFICATION_ID = 2
}
