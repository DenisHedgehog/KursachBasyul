package org.exazoom.kursovayabasyul.db

import android.arch.persistence.room.*
import android.arch.persistence.room.Database
import android.content.Context
import java.util.*
import android.arch.persistence.room.Room


// Создание класса, с помощью которого будем получать доступ к БД
@Database(entities = [Clients::class, Cars::class, Employees::class, Tariffs::class, Movings::class, Count::class], version = 6)
@TypeConverters(MyDateConverter::class)
abstract class ParkingDatabase : RoomDatabase() {
    companion object {
        private val DB_NAME = "parkingDatabase.db"
        @Volatile
        private var instance: ParkingDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ParkingDatabase? {
            if (instance == null) {
                instance = create(context)
            }
            return instance
        }

        private fun create(context: Context): ParkingDatabase {
            return Room.databaseBuilder(
                    context,
                    ParkingDatabase::class.java,
                    DB_NAME).fallbackToDestructiveMigration().build()
        }

    }
    abstract fun getClientsDao(): ClientsDao
    abstract fun getCarsDao(): CarsDao
    abstract fun getEmployeesDao(): EmployeesDao
    abstract fun getTariffsDao(): TariffsDao
    abstract fun getCountDao(): CountDao
    abstract fun getMovingsDao(): MovingsDao
}



//  Описания таблиц и создание интерфейса для доступа к ним

@Entity
data class Employees(
        @PrimaryKey val emp_key: String,
        val fio_emp: String,
        var tel_emp: String,
        var adr_emp: String)

@Dao
interface EmployeesDao {
    @Query("SELECT* FROM Employees")
    fun getAllEmployees(): Array<Employees>

    @Query("SELECT* FROM Employees WHERE emp_key = :key")
    fun getEmployByKey(key: String): Array<Employees>

    @Insert
    fun insert(employees: Employees)

    @Delete
    fun delete(employees: Employees)

    @Update
    fun update(employees: Employees)
}

@Entity(indices = [Index(value = ["login"], unique = true)],
        foreignKeys = [(ForeignKey(entity = Cars::class, parentColumns = ["numb_car"], childColumns = ["car"]))])
data class Clients(
        @PrimaryKey(autoGenerate = true) val id_client: Int = 0,
        val login: String,
        var password: String,
        val fio_client: String,
        var tel_client: String,
        var address: String?,
        val car: String?,
        var tariff: String = "hourly")

@Dao
interface ClientsDao {
    @Query("SELECT* FROM Clients")
    fun getAllClients(): Array<Clients>

    @Query("SELECT* FROM Clients WHERE login = :login")
    fun getClientByLogin(login: String): Array<Clients>

    @Query("SELECT* FROM Clients WHERE id_client = :id_client")
    fun getClientById(id_client: Int): Array<Clients>

    @Insert
    fun insert(clients: Clients)

    @Delete
    fun delete(clients: Clients)

    @Update
    fun update(clients: Clients)
}

@Entity(primaryKeys = ["numb_car", "id_parking"])
data class Parkings(
        val id_client: Int,
        val numb_car: String,
        val date_arrival: Date,
        val date_depart: Date,
        val state: String,
        val price: Int)

@Entity
data class Movings(
        @PrimaryKey(autoGenerate = true) val id_parking: Int = 0,
        val id_client: Int,
        var date_arrival: Date? = null,
        var date_depart: Date? = null,
        var emp_arrival_id: String? = null,
        var emp_depart_id: String? = null,
        var state: String,
        var price: Int? = null)

@Dao
interface MovingsDao {
    @Query("SELECT* FROM Movings")
    fun getAllMovings(): Array<Movings>

    @Query("SELECT* FROM Movings WHERE state = :state")
    fun getMovingsByState(state: String): Array<Movings>

    @Insert
    fun insert(movings: Movings)

    @Delete
    fun delete(movings: Movings)

    @Update
    fun update(movings: Movings)
}

@Entity
data class Cars(
        @PrimaryKey val numb_car: String,
        val brand: String,
        val model: String,
        var color: String)

@Dao
interface CarsDao {
    @Query("SELECT* FROM Cars")
    fun getAllCars(): Array<Cars>

    @Query("SELECT* FROM Cars WHERE numb_car = :carNumber")
    fun getCarByNumber(carNumber: String): Array<Cars>

    @Insert
    fun insert(cars: Cars)

    @Delete
    fun delete(cars: Cars)

    @Update
    fun update(cars: Cars)
}

@Entity
data class Tariffs(
        @PrimaryKey val tariff: String,
        var price: Int)

@Dao
interface TariffsDao {
    @Query("SELECT* FROM Tariffs")
    fun getAllTariffs(): Array<Tariffs>

    @Insert
    fun insert(tariffs: Tariffs)

    @Delete
    fun delete(tariffs: Tariffs)

    @Update
    fun update(tariffs: Tariffs)
}

@Entity
data class Count(
        @PrimaryKey val placesLeft: String,
        var count: Int
)

@Dao
interface CountDao {
    @Query("SELECT* FROM Count")
    fun getAllCount(): Array<Count>

    @Insert
    fun insert(count: Count)

    @Delete
    fun delete(count: Count)

    @Update
    fun update(count: Count)
}
