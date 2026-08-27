package com.example.data

import com.example.data.room.TaskEntity
import java.util.Calendar
import java.util.TimeZone

<<<<<<< HEAD
object TaskGenerator {

    fun generateTasksForToday(): List<TaskEntity> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val dayIndex = dayOfYear % 30 // 30-day loop

        val tasks = mutableListOf<TaskEntity>()

        // Base Obligatory Salah tasks - always present
        tasks.addAll(
            listOf(
                TaskEntity("1", "Offer Fajr Namaz", false, "Salah", "First obligatory prayer.", 30, "OBLIGATORY", 0, true, actionRoute = "home"),
                TaskEntity("2", "Offer Dhuhr Namaz", false, "Salah", "Second obligatory prayer.", 20, "OBLIGATORY", 0, true, actionRoute = "home"),
                TaskEntity("3", "Offer Asr Namaz", false, "Salah", "Third obligatory prayer.", 20, "OBLIGATORY", 0, true, actionRoute = "home"),
                TaskEntity("4", "Offer Maghrib Namaz", false, "Salah", "Fourth obligatory prayer.", 20, "OBLIGATORY", 0, true, actionRoute = "home"),
                TaskEntity("5", "Offer Isha Namaz", false, "Salah", "Fifth obligatory prayer.", 30, "OBLIGATORY", 0, true, actionRoute = "home")
            )
        )

        // Dynamic Auto Tasks (Based on day index)
        when (dayIndex % 7) {
            0 -> {
                tasks.add(TaskEntity("auto_1", "Read Surah Baqarah for 10 Mins", false, "Quran", "Spend 10 minutes reciting the longest Surah.", 100, "AUTO", 0, true, isAuto = true, autoType = "SURAH", autoTarget = 600, actionRoute = "quran"))
                tasks.add(TaskEntity("auto_2", "Recite Astaghfar 100x", false, "Dhikr", "Seek forgiveness 100 times.", 50, "AUTO", 0, true, isAuto = true, autoType = "TASBEEH", autoTarget = 100, actionRoute = "tasbeeh"))
            }
            1 -> {
                tasks.add(TaskEntity("auto_1", "Read 5 Hadiths", false, "Knowledge", "Read 5 authentic Hadiths to gain knowledge.", 80, "AUTO", 0, true, isAuto = true, autoType = "HADITH", autoTarget = 5, actionRoute = "hadith"))
                tasks.add(TaskEntity("auto_2", "Recite SubhanAllah 33x", false, "Dhikr", "Glorify Allah 33 times.", 30, "AUTO", 0, true, isAuto = true, autoType = "TASBEEH", autoTarget = 33, actionRoute = "tasbeeh"))
            }
            2 -> {
                tasks.add(TaskEntity("auto_1", "Learn 5 Names of Allah", false, "Knowledge", "Memorize or reflect upon 5 names of Allah.", 70, "AUTO", 0, true, isAuto = true, autoType = "99_NAMES", autoTarget = 5, actionRoute = "names"))
                tasks.add(TaskEntity("auto_2", "Recite Alhamdulillah 33x", false, "Dhikr", "Praise Allah 33 times.", 30, "AUTO", 0, true, isAuto = true, autoType = "TASBEEH", autoTarget = 33, actionRoute = "tasbeeh"))
            }
            3 -> {
                tasks.add(TaskEntity("auto_1", "Read 3 Duas", false, "Supplication", "Read 3 Duas from the library.", 60, "AUTO", 0, true, isAuto = true, autoType = "DUA", autoTarget = 3, actionRoute = "dua"))
                tasks.add(TaskEntity("auto_2", "Recite Allahu Akbar 34x", false, "Dhikr", "Proclaim Allah's greatness 34 times.", 30, "AUTO", 0, true, isAuto = true, autoType = "TASBEEH", autoTarget = 34, actionRoute = "tasbeeh"))
            }
            4 -> { // Jummah
                tasks.add(TaskEntity("auto_1", "Read Surah Kahf", false, "Quran", "Read Surah Al-Kahf on Friday.", 150, "AUTO", 0, true, isAuto = true, autoType = "SURAH", autoTarget = 900, actionRoute = "quran"))
                tasks.add(TaskEntity("auto_2", "Send Durood 100x", false, "Dhikr", "Send blessings upon the Prophet (SAW).", 80, "AUTO", 0, true, isAuto = true, autoType = "TASBEEH", autoTarget = 100, actionRoute = "tasbeeh"))
            }
            5 -> { // Charity Day
                tasks.add(TaskEntity("manual_charity", "Give Charity Today", false, "Deeds", "Give Sadaqah, even a smile counts.", 100, "WEEKLY", 0, true, actionRoute = "donate"))
                tasks.add(TaskEntity("auto_1", "Read Surah Yaseen", false, "Quran", "Read the heart of the Quran.", 120, "AUTO", 0, true, isAuto = true, autoType = "SURAH", autoTarget = 600, actionRoute = "quran"))
            }
            6 -> {
                tasks.add(TaskEntity("auto_1", "Recite La Ilaha Illallah 100x", false, "Dhikr", "Renew your faith.", 50, "AUTO", 0, true, isAuto = true, autoType = "TASBEEH", autoTarget = 100, actionRoute = "tasbeeh"))
                tasks.add(TaskEntity("auto_2", "Read 5 Hadiths", false, "Knowledge", "Increase your knowledge of the Sunnah.", 80, "AUTO", 0, true, isAuto = true, autoType = "HADITH", autoTarget = 5, actionRoute = "hadith"))
            }
        }

        // Night time specific
        tasks.add(TaskEntity("auto_night", "Read Surah Al-Mulk", false, "Quran", "A surah that intercedes for its companion.", 60, "HOT", 0, true, isAuto = true, autoType = "SURAH", autoTarget = 300, actionRoute = "quran"))

        return tasks
=======
/**
 * Enterprise-grade TaskMasterCatalog:
 * - Deterministic, immutable task definitions
 * - Globally unique IDs to prevent task duplication
 * - Fully routed action targets (no dummy tasks)
 * - Automatic + manual dual-mode completion support
 */
object TaskGenerator {

    // Stable, globally unique IDs
    const val ID_FAJR = "task_prayer_fajr"
    const val ID_DHUHR = "task_prayer_dhuhr"
    const val ID_ASR = "task_prayer_asr"
    const val ID_MAGHRIB = "task_prayer_maghrib"
    const val ID_ISHA = "task_prayer_isha"
    const val ID_MULK = "task_quran_mulk"
    const val ID_KAHF = "task_quran_kahf"
    const val ID_BAQARAH = "task_quran_baqarah"
    const val ID_YASEEN = "task_quran_yaseen"
    const val ID_QURAN_GENERAL = "task_quran_daily_reading"
    const val ID_TASBIH_SUBHANALLAH = "task_tasbih_subhanallah"
    const val ID_TASBIH_ASTAGHFAR = "task_tasbih_astaghfar"
    const val ID_TASBIH_ALHAMDULILLAH = "task_tasbih_alhamdulillah"
    const val ID_TASBIH_ALLAHUAKBAR = "task_tasbih_allahuakbar"
    const val ID_TASBIH_DUROOD = "task_tasbih_durood"
    const val ID_TASBIH_TAHLIL = "task_tasbih_tahlil"
    const val ID_HADITH_DAILY = "task_hadith_read_5"
    const val ID_DUAS_DAILY = "task_duas_read_3"
    const val ID_NAMES_DAILY = "task_names_reflect_5"
    const val ID_SADAQAH = "task_deeds_sadaqah"
    const val ID_SUPPORT_AD = "task_daily_support_ad"

    fun generateTasksForToday(): List<TaskEntity> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Karachi"))
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 6=Friday
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val rotationIndex = dayOfYear % 7

        val taskList = mutableListOf<TaskEntity>()

        // 1. Five Obligatory Prayers (Always present, fixed IDs, fully clickable)
        taskList.add(
            TaskEntity(
                id = ID_FAJR,
                title = "Offer Fajr Namaz",
                completed = false,
                category = "Salah",
                description = "Dawn prayer with congregation or at home.",
                points = 30,
                tag = "OBLIGATORY",
                timerSeconds = 0,
                isSystemTask = true,
                isAuto = false,
                actionRoute = "home"
            )
        )
        taskList.add(
            TaskEntity(
                id = ID_DHUHR,
                title = "Offer Dhuhr Namaz",
                completed = false,
                category = "Salah",
                description = "Midday obligatory prayer.",
                points = 20,
                tag = "OBLIGATORY",
                timerSeconds = 0,
                isSystemTask = true,
                isAuto = false,
                actionRoute = "home"
            )
        )
        taskList.add(
            TaskEntity(
                id = ID_ASR,
                title = "Offer Asr Namaz",
                completed = false,
                category = "Salah",
                description = "Afternoon prayer before sunset.",
                points = 20,
                tag = "OBLIGATORY",
                timerSeconds = 0,
                isSystemTask = true,
                isAuto = false,
                actionRoute = "home"
            )
        )
        taskList.add(
            TaskEntity(
                id = ID_MAGHRIB,
                title = "Offer Maghrib Namaz",
                completed = false,
                category = "Salah",
                description = "Sunset prayer immediately after Maghrib Azan.",
                points = 20,
                tag = "OBLIGATORY",
                timerSeconds = 0,
                isSystemTask = true,
                isAuto = false,
                actionRoute = "home"
            )
        )
        taskList.add(
            TaskEntity(
                id = ID_ISHA,
                title = "Offer Isha Namaz",
                completed = false,
                category = "Salah",
                description = "Night obligatory prayer followed by Witr.",
                points = 30,
                tag = "OBLIGATORY",
                timerSeconds = 0,
                isSystemTask = true,
                isAuto = false,
                actionRoute = "home"
            )
        )

        // 2. Friday Special vs Day-based Quran & Dhikr Tasks
        if (dayOfWeek == Calendar.FRIDAY) {
            taskList.add(
                TaskEntity(
                    id = ID_KAHF,
                    title = "Read Surah Al-Kahf",
                    completed = false,
                    category = "Quran",
                    description = "A light that shines from one Friday to the next.",
                    points = 150,
                    tag = "HOT",
                    timerSeconds = 0,
                    isSystemTask = true,
                    isAuto = true,
                    autoType = "SURAH",
                    autoTarget = 480, // 8 mins reading or 85%+ completion
                    targetSurahNumber = 18,
                    actionRoute = "quran"
                )
            )
            taskList.add(
                TaskEntity(
                    id = ID_TASBIH_DUROOD,
                    title = "Send Durood 100x",
                    completed = false,
                    category = "Dhikr",
                    description = "Send abundant blessings upon the Prophet ﷺ on Friday.",
                    points = 80,
                    tag = "RECOMMENDED",
                    timerSeconds = 0,
                    isSystemTask = true,
                    isAuto = true,
                    autoType = "TASBEEH",
                    autoTarget = 100,
                    actionRoute = "tasbeeh"
                )
            )
        } else {
            when (rotationIndex) {
                0 -> {
                    taskList.add(
                        TaskEntity(
                            id = ID_BAQARAH,
                            title = "Read Surah Al-Baqarah",
                            completed = false,
                            category = "Quran",
                            description = "Protection and blessing for your home.",
                            points = 100,
                            tag = "RECOMMENDED",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "SURAH",
                            autoTarget = 600, // 10 mins reading or comprehensive recitation
                            targetSurahNumber = 2,
                            actionRoute = "quran"
                        )
                    )
                    taskList.add(
                        TaskEntity(
                            id = ID_TASBIH_ASTAGHFAR,
                            title = "Recite Astaghfirullah 100x",
                            completed = false,
                            category = "Dhikr",
                            description = "Seek forgiveness from Allah 100 times.",
                            points = 50,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "TASBEEH",
                            autoTarget = 100,
                            actionRoute = "tasbeeh"
                        )
                    )
                }
                1 -> {
                    taskList.add(
                        TaskEntity(
                            id = ID_HADITH_DAILY,
                            title = "Read 5 Authentic Hadiths",
                            completed = false,
                            category = "Knowledge",
                            description = "Enrich your day with teachings of Rasulullah ﷺ.",
                            points = 80,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "HADITH",
                            autoTarget = 5,
                            actionRoute = "hadith"
                        )
                    )
                    taskList.add(
                        TaskEntity(
                            id = ID_TASBIH_SUBHANALLAH,
                            title = "Recite SubhanAllah 33x",
                            completed = false,
                            category = "Dhikr",
                            description = "Glorify Allah Almighty 33 times.",
                            points = 30,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "TASBEEH",
                            autoTarget = 33,
                            actionRoute = "tasbeeh"
                        )
                    )
                }
                2 -> {
                    taskList.add(
                        TaskEntity(
                            id = ID_NAMES_DAILY,
                            title = "Learn 5 Names of Allah",
                            completed = false,
                            category = "Knowledge",
                            description = "Reflect upon Asma-ul-Husna.",
                            points = 70,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "99_NAMES",
                            autoTarget = 5,
                            actionRoute = "names"
                        )
                    )
                    taskList.add(
                        TaskEntity(
                            id = ID_TASBIH_ALHAMDULILLAH,
                            title = "Recite Alhamdulillah 33x",
                            completed = false,
                            category = "Dhikr",
                            description = "Express gratitude to Allah 33 times.",
                            points = 30,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "TASBEEH",
                            autoTarget = 33,
                            actionRoute = "tasbeeh"
                        )
                    )
                }
                3 -> {
                    taskList.add(
                        TaskEntity(
                            id = ID_DUAS_DAILY,
                            title = "Read 3 Daily Duas",
                            completed = false,
                            category = "Supplication",
                            description = "Supplicate using Prophetic Duas from the library.",
                            points = 60,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "DUA",
                            autoTarget = 3,
                            actionRoute = "dua"
                        )
                    )
                    taskList.add(
                        TaskEntity(
                            id = ID_TASBIH_ALLAHUAKBAR,
                            title = "Recite Allahu Akbar 34x",
                            completed = false,
                            category = "Dhikr",
                            description = "Proclaim the greatness of Allah 34 times.",
                            points = 30,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "TASBEEH",
                            autoTarget = 34,
                            actionRoute = "tasbeeh"
                        )
                    )
                }
                4 -> {
                    taskList.add(
                        TaskEntity(
                            id = ID_YASEEN,
                            title = "Read Surah Yaseen",
                            completed = false,
                            category = "Quran",
                            description = "Recite the heart of the Quran.",
                            points = 120,
                            tag = "RECOMMENDED",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "SURAH",
                            autoTarget = 300,
                            targetSurahNumber = 36,
                            actionRoute = "quran"
                        )
                    )
                    taskList.add(
                        TaskEntity(
                            id = ID_TASBIH_TAHLIL,
                            title = "Recite La Ilaha Illallah 100x",
                            completed = false,
                            category = "Dhikr",
                            description = "Renew your faith with the Kalimah.",
                            points = 50,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "TASBEEH",
                            autoTarget = 100,
                            actionRoute = "tasbeeh"
                        )
                    )
                }
                5 -> {
                    taskList.add(
                        TaskEntity(
                            id = ID_SADAQAH,
                            title = "Give Charity or Good Deed",
                            completed = false,
                            category = "Deeds",
                            description = "Give Sadaqah, feed someone, or perform a good deed.",
                            points = 80,
                            tag = "RECOMMENDED",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = false,
                            actionRoute = "donate"
                        )
                    )
                    taskList.add(
                        TaskEntity(
                            id = ID_QURAN_GENERAL,
                            title = "Read Holy Quran (Daily)",
                            completed = false,
                            category = "Quran",
                            description = "Daily reflection with translation (15+ ayahs, 3+ mins).",
                            points = 60,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "SURAH",
                            autoTarget = 180, // 3 mins active reading & 15+ ayahs
                            targetSurahNumber = null, // Universal task: counts from any Surah
                            actionRoute = "quran"
                        )
                    )
                }
                else -> {
                    taskList.add(
                        TaskEntity(
                            id = ID_HADITH_DAILY,
                            title = "Read 5 Authentic Hadiths",
                            completed = false,
                            category = "Knowledge",
                            description = "Increase your knowledge of the Sunnah.",
                            points = 80,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "HADITH",
                            autoTarget = 5,
                            actionRoute = "hadith"
                        )
                    )
                    taskList.add(
                        TaskEntity(
                            id = ID_TASBIH_ASTAGHFAR,
                            title = "Recite Astaghfirullah 100x",
                            completed = false,
                            category = "Dhikr",
                            description = "Seek forgiveness from Allah 100 times.",
                            points = 50,
                            tag = "AUTO",
                            timerSeconds = 0,
                            isSystemTask = true,
                            isAuto = true,
                            autoType = "TASBEEH",
                            autoTarget = 100,
                            actionRoute = "tasbeeh"
                        )
                    )
                }
            }
        }

        // 3. Night-time Sunnah Task: Surah Al-Mulk (Always present, fixed ID, Medium Category: 30 ayahs)
        taskList.add(
            TaskEntity(
                id = ID_MULK,
                title = "Read Surah Al-Mulk",
                completed = false,
                category = "Quran",
                description = "The protector from the punishment of the grave (30 ayahs, 4+ mins).",
                points = 60,
                tag = "HOT",
                timerSeconds = 0,
                isSystemTask = true,
                isAuto = true,
                autoType = "SURAH",
                autoTarget = 240, // 4 mins reading & 90%+ (27+) ayahs
                targetSurahNumber = 67,
                actionRoute = "quran"
            )
        )

        // 4. Daily Reward Task: Support Developers Ad
        taskList.add(
            TaskEntity(
                id = ID_SUPPORT_AD,
                title = "Watch Ad to Support Developers",
                completed = false,
                category = "Bonus",
                description = "Watch a rewarded ad once a day to help developers and earn bonus XP.",
                points = 110,
                tag = "BONUS",
                timerSeconds = 0,
                isSystemTask = true,
                isAuto = false,
                actionRoute = "reward_ad"
            )
        )

        // Strict Deduplication by unique ID
        return taskList.distinctBy { it.id }
>>>>>>> 6e834ed (Update Taqwahub)
    }
}
