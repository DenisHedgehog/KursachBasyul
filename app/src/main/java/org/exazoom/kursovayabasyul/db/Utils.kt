package org.exazoom.kursovayabasyul.db

import android.arch.persistence.room.TypeConverter
import java.util.*


class MyDateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?) = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?) = date?.time
}