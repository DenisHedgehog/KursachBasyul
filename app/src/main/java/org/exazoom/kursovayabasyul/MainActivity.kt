package org.exazoom.kursovayabasyul

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.exazoom.kursovayabasyul.db.ParkingDatabase
import org.exazoom.kursovayabasyul.utils.*


//  Главная активность

class MainActivity : AppCompatActivity() {

    // Экземпляр БД
    private lateinit var dbInstance: ParkingDatabase

    // Слушатель для нижнего меню
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.profile -> {
                when {
                    isActiveEmpExist(this) -> {
                        Log.i("EMP IS ACTIVE", "${isActiveEmpExist(this)}")
                        supportFragmentManager.beginTransaction().replace(R.id.container, EmpProfileFragment()).commit()
                    }
                    else -> supportFragmentManager.beginTransaction().replace(R.id.container, ProfileFragment()).commit()
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.parking -> {
                when {
                    isActiveEmpExist(this) -> {
                        supportFragmentManager.beginTransaction().replace(R.id.container, MovingsFragment()).commit()
                        Log.i("EMP IS ACTIVE", "MovingsFragment was activated")
                    }
                        (getActiveUser(this) == "admin") ->
                        supportFragmentManager.beginTransaction().replace(R.id.container, AdminParkingFragment()).commit()
                    else -> supportFragmentManager.beginTransaction().replace(R.id.container, ParkingFragment()).commit()
                }

                return@OnNavigationItemSelectedListener true
            }
            R.id.employees -> {
                supportFragmentManager.beginTransaction().replace(R.id.container, EmployeeFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    // Создание главной активности
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        dbInstance = ParkingDatabase.getInstance(applicationContext)!!
        navigation.menu.getItem(2).isEnabled = false
        Log.i("MainActivity", "ACTIVE EMP is ${getActiveEmp(this)}")
        when (isActiveEmpExist(this)) {
            true -> {
                navigation.selectedItemId = R.id.parking
                navigation.menu.getItem(2).isEnabled = false
            }
            false -> {
                Log.i("MainActivity", "before when user is ${isActiveUserExist(this)}")
                when (isActiveUserExist(this)) {
                    true -> {
                        Log.i("MainActivity", "after when TRUE user is ${isActiveUserExist(this)}")
                        navigation.menu.getItem(2).isEnabled = getActiveUser(this) == "admin"
                        navigation.selectedItemId = R.id.parking
                    }
                    false -> {
                        Log.i("MainActivity", "after when FALSE user is ${isActiveUserExist(this)}")
                        navigation.visibility = View.INVISIBLE
                        supportFragmentManager.beginTransaction().replace(R.id.container, LoginFragment()).commit()
                    }
                }
            }
        }
    }

    companion object {
        val instance = MainActivity()
    }

}
