package com.example.data.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.example.data.model.MonitoredApp

data class PresetAppDefinition(
    val packageName: String,
    val appName: String,
    val defaultKeywords: String,
    val description: String,
    val category: String = "Freelance"
)

object PresetAppsData {
    val PRESETS = listOf(
        PresetAppDefinition(
            packageName = "com.fiverr.fiverr",
            appName = "Fiverr",
            defaultKeywords = "order, message, inbox, offer, buyer, delivery, urgent, request",
            description = "Never miss incoming Fiverr buyer messages & orders",
            category = "Freelance"
        ),
        PresetAppDefinition(
            packageName = "com.upwork.android.apps.main",
            appName = "Upwork",
            defaultKeywords = "invitation, interview, offer, contract, proposal, message, client, direct message",
            description = "Immediate alerts for Upwork client interview invites",
            category = "Freelance"
        ),
        PresetAppDefinition(
            packageName = "com.freelancer.android.messenger",
            appName = "Freelancer",
            defaultKeywords = "award, chat, bid, project, milestone, message",
            description = "Freelancer.com message and project award alerts",
            category = "Freelance"
        ),
        PresetAppDefinition(
            packageName = "com.toptal.talent",
            appName = "Toptal",
            defaultKeywords = "job, opportunity, client, interview, message",
            description = "Toptal exclusive opportunity notifications",
            category = "Freelance"
        ),
        PresetAppDefinition(
            packageName = "com.peopleperhour.android",
            appName = "PeoplePerHour",
            defaultKeywords = "workstream, proposal, offer, message, invoice",
            description = "PeoplePerHour buyer workstreams & alerts",
            category = "Freelance"
        ),
        PresetAppDefinition(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            defaultKeywords = "urgent, client, order, payment, project, invoice",
            description = "WhatsApp client direct messages",
            category = "Messaging"
        ),
        PresetAppDefinition(
            packageName = "com.whatsapp.w4b",
            appName = "WhatsApp Business",
            defaultKeywords = "order, inquiry, customer, client, message",
            description = "Business customer inquiries & alerts",
            category = "Messaging"
        ),
        PresetAppDefinition(
            packageName = "org.telegram.messenger",
            appName = "Telegram",
            defaultKeywords = "client, urgent, task, job, payment, update",
            description = "Telegram client channels & chats",
            category = "Messaging"
        ),
        PresetAppDefinition(
            packageName = "com.slack",
            appName = "Slack",
            defaultKeywords = "urgent, @mention, direct message, channel, review",
            description = "Workplace client workspace notifications",
            category = "Workplace"
        ),
        PresetAppDefinition(
            packageName = "com.microsoft.teams",
            appName = "Microsoft Teams",
            defaultKeywords = "meeting, chat, call, urgent, mention",
            description = "Enterprise client communications",
            category = "Workplace"
        ),
        PresetAppDefinition(
            packageName = "com.google.android.gm",
            appName = "Gmail",
            defaultKeywords = "fiverr, upwork, invoice, client, order, contract, proposal",
            description = "Emails from freelance platforms & clients",
            category = "Email"
        )
    )

    fun getDefaultMonitoredApps(): List<MonitoredApp> {
        return listOf(
            MonitoredApp(
                packageName = "com.fiverr.fiverr",
                appName = "Fiverr",
                isEnabled = true,
                keywords = "order, message, inbox, offer, buyer",
                isPreset = true,
                customSound = "SIREN"
            ),
            MonitoredApp(
                packageName = "com.upwork.android.apps.main",
                appName = "Upwork",
                isEnabled = true,
                keywords = "interview, offer, invitation, proposal, message",
                isPreset = true,
                customSound = "SIREN"
            )
        )
    }
}

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isAlreadyMonitored: Boolean
)

object AppScannerHelper {
    fun getInstalledLauncherApps(context: Context, monitoredPackages: Set<String>): List<InstalledAppItem> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = pm.queryIntentActivities(intent, 0)
        val result = mutableListOf<InstalledAppItem>()
        val seen = mutableSetOf<String>()

        for (info in resolveInfoList) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName || seen.contains(pkg)) continue
            seen.add(pkg)
            val name = try {
                info.loadLabel(pm).toString()
            } catch (e: Exception) {
                pkg
            }
            result.add(
                InstalledAppItem(
                    packageName = pkg,
                    appName = name,
                    isAlreadyMonitored = monitoredPackages.contains(pkg)
                )
            )
        }
        return result.sortedBy { it.appName.lowercase() }
    }
}
