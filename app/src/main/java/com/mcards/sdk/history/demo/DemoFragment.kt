package com.mcards.sdk.history.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.mcards.sdk.auth.AuthSdk
import com.mcards.sdk.auth.AuthSdkProvider
import com.mcards.sdk.auth.model.auth.User
import com.mcards.sdk.cards.CardsSdk
import com.mcards.sdk.cards.CardsSdkProvider
import com.mcards.sdk.core.model.AuthTokens
import com.mcards.sdk.core.model.card.Card
import com.mcards.sdk.core.network.SdkResult
import com.mcards.sdk.history.HistorySdk
import com.mcards.sdk.history.HistorySdkProvider
import com.mcards.sdk.history.demo.databinding.FragmentDemoBinding
import com.mcards.sdk.history.model.historyactivity.HistoryActivitiesResponse
import com.mcards.sdk.history.model.historyactivity.HistoryActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DemoFragment : Fragment() {

    private var _binding: FragmentDemoBinding? = null
    private val binding get() = _binding!!
    private val sdk = HistorySdkProvider.getInstance()

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
                initSdks()
            }

            override fun onFailure(message: String) {
                Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
            }
        }

        val authSdk = AuthSdkProvider.getInstance()
        binding.loginBtn.setOnClickListener {
            if (userPhoneNumber.isBlank()) {
                authSdk.login(requireContext(), loginCallback)
            } else {
                authSdk.login(requireContext(), userPhoneNumber, loginCallback)
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun initSdks() {
        sdk.init(requireContext(),
            accessToken,
            debug = true,
            useFirebase =  false,
            object : HistorySdk.InvalidTokenCallback {
                override fun onTokenInvalid(): String {
                    return AuthSdkProvider.getInstance().refreshTokens().accessToken
                }
            })

        CardsSdkProvider.getInstance().init(requireActivity(),
            accessToken,
            debug = true,
            useFirebase =  false,
            object : CardsSdk.InvalidTokenCallback {
                override fun onTokenInvalid(): String {
                    return AuthSdkProvider.getInstance().refreshTokens().accessToken
                }
            })

        getCards()
    }

    @SuppressLint("CheckResult")
    private fun getCards() {
        CardsSdkProvider.getInstance().getCardsList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<List<Card>>> {
                override fun onSubscribe(d: Disposable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: Throwable) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                        Snackbar.make(
                            requireView(),
                            e.localizedMessage!!,
                            LENGTH_LONG
                        ).show()
                    }
                }

                override fun onSuccess(t: SdkResult<List<Card>>) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                    }
                    t.result?.let {
                        if (it.isNotEmpty()) {
                            val card = it[0]
                            getActivities(card.uuid!!)
                        }
                    } ?: t.errorMsg?.let {
                        activity?.runOnUiThread {
                            Snackbar.make(requireView(), it, LENGTH_LONG)
                                .show()
                        }
                    }
                }
            })
    }

    @SuppressLint("CheckResult")
    private fun getActivities(cardId: String) {
        sdk.getPaginatedActivities(cardId, 1, 20)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<HistoryActivitiesResponse>> {
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

                override fun onSuccess(t: SdkResult<HistoryActivitiesResponse>) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                    }

                    t.result?.let { result ->
                        result.cardActivities?.get(0)?.let {
                            getActivity(cardId, it.uuid!!)
                        }
                    } ?: t.errorMsg?.let {
                        activity?.runOnUiThread {
                            Snackbar.make(requireView(), it, LENGTH_LONG).show()
                        }
                    }
                }
            })
    }

    @SuppressLint("CheckResult")
    private fun getActivity(cardId: String, activityId: String) {
        sdk.getActivity(cardId, activityId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<HistoryActivity>> {
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

                override fun onSuccess(t: SdkResult<HistoryActivity>) {
                    activity?.runOnUiThread {
                        binding.progressbar.visibility = View.GONE
                    }

                    t.result?.let {
                        val HistoryActivity = it
                        activity?.runOnUiThread {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Success")
                                .setMessage("Successfully fetched HistoryActivity with ID "
                                        + HistoryActivity.uuid + ". Debug to inspect the data.")
                                .setPositiveButton("Ok") { dialog, _ ->
                                    dialog.dismiss()
                                }.create().show()
                        }
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
