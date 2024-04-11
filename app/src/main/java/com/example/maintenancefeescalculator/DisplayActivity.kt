package com.example.maintenancefeescalculator

import android.content.ContentValues
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maintenancefeescalculator.databinding.ActivityDisplayBinding
import java.io.IOException
import android.content.Intent

class DisplayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDisplayBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentAdapter: PaymentAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isFromWelcomeActivity = intent.getBooleanExtra("FROM_WELCOME_ACTIVITY", false)
        if (isFromWelcomeActivity) {
            // Disable the downloadButton
            val downloadButton: Button = findViewById(R.id.downloadButton)
            downloadButton.isEnabled = false
        }

        val paymentsData = getPaymentsData()
        val numApartments = intent.getIntExtra("NUM_APARTMENTS", 0)

        // Set up RecyclerView
        recyclerView = findViewById<RecyclerView>(R.id.paymentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        paymentAdapter = PaymentAdapter(paymentsData)
        recyclerView.adapter = paymentAdapter

        val downloadButton: Button = findViewById(R.id.downloadButton)
        downloadButton.setOnClickListener {
            Log.d("PDF Generation", "Download button clicked")
            Log.d("PositionInfo", "numApartments: $numApartments")

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.scrollToPosition(paymentAdapter.itemCount - 1)
            recyclerView.post {
                // Get the position of the last visible item
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                // Calculate the endPosition as the lastVisibleItemPosition + 1
                val endPosition = lastVisibleItemPosition + 1

                // Calculate the startPosition as endPosition - numApartments,
                // making sure it doesn't go below 0
                val startPosition = maxOf(endPosition - numApartments, 0)

                Log.d("PDF Generation", "Start position: $startPosition, End position: $endPosition")
                // Now, generate the PDF using startPosition and endPosition
                generatePDF(startPosition, endPosition)
            }


            // Optionally, show a message or perform other actions after generating the PDF
            Toast.makeText(this, "PDF downloaded successfully", Toast.LENGTH_SHORT).show()
        }
        val telosButton: Button = findViewById(R.id.telosButton)
        telosButton.setOnClickListener {
            val buildingIntent = Intent(this@DisplayActivity, Welcome::class.java)

            startActivity(buildingIntent)
        }
    }

    data class Payment(
        val name: String,
        val surname: String,
        val phone: String,
        val monthYear: String,
        val totalFees: Float,
        val generalPayFees: Float,
        val elevatorPayFees: Float,
        val heatingPayFees: Float,
        val apartmentsId: Long
    )

    private fun getPaymentsData(): List<Payment> {
        val paymentsList = mutableListOf<Payment>()
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_NAME,
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_SURNAME,
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_PHONE,
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_MONTH_YEAR,
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_TOTAL_FEES,
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_GENERAL_PAY_FEES,
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_ELEVATOR_PAY_FEES,
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_HEATING_PAY_FEES,
            DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_APARTMENTS_ID
        )

        val cursor = db.query(
            DatabaseHelper.TotalPaymentsEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        cursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_NAME))
                val surname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_SURNAME))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_PHONE))
                val monthYear = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_MONTH_YEAR))
                val totalFees = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_TOTAL_FEES))
                val generalPayFees = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_GENERAL_PAY_FEES))
                val elevatorPayFees = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_ELEVATOR_PAY_FEES))
                val heatingPayFees = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_HEATING_PAY_FEES))
                val apartmentId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.TotalPaymentsEntry.COLUMN_NAME_APARTMENTS_ID))

                val payment = Payment(name, surname, phone, monthYear, totalFees, generalPayFees, elevatorPayFees, heatingPayFees, apartmentId)
                paymentsList.add(payment)
            }
        }

        cursor?.close()
        return paymentsList
    }

    private fun generatePDF(startPosition: Int, endPosition: Int) {
        Log.d("PDF Generation", "Generating PDF document")
        val document = PdfDocument()

        val headerView = LayoutInflater.from(this).inflate(R.layout.item_payment_header, null) // Replace R.layout.header_layout with your actual header layout
        headerView.measure(
            View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        headerView.layout(0, 0, headerView.measuredWidth, headerView.measuredHeight)

        val pageInfo = PdfDocument.PageInfo.Builder(headerView.width, headerView.height, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        canvas.drawColor(Color.BLACK)
        headerView.draw(canvas)
        document.finishPage(page)

        for (i in startPosition until endPosition) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i)
            viewHolder?.itemView?.let { itemView ->
                Log.d("PDF Generation", "Drawing page $i")
                val pageInfo = PdfDocument.PageInfo.Builder(itemView.width, itemView.height, i).setContentRect(
                    Rect(0, 0, itemView.width, itemView.height)
                ).create()
                val backgroundColor = Color.rgb(50, 50, 50)
                val page = document.startPage(pageInfo)
                val canvas: Canvas = page.canvas
                canvas.drawColor(backgroundColor)
                itemView.draw(canvas)
                document.finishPage(page)
            }
        }

        // Save the PDF document
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "PaymentsTable.pdf")
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues)

        try {
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    document.writeTo(outputStream)
                }
                Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show()
        }

        document.close()
    }

}
