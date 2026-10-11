package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_sounds")
data class CustomSound(
    @PrimaryKey
    val id: String,
    val name: String,
    val filePath: String,
    val fileSize: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

data class PredefinedSound(
    val key: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val category: String = "Classic"
)

object PredefinedSoundsData {
    val SOUNDS = listOf(
        PredefinedSound(
            key = "SIREN",
            title = "জরুরি সাইরেন (Urgent Siren)",
            subtitle = "উচ্চ তীব্রতার জরুরি সাইরেন সিগন্যাল",
            iconEmoji = "🚨",
            category = "Urgent"
        ),
        PredefinedSound(
            key = "RADAR",
            title = "রাডার পিং (Radar Ping)",
            subtitle = "ধারাবাহিক তীক্ষ্ণ রাডার পালস",
            iconEmoji = "📡",
            category = "Tech"
        ),
        PredefinedSound(
            key = "PULSE",
            title = "ইলেকট্রিক পালস (Rapid Pulse)",
            subtitle = "দ্রুত গতির ইলেকট্রনিক বিপ অ্যালার্ট",
            iconEmoji = "⚡",
            category = "Tech"
        ),
        PredefinedSound(
            key = "CASH_REGISTER",
            title = "কা-চিং মানি অ্যালার্ট (Cha-Ching Money)",
            subtitle = "নতুন অর্ডার ও পেমেন্টের বিশেষ ফ্রিল্যান্স শব্দ",
            iconEmoji = "💰",
            category = "Freelance"
        ),
        PredefinedSound(
            key = "CHIME",
            title = "মেলোডিক চাইম (Melodic Chime)",
            subtitle = "মধুর সুরেলা দুই-টোন অ্যালার্ট",
            iconEmoji = "🔔",
            category = "Melodic"
        ),
        PredefinedSound(
            key = "DIGITAL_ALARM",
            title = "ডিজিটাল ক্লক (Digital Alarm)",
            subtitle = "চিরাচরিত ডিজিটাল ঘড়ির অ্যালার্ম শব্দ",
            iconEmoji = "⏱️",
            category = "Classic"
        ),
        PredefinedSound(
            key = "SPACE_ALERT",
            title = "স্পেস অ্যালার্ট (Sci-Fi Alert)",
            subtitle = "ভবিষ্যতবাদী স্পেস ক্ল্যাক্সন সংকেত",
            iconEmoji = "🚀",
            category = "Tech"
        ),
        PredefinedSound(
            key = "SYSTEM_ALARM",
            title = "সিস্টেম ডিফল্ট (System Alarm)",
            subtitle = "আপনার ফোনের ডিফল্ট অ্যালার্ম রিংটোন",
            iconEmoji = "⏰",
            category = "System"
        )
    )
}
