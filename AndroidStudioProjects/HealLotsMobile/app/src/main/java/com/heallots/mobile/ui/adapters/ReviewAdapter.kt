package com.heallots.mobile.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.models.Review
import com.heallots.mobile.utils.Constants
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.util.Locale

class ReviewAdapter(
    private val context: Context,
    reviews: List<Review>?
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    private var reviews: List<Review> = reviews ?: ArrayList()

    fun updateReviews(updatedReviews: List<Review>?) {
        reviews = updatedReviews ?: ArrayList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.reviewAvatar)
        private val avatarText: TextView = itemView.findViewById(R.id.reviewAvatarText)
        private val patientName: TextView = itemView.findViewById(R.id.reviewPatientName)
        private val rating: TextView = itemView.findViewById(R.id.reviewRating)
        private val meta: TextView = itemView.findViewById(R.id.reviewMeta)
        private val body: TextView = itemView.findViewById(R.id.reviewBody)

        fun bind(review: Review) {
            val name = safeText(review.patientName, "Guest")
            patientName.text = name
            avatarText.text = name.substring(0, 1).uppercase(Locale.getDefault())
            rating.text = buildStars(review.rating)
            meta.text = "${safeText(review.serviceName, "Hilot Session")} with ${safeText(review.specialistName, "Specialist")}"
            body.text = safeText(review.reviewText, "Shared a positive experience.")

            val profilePicture = review.patientProfilePictureUrl
            if (!profilePicture.isNullOrBlank()) {
                avatar.visibility = View.VISIBLE
                avatarText.visibility = View.VISIBLE
                val imageUrl = if (profilePicture.startsWith("http")) {
                    profilePicture
                } else {
                    "${Constants.BASE_URL}/api/user/profile-picture/$profilePicture"
                }
                Picasso.get().load(imageUrl).fit().centerCrop().into(avatar, object : Callback {
                    override fun onSuccess() {
                        avatarText.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                        avatar.setImageDrawable(null)
                        avatarText.visibility = View.VISIBLE
                    }
                })
            } else {
                avatar.setImageDrawable(null)
                avatarText.visibility = View.VISIBLE
            }
        }

        private fun buildStars(stars: Int): String {
            val safeStars = stars.coerceIn(1, 5)
            return buildString {
                repeat(safeStars) {
                    append('\u2605')
                }
            }
        }

        private fun safeText(value: String?, fallback: String): String {
            return if (value.isNullOrBlank()) fallback else value
        }
    }
}
