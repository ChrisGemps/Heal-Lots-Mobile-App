package com.heallots.mobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.heallots.mobile.R;
import com.heallots.mobile.models.Review;
import com.heallots.mobile.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private final Context context;
    private List<Review> reviews = new ArrayList<>();

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews != null ? reviews : new ArrayList<>();
    }

    public void updateReviews(List<Review> updatedReviews) {
        this.reviews = updatedReviews != null ? updatedReviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView avatarText;
        private final TextView patientName;
        private final TextView rating;
        private final TextView meta;
        private final TextView body;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.reviewAvatar);
            avatarText = itemView.findViewById(R.id.reviewAvatarText);
            patientName = itemView.findViewById(R.id.reviewPatientName);
            rating = itemView.findViewById(R.id.reviewRating);
            meta = itemView.findViewById(R.id.reviewMeta);
            body = itemView.findViewById(R.id.reviewBody);
        }

        void bind(Review review) {
            String name = safeText(review.getPatientName(), "Guest");
            patientName.setText(name);
            avatarText.setText(name.substring(0, 1).toUpperCase());
            rating.setText(buildStars(review.getRating()));
            meta.setText(safeText(review.getServiceName(), "Hilot Session") + " with " + safeText(review.getSpecialistName(), "Specialist"));
            body.setText(safeText(review.getReviewText(), "Shared a positive experience."));

            String profilePicture = review.getPatientProfilePictureUrl();
            if (profilePicture != null && !profilePicture.trim().isEmpty()) {
                avatar.setVisibility(View.VISIBLE);
                avatarText.setVisibility(View.VISIBLE);
                String imageUrl = profilePicture.startsWith("http")
                        ? profilePicture
                        : Constants.BASE_URL + "/api/user/profile-picture/" + profilePicture;
                Picasso.get().load(imageUrl).fit().centerCrop().into(avatar, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        avatarText.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        avatar.setImageDrawable(null);
                        avatarText.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                avatar.setImageDrawable(null);
                avatarText.setVisibility(View.VISIBLE);
            }
        }

        private String buildStars(int stars) {
            int safeStars = Math.max(1, Math.min(stars, 5));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < safeStars; i++) {
                builder.append("★");
            }
            return builder.toString();
        }

        private String safeText(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
