package com.heallots.mobile.utils

object MockData {
    data class Service(
        val id: String,
        val name: String,
        val specialist: String,
        val description: String,
        val icon: String
    )

    fun getServices(): List<Service> = listOf(
        Service("1", "Traditional Hilot", "Manang Rosa", "Most popular hilot session", "\uD83E\uDD32\uD83C\uDFFB"),
        Service("2", "Herbal Compress", "Mang Berting", "Best for pain relief", "\uD83C\uDF3F"),
        Service("3", "Head & Neck Relief", "Ate Cora", "Stress relief for upper body tension", "\uD83D\uDC86\uD83C\uDFFB\u200D\u2640\uFE0F"),
        Service("4", "Foot Reflexology", "Manang Lourdes", "Relaxing foot pressure therapy", "\uD83E\uDDB6\uD83C\uDFFC"),
        Service("5", "Hot Oil Massage", "Mang Totoy", "Warm oil massage for deep relaxation", "\uD83E\uDED9"),
        Service("6", "Whole-Body Hilot", "Ate Nena", "Premium full-body treatment", "\uD83E\uDDD8\uD83C\uDFFB")
    )

    fun getMorningTimeSlots(): List<String> = listOf(
        "08:00 AM",
        "09:00 AM",
        "10:00 AM",
        "11:00 AM",
        "12:00 PM"
    )

    fun getAfternoonTimeSlots(): List<String> = listOf(
        "01:00 PM",
        "02:00 PM",
        "03:00 PM",
        "04:00 PM",
        "05:00 PM"
    )
}
