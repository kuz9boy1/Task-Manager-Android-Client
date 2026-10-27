package com.example.myapplication

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.dto.CurseDto
import com.example.myapplication.model.ExchangeState
import com.example.myapplication.model.ExchangeViewModel
import kotlinx.coroutines.launch

class ExchangeRateActivity : AppCompatActivity() {


    private val viewModel: ExchangeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_rate)

        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val buttonFetch = findViewById<Button>(R.id.buttonFetch)


        buttonFetch.setOnClickListener {
            viewModel.fetchExchangeRates()
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ExchangeState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            textViewResult.visibility = View.GONE
                        }
                        is ExchangeState.Success -> {
                            progressBar.visibility = View.GONE
                            textViewResult.visibility = View.VISIBLE

                            val usd = state.data.Valute["USD"]
                            val eur = state.data.Valute["EUR"]

                            val usdRate = if (usd != null) usd.Value / usd.Nominal else 0.0
                            val eurRate = if (eur != null) eur.Value / eur.Nominal else 0.0

                            textViewResult.text = "Курс ЦБ РФ:\n" +
                                    "USD: %.2f RUB\n".format(usdRate) +
                                    "EUR: %.2f RUB".format(eurRate)
                        }
                        is ExchangeState.Error -> {
                            progressBar.visibility = View.GONE
                            textViewResult.visibility = View.VISIBLE
                            textViewResult.text = state.message
                        }
                    }
                }
            }
        }


        viewModel.fetchExchangeRates()
    }
}