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
package nl.eduvpn.app.service

import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import nl.eduvpn.app.entity.AddedServers
import nl.eduvpn.app.entity.CertExpiryTimes
import nl.eduvpn.app.entity.CookieAndProfileMapData
import nl.eduvpn.app.entity.CookieAndStringArrayData
import nl.eduvpn.app.entity.CookieAndStringData
import nl.eduvpn.app.entity.CurrentServer
import nl.eduvpn.app.entity.Instance
import nl.eduvpn.app.entity.Organization
import nl.eduvpn.app.entity.OrganizationList
import nl.eduvpn.app.entity.Profile
import nl.eduvpn.app.entity.SerializedVpnConfig
import nl.eduvpn.app.entity.ServerList
import nl.eduvpn.app.entity.Settings
import nl.eduvpn.app.entity.TranslatableString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * This service is responsible for (de)serializing objects used in the app.
 * Created by Daniel Zolnai on 2016-10-12.
 */
class SerializerService {
    class UnknownFormatException internal constructor(throwable: Throwable?) : Exception(throwable)

    @Throws(UnknownFormatException::class)
    fun deserializeProfileList(json: String?): List<Profile> {
        return try {
            jsonSerializer.decodeFromString(ListSerializer(Profile.serializer()), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    /**
     * Serializes an instance to a JSON format.
     *
     * @param instance The instance to serialize.
     * @return The JSON object if the serialization was successful.
     * @throws UnknownFormatException Thrown if there was an error.
     */
    @Throws(UnknownFormatException::class)
    fun serializeInstance(instance: Instance): String {
        return try {
            jsonSerializer.encodeToString(Instance.serializer(), instance)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    /**
     * Deserializes an instance object from a JSON.
     *
     * @param json The JSON object to parse.
     * @return The instance as a POJO.
     * @throws UnknownFormatException Thrown when the format was not as expected.
     */
    @Throws(UnknownFormatException::class)
    fun deserializeInstance(json: String?): Instance {
        return try {
            jsonSerializer.decodeFromString(Instance.serializer(), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    /**
     * Deserializes the app settings from JSON to POJO.
     *
     * @param jsonObject The json containing the settings.
     * @return The settings as an object.
     * @throws UnknownFormatException Thrown if there was a problem while parsing the JSON.
     */
    @Throws(UnknownFormatException::class)
    fun deserializeAppSettings(jsonObject: JSONObject): Settings {
        return try {
            var useCustomTabs = Settings.USE_CUSTOM_TABS_DEFAULT_VALUE
            if (jsonObject.has("use_custom_tabs")) {
                useCustomTabs = jsonObject.getBoolean("use_custom_tabs")
            }
            var preferTcp = Settings.PREFER_TCP_DEFAULT_VALUE
            if (jsonObject.has("prefer_tcp")) {
                preferTcp = jsonObject.getBoolean("prefer_tcp")
            }
            Settings(useCustomTabs, preferTcp)
        } catch (ex: JSONException) {
            throw UnknownFormatException(ex)
        }
    }

    /**
     * Serializes the app settings to JSON.
     *
     * @param settings The settings to serialize.
     * @return The app settings in a JSON format.
     * @throws UnknownFormatException Thrown if there was an error while deserializing.
     */
    @Throws(UnknownFormatException::class)
    fun serializeAppSettings(settings: Settings): JSONObject {
        val result = JSONObject()
        return try {
            result.put("use_custom_tabs", settings.useCustomTabs())
            result.put("prefer_tcp", settings.preferTcp())
            result
        } catch (ex: JSONException) {
            throw UnknownFormatException(ex)
        }
    }

    /**
     * Deserializes a list of organizations.
     *
     * @param json The json to deserialize from.
     * @return The list of organizations servers created from the JSON.
     * @throws UnknownFormatException Thrown if there was an error while deserializing.
     */
    @Throws(UnknownFormatException::class)
    fun deserializeOrganizationList(json: String?): OrganizationList {
        return try {
            jsonSerializer.decodeFromString(OrganizationList.serializer(), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    /**
     * Deserializes a list of secure internet / institute access servers.
     *
     * @param json The json to deserialize from.
     * @return The list of servers created from the JSON.
     * @throws UnknownFormatException Thrown if there was an error while deserializing.
     */
    @Throws(UnknownFormatException::class)
    fun deserializeServerList(json: String?): ServerList {
        return try {
            jsonSerializer.decodeFromString(ServerList.serializer(), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    @Throws(UnknownFormatException::class)
    fun deserializeCookieAndStringData(json: String?): CookieAndStringData {
        return try {
            jsonSerializer.decodeFromString(CookieAndStringData.serializer(), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    @Throws(UnknownFormatException::class)
    fun deserializeCookieAndStringArrayData(json: String?): CookieAndStringArrayData {
        return try {
            jsonSerializer.decodeFromString(CookieAndStringArrayData.serializer(), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    @Throws(UnknownFormatException::class)
    fun deserializeCookieAndCookieAndProfileListData(json: String?): CookieAndProfileMapData {
        return try {
            jsonSerializer.decodeFromString(CookieAndProfileMapData.serializer(), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    @Throws(UnknownFormatException::class)
    fun deserializeAddedServers(json: String?): AddedServers {
        return try {
            jsonSerializer.decodeFromString(AddedServers.serializer(), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    @Throws(UnknownFormatException::class)
    fun deserializeSerializedVpnConfig(json: String?): SerializedVpnConfig {
        return try {
            jsonSerializer.decodeFromString(SerializedVpnConfig.serializer(), json!!)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    @Throws(UnknownFormatException::class)
    fun deserializeCurrentServer(json: String): CurrentServer {
        return try {
            jsonSerializer.decodeFromString(CurrentServer.serializer(), json)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    @Throws(UnknownFormatException::class)
    fun deserializeCertExpiryTimes(json: String): CertExpiryTimes {
        return try {
            jsonSerializer.decodeFromString(CertExpiryTimes.serializer(), json)
        } catch (ex: SerializationException) {
            throw UnknownFormatException(ex)
        }
    }

    companion object {
        private val API_DATE_FORMAT: DateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        private val jsonSerializer: Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}