package com.example.xml

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.model.DeckButton
import com.example.util.ImageCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Adaptador de alto rendimiento para RecyclerView basado en vistas XML tradicionales.
 * Diseñado específicamente para clientes Macro Deck con:
 * - [DiffUtil] para actualizaciones incrementales O(N).
 * - Carga asíncrona de imágenes Base64 offloaded en segundo plano para evitar jank.
 * - Soporte para eventos táctiles inmediatos (ACTION_DOWN y ACTION_UP).
 * - Cancelación de Jobs coroutine por ViewHolder al reciclar vistas.
 */
class DeckRecyclerAdapter(
    private val scope: CoroutineScope,
    private val onButtonDown: (x: Int, y: Int) -> Unit,
    private val onButtonUp: (x: Int, y: Int) -> Unit,
    private val onButtonLongPress: (x: Int, y: Int) -> Unit
) : ListAdapter<DeckButton, DeckRecyclerAdapter.DeckViewHolder>(DeckButtonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_button, parent, false)
        return DeckViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        val button = getItem(position)
        holder.bind(button)
    }

    override fun onViewRecycled(holder: DeckViewHolder) {
        super.onViewRecycled(holder)
        holder.cancelImageLoad()
    }

    inner class DeckViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: FrameLayout = itemView.findViewById(R.id.cardDeckButton)
        private val tvCoords: TextView = itemView.findViewById(R.id.tvCoordinates)
        private val viewLed: View = itemView.findViewById(R.id.viewStateLed)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivButtonIcon)
        private val tvLabel: TextView = itemView.findViewById(R.id.tvButtonLabel)

        private var imageJob: Job? = null

        fun bind(button: DeckButton) {
            tvCoords.text = "${button.positionX},${button.positionY}"
            tvLabel.text = button.label

            // Color de fondo
            val bgColor = try {
                if (!button.backgroundColorHex.isNullOrBlank()) {
                    Color.parseColor(button.backgroundColorHex)
                } else {
                    Color.parseColor("#1A1C29")
                }
            } catch (_: Exception) {
                Color.parseColor("#1A1C29")
            }
            cardView.setBackgroundColor(bgColor)

            // Color del texto
            try {
                if (!button.labelColorHex.isNullOrBlank()) {
                    tvLabel.setTextColor(Color.parseColor(button.labelColorHex))
                } else {
                    tvLabel.setTextColor(Color.WHITE)
                }
            } catch (_: Exception) {
                tvLabel.setTextColor(Color.WHITE)
            }

            // Indicador LED de estado
            viewLed.visibility = if (button.state) View.VISIBLE else View.GONE

            // Carga asíncrona de icono Base64 con ImageCacheManager
            cancelImageLoad()
            if (!button.iconBase64.isNullOrBlank()) {
                imageJob = scope.launch {
                    val bitmap = ImageCacheManager.decodeBase64(button.iconBase64)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            ivIcon.setImageBitmap(bitmap)
                            ivIcon.visibility = View.VISIBLE
                        } else {
                            ivIcon.setImageDrawable(null)
                            ivIcon.visibility = View.GONE
                        }
                    }
                }
            } else {
                ivIcon.setImageDrawable(null)
                ivIcon.visibility = View.GONE
            }

            // Detección de eventos táctiles inmediatos (Touch Down / Up)
            itemView.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start()
                        onButtonDown(button.positionX, button.positionY)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start()
                        onButtonUp(button.positionX, button.positionY)
                        view.performClick()
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start()
                        true
                    }
                    else -> false
                }
            }

            itemView.setOnLongClickListener {
                onButtonLongPress(button.positionX, button.positionY)
                true
            }
        }

        fun cancelImageLoad() {
            imageJob?.cancel()
            imageJob = null
        }
    }

    class DeckButtonDiffCallback : DiffUtil.ItemCallback<DeckButton>() {
        override fun areItemsTheSame(oldItem: DeckButton, newItem: DeckButton): Boolean {
            return oldItem.id == newItem.id &&
                   oldItem.positionX == newItem.positionX &&
                   oldItem.positionY == newItem.positionY
        }

        override fun areContentsTheSame(oldItem: DeckButton, newItem: DeckButton): Boolean {
            return oldItem == newItem
        }
    }
}
