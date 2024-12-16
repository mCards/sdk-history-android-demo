package com.mcards.sdk.history.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.mcards.sdk.auth.AuthSdk
import com.mcards.sdk.auth.AuthSdkProvider
import com.mcards.sdk.auth.model.auth.User
import com.mcards.sdk.core.model.AuthTokens
import com.mcards.sdk.core.network.SdkResult
import com.mcards.sdk.history.HistorySdk
import com.mcards.sdk.history.HistorySdkProvider
import com.mcards.sdk.history.demo.databinding.FragmentDemoBinding
import com.mcards.sdk.history.model.cardactivity.CardActivitiesResponse
import com.mcards.sdk.history.model.cardactivity.CardActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DemoFragment : Fragment() {

    private var _binding: FragmentDemoBinding? = null
    private val binding get() = _binding!!

    private var userPhoneNumber = ""
    private var accessToken = ""
    private var idToken = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDemoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authSdk = AuthSdkProvider.getInstance()


        val loginCallback = object : AuthSdk.LoginCallback {
            override fun onSuccess(
                user: User,
                tokens: AuthTokens,
                regionChanged: Boolean,
                cardId: String?
            ) {
                accessToken = tokens.accessToken
                idToken = tokens.idToken
                userPhoneNumber = user.userClaim.phoneNumber
                initHistorySdk()
            }

            override fun onFailure(message: String) {
                Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
            }
        }

        binding.loginBtn.setOnClickListener {
            if (userPhoneNumber.isBlank()) {
                authSdk.login(requireContext(), loginCallback)
            } else {
                authSdk.login(requireContext(), userPhoneNumber, loginCallback)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun initHistorySdk() {
        val sdk = HistorySdkProvider.getInstance()

        sdk.init(requireContext(),
            accessToken,
            debug = true,
            useFirebase =  false,
            object : HistorySdk.InvalidTokenCallback {
                override fun onTokenInvalid(): String {
                    return AuthSdkProvider.getInstance().refreshTokens().accessToken
                }
            })

        val cardId = "" //TODO get a card (from the cards SDK?) and use its id field here
        sdk.getPaginatedCardActivities("", 1, 20)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<CardActivitiesResponse>> {
                override fun onSubscribe(d: Disposable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: Throwable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                        Snackbar.make(requireView(), e.message!!, LENGTH_LONG).show()
                    }
                }

                override fun onSuccess(t: SdkResult<CardActivitiesResponse>) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                    }

                    t.result?.let {
                        val activity = it.cardActivities?.get(0)
                    } ?: t.errorMsg?.let {
                        activity?.runOnUiThread {
                            Snackbar.make(requireView(), it, LENGTH_LONG).show()
                        }
                    }
                }
            })

        val activityId = "" //TODO ID of a known card activity associated with cardId
        sdk.getCardActivity(cardId, activityId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<CardActivity>> {
                override fun onSubscribe(d: Disposable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: Throwable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                        Snackbar.make(requireView(), e.message!!, LENGTH_LONG).show()
                    }
                }

                override fun onSuccess(t: SdkResult<CardActivity>) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                    }

                    t.result?.let {
                        val activity = it
                        //TODO take some action with the CardActivity
                    } ?: t.errorMsg?.let {
                        activity?.runOnUiThread {
                            Snackbar.make(requireView(), it, LENGTH_LONG).show()
                        }
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
