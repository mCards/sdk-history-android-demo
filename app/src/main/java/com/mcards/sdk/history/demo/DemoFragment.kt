package com.mcards.sdk.history.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.paging.PagingData
import com.google.android.material.snackbar.Snackbar
import com.mcards.sdk.auth.AuthSdk
import com.mcards.sdk.auth.AuthSdkProvider
import com.mcards.sdk.auth.model.auth.User
import com.mcards.sdk.core.model.AuthTokens
import com.mcards.sdk.history.HistorySdk
import com.mcards.sdk.history.HistorySdkProvider
import com.mcards.sdk.history.HistoryViewModel
import com.mcards.sdk.history.demo.databinding.FragmentDemoBinding
import com.mcards.sdk.history.model.cardactivity.CardActivity

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DemoFragment : Fragment() {

    private var _binding: FragmentDemoBinding? = null
    private val binding get() = _binding!!
    private val historyVM: HistoryViewModel by activityViewModels()

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

        requireActivity().runOnUiThread {
            historyVM.cardActivityResponse.observe(viewLifecycleOwner) { response ->
                response?.getData()?.let {
                    //TODO do something with the card activity
                    val description = it.description //etc
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun initHistorySdk() {
        HistorySdkProvider.getInstance().init(requireContext(),
            accessToken,
            debug = true,
            useFirebase =  false,
            object : HistorySdk.InvalidTokenCallback {
                override fun onTokenInvalid(): String {
                    return AuthSdkProvider.getInstance().refreshTokens().accessToken
                }
            })

        val cardId = "" //TODO get a card (from the cards SDK?) and use its id field here
        historyVM.getHistoryPagingData(cardId)
            .subscribe { cardActivities: PagingData<CardActivity> ->
                //TODO do something with the paginated data
            }

        val activityId = "" //TODO ID of a known card activity associated with cardId
        historyVM.requestCardActivity(cardId, activityId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
