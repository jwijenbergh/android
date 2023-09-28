/*
 * This file is part of eduVPN.
 *
 * eduVPN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * eduVPN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with eduVPN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.eduvpn.app.viewmodel

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wireguard.config.Config
import de.blinkt.openvpn.VpnProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.eduvpn.app.R
import nl.eduvpn.app.entity.*
import nl.eduvpn.app.entity.v3.ProfileV3API
import nl.eduvpn.app.livedata.toSingleEvent
import nl.eduvpn.app.service.*
import nl.eduvpn.app.service.SerializerService.UnknownFormatException
import nl.eduvpn.app.utils.FormattingUtils
import nl.eduvpn.app.utils.Log
import nl.eduvpn.app.utils.runCatchingCoroutine
import org.eduvpn.common.Protocol
import java.io.BufferedReader
import java.io.StringReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws

/**
 * This viewmodel takes care of the entire flow, from connecting to the servers to fetching profiles.
 */
abstract class BaseConnectionViewModel(
    private val context: Context,
    private val apiService: APIService,
    private val backendService: BackendService,
    private val serializerService: SerializerService,
    private val historyService: HistoryService,
    private val preferencesService: PreferencesService,
    private val eduVpnOpenVpnService: EduVPNOpenVPNService,
    private val vpnConnectionService: VPNConnectionService,
) : ViewModel() {

    sealed class ParentAction {
        data class DisplayError(@StringRes val title: Int, val message: String) : ParentAction()
        data class OpenProfileSelector(val profiles: List<ProfileV3API>) : ParentAction()
        data class ConnectWithConfig(val vpnConfig: VPNConfig) : ParentAction()
    }

    val connectionState =
        MutableLiveData<ConnectionState>().also { it.value = ConnectionState.Ready }

    val warning = MutableLiveData<String>()

    val _parentAction = MutableLiveData<ParentAction?>()
    val parentAction = _parentAction.toSingleEvent()

    fun discoverApi(instance: Instance) {
        // If no discovered API, fetch it first, then initiate the connection for the login
        connectionState.value = ConnectionState.DiscoveringApi
        // Discover the API
        viewModelScope.launch(Dispatchers.IO) {
            runCatchingCoroutine {
                backendService.addServer(instance)
            }.onSuccess {
                getProfiles(instance)
            }.onFailure { throwable ->
                Log.e(TAG, "Error while fetching discovered API.", throwable)
                connectionState.postValue(ConnectionState.Ready)
                _parentAction.postValue(ParentAction.DisplayError(
                    R.string.error_dialog_title,
                    context.getString(
                        R.string.error_discover_api,
                        instance.sanitizedBaseURI,
                        throwable.toString()
                    )
                ))
            }
        }
    }

    fun getProfiles(instance: Instance) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = backendService.getConfig(instance, false)
                val vpnConfig = parseConfig(instance, backendService.lastSelectedProfile, result)
                preferencesService.setCurrentProtocol(result.protocol)
                _parentAction.postValue(ParentAction.ConnectWithConfig(vpnConfig))
            } catch (ex: Exception) {
                _parentAction.postValue(ParentAction.DisplayError(R.string.error_downloading_vpn_config, ex.toString()))
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun parseConfig(instance: Instance, lastSelectedProfileId: String?, result: SerializedVpnConfig): VPNConfig {
        return if (result.protocol == Protocol.OpenVPN.nativeValue) {
            val configName = FormattingUtils.formatProfileName(
                context,
                instance,
                lastSelectedProfileId
            )
            eduVpnOpenVpnService.importConfig(
                result.config,
                configName,
            )?.let {
                VPNConfig.OpenVPN(it)
            } ?:  throw IllegalArgumentException("Unable to parse profile")
        } else if (result.protocol == Protocol.WireGuard.nativeValue) {
            return VPNConfig.WireGuard(Config.parse(BufferedReader(StringReader(result.config))))
        } else {
            throw IllegalArgumentException("Unexpected protocol type: ${result.protocol}")
        }
    }

    public fun selectProfileToConnectTo(profile: ProfileV3API) : Result<Unit> {
        backendService.selectProfile(profile)
        return Result.success(Unit)
    }

    open fun onResume() {
        if (connectionState.value == ConnectionState.Authorizing) {
            connectionState.value = ConnectionState.Ready
        }
    }

    private fun <T> showError(thr: Throwable?, resourceId: Int): Result<T> {
        val message = context.getString(resourceId, thr)
        Log.e(TAG, message, thr)
        connectionState.value = ConnectionState.Ready
        _parentAction.value = ParentAction.DisplayError(
            R.string.error_dialog_title,
            message
        )
        return Result.failure(thr ?: RuntimeException(message))
    }


    private fun getExpiryFromHeaders(headers: Map<String, List<String>>): Date? {
        return headers["Expires"]
            ?.let { hl: List<String> -> hl.firstOrNull() }
            ?.let { expiredValue ->
                try {
                    SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(
                        expiredValue
                    )
                } catch (ex: ParseException) {
                    Log.e(TAG, "Unable to parse expired header", ex)
                    null
                }
            }
    }

    fun disconnectWithCall(vpnService: VPNService) {
        vpnConnectionService.disconnect(context, vpnService)
    }

    fun deleteAllDataForInstance(instance: Instance) {
        historyService.removeAllDataForInstance(instance)
    }

    fun getProfileInstance(): Instance {
        return preferencesService.getCurrentInstance()!!
    }

    fun connectionToConfig(activity: Activity, vpnConfig: VPNConfig): VPNService {
        connectionState.value = ConnectionState.Ready
        return vpnConnectionService.connectionToConfig(viewModelScope, activity, vpnConfig)
    }

    companion object {
        private val TAG = BaseConnectionViewModel::class.java.name
    }

}
