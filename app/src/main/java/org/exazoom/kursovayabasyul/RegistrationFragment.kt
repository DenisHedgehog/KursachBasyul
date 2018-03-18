package org.exazoom.kursovayabasyul

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.*
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.Clients
import org.exazoom.kursovayabasyul.db.ParkingDatabase
import android.widget.Toast
import kotlinx.coroutines.experimental.async
import org.exazoom.kursovayabasyul.db.Cars


//  Фрагмент регистрации для пользователя

class RegistrationFragment : Fragment() {

    private lateinit var dbInstance: ParkingDatabase

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_registration, container, false)!!
        dbInstance = ParkingDatabase.getInstance(activity.applicationContext)!!

        val login = view.findViewById<EditText>(R.id.registration_login_edit_text)
        val fio = view.findViewById<EditText>(R.id.registration_fio_edit_text)
        val address = view.findViewById<EditText>(R.id.registration_address_edit_text)
        val number = view.findViewById<EditText>(R.id.registration_number_edit_text)
        val password = view.findViewById<EditText>(R.id.registration_password_edit_text)
        val repeatPassword = view.findViewById<EditText>(R.id.registration_repeat_password_edit_text)

        val tariffsRg = view.findViewById<RadioGroup>(R.id.tariffs_rg)
        val hourly = view.findViewById<RadioButton>(R.id.hourly_tariff)
        val daily = view.findViewById<RadioButton>(R.id.daily_tariff)

        val carNumber = view.findViewById<EditText>(R.id.car_number)
        val carBrand = view.findViewById<EditText>(R.id.car_brand)
        val carModel = view.findViewById<EditText>(R.id.car_model)
        val carColor = view.findViewById<EditText>(R.id.car_color)

        val createAccount = view.findViewById<Button>(R.id.create_account_id)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button_id)

        val carNumberListener = MaskedTextChangedListener(
                "[A] [000][AA] RUS [009]",
                true,
                carNumber,
                null,
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                        Log.d(MainActivity::class.java.simpleName, extractedValue)
                        Log.d(MainActivity::class.java.simpleName, maskFilled.toString())
                    }
                }
        )

        val telNumberListener = MaskedTextChangedListener(
                "+7 ([000]) [000]-[00]-[00]",
                true,
                number,
                null,
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                        Log.d(MainActivity::class.java.simpleName, extractedValue)
                        Log.d(MainActivity::class.java.simpleName, maskFilled.toString())
                    }
                }
        )

        carNumber.addTextChangedListener(carNumberListener)
        carNumber.onFocusChangeListener = carNumberListener
        carNumber.hint = "Номер: ${carNumberListener.placeholder()}"

        number.addTextChangedListener(telNumberListener)
        number.onFocusChangeListener = telNumberListener
        number.hint = telNumberListener.placeholder()

        var tariff = "hourly"

        createAccount.setOnClickListener {
            when {

                // Проверка введенной информации на корректность

                login.text.toString().length < 3 -> toast("Слишком короткий логин")
                !isUniqueLogin(login.text.toString()) -> toast("Пользователь с таким логином уже существует")
                password.text.toString().length < 6 -> toast("Слишком короткий пароль")
                password.text.toString() != repeatPassword.text.toString() -> toast("Пароли не совпадают")
                fio.text.toString().isEmpty() -> toast("ФИО не должно быть пустым")
                address.text.toString().isEmpty() -> toast("Адрес не должен быть пустым")
                number.text.toString().isEmpty() -> toast("Номер не должен быть пустым")
                !hourly.isChecked && !daily.isChecked -> toast("Не выбран тариф")
                !isUniqueCarNumber(carNumber.text.toString()) -> toast("Машина с таким номером уже зарегистрирована")
                carBrand.text.toString().isEmpty() -> toast("Марка не должна быть пустой")
                carModel.text.toString().isEmpty() -> toast("Модель не должна быть пустой")
                carColor.text.toString().isEmpty() -> toast("Цвет не должен быть пустым")

                // Создание запись машины и пользователя в БД

                else -> {
                    when {
                        hourly.isChecked -> tariff = "hourly"
                        daily.isChecked -> tariff = "daily"
                    }
                    newCar(Cars(carNumber.text.toString(),
                            carBrand.text.toString(),
                            carModel.text.toString(),
                            carColor.text.toString()))
                    newClient(Clients(login = login.text.toString(),
                        fio_client = fio.text.toString(),
                        address = address.text.toString(),
                        tel_client = number.text.toString(),
                        password = password.text.toString(),
                        car = carNumber.text.toString(),
                        tariff = tariff))
                    toast("Аккаунт успешно создан")
                    activity.supportFragmentManager.beginTransaction().replace(R.id.container, LoginFragment()).commit()
                }
            }
        }

        cancelButton.setOnClickListener {

        }

        return view
    }

    private fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun newClient(client: Clients) = runBlocking {
       async { dbInstance.getClientsDao().insert(client) }.await()
    }

    private fun newCar(car: Cars) = runBlocking {
        async { dbInstance.getCarsDao().insert(car) }.await()
    }

    private fun isUniqueLogin(login: String): Boolean = runBlocking {
        async { dbInstance.getClientsDao().getClientByLogin(login).isEmpty() }.await()
    }

    private fun isUniqueCarNumber(carNumber: String): Boolean = runBlocking {
        async { dbInstance.getCarsDao().getCarByNumber(carNumber).isEmpty() }.await()
    }

}
