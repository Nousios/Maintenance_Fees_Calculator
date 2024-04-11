package com.example.maintenancefeescalculator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_USER_ENTRIES)
        db.execSQL(SQL_CREATE_ADDRESS_ENTRIES)
        db.execSQL(SQL_CREATE_APARTMENTS_ENTRIES)
        db.execSQL(SQL_CREATE_TOTAL_PAYMENTS_ENTRIES)
        db.execSQL(SQL_CREATE_TOTAL_EXPENSES_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_USER_ENTRIES)
        db.execSQL(SQL_DELETE_ADDRESS_ENTRIES)
        db.execSQL(SQL_DELETE_APARTMENTS_ENTRIES)
        db.execSQL(SQL_DELETE_TOTAL_PAYMENTS_ENTRIES)
        db.execSQL(SQL_DELETE_TOTAL_EXPENSES_ENTRIES)
        onCreate(db)
    }

    companion object {
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "MyDatabase.db"

        private const val SQL_CREATE_USER_ENTRIES =
            "CREATE TABLE ${UsernameEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${UsernameEntry.COLUMN_NAME_USERNAME} TEXT)"

        private const val SQL_DELETE_USER_ENTRIES =
            "DROP TABLE IF EXISTS ${UsernameEntry.TABLE_NAME}"

        private const val SQL_CREATE_ADDRESS_ENTRIES =
            "CREATE TABLE ${AddressEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${AddressEntry.COLUMN_NAME_ADDRESS} TEXT," +
                    "${AddressEntry.COLUMN_NAME_ADDRESS_NUMBER} TEXT," +
                    "${AddressEntry.COLUMN_NAME_FLOORS} TEXT," +
                    "${AddressEntry.COLUMN_NAME_FLATS} TEXT," +
                    "${AddressEntry.COLUMN_NAME_TOTAL_SQ_METERS} FLOAT," +
                    "${AddressEntry.COLUMN_NAME_ELEVATOR_CHECKED} INTEGER," +
                    "${AddressEntry.COLUMN_NAME_HEATING_CHECKED} INTEGER," +
                    "${AddressEntry.COLUMN_NAME_M2CALC_CHECKED} INTEGER)"

        private const val SQL_DELETE_ADDRESS_ENTRIES =
            "DROP TABLE IF EXISTS ${AddressEntry.TABLE_NAME}"

        private const val SQL_CREATE_APARTMENTS_ENTRIES =
            "CREATE TABLE ${ApartmentsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${ApartmentsEntry.COLUMN_NAME_NAME} TEXT," +
                    "${ApartmentsEntry.COLUMN_NAME_SURNAME} TEXT," +
                    "${ApartmentsEntry.COLUMN_NAME_FLOOR} TEXT," +
                    "${ApartmentsEntry.COLUMN_NAME_PHONE} TEXT," +
                    "${ApartmentsEntry.COLUMN_NAME_EMAIL} TEXT," +
                    "${ApartmentsEntry.COLUMN_NAME_SQ_METERS} FLOAT," +
                    "${ApartmentsEntry.COLUMN_NAME_SHARED_FEES} FLOAT," +
                    "${ApartmentsEntry.COLUMN_NAME_ELEVATOR_FEES} FLOAT," +
                    "${ApartmentsEntry.COLUMN_NAME_HEATING_UNITS} FLOAT," +
                    "${ApartmentsEntry.COLUMN_NAME_BUILDING_ID} INTEGER," +

                    "FOREIGN KEY(${ApartmentsEntry.COLUMN_NAME_BUILDING_ID}) REFERENCES ${AddressEntry.TABLE_NAME}(${BaseColumns._ID}) ON DELETE CASCADE ON UPDATE CASCADE)"

        private const val SQL_DELETE_APARTMENTS_ENTRIES =
            "DROP TABLE IF EXISTS ${ApartmentsEntry.TABLE_NAME}"


        private const val SQL_CREATE_TOTAL_PAYMENTS_ENTRIES =
            "CREATE TABLE ${TotalPaymentsEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${TotalPaymentsEntry.COLUMN_NAME_NAME} TEXT," +
                    "${TotalPaymentsEntry.COLUMN_NAME_SURNAME} TEXT," +
                    "${TotalPaymentsEntry.COLUMN_NAME_PHONE} NUMBER," +
                    "${TotalPaymentsEntry.COLUMN_NAME_MONTH_YEAR} FLOAT," +
                    "${TotalPaymentsEntry.COLUMN_NAME_TOTAL_FEES} FLOAT," +
                    "${TotalPaymentsEntry.COLUMN_NAME_GENERAL_PAY_FEES} FLOAT," +
                    "${TotalPaymentsEntry.COLUMN_NAME_ELEVATOR_PAY_FEES} FLOAT," +
                    "${TotalPaymentsEntry.COLUMN_NAME_HEATING_PAY_FEES} FLOAT," +
                    "${TotalPaymentsEntry.COLUMN_NAME_APARTMENTS_ID} INTEGER," +


                    "FOREIGN KEY(${TotalPaymentsEntry.COLUMN_NAME_APARTMENTS_ID}) REFERENCES ${ApartmentsEntry.TABLE_NAME}(${BaseColumns._ID}) ON DELETE CASCADE ON UPDATE CASCADE)"

        private const val SQL_DELETE_TOTAL_PAYMENTS_ENTRIES =
            "DROP TABLE IF EXISTS ${TotalPaymentsEntry.TABLE_NAME}"



        private const val SQL_CREATE_TOTAL_EXPENSES_ENTRIES =
            "CREATE TABLE ${TotalExpensesEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${TotalExpensesEntry.COLUMN_NAME_ADDRESS} FLOAT," +
                    "${TotalExpensesEntry.COLUMN_NAME_MONTH_YEAR} FLOAT," +
                    "${TotalExpensesEntry.COLUMN_NAME_TOTAL_EXPENSES} FLOAT," +
                    "${TotalExpensesEntry.COLUMN_NAME_GENERAL_EXPENSES} FLOAT," +
                    "${TotalExpensesEntry.COLUMN_NAME_ELEVATOR_EXPENSES} FLOAT," +
                    "${TotalExpensesEntry.COLUMN_NAME_HEATING_EXPENSES} FLOAT," +
                    "${TotalExpensesEntry.COLUMN_NAME_BUILDING_ID} INTEGER," +


                    "FOREIGN KEY(${TotalExpensesEntry.COLUMN_NAME_BUILDING_ID}) REFERENCES ${AddressEntry.TABLE_NAME}(${BaseColumns._ID}) ON DELETE CASCADE ON UPDATE CASCADE)"

        private const val SQL_DELETE_TOTAL_EXPENSES_ENTRIES =
            "DROP TABLE IF EXISTS ${TotalExpensesEntry.TABLE_NAME}"

    }

    object UsernameEntry : BaseColumns {                            //ΚΑΤΑΧΩΡΗΣΗ USER
        const val TABLE_NAME = "username"
        const val COLUMN_NAME_USERNAME = "username"
    }

    object AddressEntry : BaseColumns {                             //ΚΑΤΑΧΩΡΗΣΗ ΠΟΛΥΚΑΤΟΙΚΙΑΣ
        const val TABLE_NAME = "address"
        const val COLUMN_NAME_ADDRESS = "address"
        const val COLUMN_NAME_ADDRESS_NUMBER = "address_number"
        const val COLUMN_NAME_FLOORS = "floors"
        const val COLUMN_NAME_FLATS = "flats"
        const val COLUMN_NAME_TOTAL_SQ_METERS = "total_sq_meters"
        const val COLUMN_NAME_ELEVATOR_CHECKED = "elevator"
        const val COLUMN_NAME_HEATING_CHECKED = "heating"
        const val COLUMN_NAME_M2CALC_CHECKED = "m2calc"
    }

    object ApartmentsEntry : BaseColumns {                          //ΚΑΤΑΧΩΡΗΣΗ ΕΝΟΙΚΟΥ
        const val TABLE_NAME = "apartment"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_SURNAME = "surname"
        const val COLUMN_NAME_FLOOR = "floor"
        const val COLUMN_NAME_PHONE = "phone"
        const val COLUMN_NAME_EMAIL = "email"
        const val COLUMN_NAME_SQ_METERS = "sq_meters"
        const val COLUMN_NAME_SHARED_FEES = "shared_fees"
        const val COLUMN_NAME_ELEVATOR_FEES = "elevator_fees"
        const val COLUMN_NAME_HEATING_UNITS = "heating_units"
        const val COLUMN_NAME_BUILDING_ID = "building_id"
    }

    object TotalPaymentsEntry : BaseColumns {                           //ΕΞΟΔΑ ΕΝΟΙΚΟΥ
        const val TABLE_NAME = "totalPayments"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_SURNAME = "surname"
        const val COLUMN_NAME_PHONE = "phone"
        const val COLUMN_NAME_MONTH_YEAR = "yearMonth"
        const val COLUMN_NAME_TOTAL_FEES = "total_fees"
        const val COLUMN_NAME_GENERAL_PAY_FEES = "general_pay_fees"
        const val COLUMN_NAME_ELEVATOR_PAY_FEES = "elevator_pay_fees"
        const val COLUMN_NAME_HEATING_PAY_FEES= "heating_pay_fees"
                                                                        //ΤΣΕΚ ΑΝ ΤΑ ΠΛΗΡΩΣΕ
        const val COLUMN_NAME_APARTMENTS_ID = "apartment_id"
    }

    object TotalExpensesEntry : BaseColumns {                           //ΕΞΟΔΑ ΠΟΛΥΚΑΤΟΙΚΙΑΣ
        const val TABLE_NAME = "totalExpenses"
        const val COLUMN_NAME_ADDRESS = "address"
        const val COLUMN_NAME_MONTH_YEAR = "yearMonth"
        const val COLUMN_NAME_TOTAL_EXPENSES = "total_expenses"
        const val COLUMN_NAME_GENERAL_EXPENSES = "general_expenses"
        const val COLUMN_NAME_ELEVATOR_EXPENSES = "elevator_expenses"
        const val COLUMN_NAME_HEATING_EXPENSES = "heating_expenses"
        const val COLUMN_NAME_BUILDING_ID = "building_id"

    }

        fun getBuildingList(): List<Building> {
        val db = readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            AddressEntry.COLUMN_NAME_ADDRESS,
            AddressEntry.COLUMN_NAME_ADDRESS_NUMBER,
            AddressEntry.COLUMN_NAME_FLOORS,
            AddressEntry.COLUMN_NAME_FLATS,
            AddressEntry.COLUMN_NAME_ELEVATOR_CHECKED,
            AddressEntry.COLUMN_NAME_HEATING_CHECKED,
            AddressEntry.COLUMN_NAME_M2CALC_CHECKED
        )

        val sortOrder = "${BaseColumns._ID} ASC"

        val cursor = db.query(
            AddressEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            sortOrder
        )

        val buildingList = mutableListOf<Building>()

        with(cursor) {
            while (moveToNext()) {
                val buildingId = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val address = getString(getColumnIndexOrThrow(AddressEntry.COLUMN_NAME_ADDRESS))
                val addressNumber = getString(getColumnIndexOrThrow(AddressEntry.COLUMN_NAME_ADDRESS_NUMBER))
                val floors = getString(getColumnIndexOrThrow(AddressEntry.COLUMN_NAME_FLOORS))
                val flats = getString(getColumnIndexOrThrow(AddressEntry.COLUMN_NAME_FLATS))
                val elevatorChecked = getInt(getColumnIndexOrThrow(AddressEntry.COLUMN_NAME_ELEVATOR_CHECKED)) == 1
                val heatingChecked = getInt(getColumnIndexOrThrow(AddressEntry.COLUMN_NAME_HEATING_CHECKED)) == 1
                val m2calcChecked = getInt(getColumnIndexOrThrow(AddressEntry.COLUMN_NAME_M2CALC_CHECKED)) == 1

                val building = Building(buildingId, address, addressNumber, floors, flats, elevatorChecked, heatingChecked, m2calcChecked)
                buildingList.add(building)
            }
        }

        cursor.close()

        return buildingList
    }


    fun deleteBuilding(buildingId: Long): Boolean {
        val db = writableDatabase
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(buildingId.toString())
        val deletedRows = db.delete(AddressEntry.TABLE_NAME, selection, selectionArgs)
        db.close()
        return deletedRows > 0
    }


    fun deleteApartmentByBuildingId(buildingId: Long) {
        val db = writableDatabase
        val selection = "${ApartmentsEntry.COLUMN_NAME_BUILDING_ID} = ?"
        val selectionArgs = arrayOf(buildingId.toString())
        db.delete(ApartmentsEntry.TABLE_NAME, selection, selectionArgs)
        db.close()
    }

    fun getApartmentList(buildingId: Long): List<Apartment> {
        val apartmentList = mutableListOf<Apartment>()
        val db = this.readableDatabase

        val selection = "${ApartmentsEntry.COLUMN_NAME_BUILDING_ID} = ?"
        val selectionArgs = arrayOf(buildingId.toString())

        val apartmentProjection = arrayOf(
            BaseColumns._ID,
            ApartmentsEntry.COLUMN_NAME_NAME,
            ApartmentsEntry.COLUMN_NAME_SURNAME,
            ApartmentsEntry.COLUMN_NAME_FLOOR,
            ApartmentsEntry.COLUMN_NAME_PHONE,
            ApartmentsEntry.COLUMN_NAME_EMAIL,
            ApartmentsEntry.COLUMN_NAME_SQ_METERS,
            ApartmentsEntry.COLUMN_NAME_SHARED_FEES,
            ApartmentsEntry.COLUMN_NAME_ELEVATOR_FEES
        )

        val apartmentCursor = db.query(
            ApartmentsEntry.TABLE_NAME,
            apartmentProjection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        while (apartmentCursor.moveToNext()) {
            val tenantId = apartmentCursor.getLong(apartmentCursor.getColumnIndexOrThrow(BaseColumns._ID))
            val name = apartmentCursor.getString(apartmentCursor.getColumnIndexOrThrow(ApartmentsEntry.COLUMN_NAME_NAME))
            val surname = apartmentCursor.getString(apartmentCursor.getColumnIndexOrThrow(ApartmentsEntry.COLUMN_NAME_SURNAME))
            val floor = apartmentCursor.getString(apartmentCursor.getColumnIndexOrThrow(ApartmentsEntry.COLUMN_NAME_FLOOR))
            val phone = apartmentCursor.getString(apartmentCursor.getColumnIndexOrThrow(ApartmentsEntry.COLUMN_NAME_PHONE))
            val email = apartmentCursor.getString(apartmentCursor.getColumnIndexOrThrow(ApartmentsEntry.COLUMN_NAME_EMAIL))
            val sqMeters = apartmentCursor.getString(apartmentCursor.getColumnIndexOrThrow(ApartmentsEntry.COLUMN_NAME_SQ_METERS))
            val sharedFees = apartmentCursor.getString(apartmentCursor.getColumnIndexOrThrow(ApartmentsEntry.COLUMN_NAME_SHARED_FEES))
            val elevatorFees = apartmentCursor.getString(apartmentCursor.getColumnIndexOrThrow(ApartmentsEntry.COLUMN_NAME_ELEVATOR_FEES))

            val apartment = Apartment(
                id = tenantId,
                name = name,
                surname = surname,
                floor = floor,
                phone = phone,
                email = email,
                sqMeters = sqMeters,
                sharedFees = sharedFees,
                elevatorFees = elevatorFees
            )
            apartmentList.add(apartment)
        }

        apartmentCursor.close()
        db.close()

        return apartmentList
    }


    fun insertApartment(
        name: String,
        surname: String,
        floor: String,
        phone: String,
        email: String,
        sqMeters: String,
        sharedFees: String,
        elevator: String,
        buildingId: Long
    ): Long {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(ApartmentsEntry.COLUMN_NAME_NAME, name)
            put(ApartmentsEntry.COLUMN_NAME_SURNAME, surname)
            put(ApartmentsEntry.COLUMN_NAME_FLOOR, floor)
            put(ApartmentsEntry.COLUMN_NAME_PHONE, phone)
            put(ApartmentsEntry.COLUMN_NAME_EMAIL, email)
            put(ApartmentsEntry.COLUMN_NAME_SQ_METERS, sqMeters)
            put(ApartmentsEntry.COLUMN_NAME_SHARED_FEES, sharedFees)
            put(ApartmentsEntry.COLUMN_NAME_ELEVATOR_FEES, elevator)
            put(ApartmentsEntry.COLUMN_NAME_BUILDING_ID, buildingId)
        }

        // Insert tenant data into the TenantsEntry table
        val newTenantId = db.insert(ApartmentsEntry.TABLE_NAME, null, values)

        db.close()

        return newTenantId
    }

    fun updateApartment(apartment: Apartment): Int {
        val db = writableDatabase

        val values = ContentValues().apply {
            put(ApartmentsEntry.COLUMN_NAME_NAME, apartment.name)
            put(ApartmentsEntry.COLUMN_NAME_SURNAME, apartment.surname)
            put(ApartmentsEntry.COLUMN_NAME_FLOOR, apartment.floor)
            put(ApartmentsEntry.COLUMN_NAME_PHONE, apartment.phone)
            put(ApartmentsEntry.COLUMN_NAME_EMAIL, apartment.email)
            put(ApartmentsEntry.COLUMN_NAME_SQ_METERS, apartment.sqMeters)
            put(ApartmentsEntry.COLUMN_NAME_SHARED_FEES, apartment.sharedFees)
            put(ApartmentsEntry.COLUMN_NAME_ELEVATOR_FEES, apartment.elevatorFees)
        }

        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(apartment.id.toString())

        // Update tenant data in the TenantsEntry table
        val rowsAffected = db.update(ApartmentsEntry.TABLE_NAME, values, selection, selectionArgs)

        db.close()

        return rowsAffected
    }


    fun copyAddress(buildingId: Long) {  // Υπολογισμός και ενημέρωση του συνολικού εξόδου ΠΟΛΥΚΑΤΟΙΚΙΑΣ
        val db = writableDatabase

        val query = """
        UPDATE ${TotalExpensesEntry.TABLE_NAME}
        SET 
            ${TotalExpensesEntry.COLUMN_NAME_ADDRESS} = (
                SELECT ${AddressEntry.COLUMN_NAME_ADDRESS}
                FROM ${AddressEntry.TABLE_NAME}
                WHERE ${AddressEntry.TABLE_NAME}.${BaseColumns._ID} = $buildingId
            ),
            ${TotalExpensesEntry.COLUMN_NAME_TOTAL_EXPENSES} = 
                COALESCE(${TotalExpensesEntry.COLUMN_NAME_GENERAL_EXPENSES}, 0) +
                COALESCE(${TotalExpensesEntry.COLUMN_NAME_ELEVATOR_EXPENSES}, 0) +
                COALESCE(${TotalExpensesEntry.COLUMN_NAME_HEATING_EXPENSES}, 0)
        WHERE ${TotalExpensesEntry.COLUMN_NAME_BUILDING_ID} = $buildingId
    """.trimIndent()

        Log.d("CopyAddressQuery", query)

        try {
            db.execSQL(query)
            Log.d("CopyAddress", "Executed successfully")
        } catch (e: Exception) {
            Log.e("CopyAddress", "Error executing query: ${e.message}")
        } finally {
            db.close()
        }
    }

    fun copyPayments(apartmentId: Long) {   // Υπολογισμός και ενημέρωση του συνολικού εξόδου ΕΝΟΙΚΟΥ
        val db = writableDatabase

        val query = """
        UPDATE ${TotalPaymentsEntry.TABLE_NAME}
        SET 
            ${TotalPaymentsEntry.COLUMN_NAME_TOTAL_FEES} =
                    COALESCE(${TotalPaymentsEntry.COLUMN_NAME_GENERAL_PAY_FEES}, 0) +
                    COALESCE(${TotalPaymentsEntry.COLUMN_NAME_ELEVATOR_PAY_FEES}, 0) +
                    COALESCE(${TotalPaymentsEntry.COLUMN_NAME_HEATING_PAY_FEES}, 0)
        WHERE ${TotalPaymentsEntry.COLUMN_NAME_APARTMENTS_ID} = $apartmentId
    """.trimIndent()

        db.execSQL(query)
    }
}