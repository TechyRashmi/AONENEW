package com.vendorsapp.activities.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blucor.aoneenterprises.LoginResponse
import com.blucor.aoneenterprises.main.RequestLogin

import com.vendorsapp.network.Coroutines


class AuthVM(
    private val repository: AuthRepo
) : ViewModel() {

    var loginResponse = MutableLiveData<LoginResponse>()
    fun login(request: RequestLogin) {
        Coroutines.main {
            loginResponse.postValue(repository.login(request))
        }
    }


    /*var driverLoginRes = MutableLiveData<DriverLoginResponse>()
    fun driverLogin(request: LoginRequest) {
        Coroutines.main {
            driverLoginRes.postValue(repository.driverLogin(request))
        }
    }*/


    /*  var resentOtpRes = MutableLiveData<OTPResendResponse>()
      fun otpResend() {
          Coroutines.main {
              resentOtpRes.postValue(repository.resendOTP())
          }
      }

      var verifyOTPRes = MutableLiveData<OTPVerifyResponse>()
      fun verifyOTP(request: OTPRequest) {
          Coroutines.main {
              verifyOTPRes.postValue(repository.otpVerify(request))
          }
      }
  */
    /*var forgetPasswordResponse = MutableLiveData<ForgetPasswordResponse>()
    fun forgetPassword(request: String) {
        Coroutines.main {
            forgetPasswordResponse.postValue(repository.forgetPassword(request))
        }
    }*/

    /*  var changePasswordResponse = MutableLiveData<ChangePasswordResponse>()
      fun changePassword(request: ChangePasswordRequest) {
          Coroutines.main {
              changePasswordResponse.postValue(repository.changePassword(request))
          }
      }*/
}
