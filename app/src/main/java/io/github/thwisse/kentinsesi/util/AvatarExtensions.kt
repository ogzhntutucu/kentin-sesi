package io.github.thwisse.kentinsesi.util

import android.widget.ImageView
import coil.decode.SvgDecoder
import coil.load
import coil.request.ImageRequest
import io.github.thwisse.kentinsesi.R

/**
 * DiceBear API kullanarak avatar yükler
 * @param seed DiceBear seed string (UUID format)
 */
fun ImageView.loadAvatar(seed: String?) {
    if (seed.isNullOrBlank()) {
        // Fallback: Default placeholder icon
        setImageResource(R.drawable.ic_person_placeholder)
        return
    }
    
    val url = "https://api.dicebear.com/9.x/personas/svg?seed=$seed&backgroundColor=transparent"
    
    load(url) {
        decoderFactory { result, options, _ ->
            SvgDecoder(result.source, options)
        }
        crossfade(true)
        placeholder(R.drawable.ic_person_placeholder)
        error(R.drawable.ic_person_placeholder)
    }
}
