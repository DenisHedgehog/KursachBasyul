package org.exazoom.kursovayabasyul


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.Cars
import org.exazoom.kursovayabasyul.db.Clients
import org.exazoom.kursovayabasyul.db.ParkingDatabase
import org.exazoom.kursovayabasyul.utils.getActiveUser
import org.exazoom.kursovayabasyul.utils.removeActiveUser


//  Фрагмент профиля для пользователя

class ProfileFragment : Fragment() {

    private lateinit var dbInstance: ParkingDatabase

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_profile, container, false)!!
        dbInstance = ParkingDatabase.getInstance(context)!!

        val user = getUser()
        val car = getCar(user.car!!)

        view.profile_login.setText(user.login, TextView.BufferType.EDITABLE)
        view.profile_fio.setText(user.fio_client, TextView.BufferType.EDITABLE)
        view.profile_address.setText(user.address, TextView.BufferType.EDITABLE)
        view.profile_number.setText(user.tel_client, TextView.BufferType.EDITABLE)
        view.profile_car_number.setText(car.numb_car, TextView.BufferType.EDITABLE)
        view.profile_car_brand.setText(car.brand, TextView.BufferType.EDITABLE)
        view.profile_car_model.setText(car.model, TextView.BufferType.EDITABLE)
        view.profile_car_color.setText(car.color, TextView.BufferType.EDITABLE)

        when (user.tariff) {
            "hourly" -> view.profile_hourly_tariff.isChecked = true
            "daily" -> view.profile_daily_tariff.isChecked = true
        }

        view.change_info.setOnClickListener {
            user.address = view.profile_address.text.toString()
            user.tel_client = view.profile_number.text.toString()
            when {
                view.profile_hourly_tariff.isChecked -> user.tariff = "hourly"
                view.profile_daily_tariff.isChecked -> user.tariff = "daily"
            }
            changeClientInfo(user)

            car.color = view.profile_car_color.text.toString()
            changeCar(car)
            toast("Информация изменена")
        }

        view.change_password.setOnClickListener {
            when {
                view.profile_old_password.text.toString() != user.password -> toast("Введён неверный пароль")
                view.profile_new_password.text.toString().length < 6 -> toast("Слишком короткий пароль")
                view.profile_new_password.text.toString() != view.profile_repeat_new_password.text.toString() -> toast("Пароли не совпадают")
                else -> {
                    user.password = view.profile_new_password.text.toString()
                    changePassword(user)
                    toast("Пароль успешно изменён")
                }
            }
        }

        view.log_out.setOnClickListener {
            removeActiveUser(activity)
            activity.navigation.visibility = View.INVISIBLE
            activity.supportFragmentManager.beginTransaction().replace(R.id.container, LoginFragment()).commit()
        }

        return view
    }

    private fun getUser(): Clients = runBlocking {
        async { dbInstance.getClientsDao().getClientByLogin(getActiveUser(activity)!!).first() }.await()
    }

    private fun changePassword(user: Clients) = runBlocking {
        async { dbInstance.getClientsDao().update(user) }.await()
    }

    private fun getCar(carNumber: String): Cars = runBlocking {
        async { dbInstance.getCarsDao().getCarByNumber(carNumber).first() }.await()
    }

    private fun changeCar(car: Cars) = runBlocking {
        async { dbInstance.getCarsDao().update(car) }.await()
    }

    private fun toast(text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

    private fun changeClientInfo(client: Clients) = runBlocking {
        async { dbInstance.getClientsDao().update(client) }.await()
    }

}
