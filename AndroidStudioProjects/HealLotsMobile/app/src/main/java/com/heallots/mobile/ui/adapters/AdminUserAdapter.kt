package com.heallots.mobile.features.admin.dashboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.heallots.mobile.R
import com.heallots.mobile.models.User
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class AdminUserAdapter(
    private val context: Context,
    users: List<User>?
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {
    private var users: List<User> = users ?: ArrayList()

    fun updateUsers(updatedUsers: List<User>?) {
        users = updatedUsers ?: ArrayList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: TextView = itemView.findViewById(R.id.adminUserItemAvatar)
        private val name: TextView = itemView.findViewById(R.id.adminUserItemName)
        private val email: TextView = itemView.findViewById(R.id.adminUserItemEmail)
        private val phone: TextView = itemView.findViewById(R.id.adminUserItemPhone)
        private val role: TextView = itemView.findViewById(R.id.adminUserItemRole)
        private val joined: TextView = itemView.findViewById(R.id.adminUserItemJoined)

        fun bind(user: User) {
            val fullName = safeText(user.fullName, "User")
            name.text = fullName
            avatar.text = fullName.substring(0, 1).uppercase(Locale.getDefault())
            email.text = safeText(user.email, "No email")
            phone.text = safeText(user.getPhone(), "No phone")
            role.text = safeText(user.role, "USER").uppercase(Locale.getDefault())
            role.background = makeRoleBackground(user.role)
            joined.text = "Joined: ${formatJoinedDate(user.createdAt)}"
        }

        private fun makeRoleBackground(roleValue: String?): GradientDrawable {
            return GradientDrawable().apply {
                cornerRadius = 999f
                setColor(
                    if (roleValue.equals("ADMIN", ignoreCase = true)) {
                        Color.parseColor("#FEF3C7")
                    } else {
                        Color.parseColor("#E0F2FE")
                    }
                )
            }
        }

        private fun safeText(value: String?, fallback: String): String {
            return if (value.isNullOrBlank()) fallback else value
        }

        private fun formatJoinedDate(rawValue: String?): String {
            val value = safeText(rawValue, "Unknown")
            if (value == "Unknown") {
                return value
            }

            try {
                return LocalDate.parse(value).format(DISPLAY_DATE_FORMAT)
            } catch (_: DateTimeParseException) {
            }

            try {
                return OffsetDateTime.parse(value).toLocalDate().format(DISPLAY_DATE_FORMAT)
            } catch (_: DateTimeParseException) {
            }

            try {
                return Instant.parse(value)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DISPLAY_DATE_FORMAT)
            } catch (_: DateTimeParseException) {
            }

            val separatorIndex = value.indexOf('T')
            if (separatorIndex > 0) {
                try {
                    return LocalDate.parse(value.substring(0, separatorIndex)).format(DISPLAY_DATE_FORMAT)
                } catch (_: DateTimeParseException) {
                }
            }

            if (value.length >= 10) {
                try {
                    return LocalDate.parse(value.substring(0, 10)).format(DISPLAY_DATE_FORMAT)
                } catch (_: DateTimeParseException) {
                }
            }

            return value
        }
    }

    companion object {
        private val DISPLAY_DATE_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
    }
}
